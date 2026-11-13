package services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import java.util.HashSet;
import java.util.Set;

/**
 * Garde-fou central contre la construction dynamique de requetes
 * (JPQL/natives) par concatenation d'identifiants (secu M3).
 *
 * <p>Deux niveaux de defense :</p>
 * <ol>
 *   <li>format d'identifiant strict (anti-injection par construction) ;</li>
 *   <li>appartenance au modele JPA : une entite doit exister dans le
 *       {@link Metamodel} Hibernate, un champ doit exister dans l'entite.</li>
 * </ol>
 *
 * <p>Toute requete construite par concatenation DOIT passer par ces
 * verifications avant execution, meme si l'identifiant provient d'une
 * source interne statique : le cout est nul et cela immunise le code
 * contre les evolutions futures (donnees serveur, introspection SGBD,
 * refactoring).</p>
 *
 * <p>Si le metamodel est indisponible (EntityManager stubbe/moque dans les
 * tests), la verification d'appartenance est ignoree et seul le controle
 * de format s'applique : en production Hibernate fournit toujours le
 * metamodel.</p>
 */
public final class SqlGuard {

    /** Identifiant SQL/JPQL : lettre ou underscore puis alphanumeriques. */
    private static final String IDENTIFIER_PATTERN = "^[A-Za-z_][A-Za-z0-9_]{0,63}$";

    private SqlGuard() {
    }

    /** Vrai si l'identifiant respecte le format strict autorise. */
    public static boolean isSafeIdentifier(String identifier) {
        return identifier != null && identifier.matches(IDENTIFIER_PATTERN);
    }

    /**
     * Valide un identifiant de table/colonne/entite ou echoue.
     *
     * @return l'identifiant valide
     * @throws IllegalArgumentException si le format est refuse
     */
    public static String requireSafeIdentifier(String identifier, String what) {
        if (!isSafeIdentifier(identifier)) {
            throw new IllegalArgumentException(
                    "Identifiant " + what + " refuse (format invalide) : " + identifier);
        }
        return identifier;
    }

    /** Noms d'entites gerees, ou {@code null} si le metamodel est indisponible. */
    private static Set<String> managedEntityNames(EntityManager em) {
        try {
            Metamodel metamodel = em.getMetamodel();
            if (metamodel == null) {
                return null;
            }
            Set<String> names = new HashSet<>();
            for (EntityType<?> entity : metamodel.getEntities()) {
                names.add(entity.getName());
            }
            return names;
        } catch (RuntimeException ex) {
            return null;
        }
    }

    /**
     * Verifie qu'un nom d'entite correspond a une entite geree par la
     * persistance (metamodel JPA) et retourne ce nom.
     *
     * @throws IllegalArgumentException si le format est refuse ou l'entite
     *         inconnue du modele (quand le metamodel est disponible)
     */
    public static String requireKnownEntityName(EntityManager em, String entityName) {
        requireSafeIdentifier(entityName, "entite");
        if (em == null) {
            throw new IllegalArgumentException("EntityManager requis pour valider l'entite");
        }
        Set<String> known = managedEntityNames(em);
        if (known != null && !known.contains(entityName)) {
            throw new IllegalArgumentException(
                    "Entite inconnue du modele de persistance : " + entityName);
        }
        return entityName;
    }

    /**
     * Verifie qu'une classe correspond a une entite geree par la persistance
     * et retourne son nom d'entite JPQL.
     *
     * @throws IllegalArgumentException si la classe n'est pas une entite geree
     */
    public static String requireManagedEntityName(EntityManager em, Class<?> entityType) {
        if (entityType == null) {
            throw new IllegalArgumentException("Classe d'entite requise");
        }
        String simpleName = requireSafeIdentifier(entityType.getSimpleName(), "entite");
        if (em == null) {
            throw new IllegalArgumentException("EntityManager requis pour valider l'entite");
        }
        Set<String> known = managedEntityNames(em);
        if (known != null && !known.contains(simpleName)) {
            throw new IllegalArgumentException(
                    "Classe non geree par le modele de persistance : " + entityType.getName());
        }
        return simpleName;
    }

    /**
     * Verifie qu'un champ existe dans l'entite (attribut du metamodel JPA).
     * Sans metamodel, seul le format du champ est verifie.
     *
     * @throws IllegalArgumentException si le champ est inconnu ou mal forme
     */
    public static void requireEntityField(EntityManager em, Class<?> entityType, String field) {
        requireSafeIdentifier(field, "champ");
        if (em == null || entityType == null) {
            throw new IllegalArgumentException("EntityManager et classe d'entite requis");
        }
        Metamodel metamodel;
        try {
            metamodel = em.getMetamodel();
        } catch (RuntimeException ex) {
            metamodel = null;
        }
        if (metamodel == null) {
            return;
        }
        EntityType<?> entityTypeMeta;
        try {
            entityTypeMeta = metamodel.entity(entityType);
        } catch (IllegalArgumentException unknownEntity) {
            throw new IllegalArgumentException(
                    "Classe non geree par le modele de persistance : " + entityType.getName());
        }
        for (Attribute<?, ?> attribute : entityTypeMeta.getAttributes()) {
            if (attribute.getName().equals(field)) {
                return;
            }
        }
        throw new IllegalArgumentException(
                "Champ " + field + " inconnu de l'entite " + entityType.getSimpleName());
    }
}
