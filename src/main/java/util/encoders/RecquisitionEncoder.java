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
import data.Recquisition;
import tools.Constants;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class RecquisitionEncoder implements Encoder.Text<Recquisition> {

    public RecquisitionEncoder() {
    }
    
    

    @Override
    public String encode(Recquisition ins) throws EncodeException {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("uid", ins.getUid())
                .add("reference", ins.getReference())
                .add("coutAchat", ins.getCoutAchat())
                .add("type", Tables.RECQUISITION.name())
                .add("action", ins.getAction())
                .add("count", ins.getCount())
                .add("from", ins.getFrom())
                .add("payload", ins.getPayload())
                .add("priority", ins.getPriority())
                .add("counter", ins.getCounter())
                .add("observation", ins.getObservation() == null ? "" : ins.getObservation())
                .add("numlot", ins.getNumlot() == null ? Constants.TIMESTAMPED_FORMAT.format(ins.getDate()) : ins.getNumlot())
                .add("region", ins.getRegion())
                .add("quantite", ins.getQuantite())
                .add("stockAlert", ins.getStockAlert() == null ? 0 : ins.getStockAlert())
                .add("date", Constants.DATE_HEURE_FORMAT.format(ins.getDate()))
                .add("mesureId", Json.createObjectBuilder()
                        .add("uid", ins.getMesureId().getUid()).build())
                .add("productId", Json.createObjectBuilder()
                        .add("uid", ins.getProductId().getUid()).build());
        if (ins.getDateExpiry() != null) {
            builder.add("dateExpiry", Constants.DATE_ONLY_FORMAT.format(ins.getDateExpiry()));
        }
        StringWriter sw = new StringWriter();
        Json.createWriter(sw).writeObject(builder.build());
        String data = sw.toString();
        System.err.println("OBJ " + data);
        return data;
    }

    private static final Logger LOG = Logger.getLogger(RecquisitionEncoder.class.getName());

    @Override
    public void init(EndpointConfig config) {
        LOG.info("Recquisition init encoder");
    }

    @Override
    public void destroy() {
        LOG.info("Recqusition destroy encoder");
    }

}
