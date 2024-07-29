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
import data.Livraison;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class LivraisonEncoder implements Encoder.Text<Livraison> {

    public LivraisonEncoder() {
    }
    
    

    @Override
    public String encode(Livraison ins) throws EncodeException {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("uid", ins.getUid())
                .add("numPiece", ins.getNumPiece() == null ? "" : ins.getNumPiece())
                .add("observation", ins.getObservation() == null ? "" : ins.getObservation())
                .add("payed", ins.getPayed() == null ? 0 : ins.getPayed())
                .add("reduction", ins.getReduction() == null ? 0 : ins.getReduction())
                .add("type", Tables.LIVRAISON.name())
                .add("action", ins.getAction())
                .add("count", ins.getCount())
                .add("priority", ins.getPriority())
                .add("counter", ins.getCounter())
                .add("from", ins.getFrom())
                .add("payload", ins.getPayload())
                .add("reference", ins.getReference() == null ? "-" : ins.getReference())
                .add("region", ins.getRegion() == null ? "-" : ins.getRegion())
                .add("remained", ins.getRemained() == null ? 0 : ins.getRemained())
                .add("topay", ins.getTopay() == null ? 0 : ins.getTopay())
                .add("toreceive", ins.getToreceive() == null ? 0 : ins.getToreceive())
                .add("dateLivr", ins.getDateLivr() == null ? "-"
                        :ins.getDateLivr().toString()) 
                .add("fournId", Json.createObjectBuilder()
                        .add("uid", ins.getFournId() == null ? "-" : ins.getFournId().getUid()).build());
        if (ins.getLibelle() != null) {
            builder.add("libelle", ins.getLibelle());
        } else {
            builder.add("libelle", "-");
        }
        StringWriter sw = new StringWriter();
        Json.createWriter(sw).writeObject(builder.build());
        String data = sw.toString();
        System.err.println("OBJ " + data);
        return data;
    }
    private static final Logger LOG = Logger.getLogger(LivraisonEncoder.class.getName());

    @Override
    public void init(EndpointConfig config) {
        LOG.info("Livraiosn init encoder");
    }

    @Override
    public void destroy() {
        LOG.info("Livraiosn destroy encoder");
    }

}
