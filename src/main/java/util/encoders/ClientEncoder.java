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
import data.Client;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class ClientEncoder implements Encoder.Text<Client> {

    public ClientEncoder() {
    }
    
    

    @Override
    public String encode(Client ins) throws EncodeException {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("uid", ins.getUid())
                .add("adresse", ins.getAdresse())
                .add("email", ins.getEmail())
                .add("type", Tables.CLIENT.name())
                .add("action", ins.getAction())
                .add("count", ins.getCount())
                .add("from", ins.getFrom())
                .add("payload", ins.getPayload())
                .add("priority", ins.getPriority())
                .add("counter", ins.getCounter())
                .add("typeClient", ins.getTypeClient())
                .add("nomClient", ins.getNomClient())
                .add("phone", ins.getPhone());

        if (ins.getParentId() != null) {
            builder.add("parentId", Json.createObjectBuilder()
                    .add("uid", ins.getParentId().getUid()).build());
        } else {
            builder.add("parentId", Json.createObjectBuilder()
                    .add("uid", "-").build());
        }
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
