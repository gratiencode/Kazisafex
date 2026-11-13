package tools;

/**
 * Normalisation des identifiants de lot pour les requetes SQL.
 * <p>
 * Les anciens enregistrements peuvent contenir {@code NULL}, une chaine vide
 * ou des espaces pour designer le lot non renseigne. Toutes les lectures et
 * ecritures doivent converger vers une cle unique ({@code ""}) afin qu'un
 * comptage d'inventaire sans lot solde reellement le stock sans lot existant.
 * <p>
 * Les requetes associees doivent comparer avec {@code COALESCE(colonne, '')}
 * pour que les lignes heritees en {@code NULL} restent visibles.
 */
public final class LotKeys {

    private LotKeys() {
    }

    /**
     * Cle canonique d'un lot : {@code null} devient {@code ""} et les espaces
     * de bord sont retires pour eviter les doublons visuels.
     */
    public static String normalize(String lot) {
        return lot == null ? "" : lot.trim();
    }
}
