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

    public static void mergeClient(Client local, Client incoming) {
        if (incoming.getNomClient() != null && !incoming.getNomClient().trim().isEmpty()) {
            local.setNomClient(incoming.getNomClient().trim());
        }
        if (incoming.getPhone() != null && !incoming.getPhone().trim().isEmpty()) {
            local.setPhone(incoming.getPhone().trim());
        }
        if (incoming.getEmail() != null && !incoming.getEmail().trim().isEmpty()) {
            local.setEmail(incoming.getEmail().trim());
        }
        if (incoming.getAdresse() != null && !incoming.getAdresse().trim().isEmpty()) {
            local.setAdresse(incoming.getAdresse().trim());
        }
        if (incoming.getTypeClient() != null && !incoming.getTypeClient().trim().isEmpty()) {
            local.setTypeClient(incoming.getTypeClient().trim());
        }
        if (incoming.getParentId() != null) {
            if (incoming.getParentId().getUid().equals(incoming.getUid())) {
                local.setParentId(local);
            } else {
                Client parent = findClient(incoming.getParentId().getUid());
                if (parent != null) {
                    local.setParentId(parent);
                } else {
                    local.setParentId(incoming.getParentId());
                }
            }
        }
        if (incoming.getDeletedAt() != null) {
            local.setDeletedAt(incoming.getDeletedAt());
        }
        local.setUpdatedAt(LocalDateTime.now());
    }

    public static Client syncClientSafe(Client client) {
        // 1. Try exact UID match first
        Client localClient = findClient(client.getUid());
        // 2. Fallback: find by phone
        if (localClient == null && client.getPhone() != null && !client.getPhone().trim().isEmpty()) {
            List<Client> byPhone = findClientByPhone(client.getPhone().trim());
            if (byPhone != null && !byPhone.isEmpty()) {
                // If multiple by phone, prefer the one with matching name
                if (client.getNomClient() != null && !client.getNomClient().trim().isEmpty()) {
                    String incomingName = client.getNomClient().trim().toLowerCase();
                    for (Client candidate : byPhone) {
                        if (candidate.getNomClient() != null && candidate.getNomClient().trim().toLowerCase().equals(incomingName)) {
                            localClient = candidate;
                            break;
                        }
                    }
                }
                if (localClient == null) {
                    localClient = byPhone.get(0);
                }
            }
        }
        if (localClient == null) {
            return saveClient(client);
        } else {
            mergeClient(localClient, client);
            return updateClient(localClient);
        }
    }

    public static void mergeDuplicateClients(Client keeper, Client duplicate) {
        getClientStorage().mergeDuplicateClients(keeper, duplicate);
    }

    public static int mergeAllDuplicateClients() {
        return getClientStorage().mergeAllDuplicateClients();
    }
}


