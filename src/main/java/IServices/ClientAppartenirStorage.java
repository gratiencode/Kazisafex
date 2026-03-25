/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IServices;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Set;
import data.ClientAppartenir;

/**
 *
 * @author eroot
 */
public interface ClientAppartenirStorage {
    public ClientAppartenir createClientAppartenir(ClientAppartenir cat);
    public ClientAppartenir updateClientAppartenir(ClientAppartenir cat);
    public void deleteClientAppartenir(ClientAppartenir cat);
    public Long getCount();
    public ClientAppartenir findClientAppartenir(String catId);
    public List<ClientAppartenir> findClientAppartenirs();
    public List<ClientAppartenir> findClientAppartenirs(int start,int max);
    public List<ClientAppartenir> findClientAppartenirByClient(String clientId);
    public List<ClientAppartenir> findClientAppartenirByOrganisation(String orgId);
     public List<ClientAppartenir> mergeSet(Set<ClientAppartenir> bulk);
     public boolean isExists(String uid);public boolean isExists(String uid, LocalDateTime atime);
    public List<ClientAppartenir> findUnSyncedClientAppartenirs(long disconnected_at);

}
