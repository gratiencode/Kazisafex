/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util.listencoders;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import jakarta.websocket.EndpointConfig;
import data.Aretirer;
import data.Category;
import data.Client;
import data.ClientAppartenir;
import data.ClientOrganisation;
import data.CompteTresor;
import data.Depense;
import data.Destocker;
import data.Facture;
import data.Fournisseur;
import data.LigneVente;
import data.Livraison;
import data.Mesure;
import data.Operation;
import data.PrixDeVente;
import data.Produit;
import data.Recquisition;
import data.RetourDepot;
import data.RetourMagasin;
import data.Stocker;
import data.Traisorerie;
import data.Vente;

/**
 *
 * @author eroot
 */
public class ListEncoder implements Encoder.Text<List<Object>> {

    public ListEncoder() {
    }

    
    private static final Logger LOG = Logger.getLogger(ListEncoder.class.getName());

    @Override
    public void init(EndpointConfig config) {
        LOG.info("List init encoder");
    }

    @Override
    public void destroy() {
        LOG.info("List destroy encoder");
    }
   
    @Override
    public String encode(List<Object> objects) throws EncodeException {
        try {
            ObjectMapper obm = new ObjectMapper();
            obm.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            StringBuilder sb=new StringBuilder();
            Object objt = objects.get(0);
            if (objt instanceof Fournisseur) {
                List<Fournisseur> objets = objects.stream()
                        .filter(obj -> obj instanceof Fournisseur)
                        .map(obj -> (Fournisseur) obj) 
                        .collect(Collectors.toList());
                sb.append(obm.writeValueAsString(objets));
            } else if (objt instanceof Client) {
                List<Client> objets = objects.stream()
                        .filter(obj -> obj instanceof Client)
                        .map(obj -> (Client) obj) 
                        .collect(Collectors.toList());
                sb.append(obm.writeValueAsString(objets));
            } else if (objt instanceof Recquisition) {
                List<Recquisition> objets = objects.stream()
                        .filter(obj -> obj instanceof Recquisition)
                        .map(obj -> (Recquisition) obj) 
                        .collect(Collectors.toList());
                sb.append(obm.writeValueAsString(objets));
            } else if (objt instanceof Vente) {
                List<Vente> objets = objects.stream()
                        .filter(obj -> obj instanceof Vente)
                        .map(obj -> (Vente) obj) 
                        .collect(Collectors.toList());
                sb.append(obm.writeValueAsString(objets));
            } else if (objt instanceof LigneVente) {
               List<LigneVente> objets = objects.stream()
                        .filter(obj -> obj instanceof LigneVente)
                        .map(obj -> (LigneVente) obj) 
                        .collect(Collectors.toList());
                sb.append(obm.writeValueAsString(objets));
            } else if (objt instanceof PrixDeVente) {
               List<PrixDeVente> objets = objects.stream()
                        .filter(obj -> obj instanceof PrixDeVente)
                        .map(obj -> (PrixDeVente) obj) 
                        .collect(Collectors.toList());
                sb.append(obm.writeValueAsString(objets));
            } else if (objt instanceof Destocker) {
               List<Destocker> objets = objects.stream()
                        .filter(obj -> obj instanceof Destocker)
                        .map(obj -> (Destocker) obj) 
                        .collect(Collectors.toList());
                sb.append(obm.writeValueAsString(objets));
            } else if (objt instanceof Stocker) {
               List<Stocker> objets = objects.stream()
                        .filter(obj -> obj instanceof Stocker)
                        .map(obj -> (Stocker) obj) 
                        .collect(Collectors.toList());
                sb.append(obm.writeValueAsString(objets));
            } else if (objt instanceof Mesure) {
              List<Mesure> objets = objects.stream()
                        .filter(obj -> obj instanceof Mesure)
                        .map(obj -> (Mesure) obj) 
                        .collect(Collectors.toList());
                sb.append(obm.writeValueAsString(objets));
            } else if (objt instanceof Produit) {
             List<Produit> objets = objects.stream()
                        .filter(obj -> obj instanceof Produit)
                        .map(obj -> (Produit) obj) 
                        .collect(Collectors.toList());
                sb.append(obm.writeValueAsString(objets));
            } else if (objt instanceof Category) {
               List<Category> objets = objects.stream()
                        .filter(obj -> obj instanceof Category)
                        .map(obj -> (Category) obj) 
                        .collect(Collectors.toList());
                sb.append(obm.writeValueAsString(objets));
            } else if (objt instanceof Livraison) {
              List<Livraison> objets = objects.stream()
                        .filter(obj -> obj instanceof Livraison)
                        .map(obj -> (Livraison) obj) 
                        .collect(Collectors.toList());
                sb.append(obm.writeValueAsString(objets));
            } else if (objt instanceof Traisorerie) {
              List<Traisorerie> objets = objects.stream()
                        .filter(obj -> obj instanceof Traisorerie)
                        .map(obj -> (Traisorerie) obj) 
                        .collect(Collectors.toList());
                sb.append(obm.writeValueAsString(objets));
            } else if (objt instanceof Operation) {
              List<Operation> objets = objects.stream()
                        .filter(obj -> obj instanceof Operation)
                        .map(obj -> (Operation) obj) 
                        .collect(Collectors.toList());
                sb.append(obm.writeValueAsString(objets));
            } else if (objt instanceof Depense) {
             List<Depense> objets = objects.stream()
                        .filter(obj -> obj instanceof Depense)
                        .map(obj -> (Depense) obj) 
                        .collect(Collectors.toList());
                sb.append(obm.writeValueAsString(objets));
            } else if (objt instanceof CompteTresor) {
             List<CompteTresor> objets = objects.stream()
                        .filter(obj -> obj instanceof CompteTresor)
                        .map(obj -> (CompteTresor) obj) 
                        .collect(Collectors.toList());
                sb.append(obm.writeValueAsString(objets));
            } else if (objt instanceof Aretirer) {
            List<Aretirer> objets = objects.stream()
                        .filter(obj -> obj instanceof Aretirer)
                        .map(obj -> (Aretirer) obj) 
                        .collect(Collectors.toList());
                sb.append(obm.writeValueAsString(objets));
            } else if (objt instanceof RetourMagasin) {
              List<RetourMagasin> objets = objects.stream()
                        .filter(obj -> obj instanceof RetourMagasin)
                        .map(obj -> (RetourMagasin) obj) 
                        .collect(Collectors.toList());
                sb.append(obm.writeValueAsString(objets));
            } else if (objt instanceof RetourDepot) {
             List<RetourDepot> objets = objects.stream()
                        .filter(obj -> obj instanceof RetourDepot)
                        .map(obj -> (RetourDepot) obj) 
                        .collect(Collectors.toList());
                sb.append(obm.writeValueAsString(objets));
            } else if (objt instanceof Facture) {
                List<Facture> objets = objects.stream()
                        .filter(obj -> obj instanceof Facture)
                        .map(obj -> (Facture) obj) 
                        .collect(Collectors.toList());
                sb.append(obm.writeValueAsString(objets));
            } else if (objt instanceof ClientAppartenir) {
              List<ClientAppartenir> objets = objects.stream()
                        .filter(obj -> obj instanceof ClientAppartenir)
                        .map(obj -> (ClientAppartenir) obj) 
                        .collect(Collectors.toList());
                sb.append(obm.writeValueAsString(objets));
            } else if (objt instanceof ClientOrganisation) {
             List<ClientOrganisation> objets = objects.stream()
                        .filter(obj -> obj instanceof ClientOrganisation)
                        .map(obj -> (ClientOrganisation) obj) 
                        .collect(Collectors.toList());
                sb.append(obm.writeValueAsString(objets));
            } 
            String rst= sb.toString();
            System.err.println("Result LIST to SEND "+rst);
            return rst;
        } catch (JsonProcessingException ex) {
            LOG.log(Level.SEVERE, null, ex);
            return null;
        }
    }

}
