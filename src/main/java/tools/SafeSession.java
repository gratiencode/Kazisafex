/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tools;

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
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import services.SafeConnectionFactory;

/**
 *
 * @author endeleya
 */
public class SafeSession {
    private static SafeSession instance;

    public static SafeSession getInstance() {
        if(instance==null){
            instance=new SafeSession();
        }
        return instance;
    }
    private SafeSession() {
    }
    
    public List<Category> findCategories() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT c.uid, c.descritption FROM category c ");
            Query query = SafeConnectionFactory.getEntityManager().createNativeQuery(sb.toString(), Category.class);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    public List<Produit> findProduits() {
        try {
            Query query = SafeConnectionFactory.getEntityManager().createNamedQuery("Produit.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    public List<Mesure> findMesures() {
        try {
            Query query = SafeConnectionFactory.getEntityManager().createNamedQuery("Mesure.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    public List<Fournisseur> findFournisseurs() {
        try {
            Query query = SafeConnectionFactory.getEntityManager().createNativeQuery("Select * from fournisseur", Fournisseur.class);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    public List<Livraison> findLivraisons() {
        try {
            Query query = SafeConnectionFactory.getEntityManager().createNamedQuery("Livraison.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    public List<Stocker> findStockers() {
        try {
            Query query = SafeConnectionFactory.getEntityManager().createNamedQuery("Stocker.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    public List<Destocker> findDestockers() {
        try {
            Query query = SafeConnectionFactory.getEntityManager().createNamedQuery("Destocker.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Recquisition> findRecquisitions() {
        try {
            Query query = SafeConnectionFactory.getEntityManager().createNamedQuery("Recquisition.findAll");
            if (query == null) {
                return new ArrayList<>();
            }
            List<Recquisition> rst = query.getResultList();
            return rst == null ? new ArrayList<>() : rst;
        } catch (NoResultException e) {
            return null;
        }
    }
    
    public List<RetourMagasin> findRetourMagasins() {
        try{
            Query query= SafeConnectionFactory.getEntityManager().createNamedQuery("RetourMagasin.findAll");
            return query.getResultList();
        }catch(NoResultException e){
            return null;
        }
    }
    
    
    public List<PrixDeVente> findPrixDeVentes() {
        try {
            Query query = SafeConnectionFactory.getEntityManager().createNamedQuery("PrixDeVente.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    public List<Vente> findVentes() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM vente v ORDER BY v.dateVente DESC ");
            Query query = SafeConnectionFactory.getEntityManager()
                    .createNativeQuery(sb.toString(), Vente.class);
            return query.getResultList();
        } catch (NoResultException | jakarta.persistence.EntityNotFoundException e) {
            return null;
        }
    }
    
     public List<Facture> findFactures() {
        try{
            Query query=SafeConnectionFactory.getEntityManager().createNamedQuery("Facture.findAll");
            return query.getResultList();
        }catch(NoResultException e){
            return null;
        }
    }
    
    public List<Aretirer> findAretirer() {
        try{
            Query query=SafeConnectionFactory.getEntityManager().createNamedQuery("Aretirer.findAll");
            return query.getResultList();
        }catch(NoResultException e){
            return null;
        }
    }
    
    public List<LigneVente> findLigneVentes() {
        try {
            Query query = SafeConnectionFactory.getEntityManager().createNamedQuery("LigneVente.findAll");
            return query.getResultList();
        } catch (NoResultException | jakarta.persistence.EntityNotFoundException e) {
            return null;
        }
    }
    
    
    public List<Traisorerie> findTraisoreries() {
        try {
            Query query = SafeConnectionFactory.getEntityManager().createNamedQuery("Traisorerie.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    public List<Operation> findOperations() {
        try{
            Query query=  SafeConnectionFactory.getEntityManager().createNamedQuery("Operation.findAll");
            return query.getResultList();
        }catch(NoResultException e){
            return null;
        }
    }
    
    public List<RetourDepot> findRetourDepots() {
        try{
            Query query= SafeConnectionFactory.getEntityManager().createNamedQuery("RetourDepot.findAll");
            return query.getResultList();
        }catch(NoResultException e){
            return null;
        }
    }
    
    public List<Client> findClients() {
        try{
            Query query= SafeConnectionFactory.getEntityManager().createNamedQuery("Client.findAll");
            return query.getResultList();
        }catch(NoResultException e){
            return null;
        }
    }
    
    public List<ClientOrganisation> findClientOrganisations() {
        try{
            Query query=SafeConnectionFactory.getEntityManager().createNamedQuery("ClientOrganisation.findAll");
            return query.getResultList();
        }catch(NoResultException e){
            return null;
        }
    }
    
    public List<ClientAppartenir> findClientAppartenirs() {
        try {
            Query query = SafeConnectionFactory.getEntityManager().createNamedQuery("ClientAppartenir.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    public List<CompteTresor> findCompteTresors() {
        try {
            Query query = SafeConnectionFactory.getEntityManager().createNamedQuery("CompteTresor.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    public List<Depense> findDepenses() {
        try{
            Query query= SafeConnectionFactory.getEntityManager().createNamedQuery("Depense.findAll");
            return query.getResultList();
        }catch(NoResultException e){
            return null;
        }
    }
    
}
