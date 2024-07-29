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
import data.Abonnement;
import tools.Constants;
import tools.Tables;


/**
 *
 * @author eroot
 */
public class AbonnementEncoder implements Encoder.Text<Abonnement>{

    public AbonnementEncoder() {
    }
    

    @Override
    public String encode(Abonnement obj) throws EncodeException {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("uid", obj.getUid())
                .add("typeAbonnement",obj.getTypeAbonnement())
                .add("nombreOperation",obj.getNombreOperation())
                .add("etat",obj.getEtat())
                .add("dateAbonnement",Constants.dateFormater.format(obj.getDateAbonnement()))
                .add("type", Tables.ABONNEMENT.name())
                .add("action", "read")
                .add("priority", obj.getPriority())
                .add("from", obj.getFrom())
                .add("payload", obj.getPayload())
                .add("count", obj.getCount())
                .add("counter", obj.getCounter());
        StringWriter sw = new StringWriter();
        Json.createWriter(sw).writeObject(builder.build());
        String data=sw.toString();
        System.err.println("OBJ "+data);
        return data;
    }

    @Override
    public void init(EndpointConfig config) {
    }

    @Override
    public void destroy() {
     
    }
    
}
