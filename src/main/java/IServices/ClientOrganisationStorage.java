/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IServices;

import java.util.List;
import java.util.Set;
import data.ClientOrganisation;

/**
 *
 * @author eroot
 */
public interface ClientOrganisationStorage {
    public ClientOrganisation createClientOrganisation(ClientOrganisation obj);
    public ClientOrganisation updateClientOrganisation(ClientOrganisation obj);
    public void deleteClientOrganisation(ClientOrganisation obj);
    public Long getCount();
    public ClientOrganisation findClientOrganisation(String objId);
    public List<ClientOrganisation> findClientOrganisations();
     public List<ClientOrganisation> findClientOrganisations(int start,int limit);
    public List<ClientOrganisation> findClientOrganisationByName(String objId);
     public List<ClientOrganisation> mergeSet(Set<ClientOrganisation> bulk);
}
