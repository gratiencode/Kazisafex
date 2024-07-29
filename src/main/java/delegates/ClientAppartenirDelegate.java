/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delegates;

import IServices.ClientAppartenirStorage;
import java.util.List;
import data.ClientAppartenir;
import tools.ServiceLocator;
import tools.Tables;
import static delegates.ClientAppartenirDelegate.getStorage;

/**
 *
 * @author eroot
 */
public class ClientAppartenirDelegate {
    
    
    public static ClientAppartenir saveClientAppartenir(ClientAppartenir cat) {
        return getStorage().createClientAppartenir(cat);
    }

    public static ClientAppartenir updateClientAppartenir(ClientAppartenir cat) {
        return getStorage().updateClientAppartenir(cat);
    }

    public static void deleteClientAppartenir(ClientAppartenir cat) {
        getStorage().deleteClientAppartenir(cat);
    }

    public static ClientAppartenir findClientAppartenir(String objId) {
        return getStorage().findClientAppartenir(objId);
    }
    
    
    public static List<ClientAppartenir> findClientAppartenirs(){
       return getStorage().findClientAppartenirs();
    }
    
    public static List<ClientAppartenir> findClientAppartenirs(int s,int m){
       return getStorage().findClientAppartenirs(s,m);
    }
    
    public static ClientAppartenirStorage getStorage(){
        ClientAppartenirStorage cats=(ClientAppartenirStorage)ServiceLocator.getInstance().getService(Tables.CLIENTAPPARTENIR);
        return cats;
    }

    public static List<ClientAppartenir> findAppartenanceFor(String cluid) {
        return getStorage().findClientAppartenirByClient(cluid);
    }

    public static Long getCount() {
        return getStorage().getCount();
    }
}
