/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util.encoders;

import java.io.StringWriter;
import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;

import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import jakarta.websocket.EndpointConfig;
import data.ClientOrganisation;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class ClientOrganisationEncoder implements Encoder.Text<ClientOrganisation> {

    public ClientOrganisationEncoder() {
    }
    
    

    @Override
    public String encode(ClientOrganisation oper) throws EncodeException {
        JsonObjectBuilder builder = Json.createObjectBuilder();

        builder.add("uid", oper.getUid())
                .add("adresse", oper.getAdresse())
                .add("boitePostalOrganisation", oper.getBoitePostalOrganisation())
                .add("domaineOrganisation", oper.getDomaineOrganisation())
                .add("emailOrganisation", oper.getEmailOrganisation())
                .add("region", oper.getRegion())
                .add("nomOrganisation", oper.getNomOrganisation())
                .add("phoneOrganisation", oper.getPhoneOrganisation())
                .add("rccmOrganisation", oper.getRccmOrganisation())
                .add("type", Tables.CLIENTORGANISATION.name())
                .add("action", oper.getAction())
                .add("count", oper.getCount())
                .add("from", oper.getFrom())
                .add("payload", oper.getPayload())
                .add("priority", oper.getPriority())
                .add("counter", oper.getCounter())
                .add("websiteOrganisation", oper.getWebsiteOrganisation());

        StringWriter sw = new StringWriter();
        Json.createWriter(sw).writeObject(builder.build());
        String data = sw.toString();
        System.err.println("OBJ " + data);
        return data;

    }

    @Override
    public void init(EndpointConfig config) {

    }

    @Override
    public void destroy() {

    }

}
