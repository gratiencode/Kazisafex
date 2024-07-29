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
import data.Traisorerie;
import tools.Constants;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class TraisorerieEncoder implements Encoder.Text<Traisorerie> {

    public TraisorerieEncoder() {
    }

    @Override
    public String encode(Traisorerie ins) throws EncodeException {
        JsonObjectBuilder builder = Json.createObjectBuilder();

        builder.add("uid", ins.getUid())
                .add("libelle", ins.getLibelle())
                .add("mouvement", ins.getMouvement())
                .add("typeTresorerie", ins.getTypeTresorerie())
                .add("montantCdf", ins.getMontantCdf())
                .add("region", ins.getRegion())
                .add("montantUsd", ins.getMontantUsd())
                .add("reference", ins.getReference())
                .add("type", Tables.TRAISORERIE.name())
                .add("action", ins.getAction())
                .add("from", ins.getFrom())
                .add("payload", ins.getPayload())
                .add("count", ins.getCount())
                .add("priority", ins.getPriority())
                .add("counter", ins.getCounter())
                .add("tresorId", Json.createObjectBuilder().add("uid", ins.getTresorId() == null ? "" : ins.getTresorId().getUid()))
                .add("date", Constants.DATE_HEURE_FORMAT.format(ins.getDate()));

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
