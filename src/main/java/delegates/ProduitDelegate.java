/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delegates;

import IServices.ProduitStorage;
import java.util.List;
import java.util.Set;
import data.Produit;
import java.time.LocalDateTime;
import tools.ServiceLocator;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class ProduitDelegate {

    public static Produit saveProduit(Produit cat) {
        return getStorage().createProduit(cat);
    }

    public static Produit updateProduit(Produit cat) {
        return getStorage().updateProduit(cat);
    }

    public static void deleteProduit(Produit cat) {
        getStorage().deleteProduit(cat);
    }

    public static Produit findProduit(String objId) {
        return getStorage().findProduit(objId);
    }

    public static Produit findByCodebar(String codebar) {
        return getStorage().findByBarcode(codebar);
    }

    public static List<Produit> findProduits() {
        return getStorage().findProduits();
    }

    public static List<Produit> findProduits(int s, int m) {
        return getStorage().findProduits(s, m);
    }

    public static List<Produit> findProduitByCategory(String catId) {
        return getStorage().findProduitByCategory(catId);
    }

    public static ProduitStorage getStorage() {
        ProduitStorage cats = (ProduitStorage) ServiceLocator.getInstance().getService(Tables.PRODUIT);
        return cats;
    }

    public static long getCount() {
        return getStorage().getCount();
    }

    public static List<Produit> findAllByCodebar(String codebarr) {
        return getStorage().findAllByCodebar(codebarr);
    }

    public static List<Produit> mergeSet(Set<Produit> set) {
        return getStorage().mergeSet(set);
    }

    public static List<Produit> findByDescription(String nomProduit, String marque, String modele, String taille) {
        return getStorage().findByDescription(nomProduit, marque, modele, taille);
    }

    public static List<Produit> findProduitByName(String query) {
        return getStorage().findProduitByName(query);
    }

    public static List<Produit> findUnSyncedProduct(long disconnected_at) {
        return getStorage().findUnSyncedProduct(disconnected_at);
    }

    public static boolean isExists(String uid, LocalDateTime attime) {
        return getStorage().isExists(uid, attime);
    }

    public static boolean isExists(String uid) {
        return getStorage().isExists(uid);
    }

}
