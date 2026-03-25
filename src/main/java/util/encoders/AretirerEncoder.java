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
import data.Aretirer;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class AretirerEncoder implements Encoder.Text<Aretirer>{

    public AretirerEncoder() {
    }
    
    

    @Override
    public String encode(Aretirer oper) throws EncodeException { 
        JsonObjectBuilder builder = Json.createObjectBuilder(); 
        
         builder.add("uid", oper.getUid())
                    .add("numlot", oper.getNumlot())
                    .add("prixVente", oper.getPrixVente())
                    .add("quantite", oper.getQuantite())
                    .add("referenceVente", oper.getReferenceVente())
                    .add("region", oper.getRegion())
                    .add("status", oper.getStatus())
                 .add("type", Tables.ARETIRER.name())
                  .add("action", oper.getAction())
                  .add("priority", oper.getPriority())
                 .add("from", oper.getFrom())
                  .add("payload", oper.getPayload())
                 .add("count", oper.getCount())
                 .add("counter", oper.getCounter())
                    .add("date", oper.getDate().toString())
                    .add("clientId", Json.createObjectBuilder().add("uid", oper.getClientId().getUid()).build())
                    .add("ligneVenteId", Json.createObjectBuilder().add("uid", oper.getLigneVenteId().getUid()).build())
                    .add("mesureId", Json.createObjectBuilder().add("uid", oper.getMesureId().getUid()).build());

        
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
