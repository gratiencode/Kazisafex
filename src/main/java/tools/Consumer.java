/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import delegates.AretirerDelegate;
import delegates.CategoryDelegate;
import delegates.ClientAppartenirDelegate;
import delegates.ClientDelegate;
import delegates.ClientOrganisationDelegate;
import delegates.CompteTresorDelegate;
import delegates.DepenseDelegate;
import delegates.DestockerDelegate;
import delegates.FactureDelegate;
import delegates.FournisseurDelegate;
import delegates.LigneVenteDelegate;
import delegates.LivraisonDelegate;
import delegates.MesureDelegate;
import delegates.OperationDelegate;
import delegates.PrixDeVenteDelegate;
import delegates.ProduitDelegate;
import delegates.RecquisitionDelegate;
import delegates.RetourDepotDelegate;
import delegates.RetourMagasinDelegate;
import delegates.StockerDelegate;
import delegates.TraisorerieDelegate;
import delegates.VenteDelegate;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import data.Aretirer;
import data.BaseModel;
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
import data.Refresher;
import data.RetourDepot;
import data.RetourMagasin;
import data.Stocker;
import data.Traisorerie;
import data.Vente;

/**
 *
 * @author eroot
 */
public class Consumer implements Runnable {

    BlockingQueue<BaseModel> bases;

    public Consumer(BlockingQueue<BaseModel> bases) {
        this.bases = bases;
    }

    @Override
    public void run() {
        while (true) {
            try {
                System.out.println("Consummer data size " + Thread.currentThread().getName() + " " + bases.remainingCapacity());
                BaseModel elt = bases.take();
                if (elt instanceof Refresher) {
                    break;
                } else {
                    saveNow(elt);
                }
                TimeUnit.SECONDS.sleep(2);
                System.out.println("Consumer Thread - " + Thread.currentThread().getName() + " " + elt);
            } catch (InterruptedException ex) {
                Logger.getLogger(Consumer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private void saveNow(BaseModel obj) {
        if (obj instanceof Category) {
            Category ins = (Category) obj;
            CategoryDelegate.updateCategory(ins);
            
        } else if (obj instanceof Produit ins) {
            Category exist = CategoryDelegate.findCategory(ins.getCategoryId().getUid());
            if (exist != null) {
                ProduitDelegate.updateProduit(ins);
            }
            
        } else if (obj instanceof Mesure ins) {
            Produit p = ProduitDelegate.findProduit(ins.getProduitId().getUid());
            if (p != null) {
                MesureDelegate.updateMesure(ins);
            }
            
        } else if (obj instanceof Fournisseur ins) {
            FournisseurDelegate.updateFournisseur(ins);
            
        } else if (obj instanceof Livraison) {
            Livraison ins = (Livraison) obj;
            LivraisonDelegate.updateLivraison(ins);
            
        } else if (obj instanceof Stocker) {
            Stocker ins = (Stocker) obj;
            StockerDelegate.updateStocker(ins);
            
        } else if (obj instanceof Destocker) {
            Destocker ins = (Destocker) obj;
            DestockerDelegate.updateDestocker(ins);
            
        } else if (obj instanceof Recquisition) {
            Recquisition ins = (Recquisition) obj;
            RecquisitionDelegate.updateRecquisition(ins);
            
        } else if (obj instanceof PrixDeVente) {
            PrixDeVente ins = (PrixDeVente) obj;
            PrixDeVenteDelegate.updatePrixDeVente(ins);
            
        } else if (obj instanceof Client) {
            Client ins = (Client) obj;
            ClientDelegate.updateClient(ins);
            
        } else if (obj instanceof ClientAppartenir) {
            ClientAppartenir ins = (ClientAppartenir) obj;
            ClientAppartenirDelegate.updateClientAppartenir(ins);
            
        } else if (obj instanceof ClientOrganisation) {
            ClientOrganisation ins = (ClientOrganisation) obj;
            ClientOrganisationDelegate.updateClientOrganisation(ins);
            
        } else if (obj instanceof Vente) {
            Vente ins = (Vente) obj;
            VenteDelegate.updateVente(ins);
            
        } else if (obj instanceof LigneVente) {
            LigneVente ins = (LigneVente) obj;
            LigneVenteDelegate.updateLigneVente(ins);
            
        } else if (obj instanceof RetourMagasin) {
            RetourMagasin ins = (RetourMagasin) obj;
            RetourMagasinDelegate.updateRetourMagasin(ins);
            
        } else if (obj instanceof RetourDepot) {
            RetourDepot ins = (RetourDepot) obj;
            RetourDepotDelegate.updateRetourDepot(ins);
            
        } else if (obj instanceof Aretirer) {
            Aretirer ins = (Aretirer) obj;
            AretirerDelegate.updateAretirer(ins);
            
        } else if (obj instanceof CompteTresor) {
            CompteTresor ins = (CompteTresor) obj;
            CompteTresorDelegate.updateCompteTresor(ins);
            
        } else if (obj instanceof Depense) {
            Depense ins = (Depense) obj;
            DepenseDelegate.updateDepense(ins);
            
        } else if (obj instanceof Traisorerie) {
            Traisorerie ins = (Traisorerie) obj;
            TraisorerieDelegate.updateTraisorerie(ins);
            
        } else if (obj instanceof Operation) {
            Operation ins = (Operation) obj;
            OperationDelegate.updateOperation(ins);
            
        } else if (obj instanceof Facture) {
            Facture ins = (Facture) obj;
            FactureDelegate.updateFacture(ins);
            
        }
    }

}
