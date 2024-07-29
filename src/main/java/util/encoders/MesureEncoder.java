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
import data.Mesure;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class MesureEncoder implements Encoder.Text<Mesure> {

    public MesureEncoder() {
    }
    
    

    @Override
    public String encode(Mesure ins) throws EncodeException {
        JsonObjectBuilder builder = Json.createObjectBuilder();

        builder.add("uid", ins.getUid())
                .add("description", ins.getDescription())
                .add("quantContenu", ins.getQuantContenu())
                .add("type", Tables.MESURE.name())
                .add("action", ins.getAction())
                .add("priority", ins.getPriority())
                .add("count", ins.getCount())
                .add("counter", ins.getCounter())
                .add("from", ins.getFrom())
                .add("payload", ins.getPayload())
                .add("produitId", Json.createObjectBuilder()
                        .add("uid", ins.getProduitId().getUid()).build());

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
