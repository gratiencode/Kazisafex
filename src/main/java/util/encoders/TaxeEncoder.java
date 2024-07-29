/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
 package util.encoders; import java.io.StringWriter;
import jakarta.json.Json; import jakarta.json.JsonObjectBuilder;

import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import jakarta.websocket.EndpointConfig;
import data.Taxe;

/**
 *
 * @author eroot
 */
public class TaxeEncoder implements Encoder.Text<Taxe> {

    public TaxeEncoder() {
    }

    @Override
    public String encode(Taxe ins) throws EncodeException { 
        JsonObjectBuilder builder = Json.createObjectBuilder();
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
