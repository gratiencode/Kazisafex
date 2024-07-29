/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util.encoders;


import java.io.StringWriter;
import java.util.logging.Logger;
import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import jakarta.websocket.EndpointConfig;
import data.Category;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class CategoryEncoder implements Encoder.Text<Category> {

    public CategoryEncoder() {
    }
    

    @Override
    public String encode(Category category) throws EncodeException {

        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("uid", category.getUid())
                .add("descritption", category.getDescritption())
                .add("type", Tables.CATEGORY.name())
                .add("action", category.getAction())
                 .add("priority", category.getPriority())
                .add("count", category.getCount())
                .add("from", category.getFrom())
                 .add("payload", category.getPayload())
                .add("counter", category.getCounter());
        
        StringWriter sw = new StringWriter();
        Json.createWriter(sw).writeObject(builder.build());
        String data=sw.toString();
        System.err.println("OBJ "+data);
        return data;
    }
    private static final Logger LOG = Logger.getLogger(CategoryEncoder.class.getName());

    @Override
    public void init(EndpointConfig config) {
        LOG.info("Category init encoder");
    }

    @Override
    public void destroy() {

    }

}
