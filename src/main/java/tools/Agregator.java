/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tools;

import data.Client;
import data.Produit;
import delegates.RecquisitionDelegate;
import delegates.ProduitDelegate;
import delegates.RepportDelegate;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import data.LigneVente;
import data.Mesure;
import data.Vente;
import delegates.ClientDelegate;
import delegates.LigneVenteDelegate;
import delegates.MesureDelegate;
import static delegates.VenteDelegate.saveVente;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import utilities.Peremption;

/**
 *
 * @author endeleya
 */
public class Agregator {

    Preferences pref;
    String region;
    private boolean finish = false;

    private static Agregator instance = null;

    private LocalTaskStateListener localTaskStateListener;
    private OnReportSavedListener onReportSavedListener;

    private Agregator() {
        pref = Preferences.userNodeForPackage(SyncEngine.class);
        this.region = pref.get("region", null);
    }

    public static Agregator getInstance() {
        if (instance == null) {
            instance = new Agregator();
        }
        return instance;
    }

    public void agregate() {
        Executors.newSingleThreadExecutor()
                .submit(() -> {
                    try {
                        if (region == null) {
                            return;
                        }
                        LocalDate today = LocalDate.now();
                        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
                        ScheduledFuture<Boolean> futur = ses.schedule(() -> {
                            return RecquisitionDelegate.cloturerStocks(region, today, today, "Journalier du " + today.toString());
                        }, 3, TimeUnit.SECONDS);
                        finish = futur.get();
                        notifyFinish(finish,"stock-cloture");
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Agregator.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ExecutionException ex) {
                        Logger.getLogger(Agregator.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
    }

    public void inventorier(Produit produit) {
        if (region == null) {
            return;
        }
        Executors.newSingleThreadExecutor()
                .submit(() -> {
                    LocalDate today = LocalDate.now();
                    RecquisitionDelegate.cloturerUnProduit(produit, region, today, today, "Journalier du " + today.toString());
                });
    }

    public void inventorier(Produit produit, LocalDate debut, LocalDate fin, String context) {
        if (region == null) {
            return;
        }
        Executors.newSingleThreadExecutor()
                .submit(() -> {
                    RecquisitionDelegate.cloturerUnProduit(produit, region, debut, fin, context);
                });
    }

    public void inventorier(List<LigneVente> lvs) {
        if (region == null) {
            return;
        }
        Executors.newSingleThreadExecutor()
                .submit(() -> {
                    LocalDate today = LocalDate.now();
                    for (LigneVente lv : lvs) {
                        Produit produit = lv.getProductId();
                        RecquisitionDelegate.cloturerUnProduit(produit, region, today, today, "Journalier du " + today.toString());
                    }
                });
    }

    public void agregate(LocalDate date1, LocalDate date2, String context) {
        Executors.newSingleThreadExecutor()
                .submit(() -> {
                    try {
                        if (region == null) {
                            return;
                        }
                        List<Produit> produits = ProduitDelegate.findProduits();
                        int size = produits.size();
                        for (int i = 0; i < size; i++) {
                            Produit produit = produits.get(i);
                            RecquisitionDelegate.cloturerUnProduit(produit, region, date1, date2, context);
                            notifyProgress(i, size, "Stock " + produit.getNomProduit() + " cloturé");
                        }
                        finish = true;
                        notifyFinish(finish, "stock-cloture");

                    } catch (Exception ex) {
                        Logger.getLogger(Agregator.class.getName()).log(Level.SEVERE, null, ex);
                        notifyFinish(false, "stock-cloture");
                    }
                });
    }

    public void setLocalTaskStateListener(LocalTaskStateListener onFinishListener) {
        this.localTaskStateListener = onFinishListener;
    }

    private void notifyFinish(boolean ok,String name) {
        if (this.localTaskStateListener != null) {
            this.localTaskStateListener.onFinish(ok,name);
        }
    }

    private void notifyProgress(double pr, double size, String message) {
        if (this.localTaskStateListener != null) {
            double prog = (pr / size);
            this.localTaskStateListener.onProgress(prog, message);
        }
    }

    public boolean isFinish() {
        return finish;
    }

    public void reportInBackground() {
        LocalDate today = LocalDate.now();
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        ses.scheduleAtFixedRate(() -> {
//            boolean finish = RepportDelegate.repportInBackground(today, today, region == null ? "%" : region);
//            if (finish) {
                System.out.println("Raport fait");
                double ca = RepportDelegate.turnOverOf(today, today, region == null ? "%" : region);
                        //RepportDelegate.chiffreDaffaire(today, today, region == null ? "%" : region);
                double cv = RepportDelegate.expenseOf(today, today, region == null ? "%" : region);
                        //RepportDelegate.chargeVariable(today, today, region == null ? "%" : region);
                notifyReports(ca, cv);
//            }
        }, 3, 10, TimeUnit.SECONDS);
    }

    public void notifyReports(double ca, double cv) {
        if (onReportSavedListener != null) {
            onReportSavedListener.onReportSavedListener(ca, cv);
        }
    }

    public void setOnReportSavedListener(OnReportSavedListener onReportSavedListener) {
        this.onReportSavedListener = onReportSavedListener;
    }

    public Future<List<Peremption>> deStockerLesExpiree(LocalDate per, List<Peremption> listAdeClasser, String region) {
        ExecutorService ex=Executors.newSingleThreadExecutor();
        Future<List<Peremption>> result = ex.submit(() -> {
            int r = DataId.generateInt();
            Vente v = new Vente(r);
            Client clt = ClientDelegate.findAnonymousClient();//db.findWithAndClause(Client.class, new String[]{"phone"}, new String[]{"0000"});
            v.setClientId(clt);
            v.setDateVente(per.atTime(23, 59, 59));
            v.setDeviseDette("USD");
            v.setLatitude(0d);
            v.setLibelle("Déclassement de stock");
            v.setLongitude(0d);
            v.setMontantCdf(0);
            v.setMontantDette(0d);
            v.setMontantUsd(0);
            v.setObservation("DEC-" + r);
            v.setPayment("NA");
            v.setReference("DEC" + r);
            v.setRegion(region);
            Vente ventura = saveVente(v);
            List<Peremption> toRemove = new ArrayList<>();
            for (Peremption perimee : listAdeClasser) {
                Produit p = ProduitDelegate.findProduit(perimee.getProduitUid());
                if (p == null) {
                    continue;
                }
                LigneVente lv = new LigneVente(DataId.generateLong());
                lv.setQuantite(perimee.getQuantite());
                lv.setCoutAchat(perimee.getCoutAchat());
                lv.setNumlot(perimee.getLot());
                lv.setPrixUnit(0d);
                lv.setProductId(p);
                List<Mesure> fmz = MesureDelegate.findMesureByProduit(p.getUid(), perimee.getMesure());
                if (fmz.isEmpty()) {
                    continue;
                }
                lv.setMesureId(fmz.get(0));
                lv.setReference(ventura);
                lv.setClientId("RABBISH");
                LigneVenteDelegate.saveLigneVente(lv);
                toRemove.add(perimee);
                Mesure m = lv.getMesureId();
                double cau = lv.getCoutAchat() / m.getQuantContenu();
                RecquisitionDelegate.rectifyStock(lv.getProductId(), LocalDate.now(), LocalDate.now(), region, cau);
                notifyProgress(listAdeClasser.indexOf(perimee), listAdeClasser.size(), "Stock expiré de " + p.getNomProduit() + " déclassé");
            }
            notifyFinish(true,"declass");
            return toRemove;
        });
        return result;
    }

}
