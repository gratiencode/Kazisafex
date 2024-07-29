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
import data.PrixDeVente;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class PrixDeVenteEncoder implements Encoder.Text<PrixDeVente> {

    public PrixDeVenteEncoder() {
    }
    
    

    @Override
    public String encode(PrixDeVente ins) throws EncodeException {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("uid", ins.getUid())
                .add("qmax", ins.getQmax())
                .add("qmin", ins.getQmin())
                .add("devise", ins.getDevise())
                .add("type", Tables.PRIXDEVENTE.name())
                .add("action", ins.getAction())
                .add("count", ins.getCount())
                .add("priority", ins.getPriority())
                .add("counter", ins.getCounter())
                .add("from", ins.getFrom())
                .add("payload", ins.getPayload())
                .add("prixUnitaire", ins.getPrixUnitaire());
        builder.add("mesureId", Json.createObjectBuilder()
                .add("uid", ins.getMesureId().getUid()));
        builder.add("recquisitionId", Json.createObjectBuilder()
                .add("uid", ins.getRecquisitionId().getUid()).build());
        StringWriter sw = new StringWriter();
        Json.createWriter(sw).writeObject(builder.build());
        String data = sw.toString();
        System.err.println("OBJ " + data);
        return data;
    }

    private static final Logger LOG = Logger.getLogger(PrixDeVenteEncoder.class.getName());

    @Override
    public void init(EndpointConfig config) {
        LOG.info("PrixDevente init encoder");
    }

    @Override
    public void destroy() {
        LOG.info("PrixDevente destroy encoder");
    }

}
