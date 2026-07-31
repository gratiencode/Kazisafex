/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delegates;

import IServices.FournisseurStorage;
import data.Entreprise;
import java.util.List;
import data.Fournisseur;
import tools.ServiceLocator;
import tools.Tables;
import static delegates.FournisseurDelegate.getStorage;
import java.time.LocalDateTime;
import java.util.Set;

/**
 *
 * @author eroot
 */
public class FournisseurDelegate {
    public static Fournisseur saveFournisseur(Fournisseur cat) {
        return getStorage().createFournisseur(cat);
    }

    public static Fournisseur updateFournisseur(Fournisseur cat) {
        return getStorage().updateFournisseur(cat);
    }

    public static void deleteFournisseur(Fournisseur cat) {
        getStorage().deleteFournisseur(cat);
    }

    public static Fournisseur findFournisseur(String objId) {
        return getStorage().findFournisseur(objId);
    }

    public static List<Fournisseur> findFournisseurs() {
        return getStorage().findFournisseurs();
    }

    public static List<Fournisseur> findFournisseurs(int s, int m) {
        return getStorage().findFournisseurs(s, m);
    }

    public static FournisseurStorage getStorage() {
        FournisseurStorage cats = (FournisseurStorage) ServiceLocator.getInstance().getService(Tables.FOURNISSEUR);
        return cats;
    }

    public static List<Fournisseur> findByPhone(String text) {
        return getStorage().findByPhone(text);
    }

    public static Long getCount() {
        return getStorage().getCount();
    }

    public static List<Fournisseur> mergeSet(Set<Fournisseur> fs) {
        return getStorage().mergeSet(fs);
    }

    public static List<Fournisseur> findUnSyncedFournisseurs(long disconnected_at) {
        return getStorage().findUnSyncedFournisseurs(disconnected_at);
    }

    public static Fournisseur findOrCreate(Entreprise entreprise) {
        return getStorage().findOrCreate(entreprise);
    }

    public static boolean isExists(String uid, LocalDateTime attime) {
        return getStorage().isExists(uid, attime);
    }

    public static boolean isExists(String uid) {
        return getStorage().isExists(uid);
    }

    public static double getTotalDebt() {
        return getStorage().getTotalDebt();
    }

    public static void mergeFournisseur(Fournisseur local, Fournisseur incoming) {
        if (incoming.getNomFourn() != null && !incoming.getNomFourn().trim().isEmpty()) {
            local.setNomFourn(incoming.getNomFourn().trim());
        }
        if (incoming.getAdresse() != null && !incoming.getAdresse().trim().isEmpty()) {
            local.setAdresse(incoming.getAdresse().trim());
        }
        if (incoming.getIdentification() != null && !incoming.getIdentification().trim().isEmpty()) {
            local.setIdentification(incoming.getIdentification().trim());
        }
        if (incoming.getPhone() != null && !incoming.getPhone().trim().isEmpty()) {
            local.setPhone(incoming.getPhone().trim());
        }
        if (incoming.getDeletedAt() != null) {
            local.setDeletedAt(incoming.getDeletedAt());
        }
        local.setUpdatedAt(LocalDateTime.now());
    }

    public static Fournisseur syncFournisseurSafe(Fournisseur supplier) {
        Fournisseur localSupplier = findFournisseur(supplier.getUid());
        if (localSupplier == null && supplier.getPhone() != null && !supplier.getPhone().trim().isEmpty()) {
            List<Fournisseur> byPhone = findByPhone(supplier.getPhone().trim());
            if (byPhone != null) {
                for (Fournisseur f : byPhone) {
                    if (f.getPhone() != null && f.getPhone().trim().equals(supplier.getPhone().trim())) {
                        localSupplier = f;
                        break;
                    }
                }
            }
        }
        if (localSupplier == null) {
            return saveFournisseur(supplier);
        } else {
            mergeFournisseur(localSupplier, supplier);
            return updateFournisseur(localSupplier);
        }
    }
}
