 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
 package util.encoders;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import jakarta.websocket.EndpointConfig; 
import utilities.ImageProduit;

/**
 *
 * @author eroot
 */
public class ImageProductEncoder implements Encoder.Binary<ImageProduit> {

    public ImageProductEncoder() {
    }
    

    @Override
    public ByteBuffer encode(ImageProduit arg0) throws EncodeException {
        ByteArrayOutputStream boas = new ByteArrayOutputStream();
        try (ObjectOutputStream ois = new ObjectOutputStream(boas)) {
            ois.writeObject(arg0);
            return ByteBuffer.wrap(boas.toByteArray());
        } catch (IOException ex) {
            Logger.getLogger(ImageProductEncoder.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public void init(EndpointConfig config) {
    }

    @Override
    public void destroy() {

    }

}
