/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util.decoders;

import utilities.ImageProduit; 
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.websocket.DecodeException;
import jakarta.websocket.Decoder;
import jakarta.websocket.EndpointConfig;

/**
 *
 * @author eroot
 */
public class ImageProductDecoder implements Decoder.Binary<ImageProduit> {

    public ImageProductDecoder() {
    }
    

    @Override
    public ImageProduit decode(ByteBuffer arg0) throws DecodeException {
        if (arg0.hasArray()) {
            ObjectInputStream is = null;
            try {
                byte[] array = arg0.array();
                ByteArrayInputStream in = new ByteArrayInputStream(array);
                is = new ObjectInputStream(in);
                return (ImageProduit) is.readObject();
            } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(ImageProductDecoder.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    is.close();
                } catch (IOException ex) {
                    Logger.getLogger(ImageProductDecoder.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }
        return null;
    }

    @Override
    public boolean willDecode(ByteBuffer arg0) {
        return arg0.hasArray();
    }

    @Override
    public void init(EndpointConfig config) {

    }

    @Override
    public void destroy() {

    }

}
