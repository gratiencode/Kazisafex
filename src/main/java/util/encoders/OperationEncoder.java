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
import data.Operation;
import tools.Constants;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class OperationEncoder implements Encoder.Text<Operation> {

    public OperationEncoder() {
    }
    
    

    @Override
    public String encode(Operation ins) throws EncodeException {
        JsonObjectBuilder builder = Json.createObjectBuilder();

        builder.add("uid", ins.getUid())
                .add("libelle", ins.getLibelle())
                .add("mouvement", ins.getMouvement())
                .add("imputation", ins.getImputation())
                .add("montantCdf", ins.getMontantCdf())
                .add("region", ins.getRegion())
                .add("type", Tables.OPERATION.name())
                 .add("action", ins.getAction())
                 .add("count", ins.getCount())
                .add("priority", ins.getPriority())
                 .add("counter", ins.getCounter())
                .add("from", ins.getFrom())
                .add("payload", ins.getPayload())
                .add("tresorId", Json.createObjectBuilder().add("uid", ins.getTresorId() == null ? "" : ins.getTresorId().getUid()))
                .add("depenseId", Json.createObjectBuilder().add("uid", ins.getDepenseId() == null ? "" : ins.getDepenseId().getUid()))
                .add("montantUsd", ins.getMontantUsd())
                .add("referenceOp", ins.getReferenceOp())
                .add("date", Constants.DATE_HEURE_FORMAT.format(ins.getDate()))
                .add("caisseOpId", Json.createObjectBuilder()
                        .add("uid", ins.getCaisseOpId().getUid())
                        .add("libelle", ins.getCaisseOpId().getLibelle())
                        .add("mouvement", ins.getCaisseOpId().getMouvement())
                        .add("typeTresorerie", ins.getCaisseOpId().getTypeTresorerie())
                        .add("montantCdf", ins.getCaisseOpId().getMontantCdf())
                        .add("region", ins.getCaisseOpId().getRegion())
                        .add("montantUsd", ins.getCaisseOpId().getMontantUsd())
                        .add("reference", ins.getCaisseOpId().getReference())
                        .add("date", Constants.DATE_HEURE_FORMAT.format(ins.getCaisseOpId().getDate())).build());

        StringWriter sw = new StringWriter();
        Json.createWriter(sw).writeObject(builder.build());
        String data=sw.toString();
        System.err.println("OBJ "+data);
        return data;

    }

    @Override
    public void init(EndpointConfig config) {

    }

    @Override
    public void destroy() {

    }

}
