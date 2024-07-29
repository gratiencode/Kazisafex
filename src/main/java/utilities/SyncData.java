/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

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
import java.util.List;
import java.util.concurrent.Callable;
import data.network.Kazisafe;
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
import tools.Tables;
import tools.Util;

/**
 *
 * @author eroot
 */
public class SyncData implements Callable<String> {

    int itemOnPage = 15;
    int counter = 0;
    int syncFreqs = 4000;
    Kazisafe kazisafe;
    String entr;

    public SyncData(Integer items, String entr, Kazisafe ksf) {
        this.itemOnPage = items == 0 ? 1 : items;
        this.kazisafe = ksf;
        this.entr = entr;
    }

    @Override
    public String call() throws Exception {
        int pgfCount = pagefy(itemOnPage, FournisseurDelegate.getCount());//pagefy(itemOnPage);
        for (int i = 0; i < pgfCount; i++) {
            int start = i * itemOnPage;
            List<Fournisseur> objs = FournisseurDelegate.findFournisseurs(start, itemOnPage);//db.findAll(Fournisseur.class);
            counter++;
            if (objs != null) {
                objs.forEach((obj) -> {
                    counter++;
                    obj.setType(Tables.FOURNISSEUR.name());
                    obj.setPriority(tools.Constants.PRIORITY_SYNC);
                    Util.syncModel(obj, tools.Constants.ACTION_CREATE, counter, objs.size());
                });
            }
            Thread.sleep(syncFreqs);
        }
        int pgcCount = pagefy(itemOnPage, ClientDelegate.getCount());
        for (int i = 0; i < pgcCount; i++) {
            int start = i * itemOnPage;
            counter = 0;
            List<Client> clients = ClientDelegate.findClients(start, itemOnPage);//db.findAll(Client.class);
            if (clients != null) {
                clients.forEach((obj) -> {
                    counter++;
                    obj.setType(Tables.CLIENT.name());
                    obj.setPriority(tools.Constants.PRIORITY_SYNC);
                    Util.syncModel(obj, tools.Constants.ACTION_CREATE, counter, clients.size());

                });
            }
            Thread.sleep(syncFreqs);
        }
        int pgctCount = pagefy(itemOnPage, CompteTresorDelegate.getCount());
        for (int i = 0; i < pgctCount; i++) {
            int start = i * itemOnPage;
            counter = 0;
            List<CompteTresor> tr = CompteTresorDelegate.findCompteTresors(start, itemOnPage);// db.findAll(CompteTresor.class);
            if (tr != null) {
                tr.forEach((obj) -> {
                    counter++;
                    obj.setType(Tables.COMPTETRESOR.name());
                    obj.setPriority(tools.Constants.PRIORITY_SYNC);
                    Util.syncModel(obj, tools.Constants.ACTION_CREATE, counter, tr.size());
                });
            }
            Thread.sleep(syncFreqs);
        }
        int pgtCount = pagefy(itemOnPage, TraisorerieDelegate.getCount());
        for (int i = 0; i < pgtCount; i++) {
            int start = i * itemOnPage;
            counter = 0;
            List<Traisorerie> traisor = TraisorerieDelegate.findTraisoreries(start, itemOnPage);//db.findAll(Traisorerie.class);
            if (traisor != null) {
                traisor.forEach((obj) -> {
                    counter++;
                    obj.setType(Tables.TRAISORERIE.name());
                    obj.setPriority(tools.Constants.PRIORITY_SYNC);
                    Util.syncModel(obj, tools.Constants.ACTION_CREATE, counter, traisor.size());
                });
            }
            Thread.sleep(syncFreqs);

        }
        int pgdCount = pagefy(itemOnPage, DepenseDelegate.getCount());
        for (int i = 0; i < pgdCount; i++) {
            int start = i * itemOnPage;
            counter = 0;
            List<Depense> oper = DepenseDelegate.findDepenses(start, itemOnPage);//db.findAll(Depense.class);

            if (oper != null) {
                oper.forEach((obj) -> {
                    counter++;
                    obj.setType(Tables.DEPENSE.name());
                    obj.setPriority(tools.Constants.PRIORITY_SYNC);
                    Util.syncModel(obj, tools.Constants.ACTION_CREATE, counter, oper.size());
                });
            }
            Thread.sleep(syncFreqs);
            //=============================================
        }
        int pgoCount = pagefy(itemOnPage, OperationDelegate.getCount());
        for (int i = 0; i < pgoCount; i++) {
            int start = i * itemOnPage;
            counter = 0;
            List<Operation> operati = OperationDelegate.findOperations(start, itemOnPage);//db.findAll(Operation.class);
            if (operati != null) {
                operati.forEach((obj) -> {
                    counter++;
                    obj.setType(Tables.OPERATION.name());
                    obj.setPriority(tools.Constants.PRIORITY_SYNC);
                    Util.syncModel(obj, tools.Constants.ACTION_CREATE, counter, operati.size());
                });
            }
            Thread.sleep(syncFreqs);
//======================================
        }
        int pggCount = pagefy(itemOnPage, ClientOrganisationDelegate.getCount());
        for (int i = 0; i < pggCount; i++) {
            int start = i * itemOnPage;
            counter = 0;
            List<ClientOrganisation> operatio = ClientOrganisationDelegate.findClientOrganisations(start, itemOnPage);//db.findAll(ClientOrganisation.class);
            if (operatio != null) {
                operatio.forEach((obj) -> {
                    counter++;
                    obj.setType(Tables.CLIENTORGANISATION.name());
                    obj.setPriority(tools.Constants.PRIORITY_SYNC);
                    Util.syncModel(obj, tools.Constants.ACTION_CREATE, counter, operatio.size());
                });
            }
            Thread.sleep(syncFreqs);
        }
        int pgcaCount = pagefy(itemOnPage, ClientAppartenirDelegate.getCount());
        for (int i = 0; i < pgcaCount; i++) {
            int start = i * itemOnPage;
            counter = 0;
            List<ClientAppartenir> operatix = ClientAppartenirDelegate.findClientAppartenirs(start, itemOnPage);//db.findAll(ClientAppartenir.class);
            if (operatix != null) {
                operatix.forEach((obj) -> {
                    counter++;
                    obj.setType(Tables.CLIENTAPPARTENIR.name());
                    obj.setPriority(tools.Constants.PRIORITY_SYNC);
                    Util.syncModel(obj, tools.Constants.ACTION_CREATE, counter, operatix.size());
                });
            }
            Thread.sleep(syncFreqs);
        }
        int pgrmCount = pagefy(itemOnPage, RetourMagasinDelegate.getCount());
        for (int i = 0; i < pgrmCount; i++) {
            int start = i * itemOnPage;
            counter = 0;
            List<RetourMagasin> operatir = RetourMagasinDelegate.findRetourMagasins(start, itemOnPage);// db.findAll(RetourMagasin.class);
            if (operatir != null) {
                operatir.forEach((obj) -> {
                    counter++;
                    obj.setType(Tables.RETOURMAGASIN.name());
                    obj.setPriority(tools.Constants.PRIORITY_SYNC);
                    Util.syncModel(obj, tools.Constants.ACTION_CREATE, counter, operatir.size());
                });
            }
            Thread.sleep(syncFreqs);
        }
        int pgrdCount = pagefy(itemOnPage, RetourDepotDelegate.getCount());
        for (int i = 0; i < pgrdCount; i++) {
            int start = i * itemOnPage;
            counter = 0;
            List<RetourDepot> operatid = RetourDepotDelegate.findRetourDepots(start, itemOnPage);// db.findAll(RetourDepot.class);
            if (operatid != null) {
                operatid.forEach((obj) -> {
                    counter++;
                    obj.setType(Tables.RETOURDEPOT.name());
                    obj.setPriority(tools.Constants.PRIORITY_SYNC);
                    Util.syncModel(obj, tools.Constants.ACTION_CREATE, counter, operatid.size());
                });
            }
            Thread.sleep(syncFreqs);
//
        }
        int pgfaCount = pagefy(itemOnPage, FactureDelegate.getCount());
        for (int i = 0; i < pgfaCount; i++) {
            int start = i * itemOnPage;
            counter = 0;
            List<Facture> op = FactureDelegate.findFactures(start, itemOnPage);//db.findAll(Facture.class);
            if (op != null) {
                op.forEach((obj) -> {
                    counter++;
                    obj.setType(Tables.FACTURE.name());
                    obj.setPriority(tools.Constants.PRIORITY_SYNC);
                    Util.syncModel(obj, tools.Constants.ACTION_CREATE, counter, op.size());
                });
            }
            Thread.sleep(syncFreqs);
        }
        int pgaCount = pagefy(itemOnPage, AretirerDelegate.getCount());
        for (int i = 0; i < pgaCount; i++) {
            int start = i * itemOnPage;
            counter = 0;
            List<Aretirer> opr = AretirerDelegate.findAretirers(start, itemOnPage);//db.findAll(Facture.class);
            if (opr != null) {
                opr.forEach((obj) -> {
                    counter++;
                    obj.setType(Tables.ARETIRER.name());
                    obj.setPriority(tools.Constants.PRIORITY_SYNC);
                    Util.syncModel(obj, tools.Constants.ACTION_CREATE, counter, opr.size());
                });
            }
            Thread.sleep(syncFreqs);
        }
        int pgyCount = pagefy(itemOnPage, CategoryDelegate.findCount());
        for (int i = 0; i < pgyCount; i++) {
            int start = i * itemOnPage;
            counter = 0;
            List<Category> categs = CategoryDelegate.findCategories(start, itemOnPage);//db.findAll(Category.class);
            if (categs != null) {
                categs.forEach((obj) -> {
                    counter++;
                    obj.setType(Tables.CATEGORY.name());
                    obj.setPriority(tools.Constants.PRIORITY_SYNC);
                    Util.syncModel(obj, tools.Constants.ACTION_CREATE, counter, categs.size());
                });
            }
            Thread.sleep(syncFreqs);
        }
        int pgpCount = pagefy(itemOnPage, ProduitDelegate.getCount());
        for (int i = 0; i < pgpCount; i++) {
            int start = i * itemOnPage;
            counter = 0;
            List<Produit> produits = ProduitDelegate.findProduits(start, itemOnPage);// db.findAll(Produit.class);
            if (produits != null) {
                produits.forEach((obj) -> {
                    counter++;
                    obj.setType(Tables.PRODUIT.name());
                    obj.setPriority(tools.Constants.PRIORITY_SYNC);
                    Util.syncModel(obj, tools.Constants.ACTION_CREATE, counter, produits.size());
                });
            }
            Thread.sleep(syncFreqs);
        }
        int pgmCount = pagefy(itemOnPage, MesureDelegate.getCount());
        for (int i = 0; i < pgmCount; i++) {
            int start = i * itemOnPage;
            counter = 0;
            List<Mesure> mesures = MesureDelegate.findMesures(start, itemOnPage);//db.findAll(Mesure.class);
            if (mesures != null) {
                mesures.forEach((obj) -> {
                    counter++;
                    obj.setType(Tables.MESURE.name());
                    obj.setPriority(tools.Constants.PRIORITY_SYNC);
                    Util.syncModel(obj, tools.Constants.ACTION_CREATE, counter, mesures.size());
                });
            }
            Thread.sleep(syncFreqs);
        }
        int pglCount = pagefy(itemOnPage, LivraisonDelegate.getCount());
        for (int i = 0; i < pglCount; i++) {
            int start = i * itemOnPage;
            counter = 0;
            List<Livraison> livrs = LivraisonDelegate.findLivraisons(start, itemOnPage);//db.findAll(Livraison.class);
            if (livrs != null) {
                livrs.forEach((obj) -> {
                    counter++;
                    obj.setType(Tables.LIVRAISON.name());
                    obj.setPriority(tools.Constants.PRIORITY_SYNC);
                    Util.syncModel(obj, tools.Constants.ACTION_CREATE, counter, livrs.size());
                });
            }
            Thread.sleep(syncFreqs);
        }
        int pgsCount = pagefy(itemOnPage, StockerDelegate.getCount());
        for (int i = 0; i < pgsCount; i++) {
            int start = i * itemOnPage;
            counter = 0;
            List<Stocker> stockers = StockerDelegate.findStockers(start, itemOnPage);// db.findAll(Stocker.class);
            if (stockers != null) {
                stockers.forEach((obj) -> {
                    counter++;
                    obj.setType(Tables.STOCKER.name());
                    obj.setPriority(tools.Constants.PRIORITY_SYNC);
                    Util.syncModel(obj, tools.Constants.ACTION_CREATE, counter, stockers.size());
                });
            }
            Thread.sleep(syncFreqs);
        }
        int pgdsCount = pagefy(itemOnPage, DestockerDelegate.getCount());
        for (int i = 0; i < pgdsCount; i++) {
            int start = i * itemOnPage;
            counter = 0;
            List<Destocker> destockers = DestockerDelegate.findDestockers(start, itemOnPage);//db.findAll(Destocker.class);
            if (destockers != null) {
                destockers.forEach((obj) -> {
                    counter++;
                    obj.setType(Tables.DESTOCKER.name());
                    obj.setPriority(tools.Constants.PRIORITY_SYNC);
                    Util.syncModel(obj, tools.Constants.ACTION_CREATE, counter, destockers.size());
                });
            }
            Thread.sleep(syncFreqs);
        }
        int pgrCount = pagefy(itemOnPage, RecquisitionDelegate.getCount());
        for (int i = 0; i < pgrCount; i++) {
            int start = i * itemOnPage;
            counter = 0;
            List<Recquisition> recqusitz = RecquisitionDelegate.findRecquisitions(start, itemOnPage);// db.findAll(Recquisition.class);
            if (recqusitz != null) {
                recqusitz.forEach((obj) -> {
                    counter++;
                    obj.setType(Tables.RECQUISITION.name());
                    obj.setPriority(tools.Constants.PRIORITY_SYNC);
                    Util.syncModel(obj, tools.Constants.ACTION_CREATE, counter, recqusitz.size());
                });
            }
            Thread.sleep(syncFreqs);
        }
        int pgpvCount = pagefy(itemOnPage, PrixDeVenteDelegate.getCount());
        for (int i = 0; i < pgpvCount; i++) {
            int start = i * itemOnPage;
            counter = 0;
            List<PrixDeVente> prices = PrixDeVenteDelegate.findPrixDeVentes(start, itemOnPage);//db.findAll(PrixDeVente.class);
            if (prices != null) {
                prices.forEach((obj) -> {
                    counter++;
                    obj.setType(Tables.PRIXDEVENTE.name());
                    obj.setPriority(tools.Constants.PRIORITY_SYNC);
                    Util.syncModel(obj, tools.Constants.ACTION_CREATE, counter, prices.size());
                });
            }
            Thread.sleep(syncFreqs);
        }
        int pgvCount = pagefy(itemOnPage, VenteDelegate.getCount());
        for (int i = 0; i < pgvCount; i++) {
            int start = i * itemOnPage;
            counter = 0;
            List<Vente> ventes = VenteDelegate.findVentes(start, itemOnPage);//db.findAll(Vente.class);
            if (ventes != null) {
                ventes.forEach((obj) -> {
                    counter++;
                    obj.setType(Tables.VENTE.name());
                    obj.setPriority(tools.Constants.PRIORITY_SYNC);
                    Util.syncModel(obj, tools.Constants.ACTION_CREATE, counter, ventes.size());
                });
            }
            Thread.sleep(syncFreqs);
        }
        int pglvCount = pagefy(itemOnPage, LigneVenteDelegate.getCount());
        for (int i = 0; i < pglvCount; i++) {
            int start = i * itemOnPage;
            counter = 0;
            List<LigneVente> lignes = LigneVenteDelegate.findLigneVentes(start, itemOnPage);//db.findAll(LigneVente.class);
            if (lignes != null) {
                lignes.forEach((obj) -> {
                    counter++;
                    obj.setType(Tables.LIGNEVENTE.name());
                    obj.setPriority(tools.Constants.PRIORITY_SYNC);
                    Util.syncModel(obj, tools.Constants.ACTION_CREATE, counter, lignes.size());
                });
            }
            Thread.sleep(syncFreqs);
        }
        return "Sync-finish";
    }

    public int pagefy(int itemPerpage, Long maxi) {
//        List<Long> counts = new ArrayList<>();
//        counts.add(CategoryDelegate.findCount());
//        counts.add(ProduitDelegate.getCount());
//        counts.add(MesureDelegate.getCount());
//        counts.add(FournisseurDelegate.getCount());
//        counts.add(LivraisonDelegate.getCount());
//        counts.add(StockerDelegate.getCount());
//        counts.add(DestockerDelegate.getCount());
//        counts.add(RecquisitionDelegate.getCount());
//        counts.add(PrixDeVenteDelegate.getCount());
//        counts.add(ClientDelegate.getCount());
//        counts.add(ClientOrganisationDelegate.getCount());
//        counts.add(ClientAppartenirDelegate.getCount());
//        counts.add(VenteDelegate.getCount());
//        counts.add(LigneVenteDelegate.getCount());
//        counts.add(CompteTresorDelegate.getCount());
//        counts.add(TraisorerieDelegate.getCount());
//        counts.add(DepenseDelegate.getCount());
//        counts.add(OperationDelegate.getCount());
//        counts.add(RetourMagasinDelegate.getCount());
//        counts.add(RetourDepotDelegate.getCount());
//        counts.add(FactureDelegate.getCount());
//        counts.add(AretirerDelegate.getCount());
//        Long maxi = counts.stream().reduce(Long.MIN_VALUE, Long::max);
        int pg = maxi.intValue() / itemPerpage;
        return pg + 1;
    }

}
