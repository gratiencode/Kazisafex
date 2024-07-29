/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delegates;

import IServices.ClientOrganisationStorage;
import static delegates.ClientOrganisationDelegate.getClientOrganisationStorage;
import java.util.List;
import data.ClientOrganisation;
import tools.ServiceLocator;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class ClientOrganisationDelegate {
    
    public static ClientOrganisation saveClientOrganisation(ClientOrganisation cat) {
        return getClientOrganisationStorage().createClientOrganisation(cat);
    }

    public static ClientOrganisation updateClientOrganisation(ClientOrganisation cat) {
        return getClientOrganisationStorage().updateClientOrganisation(cat);
    }

    public static void deleteClientOrganisation(ClientOrganisation cat) {
        getClientOrganisationStorage().deleteClientOrganisation(cat);
    }

    public static ClientOrganisation findClientOrganisation(String objId) {
        return getClientOrganisationStorage().findClientOrganisation(objId);
    }
    
    
    public static List<ClientOrganisation> findClientOrganisations(){
       return getClientOrganisationStorage().findClientOrganisations();
    }
    
    public static List<ClientOrganisation> findClientOrganisations(int s,int m){
       return getClientOrganisationStorage().findClientOrganisations(s,m);
    }
    
    public static ClientOrganisationStorage getClientOrganisationStorage(){
        ClientOrganisationStorage cats=(ClientOrganisationStorage)ServiceLocator.getInstance().getService(Tables.CLIENTORGANISATION);
        return cats;
    }

    public static Long getCount() {
       return getClientOrganisationStorage().getCount();
    }
}
