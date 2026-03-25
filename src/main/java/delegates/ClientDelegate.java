/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delegates;

import IServices.ClientStorage;
import static delegates.ClientDelegate.getClientStorage;
import java.util.List;
import data.Client;
import static delegates.AretirerDelegate.getStorage;
import java.time.LocalDateTime;
import tools.ServiceLocator;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class ClientDelegate {

    public static Client saveClient(Client cat) {
        return getClientStorage().createClient(cat);
    }

    public static Client updateClient(Client cat) {
        return getClientStorage().updateClient(cat);
    }

    public static void deleteClient(Client cat) {
        getClientStorage().deleteClient(cat);
    }

    public static Client findClient(String objId) {
        return getClientStorage().findClient(objId);
    }

    public static List<Client> findClients() {
        return getClientStorage().findClients();
    }

    public static List<Client> findClients(int s, int m) {
        return getClientStorage().findClients(s, m);
    }

    public static Client findAnonymousClient() {
        return getClientStorage().getAnonymousClient();
    }

    public static Client findImporterClient() {
        return getClientStorage().getImporterClient();
    }

    public static ClientStorage getClientStorage() {
        ClientStorage cats = (ClientStorage) ServiceLocator.getInstance().getService(Tables.CLIENT);
        return cats;
    }

    public static List<Client> findClientByPhone(String phon) {
        return getClientStorage().findClientByPhone(phon);
    }

    public static Long getCount() {
        return getClientStorage().getCount();
    }

    public static List<Client> findUnSyncedClients(long disconnected_at) {
        return getClientStorage().findUnSyncedClients(disconnected_at);
    }

    public static boolean isExists(String uid, LocalDateTime attime) {
        return getClientStorage().isExists(uid, attime);
    }

    public static boolean isExists(String uid) {
        return getClientStorage().isExists(uid);
    }

    public static double getTotalDebt() {
        return getClientStorage().getTotalDebt();
    }
}
