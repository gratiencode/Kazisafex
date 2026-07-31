package delegates;

import IServices.CommandeStorage;
import java.util.List;
import data.Commande;
import tools.ServiceLocator;
import tools.Tables;

public class CommandeDelegate {

    public static Commande saveCommande(Commande d) {
        return getStorage().saveCommande(d);
    }

    public static Commande updateCommande(Commande d) {
        return getStorage().updateCommande(d);
    }

    public static void deleteCommande(Commande d) {
        getStorage().deleteCommande(d);
    }

    public static Commande findCommande(String uid) {
        return getStorage().findCommande(uid);
    }

    public static List<Commande> findCommandes() {
        return getStorage().findCommandes();
    }

    public static CommandeStorage getStorage() {
        CommandeStorage storage = (CommandeStorage) ServiceLocator.getInstance().getService(Tables.COMMANDE);
        return storage;
    }
}
