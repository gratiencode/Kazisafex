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
import data.Depense;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class DepenseEncoder implements Encoder.Text<Depense> {

    public DepenseEncoder() {
    }
    
    

    @Override
    public String encode(Depense bill) throws EncodeException {
        JsonObjectBuilder builder = Json.createObjectBuilder();

        builder.add("uid", bill.getUid())
                .add("nomDepense", bill.getNomDepense())
                .add("type", Tables.DEPENSE.name())
                .add("action", bill.getAction())
                .add("count", bill.getCount())
                .add("priority", bill.getPriority())
                .add("counter", bill.getCounter())
                .add("from", bill.getFrom())
                .add("payload", bill.getPayload())
                .add("region", bill.getRegion());

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
