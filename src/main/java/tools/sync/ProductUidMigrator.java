package tools.sync;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import services.ManagedSessionFactory;
import tools.SyncLogger;

/**
 * Migration locale d'un produit apres une copie cross-tenant cree par le
 * serveur (le serveur copie le produit sous un nouveau uid pour le tenant
 * courant). Lorsqu'une ligne est rejetee (460 PRODUIT/MESURE) parce que le
 * produit appartient a un autre tenant, le serveur cree une copie avec un
 * NOUVEAU uid et renvoie le mapping (ancien uid -> nouveau uid, et
 * ancienUidMesure -> nouveauUidMesure) dans le champ {@code payload}.
 *
 * Cette classe re-point les foreign keys locales de l'ancien uid vers le
 * nouveau uid dans TOUTES les tables filles, puis hard-delete l'ancien produit
 * et les anciennes mesures. Elle travaille en SQL natif, fiable et
 * transactionnel, compatible avec les deux modes du recepteur JavaFX :
 *
 * - MySQL (recepteur non-embarque) : les FK SONT actives, un DELETE sur une
 *   table mere encore referencee echouerait (ex : "Cannot delete or update a
 *   parent row ... prix_de_vente.mesureid_uid"). Toutes les colonnes filles
 *   sont donc re-pointees avant suppression.
 * - SQLite (recepteur embarque, PRAGMA foreign_keys OFF et sans FK declarees
 *   dans le schema actuel) : la reconnexion est inoffensive et les DELETE
 *   restent valides.
 *
 * Pour ne JAMAIS echouer sur une contrainte FK quelle que soit la
 * denomination des colonnes (mesure_id, mesureid_uid, mesure_uid,
 * product_id, produit_id, produit_uid, production_id...), on combine :
 * 1) la decouverte DYNAMIQUE des FK filles via les metadonnees SGBD
 *    (MySQL : information_schema.KEY_COLUMN_USAGE ; SQLite : PRAGMA
 *    foreign_key_list) qui re-pointe TOUTE FK existante, meme inconnue ici ;
 * 2) des listes candidates statiques (table, colonne), appliquees seulement
 *    si la colonne existe reellement (interroge via information_schema.COLUMNS
 *    / PRAGMA table_info) pour etre tolerante aux noms absents.
 */
public final class ProductUidMigrator {

    /**
     * Candidates (table fille, colonne FK) vers mesure : conventions JPA
     * Hibernate du recepteur (mesure_id), nommage atypique de prix_de_vente
     * (mesureid_uid) et variantes legacy (mesure_uid).
     */
    private static final String[][] MESURE_FK_CANDIDATES = {
        {"ligne_vente", "mesure_id"},
        {"aretirer", "mesure_id"},
        {"commande_lister", "mesure_id"},
        {"compter", "mesure_id"},
        {"destocker", "mesure_id"},
        {"entreposer", "mesure_id"},
        {"periode", "mesure_id"},
        {"production", "mesure_id"},
        {"recquisition", "mesure_id"},
        {"sale_agregate", "mesure_id"},
        {"stock_agregate", "mesure_id"},
        {"stock_depot_agregate", "mesure_id"},
        {"stocker", "mesure_id"},
        {"retour_depot", "mesure_id"},
        {"retour_magasin", "mesure_id"},
        {"prix_de_vente", "mesureid_uid"},
        {"prix_de_vente", "mesure_uid"},
        {"ligne_vente", "mesure_uid"}
    };

    /**
     * Candidates (table fille, colonne FK) vers produit : product_id,
     * produit_id, produit_uid et production_id (entreposer).
     */
    private static final String[][] PRODUCT_FK_CANDIDATES = {
        {"ligne_vente", "product_id"},
        {"compter", "product_id"},
        {"destocker", "product_id"},
        {"periode", "product_id"},
        {"recquisition", "product_id"},
        {"sale_agregate", "product_id"},
        {"stock_agregate", "product_id"},
        {"stock_depot_agregate", "product_id"},
        {"stocker", "product_id"},
        {"production", "produit_id"},
        {"commande_lister", "produit_id"},
        {"mesure", "produit_id"},
        {"entreposer", "production_id"},
        {"ligne_vente", "produit_uid"},
        {"mesure", "produit_uid"}
    };

    private ProductUidMigrator() {
    }

    /**
     * Noms de colonnes reellement presents dans une table (active pour MySQL
     * comme pour SQLite). Retourne un ensemble vide si l'introspection
     * echoue : l'appelant repliera alors sur un essai d'UPDATE tolerant.
     */
    private static Set<String> columnNames(EntityManager em, String table) {
        Set<String> names = new HashSet<>();
        if (em == null || table == null) {
            return names;
        }
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                @SuppressWarnings("unchecked")
                List<Object[]> rows = em.createNativeQuery(
                        "PRAGMA table_info('" + table + "')").getResultList();
                for (Object[] row : rows) {
                    if (row != null && row.length > 1 && row[1] != null) {
                        names.add(String.valueOf(row[1]));
                    }
                }
            } else {
                Query q = em.createNativeQuery(
                        "SELECT COLUMN_NAME FROM information_schema.COLUMNS "
                                + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = :t");
                q.setParameter("t", table);
                // Une requete a une seule colonne peut etre renvoyee par le driver
                // JPA soit comme List<String>, soit comme List<Object[]> selon la
                // configuration. On normalise pour eviter un ClassCastException.
                for (Object row : q.getResultList()) {
                    Object name = row;
                    if (row instanceof Object[] arr) {
                        name = (arr.length > 0) ? arr[0] : null;
                    }
                    if (name != null) {
                        names.add(String.valueOf(name));
                    }
                }
            }
        } catch (Exception e) {
            SyncLogger.getInstance().log(e, "ProductUidMigrator.columnNames");
        }
        return names;
    }

    /**
     * Decouverte dynamique des FK filles : (table, colonne) referencant la
     * table mere via {code uid}. MySQL : information_schema. SQLite : PRAGMA
     * foreign_key_list. Vide si le schema n'en declare pas (cas SQLite
     * embarque actuel) ou si l'introspection echoue.
     */
    private static List<String[]> discoverChildFks(EntityManager em, String parentTable) {
        List<String[]> out = new ArrayList<>();
        if (em == null) {
            return out;
        }
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                @SuppressWarnings("unchecked")
                List<Object[]> rows = em.createNativeQuery(
                        "PRAGMA foreign_key_list('" + parentTable + "')").getResultList();
                for (Object[] row : rows) {
                    // id, seq, table, from, to, ...
                    if (row != null && row.length > 3) {
                        String child = row[2] != null ? String.valueOf(row[2]) : null;
                        String col = row[3] != null ? String.valueOf(row[3]) : null;
                        if (child != null && col != null) {
                            out.add(new String[]{child, col});
                        }
                    }
                }
            } else {
                Query q = em.createNativeQuery(
                        "SELECT TABLE_NAME, COLUMN_NAME "
                                + "FROM information_schema.KEY_COLUMN_USAGE "
                                + "WHERE TABLE_SCHEMA = DATABASE() "
                                + "AND REFERENCED_TABLE_NAME = :parent "
                                + "AND REFERENCED_COLUMN_NAME = 'uid'");
                q.setParameter("parent", parentTable);
                @SuppressWarnings("unchecked")
                List<Object[]> rows = q.getResultList();
                for (Object[] row : rows) {
                    if (row != null && row.length > 1) {
                        String child = row[0] != null ? String.valueOf(row[0]) : null;
                        String col = row[1] != null ? String.valueOf(row[1]) : null;
                        if (child != null && col != null) {
                            out.add(new String[]{child, col});
                        }
                    }
                }
            }
        } catch (Exception e) {
            SyncLogger.getInstance().log(e, "ProductUidMigrator.discoverChildFks");
        }
        return out;
    }

    /**
     * Re-pointe de {@code oldUid} vers {@code newUid} chaque FK fille de la
     * table mere. Couvre l'union de la decouverte dynamique et des candidates
     * statiques, sans doublon, et ne cible que les colonnes existantes.
     */
    private static void repointChildFks(EntityManager em, String parentTable,
            String oldUid, String newUid, String[][] candidates) {
        if (oldUid == null || oldUid.isBlank() || newUid == null || newUid.isBlank()
                || oldUid.equals(newUid)) {
            return;
        }
        LinkedHashSet<String> pairs = new LinkedHashSet<>();
        for (String[] pair : discoverChildFks(em, parentTable)) {
            if (pair[0] != null && pair[1] != null && !pair[0].equals(parentTable)) {
                pairs.add(pair[0] + "." + pair[1]);
            }
        }
        for (String[] pair : candidates) {
            String t = pair[0];
            String col = pair[1];
            if (t != null && col != null && !t.equals(parentTable)) {
                pairs.add(t + "." + col);
            }
        }
        for (String key : pairs) {
            int idx = key.indexOf('.');
            if (idx <= 0) {
                continue;
            }
            String t = key.substring(0, idx);
            String col = key.substring(idx + 1);
            // Ne jamais executer un UPDATE sur une colonne qui n'existe pas :
            // "Unknown column 'xxx_uid' in 'where clause'".
            //  - Si l'introspection a reussi (set non vide) : on ne touche que
            //    les colonnes reellement presentes.
            //  - Si l'introspection a ECHOUE (set vide) : on ne peut pas
            //    confirmer l'existence -> on saute aussi. Sans ce garde-fou, les
            //    variantes legacy (mesure_uid / produit_uid) absentes du schema
            //    feraient planter la migration.
            Set<String> existing = columnNames(em, t);
            if (existing.isEmpty() || !existing.contains(col)) {
                continue;
            }
            try {
                em.createNativeQuery(
                        "UPDATE " + t + " SET " + col + " = :new WHERE " + col + " = :old")
                        .setParameter("new", newUid).setParameter("old", oldUid)
                        .executeUpdate();
            } catch (Exception ex) {
                SyncLogger.getInstance().log(ex, "ProductUidMigrator.repoint "
                        + t + "." + col);
            }
        }
    }

    /**
     * @param oldProdUid  uid local du produit (celui de la ligne rejetee)
     * @param newProdUid  uid du produit copie (retourne par le serveur)
     * @param measureUids mapping ancien uid mesure -> nouveau uid mesure (peut
     *                    etre vide ou null si le serveur n'en fournit pas)
     * @return true si la migration s'est deroulee sans erreur
     */
    public static boolean migrate(String oldProdUid, String newProdUid, Map<String, String> measureUids) {
        if (oldProdUid == null || oldProdUid.isBlank()
                || newProdUid == null || newProdUid.isBlank()
                || oldProdUid.equals(newProdUid)) {
            return false;
        }
        try {
            ManagedSessionFactory.executeWrite(em -> {
                if (measureUids != null) {
                    for (Map.Entry<String, String> e : measureUids.entrySet()) {
                        repointChildFks(em, "mesure", e.getKey(), e.getValue(), MESURE_FK_CANDIDATES);
                    }
                }
                repointChildFks(em, "produit", oldProdUid, newProdUid, PRODUCT_FK_CANDIDATES);
                if (measureUids != null) {
                    for (Map.Entry<String, String> e : measureUids.entrySet()) {
                        String oldM = e.getKey();
                        String newM = e.getValue();
                        if (oldM == null || oldM.isBlank() || newM == null || newM.isBlank()) {
                            continue;
                        }
                        em.createNativeQuery("DELETE FROM mesure WHERE uid = :uid")
                                .setParameter("uid", oldM).executeUpdate();
                    }
                }
                em.createNativeQuery("DELETE FROM produit WHERE uid = :uid")
                        .setParameter("uid", oldProdUid).executeUpdate();
                return true;
            });
            SyncLogger.getInstance().logMessage("ProductUidMigrator",
                    "migration ok produit " + oldProdUid + " -> " + newProdUid
                            + " (mesures=" + (measureUids == null ? 0 : measureUids.size()) + ")");
            return true;
        } catch (Exception e) {
            SyncLogger.getInstance().log(e, "ProductUidMigrator.migrate");
            return false;
        }
    }

    /**
     * Migration d'une seule mesure apres copie cross-tenant : re-pointe les FK
     * (mesure_id / mesureid_uid / mesure_uid...) de l'ancien uid vers le
     * nouveau, puis supprime l'ancienne mesure.
     */
    public static boolean migrateMesure(String oldMesureUid, String newMesureUid) {
        if (oldMesureUid == null || oldMesureUid.isBlank()
                || newMesureUid == null || newMesureUid.isBlank()
                || oldMesureUid.equals(newMesureUid)) {
            return false;
        }
        try {
            ManagedSessionFactory.executeWrite(em -> {
                repointChildFks(em, "mesure", oldMesureUid, newMesureUid, MESURE_FK_CANDIDATES);
                em.createNativeQuery("DELETE FROM mesure WHERE uid = :uid")
                        .setParameter("uid", oldMesureUid).executeUpdate();
                return true;
            });
            SyncLogger.getInstance().logMessage("ProductUidMigrator.migrateMesure",
                    "migration ok mesure " + oldMesureUid + " -> " + newMesureUid);
            return true;
        } catch (Exception e) {
            SyncLogger.getInstance().log(e, "ProductUidMigrator.migrateMesure");
            return false;
        }
    }
}