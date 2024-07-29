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
import data.CompteTresor;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class CompteTresorEncoder implements Encoder.Text<CompteTresor> {

    public CompteTresorEncoder() {
    }
    
    

    @Override
    public String encode(CompteTresor compte) throws EncodeException {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("uid", compte.getUid())
                .add("bankName", compte.getBankName() == null ? "-" : compte.getBankName())
                .add("intitule", compte.getIntitule() == null ? "-" : compte.getIntitule())
                .add("type", Tables.COMPTETRESOR.name())
                .add("action", compte.getAction())
                .add("count", compte.getCount())
                .add("priority", compte.getPriority())
                .add("from", compte.getFrom())
                .add("payload", compte.getPayload())
                .add("counter", compte.getCounter())
                .add("numeroCompte", compte.getNumeroCompte() == null ? "-" : compte.getNumeroCompte())
                .add("region", compte.getRegion() == null ? "-" : compte.getRegion())
                .add("soldeMinimum", compte.getSoldeMinimum() == null ? 0 : compte.getSoldeMinimum())
                .add("typeCompte", compte.getTypeCompte() == null ? "-" : compte.getTypeCompte());
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
