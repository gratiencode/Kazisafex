/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util.encoders;

import java.io.StringWriter;
import java.util.logging.Logger;
import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import jakarta.websocket.EndpointConfig;
import data.Fournisseur;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class FournisseurEncoder implements Encoder.Text<Fournisseur> {

    public FournisseurEncoder() {
    }
    
    

    @Override
    public String encode(Fournisseur ins) throws EncodeException {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("uid", ins.getUid())
                .add("type", Tables.FOURNISSEUR.name())
                .add("action", ins.getAction())
                .add("count", ins.getCount())
                .add("priority", ins.getPriority())
                .add("counter", ins.getCounter())
                .add("from", ins.getFrom())
                .add("payload", ins.getPayload())
                .add("adresse", ins.getAdresse() == null ? "-" : ins.getAdresse())
                .add("identification", ins.getIdentification() == null ? "-" : ins.getIdentification())
                .add("nomFourn", ins.getNomFourn() == null ? "-" : ins.getNomFourn());
        if (ins.getPhone() == null) {
            builder.add("phone", "Non disponible");
        } else {
            builder.add("phone", ins.getPhone());
        }
        StringWriter sw = new StringWriter();
        Json.createWriter(sw).writeObject(builder.build());
        String data = sw.toString();
        System.err.println("OBJ " + data);
        return data;

    }
    private static final Logger LOG = Logger.getLogger(FournisseurEncoder.class.getName());

    @Override
    public void init(EndpointConfig config) {
        LOG.info("Fournisseur init encoder");
    }

    @Override
    public void destroy() {
        LOG.info("Fournisseur destroy encoder");
    }

}
