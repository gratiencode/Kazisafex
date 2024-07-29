/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util.encoders;

import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import java.io.StringWriter;

import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import jakarta.websocket.EndpointConfig;
import data.ClientAppartenir;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class ClientAppartenirEncoder implements Encoder.Text<ClientAppartenir> {

    public ClientAppartenirEncoder() {
    }

    @Override
    public String encode(ClientAppartenir oper) throws EncodeException {
        JsonObjectBuilder builder = Json.createObjectBuilder();

        builder.add("uid", oper.getUid())
                .add("region", oper.getRegion())
                .add("type", Tables.CLIENTAPPARTENIR.name())
                .add("action", oper.getAction())
                .add("count", oper.getCount())
                .add("priority", oper.getPriority())
                .add("counter", oper.getCounter())
                .add("payload", oper.getPayload())
                .add("from", oper.getFrom())
                .add("date", tools.Constants.Datetime.format(oper.getDateAppartenir()))
                .add("clientId", Json.createObjectBuilder()
                        .add("uid", oper.getClientId().getUid()).build())
                .add("clientOrganisationId", Json.createObjectBuilder()
                        .add("uid", oper.getClientOrganisationId().getUid()).build());

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
