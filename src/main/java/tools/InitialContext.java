/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import data.RetourDepot;
import services.AretirerService;
import services.CategoryService;
import services.ClientAppartenirService;
import services.ClientOrganisationService;
import services.ClientService;
import services.CompteTresorService;
import services.DepenseService;
import services.DestockerService;
import services.FactureService;
import services.FournisseurService;
import services.JournalService;
import services.LigneVenteService;
import services.LivraisonService;
import services.MesureService;
import services.OperationService;
import services.PrixDeVenteService;
import services.ProduitService;
import services.RecquisitionService;
import services.RetourDepotService;
import services.RetourMagasinService;
import services.StockerService;
import services.TraisorerieService;
import services.VenteService;

/**
 *
 * @author eroot
 */
public class InitialContext {

    public Object lookup(Tables name) {
        switch (name) {
            case CATEGORY:
                return new CategoryService();
            case PRODUIT:
                return new ProduitService();
            case MESURE:
                return new MesureService();
            case FOURNISSEUR:
                return new FournisseurService();
            case LIVRAISON:
                return new LivraisonService();
            case STOCKER:
                return new StockerService();
            case DESTOCKER:
                return new DestockerService();
            case RECQUISITION:
                return new RecquisitionService();
            case PRIXDEVENTE:
                return new PrixDeVenteService();
            case CLIENT:
                return new ClientService();
            case CLIENTAPPARTENIR:
                return new ClientAppartenirService();
            case CLIENTORGANISATION:
                return new ClientOrganisationService();
            case VENTE:
                return new VenteService();
            case LIGNEVENTE:
                return new LigneVenteService();
            case COMPTETRESOR:
                return new CompteTresorService();
            case DEPENSE:
                return new DepenseService();
            case OPERATION:
                return new OperationService();
            case RETOURDEPOT:
                return new RetourDepotService();
            case RETOURMAGASIN:
                return new RetourMagasinService();
            case TRAISORERIE:
                return new TraisorerieService();
            case FACTURE:
                return new FactureService();
            case ARETIRER:
                return new AretirerService();
            case JOURNAL:
                return new JournalService();
            default:
                return null;
        }
    }
}
