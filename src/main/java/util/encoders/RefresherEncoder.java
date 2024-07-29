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
import data.Refresher;
import java.util.prefs.Preferences;
import tools.SyncEngine;

/**
 *
 * @author eroot
 */
public class RefresherEncoder implements Encoder.Text<Refresher> {
    Preferences pref=Preferences.userNodeForPackage(SyncEngine.class);
    public RefresherEncoder() {
    }

    
    
    @Override
    public String encode(Refresher object) throws EncodeException {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        
        builder.add("target", object.getTarget())
                .add("type", "REFRESH")
                 .add("action", "read")
                .add("priority", object.getPriority())
                 .add("count", 1)
                .add("from", pref.get("userid", ""))
                .add("payload", object.getPayload())
                 .add("counter", 1);
        
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
