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
import data.RetourDepot;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class RetourDepotEncoder implements Encoder.Text<RetourDepot> {

    public RetourDepotEncoder() {
    }

    @Override
    public String encode(RetourDepot oper) throws EncodeException {
        JsonObjectBuilder builder = Json.createObjectBuilder();

        builder.add("uid", oper.getUid())
                .add("coutAchat", oper.getCoutAchat())
                .add("localisation", oper.getLocalisation())
                .add("motif", oper.getMotif())
                .add("numlot", oper.getNumlot())
                .add("region", oper.getRegion())
                .add("type", Tables.RETOURDEPOT.name())
                .add("action", oper.getAction())
                .add("count", oper.getCount())
                .add("from", oper.getFrom())
                .add("payload", oper.getPayload())
                .add("priority", oper.getPriority())
                .add("counter", oper.getCounter())
                .add("quantite", oper.getQuantite())
                .add("regionDest", oper.getRegionDest())
                .add("regionProv", oper.getRegionProv())
                .add("date", tools.Constants.Datetime.format(oper.getDate()))
                .add("destockerId", Json.createObjectBuilder().add("uid", oper.getDestockerId().getUid()).build())
                .add("recquisitionId", Json.createObjectBuilder().add("uid", oper.getRecquisitionId().getUid()).build())
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
