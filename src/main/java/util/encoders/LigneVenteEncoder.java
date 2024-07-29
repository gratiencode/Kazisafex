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
import data.LigneVente;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class LigneVenteEncoder implements Encoder.Text<LigneVente> {

    public LigneVenteEncoder() {
    }
    
    

    @Override
    public String encode(LigneVente ins) throws EncodeException {
        JsonObjectBuilder builder = Json.createObjectBuilder();

        builder.add("uid", ins.getUid())
                .add("numlot", ins.getNumlot())
                .add("type", Tables.LIGNEVENTE.name())
                .add("action", ins.getAction())
                .add("count", ins.getCount())
                .add("priority", ins.getPriority())
                .add("counter", ins.getCounter())
                .add("prixUnit", ins.getPrixUnit())
                .add("quantite", ins.getQuantite())
                .add("from", ins.getFrom())
                .add("payload", ins.getPayload())
                .add("montantCdf", ins.getMontantCdf())
                .add("montantUsd", ins.getMontantUsd())
                .add("productId", Json.createObjectBuilder()
                        .add("uid", ins.getProductId().getUid()).build())
                .add("mesureId", Json.createObjectBuilder()
                        .add("uid", ins.getMesureId().getUid()).build());

        if (ins.getReference() != null) {
            builder.add("reference", Json.createObjectBuilder()
                    .add("uid", ins.getReference().getUid()).build());
        }
        if (ins.getClientId() != null) {
            builder.add("clientId", ins.getClientId());
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
