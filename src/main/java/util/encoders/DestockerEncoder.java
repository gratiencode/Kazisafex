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
import data.Destocker;
import tools.Constants;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class DestockerEncoder implements Encoder.Text<Destocker> {

    public DestockerEncoder() {
    }
    
    

    @Override
    public String encode(Destocker ins) throws EncodeException {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("uid", ins.getUid())
                .add("libelle", ins.getLibelle() == null ? "" : ins.getLibelle())
                .add("coutAchat", ins.getCoutAchat())
                .add("observation", ins.getObservation() == null ? "" : ins.getObservation())
                .add("numlot", ins.getNumlot() == null ? Constants.TIMESTAMPED_FORMAT.format(ins.getDateDestockage()) : ins.getNumlot())
                .add("reference", ins.getReference())
                .add("region", ins.getRegion())
                .add("quantite", ins.getQuantite())
                .add("type", Tables.DESTOCKER.name())
                .add("action", ins.getAction())
                .add("count", ins.getCount())
                .add("priority", ins.getPriority())
                .add("counter", ins.getCounter())
                .add("from", ins.getFrom())
                .add("payload", ins.getPayload())
                .add("destination", ins.getDestination())
                .add("dateDestockage", Constants.DATE_HEURE_FORMAT.format(ins.getDateDestockage()))
                .add("mesureId", Json.createObjectBuilder()
                        .add("uid", ins.getMesureId().getUid()).build())
                .add("productId", Json.createObjectBuilder()
                        .add("uid", ins.getProductId().getUid()).build());

        StringWriter sw = new StringWriter();
        Json.createWriter(sw).writeObject(builder.build());
        String data = sw.toString();
        System.err.println("OBJ " + data);
        return data;

    }
    private static final Logger LOG = Logger.getLogger(Destocker.class.getName());

    @Override
    public void init(EndpointConfig config) {
        LOG.info("Destockerinit encoder");
    }

    @Override
    public void destroy() {
        LOG.info("Destocker destroy encoder");
    }

}
