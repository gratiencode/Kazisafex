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
import data.Produit;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class ProduitEncoder implements Encoder.Text<Produit> {

    public ProduitEncoder() {
    }
    
    

    @Override
    public String encode(Produit p) throws EncodeException {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add("uid", p.getUid())
                .add("nomProduit", p.getNomProduit() == null ? "" : p.getNomProduit())
                .add("marque", p.getMarque() == null ? "" : p.getMarque())
                .add("modele", p.getModele() == null ? "" : p.getModele())
                .add("couleur", "")
                .add("codebar", p.getCodebar() == null ? "" : p.getCodebar())
                .add("taille", "")
                .add("type", Tables.PRODUIT.name())
                .add("action", p.getAction())
                .add("count", p.getCount())
                .add("priority", p.getPriority())
                .add("from", p.getFrom())
                .add("payload", p.getPayload())
                .add("counter", p.getCounter())
                .add("categoryId", Json.createObjectBuilder()
                        .add("uid", p.getCategoryId().getUid()).build());
        if (p.getCouleur() != null) {
            builder.add("couleur", p.getCouleur());
        }
        if (p.getTaille() != null) {
            builder.add("taille", p.getTaille());
        }
        if (p.getMethodeInventaire() != null) {
            builder.add("methodeInventaire", p.getMethodeInventaire());
        } else {
            builder.add("methodeInventaire", "FIFO");
        }

        StringWriter sw = new StringWriter();
        Json.createWriter(sw).writeObject(builder.build());
        String enc = sw.toString();
        System.err.println("Produit encoded " + enc);
        return enc;

    }

    @Override
    public void init(EndpointConfig config) {
        System.err.println("Produit Encoder init");
    }

    @Override
    public void destroy() {

    }

}
