/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IServices;

import java.util.List;
import java.util.Set;
import data.Client;
import data.Client;

/**
 *
 * @author eroot
 */
public interface ClientStorage {
     public Client createClient(Client obj);
    public Client updateClient(Client obj);
    public void deleteClient(Client obj);
    public Long getCount();
    public Client findClient(String objId);
    public Client getAnonymousClient();
    public List<Client> findClients();
    public List<Client> findClients(int start,int max);
    public List<Client> findClientByPhone(String phone);
    public Client getImporterClient();
     public List<Client> mergeSet(Set<Client> bulk);
}
