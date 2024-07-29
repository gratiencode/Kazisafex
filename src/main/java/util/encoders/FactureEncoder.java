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
import data.Facture;
import tools.Constants;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class FactureEncoder implements Encoder.Text<Facture> {

    public FactureEncoder() {
    }
    
    

    @Override
    public String encode(Facture bill) throws EncodeException {
        JsonObjectBuilder builder = Json.createObjectBuilder();

        builder.add("uid", bill.getUid())
                .add("numero", bill.getNumero())
                .add("payedamount", bill.getPayedamount())
                .add("region", bill.getRegion())
                .add("status", bill.getStatus())
                .add("type", Tables.FACTURE.name())
                .add("action", bill.getAction())
                .add("count", bill.getCount())
                .add("priority", bill.getPriority())
                .add("counter", bill.getCounter())
                .add("from", bill.getFrom())
                .add("payload", bill.getPayload())
                .add("totalamount", bill.getTotalamount())
                .add("organisId", Json.createObjectBuilder().add("uid", bill.getOrganisId().getUid()).build());
        if (bill.getStartDate() != null) {
            builder.add("startDate", Constants.DATE_ONLY_FORMAT.format(bill.getStartDate()));
        }
        if (bill.getEndDate() != null) {
            builder.add("endDate", Constants.DATE_ONLY_FORMAT.format(bill.getEndDate()));
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
