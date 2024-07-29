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
import data.Stocker;
import tools.Constants;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class StockerEncoder implements Encoder.Text<Stocker> {

    private static final Logger LOG = Logger.getLogger(StockerEncoder.class.getName());

    public StockerEncoder() {
    }

    @Override
    public void init(EndpointConfig config) {
        LOG.info("Stocker init encoder");
    }

    @Override
    public void destroy() {
        LOG.info("Stocker destroy encoder");
    }

    @Override
    public String encode(Stocker ins) throws EncodeException {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("uid", ins.getUid())
                .add("libelle", ins.getLibelle() == null ? "" : ins.getLibelle())
                .add("coutAchat", ins.getCoutAchat())
                .add("observation", ins.getObservation() == null ? "" : ins.getObservation())
                .add("numlot", ins.getNumlot() == null ? Constants.TIMESTAMPED_FORMAT.format(ins.getDateStocker()) : ins.getNumlot())
                .add("reduction", ins.getReduction())
                .add("prixAchatTotal", ins.getPrixAchatTotal())
                .add("type", Tables.STOCKER.name())
                .add("action", ins.getAction())
                .add("count", ins.getCount())
                .add("priority", ins.getPriority())
                .add("counter", ins.getCounter())
                .add("from", ins.getFrom())
                .add("payload", ins.getPayload())
                .add("region", ins.getRegion())
                .add("quantite", ins.getQuantite())
                .add("localisation", ins.getLocalisation() == null ? "" : ins.getLocalisation())
                .add("stockAlerte", ins.getStockAlerte())
                .add("dateStocker", Constants.DATE_HEURE_FORMAT.format(ins.getDateStocker()))
                .add("livraisId", Json.createObjectBuilder()
                        .add("uid", ins.getLivraisId().getUid()).build())
                .add("mesureId", Json.createObjectBuilder()
                        .add("uid", ins.getMesureId().getUid()).build())
                .add("productId", Json.createObjectBuilder()
                        .add("uid", ins.getProductId().getUid()).build());
        if (ins.getDateExpir() != null) {
            builder.add("dateExpir", Constants.DATE_ONLY_FORMAT.format(ins.getDateExpir()));
        }
        StringWriter sw = new StringWriter();
        Json.createWriter(sw).writeObject(builder.build());
        String data=sw.toString();
        System.err.println("OBJ "+data);
        return data;

    }

}
