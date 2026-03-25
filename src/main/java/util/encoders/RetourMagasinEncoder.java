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
import data.RetourMagasin;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class RetourMagasinEncoder implements Encoder.Text<RetourMagasin> {

    public RetourMagasinEncoder() {
    }

    @Override
    public String encode(RetourMagasin oper) throws EncodeException {
        JsonObjectBuilder builder = Json.createObjectBuilder();

        builder.add("uid", oper.getUid())
                .add("prixVente", oper.getPrixVente())
                .add("referenceVente", oper.getReferenceVente())
                .add("motif", oper.getMotif())
                .add("region", oper.getRegion())
                .add("type", Tables.RETOURMAGASIN.name())
                .add("action", oper.getAction())
                .add("count", oper.getCount())
                .add("from", oper.getFrom())
                .add("payload", oper.getPayload())
                .add("priority", oper.getPriority())
                .add("counter", oper.getCounter())
                .add("quantite", oper.getQuantite())
                .add("date", oper.getDate().toString())
                .add("clientId", Json.createObjectBuilder().add("uid", oper.getClientId().getUid()).build())
                .add("ligneVenteId", Json.createObjectBuilder().add("uid", oper.getLigneVenteId().getUid()).build())
                .add("mesureId", Json.createObjectBuilder().add("uid", oper.getMesureId().getUid()).build());

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
