/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import services.CommandeListService;
import services.CommandeService;
import services.ProductionService;
import services.RepartirService;
import services.SatisfaireService;
import services.ImputerService;
import services.AretirerService;
import services.CategoryService;
import services.ClientAppartenirService;
import services.ClientOrganisationService;
import services.ClientService;
import services.CompteTresorService;
import services.CompterService;
import services.DepenseService;
import services.DepotService;
import services.DestockerService;
import services.EntreposerService;
import services.FactureService;
import services.FournisseurService;
import services.InventaireService;
import services.LigneVenteService;
import services.LivraisonService;
import services.MatiereService;
import services.MatiereSkuService;
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
import services.PeriodeService;
import services.PermissionService;
import services.RepportService;
import services.ImmobilisationService;
import services.PresenceService;
import services.FingerprintMappingService;

/**
 *
 * @author eroot
 */
public class InitialContext {

    public Object lookup(Tables name) {
        return switch (name) {
            case CATEGORY -> new CategoryService();
            case PRODUIT -> new ProduitService();
            case MESURE -> new MesureService();
            case FOURNISSEUR -> new FournisseurService();
            case LIVRAISON -> new LivraisonService();
            case STOCKER -> new StockerService();
            case DESTOCKER -> new DestockerService();
            case RECQUISITION -> new RecquisitionService();
            case PRIXDEVENTE -> new PrixDeVenteService();
            case CLIENT -> new ClientService();
            case CLIENTAPPARTENIR -> new ClientAppartenirService();
            case CLIENTORGANISATION -> new ClientOrganisationService();
            case VENTE -> new VenteService();
            case LIGNEVENTE -> new LigneVenteService();
            case COMPTETRESOR -> new CompteTresorService();
            case DEPENSE -> new DepenseService();
            case OPERATION -> new OperationService();
            case RETOURDEPOT -> new RetourDepotService();
            case RETOURMAGASIN -> new RetourMagasinService();
            case TRAISORERIE -> new TraisorerieService();
            case FACTURE -> new FactureService();
            case ARETIRER -> new AretirerService();
            case PERIODE -> new PeriodeService();
            case INVENTORY -> new InventaireService();
            case COMPTER -> new CompterService();
            case DEPOT -> new DepotService();
            case ENTREPOSER -> new EntreposerService();
            case MATIERE -> new MatiereService();
            case MATIERESKU -> new MatiereSkuService();
            case REPARTIR -> new RepartirService();
            case PRODUCTION -> new ProductionService();
            case COMMANDE -> new CommandeService();
            case COMMANDELIST -> new CommandeListService();
            case IMPUTER -> new ImputerService();
            case SATISFAIRE -> new SatisfaireService();
            case REPPORTING -> new RepportService();
            case PERMISSION -> new PermissionService();
            case IMMOBILISATION -> new ImmobilisationService();
            case PRESENCE -> new PresenceService();
            case FINGERPRINTMAPPING -> new FingerprintMappingService();
            default -> null;
        };
    }
}
