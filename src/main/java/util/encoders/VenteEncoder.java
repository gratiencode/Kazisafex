/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util.encoders;

import java.io.StringWriter;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import jakarta.websocket.EndpointConfig;
import data.Client;
import data.Vente;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class VenteEncoder implements Encoder.Text<Vente> {

    public VenteEncoder() {
    }

    @Override
    public String encode(Vente ins) throws EncodeException {
        JsonObjectBuilder builder = Json.createObjectBuilder();

        builder.add("uid", ins.getUid())
                .add("libelle", ins.getLibelle() == null ? "" : ins.getLibelle())
                .add("latitude", ins.getLatitude())
                .add("observation", ins.getObservation() == null ? "" : ins.getObservation())
                .add("longitude", ins.getLongitude())
                .add("montantCdf", ins.getMontantCdf())
                .add("montantDette", ins.getMontantDette() == null ? 0 : ins.getMontantDette())
                .add("region", ins.getRegion())
                .add("montantUsd", ins.getMontantUsd())
                .add("payment", ins.getPayment())
                .add("reference", ins.getReference())
                .add("type", Tables.VENTE.name())
                .add("action", ins.getAction())
                .add("count", ins.getCount())
                .add("from", ins.getFrom())
                .add("payload", ins.getPayload())
                .add("priority", ins.getPriority())
                .add("counter", ins.getCounter())
                .add("dateVente", ins.getDateVente().toString())
                .add("deviseDette", ins.getDeviseDette());
        JsonObjectBuilder jsob = Json.createObjectBuilder();

        jsob.add("uid", ins.getClientId().getUid())
                .add("adresse", ins.getClientId().getAdresse())
                .add("email", ins.getClientId().getEmail())
                .add("typeClient", ins.getClientId().getTypeClient())
                .add("nomClient", ins.getClientId().getNomClient())
                .add("phone", ins.getClientId().getPhone());
        Client c = ins.getClientId().getParentId();
        if (c == null) {
            jsob.add("parentId", Json.createObjectBuilder().add("uid", ins.getClientId().getUid()).build());
        } else {
            jsob.add("parentId", Json.createObjectBuilder().add("uid", c.getUid()).build()); 
        }
        JsonObject sobj = jsob.build();
        builder.add("clientId", sobj);
        if (ins.getEcheance() != null) {
            builder.add("echeance", tools.Constants.dateFormater.format(ins.getEcheance()));
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
