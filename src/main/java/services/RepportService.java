/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.RapportStorage;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import tools.SaleReport;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import data.SaleAgregate;
import data.Category;
import data.Client;
import data.ClientOrganisation;
import data.Depense;
import data.DepenseAgregate;
import data.Facture;
import data.LigneVente;
import data.Mesure;
import data.PrixDeVente;
import data.Produit;
import data.StockAgregate;
import data.Stocker;
import data.Traisorerie;
import data.Vente;
import data.Aretirer;
import data.helpers.Role;
import delegates.CategoryDelegate;
import delegates.ClientDelegate;
import delegates.LigneVenteDelegate;
import delegates.MesureDelegate;
import delegates.VenteDelegate;
import jakarta.persistence.EntityTransaction;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import javafx.geometry.Side;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import tools.Constants;
import tools.ResultStatementItem;
import tools.VenteReporter;
import tools.Metric;
import tools.RecentSale;
import utilities.Relevee;
import tools.SyncEngine;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

/**
 *
 * @author eroot
 */
public class RepportService implements RapportStorage {

    Preferences pref;
    String devise;
    double taux;

    public RepportService() {
        pref = Preferences.userNodeForPackage(SyncEngine.class);
        taux = pref.getDouble("taux2change", 2000);
        devise = pref.get("mainCur", "USD");
        // initializing...
    }

    @Override
    public StockAgregate clotureStocks(StockAgregate cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(cat);
                return cat;
            }).thenAccept(e -> {
                System.out.println("report vente enregistree");
            });
            return cat;
        }
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        ManagedSessionFactory.getEntityManager().persist(cat);
        etr.commit();
        return cat;
    }

    @Override
    public SaleAgregate createMetrics(SaleAgregate cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.persist(cat);
                return cat;
            }).thenAccept(e -> {
                System.out.println("report vente enregistree");
            });
            return cat;
        }
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        ManagedSessionFactory.getEntityManager().persist(cat);
        etr.commit();
        return cat;
    }

    @Override
    public StockAgregate updateStockAgregate(StockAgregate cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.merge(cat);
                return cat;
            }).thenAccept(e -> {
                System.out.println("report vente modifiee");
            });
            return cat;
        }
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        ManagedSessionFactory.getEntityManager().merge(cat);
        etr.commit();
        return cat;
    }

    @Override
    public SaleAgregate refeshMetrics(SaleAgregate cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.merge(cat);
                return cat;
            }).thenAccept(e -> {
                System.out.println("report vente modifiee");
            });
            return cat;
        }
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        ManagedSessionFactory.getEntityManager().merge(cat);
        etr.commit();
        return cat;
    }

    @Override
    public void deleteStockAgregate(StockAgregate cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.remove(em.merge(cat));
                return cat;
            }).thenAccept(e -> {
                System.out.println("report stok supprimee");
            });
            return;
        }
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        ManagedSessionFactory.getEntityManager().remove(ManagedSessionFactory.getEntityManager().merge(cat));
        etr.commit();
    }

    @Override
    public void deleteMetrics(SaleAgregate cat) {
        if (ManagedSessionFactory.isEmbedded()) {
            ManagedSessionFactory.submitWrite(em -> {
                em.remove(em.merge(cat));
                return cat;
            }).thenAccept(e -> {
                System.out.println("report vente supprimee");
            });
            return;
        }
        EntityTransaction etr = ManagedSessionFactory.getEntityManager().getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        ManagedSessionFactory.getEntityManager().remove(ManagedSessionFactory.getEntityManager().merge(cat));
        etr.commit();
    }

    @Override
    public StockAgregate findStockAgregate(String catId) {
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> em.find(StockAgregate.class, catId));
        }
        return ManagedSessionFactory.getEntityManager().find(StockAgregate.class, catId);
    }

    public Category findCategory(String catId) {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> em.find(Category.class, catId));
            }
            return ManagedSessionFactory.getEntityManager().find(Category.class, catId);
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public StockAgregate findMetrics(String catId) {
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> em.find(StockAgregate.class, catId));
        }
        return ManagedSessionFactory.getEntityManager().find(StockAgregate.class, catId);
    }

    @Override
    public List<StockAgregate> findStockAgregate() {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("StockAgregate.findAll");
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("StockAgregate.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<StockAgregate> findSaleAgregate() {
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNamedQuery("SaleAgregate.findAll");
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("SaleAgregate.findAll");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<StockAgregate> findByContext(String context) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM stock_agregate s WHERE s.context = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), StockAgregate.class);
                    query.setParameter(1, context);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(),
                    StockAgregate.class);
            query.setParameter(1, context);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Mesure findByProduitAndDescription(String prod, String descr) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM mesure m WHERE m.produit_id = ? AND m.description = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query q = em.createNativeQuery(sb.toString(), Mesure.class);
                    q.setParameter(1, prod).setParameter(2, descr);
                    return (Mesure) q.getSingleResult();
                });
            }
            Query q = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Mesure.class);
            q.setParameter(1, prod).setParameter(2, descr);
            return (Mesure) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Mesure findByProduitAndQuant(String prod, double quant) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM mesure m WHERE m.produit_id = ? AND m.quantcontenu = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query q = em.createNativeQuery(sb.toString(), Mesure.class);
                    q.setParameter(1, prod).setParameter(2, quant);
                    return (Mesure) q.getSingleResult();
                });
            }
            Query q = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Mesure.class);
            q.setParameter(1, prod).setParameter(2, quant);
            return (Mesure) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<VenteReporter> findReportSaleByProduct(LocalDate d1, LocalDate d2) {
        List<VenteReporter> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append(
                "SELECT v.dateVente,l.reference_uid,p.codebar,CONCAT(p.nomproduit,' ',p.marque,' ',p.modele,' ',p.taille) as prods,"
                        + "SUM(IFNULL((l.quantite*l.prixunit),0)) as c, SUM(l.quantite*m.quantcontenu) as qvpc, l.mesure_id FROM produit p,ligne_vente l,vente v,"
                        + " mesure m WHERE p.uid=l.product_id AND v.uid=l.reference_uid AND m.uid = l.mesure_id AND v.datevente BETWEEN ? AND ? "
                        + "GROUP BY l.product_id order by c desc ");
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString());
                    query.setParameter(1, d1.atStartOfDay())
                            .setParameter(2, d2.atStartOfDay());
                    List<Object[]> lis = query.getResultList();
                    for (Object[] li : lis) {
                        VenteReporter vi = new VenteReporter();
                        vi.setChiffre(Double.parseDouble(String.valueOf(li[4])));
                        vi.setCodebar(String.valueOf(li[2]));
                        vi.setQuantiteVendu(Double.parseDouble(String.valueOf(li[5])));
                        if (!Objects.isNull(li[0])) {
                            vi.setDate(LocalDate.parse(String.valueOf(li[0]).split(" ")[0]));
                        }
                        String mid = String.valueOf(li[6]);
                        Mesure mzr = MesureDelegate.findMesure(mid);
                        vi.setMesure(mzr);
                        vi.setProduit(String.valueOf(li[3]));
                        result.add(vi);

                    }
                    return result;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, d1.atStartOfDay())
                    .setParameter(2, d2.atStartOfDay());
            List<Object[]> lis = query.getResultList();
            for (Object[] li : lis) {
                VenteReporter vi = new VenteReporter();
                vi.setChiffre(Double.parseDouble(String.valueOf(li[4])));
                vi.setCodebar(String.valueOf(li[2]));
                vi.setQuantiteVendu(Double.parseDouble(String.valueOf(li[5])));
                if (!Objects.isNull(li[0])) {
                    vi.setDate(LocalDate.parse(String.valueOf(li[0]).split(" ")[0]));
                }
                String mid = String.valueOf(li[6]);
                Mesure mzr = MesureDelegate.findMesure(mid);
                vi.setMesure(mzr);
                vi.setProduit(String.valueOf(li[3]));
                result.add(vi);
            }
            return result;
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<VenteReporter> findReportSaleByProduct(LocalDate d1, LocalDate d2, String region) {
        List<VenteReporter> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append(
                "SELECT v.dateVente,l.reference_uid,p.codebar,CONCAT(p.nomproduit,' ',p.marque,' ',p.modele,' ',p.taille) as prods,"
                        + "SUM(IFNULL((l.quantite*l.prixunit),0)) as c, SUM(l.quantite*m.quantcontenu) as qvpc, l.mesure_id FROM produit p,ligne_vente l,vente v,"
                        + " mesure m WHERE p.uid=l.product_id AND v.uid=l.reference_uid AND m.uid = l.mesure_id AND v.datevente BETWEEN ? AND ? AND v.region = ? "
                        + "GROUP BY l.product_id order by c desc ");
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString());
                    query.setParameter(1, d1.atStartOfDay())
                            .setParameter(2, d2.atStartOfDay())
                            .setParameter(3, region);
                    List<Object[]> lis = query.getResultList();
                    for (Object[] li : lis) {
                        VenteReporter vi = new VenteReporter();
                        vi.setChiffre(Double.parseDouble(String.valueOf(li[4])));
                        vi.setCodebar(String.valueOf(li[2]));
                        vi.setQuantiteVendu(Double.parseDouble(String.valueOf(li[5])));
                        if (!Objects.isNull(li[0])) {
                            vi.setDate(LocalDate.parse(String.valueOf(li[0])));
                        }
                        String mid = String.valueOf(li[6]);
                        Mesure mzr = MesureDelegate.findMesure(mid);
                        vi.setMesure(mzr);
                        vi.setProduit(String.valueOf(li[3]));
                        result.add(vi);

                    }
                    return result;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, d1.atStartOfDay())
                    .setParameter(2, d2.atStartOfDay())
                    .setParameter(3, region);
            List<Object[]> lis = query.getResultList();
            for (Object[] li : lis) {
                VenteReporter vi = new VenteReporter();
                vi.setChiffre(Double.parseDouble(String.valueOf(li[4])));
                vi.setCodebar(String.valueOf(li[2]));
                vi.setQuantiteVendu(Double.parseDouble(String.valueOf(li[5])));
                if (!Objects.isNull(li[0])) {
                    vi.setDate(LocalDate.parse(String.valueOf(li[0])));
                }
                String mid = String.valueOf(li[6]);
                Mesure mzr = MesureDelegate.findMesure(mid);
                vi.setMesure(mzr);
                vi.setProduit(String.valueOf(li[3]));
                result.add(vi);

            }
            return result;
        } catch (NoResultException e) {
            return null;
        }

    }

    @Override
    public List<VenteReporter> findReportSaleByCategory(LocalDate d1, LocalDate d2) {
        List<VenteReporter> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT v.dateVente,l.reference_uid,p.categoryid_uid,c.descritption,"
                + "SUM(IFNULL(l.montantusd,0)) as som FROM produit p,ligne_vente l,vente v,"
                + " category c WHERE p.uid=l.product_id AND v.uid=l.reference_uid AND c.uid = p.categoryid_uid AND v.datevente BETWEEN ? AND ? "
                + "GROUP BY c.uid order by som desc ");
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString());
                    query.setParameter(1, d1.atStartOfDay())
                            .setParameter(2, d2.atStartOfDay());
                    List<Object[]> lis = query.getResultList();
                    for (Object[] li : lis) {
                        VenteReporter vi = new VenteReporter();
                        vi.setChiffre(Double.parseDouble(String.valueOf(li[4])));
                        Category categz = CategoryDelegate.findCategory(String.valueOf(li[2]));
                        vi.setCategory(categz);
                        result.add(vi);
                    }
                    return result;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, d1.atStartOfDay())
                    .setParameter(2, d2.atStartOfDay());
            List<Object[]> lis = query.getResultList();
            for (Object[] li : lis) {
                VenteReporter vi = new VenteReporter();
                vi.setChiffre(Double.parseDouble(String.valueOf(li[4])));
                Category categz = CategoryDelegate.findCategory(String.valueOf(li[2]));
                vi.setCategory(categz);
                result.add(vi);
            }
            return result;
        } catch (NoResultException e) {
            return null;
        }

    }

    @Override
    public List<VenteReporter> findReportSaleByCategory(LocalDate d1, LocalDate d2, String region) {
        List<VenteReporter> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT v.dateVente,l.reference_uid,p.categoryid_uid,c.descritption,"
                + "SUM(IFNULL(l.montantusd,0)) as som FROM produit p,ligne_vente l,vente v,"
                + " category c WHERE p.uid=l.product_id AND v.uid=l.reference_uid AND c.uid = p.categoryid_uid AND v.datevente BETWEEN ? AND ? AND v.region = ? "
                + "GROUP BY c.uid order by som desc ");
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString());
                    query.setParameter(1, d1.atStartOfDay())
                            .setParameter(2, d2.atStartOfDay())
                            .setParameter(3, region);
                    List<Object[]> lis = query.getResultList();
                    for (Object[] li : lis) {
                        VenteReporter vi = new VenteReporter();
                        vi.setChiffre(Double.parseDouble(String.valueOf(li[4])));
                        Category categz = CategoryDelegate.findCategory(String.valueOf(li[2]));
                        vi.setCategory(categz);
                        result.add(vi);
                    }
                    return result;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, d1.atStartOfDay())
                    .setParameter(2, d2.atStartOfDay())
                    .setParameter(3, region);
            List<Object[]> lis = query.getResultList();
            for (Object[] li : lis) {
                VenteReporter vi = new VenteReporter();
                vi.setChiffre(Double.parseDouble(String.valueOf(li[4])));
                Category categz = CategoryDelegate.findCategory(String.valueOf(li[2]));
                vi.setCategory(categz);
                result.add(vi);
            }
            return result;
        } catch (NoResultException e) {
            return null;
        }

    }

    @Override
    public List<VenteReporter> findReportSaleByClient(LocalDate d1, LocalDate d2, String region, String devise) {
        List<VenteReporter> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT v.dateVente,v.clientid_uid, CASE ")
                .append(taux).append(" "
                        + "WHEN 'CDF' THEN "
                        + "WHEN 'USD' THEN "
                        + "END AS (SUM(COALESCE(v.montantusd,0))+(SUM(COALESCE(v.montantcdf,0))/)) as som FROM vente v, client c "
                        + "WHERE c.uid=v.clientid_uid AND v.datevente BETWEEN ? AND ? AND v.region = ? "
                        + "GROUP BY c.uid order by som desc ");
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString());
                    query.setParameter(1, d1.atStartOfDay())
                            .setParameter(2, d2.atStartOfDay())
                            .setParameter(3, region);
                    List<Object[]> lis = query.getResultList();
                    for (Object[] li : lis) {
                        VenteReporter vi = new VenteReporter();
                        vi.setChiffre(Double.parseDouble(String.valueOf(li[2])));
                        Client client = ClientDelegate.findClient(String.valueOf(li[1]));
                        vi.setClient(client);
                        result.add(vi);
                    }
                    return result;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, d1.atStartOfDay())
                    .setParameter(2, d2.atStartOfDay())
                    .setParameter(3, region);
            List<Object[]> lis = query.getResultList();
            for (Object[] li : lis) {
                VenteReporter vi = new VenteReporter();
                vi.setChiffre(Double.parseDouble(String.valueOf(li[2])));
                Client client = ClientDelegate.findClient(String.valueOf(li[1]));
                vi.setClient(client);
                result.add(vi);
            }
            return result;
        } catch (NoResultException e) {
            return null;
        }

    }

    @Override
    public List<ResultStatementItem> findMargesPerProduct(LocalDate d1, LocalDate d2) {
        List<ResultStatementItem> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append(
                "SELECT v.dateVente,l.reference_uid,p.codebar,CONCAT(p.nomproduit,' ',p.marque,' ',p.modele,' ',p.taille) as prods,"
                        + "SUM(IFNULL(l.montantusd,0)) as c,(r.coutachat*l.quantite) as CA ,(SUM(IFNULL(l.montantusd,0))-r.coutachat*l.quantite) as marge, "
                        + "SUM(l.quantite) as qv FROM produit p,ligne_vente l,vente v,recquisition r "
                        + " WHERE p.uid=l.product_id AND v.uid=l.reference_uid and r.numlot=l.numlot and r.product_id=l.product_id "
                        + "AND v.datevente BETWEEN ? AND ? GROUP BY l.product_id order by c desc ");
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString());
                    query.setParameter(1, d1.atStartOfDay())
                            .setParameter(2, d2.atStartOfDay());
                    List<Object[]> objects = query.getResultList();
                    for (Object[] obj : objects) {
                        ResultStatementItem ris = new ResultStatementItem();
                        if (!Objects.isNull(obj[0])) {
                            ris.setPeriode(Constants.USER_READABLE_FORMAT.format(d1) + " - "
                                    + Constants.USER_READABLE_FORMAT.format(d2));
                        }
                        ris.setDescription(String.valueOf(obj[3]));
                        ris.setMontantRevenu(Double.valueOf(String.valueOf(obj[4])));
                        ris.setMontantDepense(Double.valueOf(String.valueOf(obj[5])));
                        ris.setMontantMarge(Double.valueOf(String.valueOf(obj[6])));
                        result.add(ris);
                    }
                    return result;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, d1.atStartOfDay())
                    .setParameter(2, d2.atStartOfDay());
            List<Object[]> objects = query.getResultList();
            for (Object[] obj : objects) {
                ResultStatementItem ris = new ResultStatementItem();
                if (!Objects.isNull(obj[0])) {
                    ris.setPeriode(Constants.USER_READABLE_FORMAT.format(d1) + " - "
                            + Constants.USER_READABLE_FORMAT.format(d2));
                }
                ris.setDescription(String.valueOf(obj[3]));
                ris.setMontantRevenu(Double.valueOf(String.valueOf(obj[4])));
                ris.setMontantDepense(Double.valueOf(String.valueOf(obj[5])));
                ris.setMontantMarge(Double.valueOf(String.valueOf(obj[6])));
                result.add(ris);
            }
        } catch (NoResultException e) {
        }
        return result;
    }

    @Override
    public List<LigneVente> findLigneVenteFor(int venteId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM ligne_vente c WHERE c.reference_uid  = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), LigneVente.class);
                    query.setParameter(1, venteId);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), LigneVente.class);
            query.setParameter(1, venteId);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Double sumUsdRecoveredTraisorerie(String ref) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(t.montantusd) s FROM traisorerie t WHERE t.libelle = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString());
                    query.setParameter(1, ref);
                    return (Double) query.getSingleResult();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, ref);
            return (Double) query.getSingleResult();
        } catch (NoResultException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Produit> searchProduit(String produx) {
        List<Produit> rsult = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append(
                "SELECT * FROM produit p WHERE CONCAT(p.codebar,' ',p.nomproduit,' ',p.marque,' ',p.modele,' ',p.taille,' ',p.couleur) LIKE ?");
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Produit.class);
                    query.setParameter(1, "%" + produx + "%");
                    rsult.addAll(query.getResultList());
                    return rsult;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Produit.class);
            query.setParameter(1, "%" + produx + "%");
            rsult.addAll(query.getResultList());
        } catch (NoResultException e) {
            System.err.println("Result is empty mon vieu");
        }
        return rsult;
    }

    @Override
    public HashMap<Long, String> getTop10ProductDesc() {
        HashMap<Long, String> hash = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        sb.append(
                "SELECT count(l.product_id) cp, CONCAT(p.nomproduit,' ',p.marque,' ',p.modele) val FROM ligne_vente l, vente v, produit p "
                        + "WHERE p.uid = l.product_id AND v.uid=l.reference_uid GROUP BY l.product_id ORDER by count(l.product_id) DESC LIMIT 10");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString());
                List<Object[]> objs = query.getResultList();
                for (Object[] obj : objs) {
                    Long e1 = (Long) obj[0];
                    String nv = String.valueOf(obj[1]);
                    hash.put(e1, nv);
                }
                return hash;
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
        List<Object[]> objs = query.getResultList();
        for (Object[] obj : objs) {
            Long e1 = (Long) obj[0];
            String nv = String.valueOf(obj[1]);
            hash.put(e1, nv);
        }
        return hash;
    }

    @Override
    public HashMap<Long, String> getTop10ProductDesc(String region) {

        HashMap<Long, String> hash = new HashMap<>();
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT count(l.product_id) cp, CONCAT(p.nomproduit,' ',p.marque,' ',p.modele) val FROM ligne_vente l,vente v, produit p "
                            + "WHERE p.uid = l.product_id AND v.uid=l.reference_uid AND v.region = ? GROUP BY l.product_id ORDER by count(l.product_id) DESC LIMIT 10");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString());
                    query.setParameter(1, region);
                    List<Object[]> objs = query.getResultList();
                    for (Object[] obj : objs) {
                        Long e1 = (Long) obj[0];
                        String nv = String.valueOf(obj[1]);
                        hash.put(e1, nv);
                    }
                    return hash;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, region);
            List<Object[]> objs = query.getResultList();
            for (Object[] obj : objs) {
                Long e1 = (Long) obj[0];
                String nv = String.valueOf(obj[1]);
                hash.put(e1, nv);
            }
        } catch (NoResultException e) {
        }
        return hash;
    }

    @Override
    public Double sumCdfRecoveredTraisorerie(String ref) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(t.montantcdf) s FROM traisorerie t WHERE t.libelle = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString());
                    query.setParameter(1, ref);
                    return (Double) query.getSingleResult();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, ref);
            return (Double) query.getSingleResult();
        } catch (NoResultException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public Double sumRecoveredByVente(int vente, double taux) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT ((SUM(t.montantcdf)/")
                    .append(taux)
                    .append(")+SUM(t.montantusd)) s FROM traisorerie t WHERE t.reference = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString());
                    query.setParameter(1, vente);
                    return (Double) query.getSingleResult();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, vente);
            return (Double) query.getSingleResult();
        } catch (NoResultException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public Double sumRecoveredByNumeroFacture(String vente, double taux) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT ((SUM(t.montantcdf)/")
                    .append(taux)
                    .append(")+SUM(t.montantusd)) s FROM traisorerie t WHERE t.reference = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString());
                    query.setParameter(1, vente);
                    return (Double) query.getSingleResult();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, vente);
            return (Double) query.getSingleResult();
        } catch (NoResultException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Vente> findVenteCredit() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM vente v WHERE v.montantDette > 0 AND v.observation != ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Vente.class);
                    query.setParameter(1, "Drafted");
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Vente.class);
            query.setParameter(1, "Drafted");
            return query.getResultList();
        } catch (NoResultException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Vente> findVenteCredit(String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM vente v WHERE v.montantDette > 0 AND v.region = ? AND v.observation != ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Vente.class);
                    query.setParameter(1, region);
                    query.setParameter(2, "Drafted");
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Vente.class);
            query.setParameter(1, region);
            query.setParameter(2, "Drafted");
            return query.getResultList();
        } catch (NoResultException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Vente> findVenteCreditByRef(String region, String ref) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT * FROM vente v WHERE v.montantDette > 0 AND v.region = ? AND v.reference = ? AND v.observation != ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Vente.class);
                    query.setParameter(1, region);
                    query.setParameter(2, ref);
                    query.setParameter(3, "Drafted");
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Vente.class);
            query.setParameter(1, region);
            query.setParameter(2, ref);
            query.setParameter(3, "Drafted");
            return query.getResultList();
        } catch (NoResultException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Vente> findVenteCreditByRef(String ref) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM vente v WHERE v.montantDette > 0 AND v.reference = ? AND v.observation != ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Vente.class);
                    query.setParameter(1, ref);
                    query.setParameter(2, "Drafted");
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Vente.class);
            query.setParameter(1, ref);
            query.setParameter(2, "Drafted");
            return query.getResultList();
        } catch (NoResultException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Vente> findVenteCreditByLocalDateInterval(LocalDate d1, LocalDate d2) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT * FROM vente v WHERE v.montantDette > 0 AND v.dateVente BETWEEN ? AND ? AND v.observation != ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Vente.class);
                    query.setParameter(1, d1.atStartOfDay())
                            .setParameter(2, d2.atStartOfDay())
                            .setParameter(3, "Drafted");
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Vente.class);
            query.setParameter(1, d1.atStartOfDay())
                    .setParameter(2, d2.atStartOfDay())
                    .setParameter(3, "Drafted");
            return query.getResultList();
        } catch (NoResultException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Vente> findVenteCreditByLocalDateInterval(LocalDate d1, LocalDate d2, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT * FROM vente v WHERE v.montantDette > 0 AND v.dateVente BETWEEN ? AND ? AND v.region = ? AND v.observation != ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Vente.class);
                    query.setParameter(1, d1.atStartOfDay())
                            .setParameter(2, d2.atStartOfDay());
                    query.setParameter(3, region);
                    query.setParameter(4, "Drafted");
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Vente.class);
            query.setParameter(1, d1.atStartOfDay())
                    .setParameter(2, d2.atStartOfDay());
            query.setParameter(3, region);
            query.setParameter(4, "Drafted");
            return query.getResultList();
        } catch (NoResultException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Traisorerie> getTresorTransactions(String traisor_id, String region) {
        StringBuilder sb = new StringBuilder();
        final boolean reg = region != null;
        if (reg) {
            sb.append("SELECT * FROM traisorerie t WHERE t.tresor_id = ? AND t.region = ?");
        } else {
            sb.append("SELECT * FROM traisorerie t WHERE t.tresor_id = ?");
        }

        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Traisorerie.class);
                if (reg) {
                    query.setParameter(1, traisor_id)
                            .setParameter(2, region);
                } else {
                    query.setParameter(1, traisor_id);
                }
                return query.getResultList();
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Traisorerie.class);
        if (reg) {
            query.setParameter(1, traisor_id)
                    .setParameter(2, region);
        } else {
            query.setParameter(1, traisor_id);
        }
        return query.getResultList();
    }

    @Override
    public ClientOrganisation findClientOrganisation(String idClient) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM client_organisation o WHERE o.uid "
                + "IN (SELECT organisation_id FROM client_appartenir c WHERE c.client_id = ? )");
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(),
                            ClientOrganisation.class);
                    query.setParameter(1, idClient);
                    return (ClientOrganisation) query.getSingleResult();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(),
                    ClientOrganisation.class);
            query.setParameter(1, idClient);
            return (ClientOrganisation) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * use ksf_d79fe68013f4405b9519d0227eee306b; SELECT
     * v.dateVente,v.libelle,c.nom_client,p.nomproduit,k.nom_client,l.quantite,l.prixunit,(l.quantite*l.prixunit)
     * as tot, c.parent_id,v.observation FROM vente v,client c,ligne_vente
     * l,client k,client_appartenir a,client_organisation o,produit p WHERE
     * v.clientid_uid=c.uid AND c.parent_id=k.uid AND c.parent_id=a.client_id
     * AND l.product_id=p.uid AND a.client_organisation_id=o.uid AND
     * l.reference_uid=v.uid AND o.uid = "b24d578c8c0e49a4ba77bdc4e810cb1e" AND
     * v.datevente BETWEEN "2023-11-01" AND "2023-12-31"
     *
     * @param orgId
     * @param d1
     * @param d2
     * @return
     */
    @Override
    public List<Relevee> getReleveFor(String orgId, LocalDate d1, LocalDate d2) {
        List<Relevee> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append(
                "SELECT v.dateVente,v.libelle,c.nom_client,p.nomproduit,k.nom_client,l.quantite,l.prixunit,(l.quantite*l.prixunit) as tot, c.parent_id,v.observation, m.uid "
                        + " FROM vente v,client c,ligne_vente l,client k,client_appartenir a,client_organisation o,produit p, mesure m"
                        + " WHERE v.clientid_uid=c.uid AND c.parent_id=k.uid AND c.parent_id=a.client_id AND l.product_id=p.uid AND l.mesure_id=m.uid"
                        + " AND a.client_organisation_id=o.uid AND l.reference_uid=v.uid AND o.uid = ? AND v.datevente BETWEEN ? AND ?");

        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString());
                    query.setParameter(1, orgId)
                            .setParameter(2, d1.atStartOfDay())
                            .setParameter(3, d2.atStartOfDay());
                    List<Object[]> objs = query.getResultList();

                    for (Object[] obj : objs) {
                        Relevee r = new Relevee();
                        try {
                            r.setDate(Constants.DATE_HEURE_FORMAT.parse(String.valueOf(obj[0])));
                        } catch (ParseException ex) {
                            Logger.getLogger(RepportService.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        String mid = String.valueOf(obj[10]);
                        Mesure m = MesureDelegate.findMesure(mid);
                        r.setMesure(m);
                        r.setNomClient(String.valueOf(obj[2]));
                        r.setNomProduit(String.valueOf(obj[3]));
                        r.setNumeroBon(String.valueOf(obj[1]));
                        r.setParent(String.valueOf(obj[4]));
                        r.setPrixunitaire(Double.parseDouble(String.valueOf(obj[6])));
                        r.setQuantite(Double.parseDouble(String.valueOf(obj[5])));
                        r.setMontant(Double.parseDouble(String.valueOf(obj[7])));
                        result.add(r);
                    }
                    return result;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, orgId)
                    .setParameter(2, d1.atStartOfDay())
                    .setParameter(3, d2.atStartOfDay());
            List<Object[]> objs = query.getResultList();

            for (Object[] obj : objs) {
                Relevee r = new Relevee();
                try {
                    r.setDate(Constants.DATE_HEURE_FORMAT.parse(String.valueOf(obj[0])));
                } catch (ParseException ex) {
                    Logger.getLogger(RepportService.class.getName()).log(Level.SEVERE, null, ex);
                }
                String mid = String.valueOf(obj[10]);
                Mesure m = MesureDelegate.findMesure(mid);
                r.setMesure(m);
                r.setNomClient(String.valueOf(obj[2]));
                r.setNomProduit(String.valueOf(obj[3]));
                r.setNumeroBon(String.valueOf(obj[1]));
                r.setParent(String.valueOf(obj[4]));
                r.setPrixunitaire(Double.parseDouble(String.valueOf(obj[6])));
                r.setQuantite(Double.parseDouble(String.valueOf(obj[5])));
                r.setMontant(Double.parseDouble(String.valueOf(obj[7])));
                result.add(r);
            }
        } catch (NoResultException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public List<Facture> getSubsBills(String billno, String org, LocalDate d1, LocalDate d2, double taux) {
        List<Facture> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT MONTHNAME(v.dateVente),v.libelle,v.region,SUM(t.montantusd+t.montantcdf/")
                .append(taux)
                .append(") as pyd, SUM(l.quantite*l.prixunit) as tot "
                        + "FROM vente v,ligne_vente l,client c,client as k,traisorerie t,client_appartenir a"
                        + " WHERE l.reference_uid=v.uid AND v.client_id=c.uid AND "
                        + "c.parent_id = k.uid AND k.uid=a.client_id AND a.client_organisation_id = ? "
                        + "AND t.libelle LIKE ? AND v.libelle LIKE ? AND v.datevente BETWEEN ? AND ?"
                        + " GROUP BY MONTHNAME(v.datevente)");

        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString());
                    query.setParameter(1, org)
                            .setParameter(2, "%" + billno + "%")
                            .setParameter(3, "%" + billno + "%")
                            .setParameter(4, d1.atStartOfDay())
                            .setParameter(5, d2.atStartOfDay());
                    List<Object[]> objs = query.getResultList();

                    for (Object[] obj : objs) {
                        Facture f = new Facture();
                        result.add(f);
                    }
                    return result;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, org)
                    .setParameter(2, "%" + billno + "%")
                    .setParameter(3, "%" + billno + "%")
                    .setParameter(4, d1.atStartOfDay())
                    .setParameter(5, d2.atStartOfDay());
            List<Object[]> objs = query.getResultList();

            for (Object[] obj : objs) {
                Facture f = new Facture();
                result.add(f);
            }
        } catch (NoResultException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Double sumUnitRetourMagasin(String prodUid, String region) {
        StringBuilder sb = new StringBuilder();
        Query query;
        Double result;
        try {
            if (region != null) {
                sb.append("SELECT SUM(s.quantite*m.quantcontenu) FROM retour_magasin s, mesure m, ligne_vente l "
                        + "WHERE s.product_id = ? AND s.region = ?  AND s.mesure_id=m.uid AND s.linge_vente_id=l.uid GROUP BY s.mesure_id ");
                if (ManagedSessionFactory.isEmbedded()) {
                    return ManagedSessionFactory.executeRead(em -> {
                        Query q = em.createNativeQuery(sb.toString());
                        q.setParameter(1, prodUid);
                        q.setParameter(2, region);
                        Double r = (Double) q.getSingleResult();
                        return r == null ? 0 : r;
                    });
                }
                query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
                query.setParameter(1, prodUid);
                query.setParameter(2, region);
            } else {
                sb.append("SELECT SUM(s.quantite*m.quantcontenu) FROM retour_magasin s, mesure m,ligne_vente l "
                        + "WHERE s.product_id = ? AND s.linge_vente_id=l.uid AND s.mesure_id=m.uid GROUP BY s.mesure_id ");
                if (ManagedSessionFactory.isEmbedded()) {
                    return ManagedSessionFactory.executeRead(em -> {
                        Query q = em.createNativeQuery(sb.toString());
                        q.setParameter(1, prodUid);
                        Double r = (Double) q.getSingleResult();
                        return r == null ? 0 : r;
                    });
                }
                query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
                query.setParameter(1, prodUid);
            }
            result = (Double) query.getSingleResult();
        } catch (jakarta.persistence.NoResultException ex) {
            return 0d;
        }
        return result == null ? 0 : result;
    }

    @Override
    public List<Facture> getFacturesByOrg(String orgUid) {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append("SELECT * FROM facture f WHERE f.organis_id = ?");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Facture.class);
                    query.setParameter(1, orgUid);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Facture.class);
            query.setParameter(1, orgUid);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Facture> getUnpaidFacturesByOrg(String orgUid) {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append("SELECT * FROM facture f WHERE f.organis_id = ? AND (f.status = ? OR f.status = ?) ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Facture.class);
                    query.setParameter(1, orgUid);
                    query.setParameter(2, Constants.BILL_STATUS_UNPAID);
                    query.setParameter(3, Constants.BILL_STATUS_INPAYMENT);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Facture.class);
            query.setParameter(1, orgUid);
            query.setParameter(2, Constants.BILL_STATUS_UNPAID);
            query.setParameter(3, Constants.BILL_STATUS_INPAYMENT);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public long getSaleItemCount(int vuid) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT COUNT(*) as c FROM ligne_vente v ")
                .append(" WHERE v.reference_uid = ? ");
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString());
                    query.setParameter(1, vuid);
                    Long r = (Long) query.getSingleResult();
                    return r == null ? 0 : r;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, vuid);
            Long r = (Long) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0l;
        }
    }

    public List<Stocker> findStockerByLivr(String livid) {

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM stocker v ")
                .append(" WHERE v.livraisid_uid = ? ");
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Stocker.class);
                    query.setParameter(1, livid);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Stocker.class);
            query.setParameter(1, livid);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }

    }

    public double sommeChiffreAffaire(LocalDate debut, LocalDate fin, String region) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT SUM(COALESCE(l.montantUsd,0)) S FROM ligne_vente l WHERE l.reference_uid IN ")
                .append("(SELECT uid FROM vente v WHERE v.dateVente BETWEEN ? AND ? AND v.region LIKE ? "
                        + "AND v.observation NOT LIKE ? "
                        + "AND v.observation NOT LIKE ? AND v.observation NOT LIKE ?"
                        + " AND v.observation NOT LIKE ? AND v.observation NOT LIKE ?)");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Double.class);
                query.setParameter(1, debut.atStartOfDay())
                        .setParameter(2, fin.atTime(23, 59, 59))
                        .setParameter(3, region)
                        .setParameter(4, "DEC%")
                        .setParameter(5, "RTR%")
                        .setParameter(6, "Cor%")
                        .setParameter(7, "Ajust%")
                        .setParameter(8, "Draf%");
                Object rst = query.getSingleResult();
                return rst == null ? 0 : (Double) rst;
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Double.class);
        query.setParameter(1, debut.atStartOfDay())
                .setParameter(2, fin.atTime(23, 59, 59))
                .setParameter(3, region)
                .setParameter(4, "DEC%")
                .setParameter(5, "RTR%")
                .setParameter(6, "Cor%")
                .setParameter(7, "Ajust%")
                .setParameter(8, "Draf%");
        Object rst = query.getSingleResult();
        return rst == null ? 0 : (Double) rst;
    }

    public double sommeSortieValeur(LocalDate debut, LocalDate fin, String region) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT SUM(COALESCE(l.coutAchat,0) * COALESCE(l.quantite, 0)) S FROM ligne_vente l"
                + " WHERE l.reference_uid IN ")
                .append("(SELECT uid FROM vente v WHERE v.dateVente BETWEEN ? AND ? AND v.region LIKE ?"
                        + " AND v.observation NOT LIKE ?"
                        + " AND v.observation NOT LIKE ? AND v.observation NOT LIKE ?"
                        + " AND v.observation NOT LIKE ? AND v.observation NOT LIKE ?)");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Double.class);
                query.setParameter(1, debut.atStartOfDay())
                        .setParameter(2, fin.atTime(23, 59, 59))
                        .setParameter(3, region)
                        .setParameter(4, "DEC%")
                        .setParameter(5, "RTR%")
                        .setParameter(6, "Cor%")
                        .setParameter(7, "Ajust%")
                        .setParameter(8, "Draf%");
                Object rst = query.getSingleResult();
                return rst == null ? 0 : (Double) rst;
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Double.class);
        query.setParameter(1, debut.atStartOfDay())
                .setParameter(2, fin.atTime(23, 59, 59))
                .setParameter(3, region)
                .setParameter(4, "DEC%")
                .setParameter(5, "RTR%")
                .setParameter(6, "Cor%")
                .setParameter(7, "Ajust%")
                .setParameter(8, "Draf%");
        Object rst = query.getSingleResult();
        return rst == null ? 0 : (Double) rst;
    }

    public double sommeChiffreAffaireSurPeriode(String produitId, LocalDate debut, LocalDate fin, String region) {
        StringBuilder sb = new StringBuilder();
        sb.append(
                "SELECT SUM(COALESCE(l.montantUsd,0)) S FROM ligne_vente l WHERE l.product_id = ? AND l.reference_uid IN ")
                .append("(SELECT uid FROM vente v WHERE v.dateVente BETWEEN ? AND ? AND v.region LIKE ? "
                        + "AND v.observation NOT LIKE ? "
                        + "AND v.observation NOT LIKE ? AND v.observation NOT LIKE ?"
                        + " AND v.observation NOT LIKE ? AND v.observation NOT LIKE ?)");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Double.class);
                query.setParameter(1, produitId)
                        .setParameter(2, debut.atStartOfDay())
                        .setParameter(3, fin.atTime(23, 59, 59))
                        .setParameter(4, region)
                        .setParameter(5, "DEC%")
                        .setParameter(6, "RTR%")
                        .setParameter(7, "Cor%")
                        .setParameter(8, "Ajust%")
                        .setParameter(9, "Draf%");
                Object rst = query.getSingleResult();
                return rst == null ? 0 : (Double) rst;
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Double.class);
        query.setParameter(1, produitId)
                .setParameter(2, debut.atStartOfDay())
                .setParameter(3, fin.atTime(23, 59, 59))
                .setParameter(4, region)
                .setParameter(5, "DEC%")
                .setParameter(6, "RTR%")
                .setParameter(7, "Cor%")
                .setParameter(8, "Ajust%")
                .setParameter(9, "Draf%");
        Object rst = query.getSingleResult();
        return rst == null ? 0 : (Double) rst;
    }

    public double sommeSortieValeurSurPeriode(String produitId, LocalDate debut, LocalDate fin, String region) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT SUM(COALESCE(l.coutAchat,0) * COALESCE(l.quantite, 0)) S FROM ligne_vente l"
                + " WHERE l.product_id = ? AND l.reference_uid IN ")
                .append("(SELECT uid FROM vente v WHERE v.dateVente BETWEEN ? AND ? AND v.region LIKE ?"
                        + " AND v.observation NOT LIKE ?"
                        + " AND v.observation NOT LIKE ? AND v.observation NOT LIKE ?"
                        + " AND v.observation NOT LIKE ? AND v.observation NOT LIKE ?)");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Double.class);
                query.setParameter(1, produitId)
                        .setParameter(2, debut.atStartOfDay())
                        .setParameter(3, fin.atTime(23, 59, 59))
                        .setParameter(4, region)
                        .setParameter(5, "DEC%")
                        .setParameter(6, "RTR%")
                        .setParameter(7, "Cor%")
                        .setParameter(8, "Ajust%")
                        .setParameter(9, "Draf%");
                Object rst = query.getSingleResult();
                return rst == null ? 0 : (Double) rst;
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Double.class);
        query.setParameter(1, produitId)
                .setParameter(2, debut.atStartOfDay())
                .setParameter(3, fin.atTime(23, 59, 59))
                .setParameter(4, region)
                .setParameter(5, "DEC%")
                .setParameter(6, "RTR%")
                .setParameter(7, "Cor%")
                .setParameter(8, "Ajust%")
                .setParameter(9, "Draf%");
        Object rst = query.getSingleResult();
        return rst == null ? 0 : (Double) rst;
    }

    public double sommeSortieQuantiteSurPeriode(String uid, LocalDate datedebut, LocalDate datefin, String region) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT SUM(COALESCE(s.quantite, 0)*COALESCE(m.quantcontenu, 0)) pieces FROM ligne_vente s, mesure m"
                + " WHERE s.product_id = ? AND s.mesure_id=m.uid AND s.reference_uid IN "
                + "(SELECT v.uid FROM vente v WHERE v.region LIKE ? "
                + "AND v.dateVente BETWEEN ? AND ? AND v.observation NOT LIKE ?"
                + " AND v.observation NOT LIKE ? AND v.observation NOT LIKE ?"
                + " AND v.observation NOT LIKE ? AND v.observation NOT LIKE ?)");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Double sorties = (Double) em.createNativeQuery(sb.toString(), Double.class)
                        .setParameter(1, uid).setParameter(2, region)
                        .setParameter(3, datedebut.atStartOfDay())
                        .setParameter(4, datefin.atTime(23, 59, 59))
                        .setParameter(5, "DEC%")
                        .setParameter(6, "RTR%")
                        .setParameter(7, "Cor%")
                        .setParameter(8, "Ajust%")
                        .setParameter(9, "Draf%")
                        .getSingleResult();
                return sorties == null ? 0 : sorties;
            });
        }
        Double sorties = (Double) (ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString())
                .setParameter(1, uid)
                .setParameter(2, region)
                .setParameter(3, datedebut.atStartOfDay())
                .setParameter(4, datefin.atTime(23, 59, 59))
                .setParameter(5, "DEC%")
                .setParameter(6, "RTR%")
                .setParameter(7, "Cor%")
                .setParameter(8, "Ajust%")
                .setParameter(9, "Draf%")
                .getSingleResult());
        return sorties == null ? 0 : sorties;
    }

    public Mesure findMinMesureForProduit(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM mesure m WHERE m.produit_id = ? ORDER BY quantcontenu ASC LIMIT 1");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Mesure.class);
                    query.setParameter(1, uid);
                    return (Mesure) query.getSingleResult();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Mesure.class);
            query.setParameter(1, uid);
            return (Mesure) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public SaleAgregate findReport(String puid, LocalDate dateDebut, LocalDate dateFin, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT * FROM sale_agregate s WHERE s.product_id = ? AND s.date BETWEEN ? AND ? AND s.region LIKE ? LIMIT 1");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), SaleAgregate.class);
                    query.setParameter(1, puid);
                    query.setParameter(2, dateDebut.atStartOfDay());
                    query.setParameter(3, dateFin.atTime(23, 59, 59));
                    query.setParameter(4, region);
                    return (SaleAgregate) query.getSingleResult();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), SaleAgregate.class);
            query.setParameter(1, puid);
            query.setParameter(2, dateDebut.atStartOfDay());
            query.setParameter(3, dateFin.atTime(23, 59, 59));
            query.setParameter(4, region);
            return (SaleAgregate) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private List<Produit> getProduits() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM produit ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Produit.class);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Produit.class);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean rapporter(LocalDate dateDebut, LocalDate dateFin, String region) {
        List<Produit> produits = getProduits();
        for (Produit produit : produits) {
            Category cat = produit.getCategoryId();
            Mesure piece = findMinMesureForProduit(produit.getUid());
            double quantVendu = sommeSortieQuantiteSurPeriode(produit.getUid(), dateDebut, dateFin, region);
            if (quantVendu == 0) {
                continue;
            }
            double ventes = sommeChiffreAffaireSurPeriode(produit.getUid(), dateDebut, dateFin, region);
            double coutAchats = sommeSortieValeurSurPeriode(produit.getUid(), dateDebut, dateFin, region);
            SaleAgregate report = findReport(produit.getUid(), dateDebut, dateFin, region);
            SaleAgregate metrix = report == null ? new SaleAgregate(tools.DataId.generate()) : report;
            metrix.setCategoryId(cat);
            metrix.setCoutAchatTotal(coutAchats);
            metrix.setDate(LocalDateTime.now());
            metrix.setMesureId(piece);
            metrix.setProductId(produit);
            metrix.setQuantite(quantVendu);
            metrix.setRegion(region);
            metrix.setTotalSaleUsd(ventes);
            if (report == null) {
                createMetrics(metrix);
            } else {
                refeshMetrics(metrix);
            }
        }
        return true;
    }

    @Override
    public Double turnOverOf(LocalDate d1, LocalDate d2, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(COALESCE(s.total_sale_usd,0)) ")
                    .append("FROM sale_agregate s WHERE s.date BETWEEN ? AND ? AND s.region LIKE ?");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Double.class);
                    query.setParameter(1, d1.atStartOfDay());
                    query.setParameter(2, d2.atTime(23, 59, 59));
                    query.setParameter(3, region);
                    Object resp = query.getSingleResult();
                    return resp == null ? 0 : (Double) resp;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Double.class);
            query.setParameter(1, d1.atStartOfDay());
            query.setParameter(2, d2.atTime(23, 59, 59));
            query.setParameter(3, region);
            Object resp = query.getSingleResult();
            return resp == null ? 0 : (Double) resp;
        } catch (Exception e) {
            return 0d;
        }
    }

    @Override
    public Double expenseOf(LocalDate d1, LocalDate d2, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(COALESCE(s.cout_achat_total,0)) ")
                    .append("FROM sale_agregate s WHERE s.date BETWEEN ? AND ? AND s.region LIKE ?");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Double.class);
                    query.setParameter(1, d1.atStartOfDay());
                    query.setParameter(2, d2.atTime(23, 59, 59));
                    query.setParameter(3, region);
                    Object resp = query.getSingleResult();
                    return resp == null ? 0 : (Double) resp;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Double.class);
            query.setParameter(1, d1.atStartOfDay());
            query.setParameter(2, d2.atTime(23, 59, 59));
            query.setParameter(3, region);
            Object resp = query.getSingleResult();
            return resp == null ? 0 : (Double) resp;
        } catch (Exception e) {
            return 0d;
        }
    }

    @Override
    public Double operationExpenseOf(LocalDate d1, LocalDate d2, String region) {
        ensureDepenseAggregate(d1, d2, region);
        try {
            String sql = "SELECT SUM(COALESCE(d.montant_usd,0)) FROM depense_agregate d WHERE d.date BETWEEN ? AND ? AND d.region LIKE ?";
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Object val = em.createNativeQuery(sql, Double.class)
                            .setParameter(1, d1.atStartOfDay())
                            .setParameter(2, d2.atTime(23, 59, 59))
                            .setParameter(3, region)
                            .getSingleResult();
                    return val == null ? 0d : ((Number) val).doubleValue();
                });
            }
            Object val = ManagedSessionFactory.getEntityManager().createNativeQuery(sql, Double.class)
                    .setParameter(1, d1.atStartOfDay())
                    .setParameter(2, d2.atTime(23, 59, 59))
                    .setParameter(3, region)
                    .getSingleResult();
            return val == null ? 0d : ((Number) val).doubleValue();
        } catch (Exception e) {
            return 0d;
        }
    }

    @Override
    public Double industrialExpenseOf(LocalDate d1, LocalDate d2, String region) {
        ensureDepenseAggregate(d1, d2, region);
        try {
            String sql = "SELECT SUM(COALESCE(d.montant_usd,0)) FROM depense_agregate d WHERE d.date BETWEEN ? AND ? AND d.region LIKE ? AND d.imputation = ?";
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Object val = em.createNativeQuery(sql, Double.class)
                            .setParameter(1, d1.atStartOfDay())
                            .setParameter(2, d2.atTime(23, 59, 59))
                            .setParameter(3, region)
                            .setParameter(4, Constants.DEPT_PRODUCTION)
                            .getSingleResult();
                    return val == null ? 0d : ((Number) val).doubleValue();
                });
            }
            Object val = ManagedSessionFactory.getEntityManager().createNativeQuery(sql, Double.class)
                    .setParameter(1, d1.atStartOfDay())
                    .setParameter(2, d2.atTime(23, 59, 59))
                    .setParameter(3, region)
                    .setParameter(4, Constants.DEPT_PRODUCTION)
                    .getSingleResult();
            return val == null ? 0d : ((Number) val).doubleValue();
        } catch (Exception e) {
            return 0d;
        }
    }

    private void ensureDepenseAggregate(LocalDate d1, LocalDate d2, String region) {
        String sql = """
                SELECT
                    o.depense_id,
                    DATE(o.date),
                    COALESCE(o.region, ''),
                    COALESCE(o.imputation, ''),
                    SUM(COALESCE(o.montantUsd,0) + (COALESCE(o.montantCdf,0) / ?))
                FROM operation o
                WHERE o.depense_id IS NOT NULL
                  AND o.date BETWEEN ? AND ?
                  AND o.region LIKE ?
                GROUP BY o.depense_id, DATE(o.date), COALESCE(o.region, ''), COALESCE(o.imputation, '')
                """;
        if (ManagedSessionFactory.isEmbedded()) {
            List<Object[]> rows = ManagedSessionFactory.executeRead(em -> em.createNativeQuery(sql)
                    .setParameter(1, taux)
                    .setParameter(2, d1.atStartOfDay())
                    .setParameter(3, d2.atTime(23, 59, 59))
                    .setParameter(4, region)
                    .getResultList());
            syncDepenseRows(rows);
            return;
        }
        List<Object[]> rows = ManagedSessionFactory.getEntityManager().createNativeQuery(sql)
                .setParameter(1, taux)
                .setParameter(2, d1.atStartOfDay())
                .setParameter(3, d2.atTime(23, 59, 59))
                .setParameter(4, region)
                .getResultList();
        syncDepenseRows(rows);
    }

    private void syncDepenseRows(List<Object[]> rows) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        for (Object[] row : rows) {
            if (row[0] == null || row[1] == null) {
                continue;
            }
            String depenseId = String.valueOf(row[0]);
            LocalDate day = parseLocalDate(row[1]);
            if (day == null) {
                continue;
            }
            String region = String.valueOf(row[2]);
            String imputation = String.valueOf(row[3]);
            double montant = row[4] == null ? 0d : ((Number) row[4]).doubleValue();

            DepenseAgregate existing = findDepenseAggregate(depenseId, day, region, imputation);
            if (existing == null) {
                Depense depense = ManagedSessionFactory.getEntityManager().find(Depense.class, depenseId);
                DepenseAgregate agreg = new DepenseAgregate();
                agreg.setDate(day.atStartOfDay());
                agreg.setDepenseId(depense);
                agreg.setRegion(region);
                agreg.setImputation(imputation);
                agreg.setMontantUsd(montant);
                if (ManagedSessionFactory.isEmbedded()) {
                    ManagedSessionFactory.submitWrite(em -> {
                        em.persist(agreg);
                        return agreg;
                    });
                } else {
                    EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
                    if (!tx.isActive()) {
                        tx.begin();
                    }
                    ManagedSessionFactory.getEntityManager().persist(agreg);
                    tx.commit();
                }
            } else {
                existing.setMontantUsd(montant);
                if (ManagedSessionFactory.isEmbedded()) {
                    ManagedSessionFactory.submitWrite(em -> em.merge(existing));
                } else {
                    EntityTransaction tx = ManagedSessionFactory.getEntityManager().getTransaction();
                    if (!tx.isActive()) {
                        tx.begin();
                    }
                    ManagedSessionFactory.getEntityManager().merge(existing);
                    tx.commit();
                }
            }
        }
    }

    private DepenseAgregate findDepenseAggregate(String depenseId, LocalDate day, String region, String imputation) {
        String sql = "SELECT * FROM depense_agregate d WHERE d.depense_id = ? AND d.date BETWEEN ? AND ? AND d.region = ? AND d.imputation = ? LIMIT 1";
        try {
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sql, DepenseAgregate.class)
                            .setParameter(1, depenseId)
                            .setParameter(2, day.atStartOfDay())
                            .setParameter(3, day.atTime(23, 59, 59))
                            .setParameter(4, region)
                            .setParameter(5, imputation);
                    return (DepenseAgregate) query.getSingleResult();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sql, DepenseAgregate.class)
                    .setParameter(1, depenseId)
                    .setParameter(2, day.atStartOfDay())
                    .setParameter(3, day.atTime(23, 59, 59))
                    .setParameter(4, region)
                    .setParameter(5, imputation);
            return (DepenseAgregate) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private LocalDate parseLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        if (value instanceof Timestamp ts) {
            return ts.toLocalDateTime().toLocalDate();
        }
        String text = String.valueOf(value);
        if (text.contains(" ")) {
            text = text.substring(0, text.indexOf(' '));
        }
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    @Override
    public HashMap<String, Double> getSalesInPerod(LocalDate d1, LocalDate d2, String region, double taux) {
        StringBuilder sb = new StringBuilder();
        HashMap<String, Double> result = new HashMap();
        if (region == null) {
            sb.append("SELECT (SUM(v.montantUsd)+(SUM(v.montantCdf)/").append(taux).append(
                    ")) as amount, MONTHNAME(v.dateVente) mois FROM vente v WHERE v.dateVente BETWEEN ? AND ? AND v.observation != ? GROUP BY MONTHNAME(v.dateVente) Order By MONTH(v.dateVente) asc");
        } else {

            sb.append("SELECT (SUM(v.montantUsd)+(SUM(v.montantCdf)/").append(taux).append(
                    ")) as amount, MONTHNAME(v.dateVente) mois FROM vente v WHERE v.dateVente BETWEEN ? AND ? AND v.region = ? AND v.observation != ? GROUP BY MONTHNAME(v.dateVente) Order By MONTH(v.dateVente) asc");
        }
        try {
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, d1.atStartOfDay());
            query.setParameter(2, d2.atStartOfDay());
            if (region != null) {
                query.setParameter(3, region);
                query.setParameter(4, "Drafted");
            } else {
                query.setParameter(3, "Drafted");
            }

            List<Object[]> objs = query.getResultList();
            for (Object[] obj : objs) {
                String mois = String.valueOf(obj[1]);
                double am = Double.parseDouble(String.valueOf(obj[0]));
                result.put(mois, am);
            }

        } catch (NoResultException e) {

        }
        return result;
    }

    public Double sumCoutAchatArticleVendu(LocalDate d1, LocalDate d2, String region) {
        StringBuilder sb = new StringBuilder();
        if (region == null) {
            sb.append("SELECT SUM(t.G) FROM (SELECT (w.x*k.y) as G FROM ")
                    .append("(SELECT (l.quantite*z.quantContenu) as y,l.numlot,l.product_id FROM ligne_vente l, mesure z WHERE l.mesure_id=z.uid AND l.reference_uid IN ")
                    .append("(SELECT v.uid FROM vente v WHERE v.dateVente BETWEEN ? AND ? AND v.observation != ? )) as k,")
                    .append("(SELECT (r.coutAchat/m.quantContenu) as x,r.numlot,r.product_id FROM recquisition r,mesure m WHERE r.mesure_id=m.uid) as w ")
                    .append("WHERE k.numlot=w.numlot AND k.product_id=w.product_id) as t");
        } else {
            sb.append("SELECT SUM(t.G) FROM (SELECT (w.x*k.y) as G FROM ")
                    .append("(SELECT (l.quantite*z.quantContenu) as y,l.numlot,l.product_id FROM ligne_vente l, mesure z WHERE l.mesure_id=z.uid AND l.reference_uid IN ")
                    .append("(SELECT v.uid FROM vente v WHERE v.dateVente BETWEEN ? AND ? AND v.region = ? AND v.observation != ? )) as k,")
                    .append("(SELECT (r.coutAchat/m.quantContenu) as x,r.numlot,r.product_id FROM recquisition r,mesure m WHERE r.mesure_id=m.uid) as w ")
                    .append("WHERE k.numlot=w.numlot AND k.product_id = w.product_id) as t");
        }
        try {
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, d1.atStartOfDay());
            query.setParameter(2, d2.atStartOfDay());
            if (region != null) {
                query.setParameter(3, region);
                query.setParameter(4, "Drafted");
            } else {
                query.setParameter(3, "Drafted");
            }
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public Double sumCoutAchatArticleVendu(String monthname, String region) {
        StringBuilder sb = new StringBuilder();
        if (region == null) {
            sb.append("SELECT SUM(t.G) FROM (SELECT (w.x*k.y) as G FROM ")
                    .append("(SELECT (l.quantite*z.quantContenu) as y,l.numlot,l.product_id FROM ligne_vente l, mesure z WHERE l.mesure_id=z.uid AND l.reference_uid IN ")
                    .append("(SELECT v.uid FROM vente v WHERE MONTHNAME(v.dateVente) = ? AND v.observation != ? )) as k,")
                    .append("(SELECT (r.coutAchat/m.quantContenu) as x,r.numlot,r.product_id FROM recquisition r,mesure m WHERE r.mesure_id=m.uid) as w ")
                    .append("WHERE k.numlot=w.numlot AND k.product_id=w.product_id) as t");
        } else {
            sb.append("SELECT SUM(t.G) FROM (SELECT (w.x*k.y) as G FROM ")
                    .append("(SELECT (l.quantite*z.quantContenu) as y,l.numlot,l.product_id FROM ligne_vente l, mesure z WHERE l.mesure_id=z.uid AND l.reference_uid IN ")
                    .append("(SELECT v.uid FROM vente v WHERE MONTHNAME(v.dateVente) = ? AND v.region = ? AND v.observation != ? )) as k,")
                    .append("(SELECT (r.coutAchat/m.quantContenu) as x,r.numlot,r.product_id FROM recquisition r,mesure m WHERE r.mesure_id=m.uid) as w ")
                    .append("WHERE k.numlot=w.numlot AND k.product_id = w.product_id) as t");
        }
        try {
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, monthname);
            if (region != null) {
                query.setParameter(2, region);
                query.setParameter(3, "Drafted");
            } else {
                query.setParameter(2, "Drafted");
            }
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public Double sumVenteUsdx(LocalDate date1, LocalDate date2, String region) {
        try {
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Vente.findBySumUSDRegion");
            query.setParameter("date1", date1.atStartOfDay());
            query.setParameter("date2", date2.atStartOfDay());
            query.setParameter("region", region);
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public double sumExpenses(String monthName, String region, double taux) {
        StringBuilder sb = new StringBuilder();
        if (region == null) {
            sb.append("SELECT (SUM(o.montantUsd)+ (SUM(o.montantCdf)*").append(taux)
                    .append(")) as tot FROM operation o WHERE MONTHNAME(o.date) = ? ");
        } else {
            sb.append("SELECT (SUM(o.montantUsd)+ (SUM(o.montantCdf)*").append(taux)
                    .append(")) as tot FROM operation o WHERE MONTHNAME(o.date) = ? AND o.region = ? ");
        }
        try {
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, monthName);
            if (region != null) {
                query.setParameter(2, region);
            }
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public Double sumOpsUsd(LocalDate date1, LocalDate date2, String region) {
        try {
            Query query = ManagedSessionFactory.getEntityManager()
                    .createNamedQuery("Operation.findSumUSDByLocalDateIntervalRegion");
            query.setParameter("date1", date1.atStartOfDay());
            query.setParameter("date2", date2.atStartOfDay());
            query.setParameter("region", region);
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public Double sumOpsCdf(LocalDate date1, LocalDate date2, String region) {
        try {
            Query query = ManagedSessionFactory.getEntityManager()
                    .createNamedQuery("Operation.findSumCDFByLocalDateIntervalRegion");
            query.setParameter("date1", date1.atStartOfDay());
            query.setParameter("date2", date2.atStartOfDay());
            query.setParameter("region", region);
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public Double sumOpsCdf(LocalDate date1, LocalDate date2) {
        try {
            Query query = ManagedSessionFactory.getEntityManager()
                    .createNamedQuery("Operation.findSumCDFByLocalDateInterval");
            query.setParameter("date1", date1.atStartOfDay());
            query.setParameter("date2", date2.atStartOfDay());
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public Double sumOpsUsd(LocalDate date1, LocalDate date2) {
        try {
            Query query = ManagedSessionFactory.getEntityManager()
                    .createNamedQuery("Operation.findSumUSDByLocalDateInterval");
            query.setParameter("date1", date1.atStartOfDay());
            query.setParameter("date2", date2.atStartOfDay());
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public Double sumVenteCdf(LocalDate date1, LocalDate date2) {
        try {
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Vente.findBySumCDF");

            query.setParameter("date1", date1.atStartOfDay());
            query.setParameter("date2", date2.atStartOfDay());
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public Double sumVenteCdf(LocalDate date1, LocalDate date2, String region) {
        try {
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Vente.findBySumCDFRegion");
            query.setParameter("date1", date1.atStartOfDay());
            query.setParameter("date2", date2.atStartOfDay());
            query.setParameter("region", region);
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public Double sumVenteDette(LocalDate date1, LocalDate date2) {
        try {
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Vente.findBySumDebt");
            query.setParameter("date1", date1.atStartOfDay());
            query.setParameter("date2", date2.atStartOfDay());
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    public Double sumVenteDette(LocalDate date1, LocalDate date2, String region) {
        try {
            Query query = ManagedSessionFactory.getEntityManager().createNamedQuery("Vente.findBySumDebtRegion");
            query.setParameter("date1", date1.atStartOfDay());
            query.setParameter("date2", date2.atStartOfDay());
            query.setParameter("region", region);
            Double r = (Double) query.getSingleResult();
            return r == null ? 0 : r;
        } catch (NoResultException e) {
            return 0d;
        }
    }

    @Override
    public double dashCardVente(String role, LocalDate d1, LocalDate kesho, double taux, String region) {
        double sumSales = 0;
        if (role.equals(Role.Trader.name()) | role.contains(Role.ALL_ACCESS.name())) {
            sumSales = VenteDelegate.sumVente(d1, kesho, taux, devise);
        } else {
            sumSales = VenteDelegate.sumVente(d1, kesho, region, taux);
        }
        return sumSales;
    }

    // public void creanceToday() {
    // if (role.equals(Role.Trader.name()) | role.contains(Role.ALL_ACCESS.name()))
    // {
    // List<Vente> ventes = getVentesDebt(LocalDate.now());
    // double sumSales = Util.sumCreditSales(ventes, taux);
    // screance.setText("$ " + BigDecimal.valueOf(sumSales).setScale(2,
    // RoundingMode.HALF_EVEN).doubleValue());
    // } else {
    // List<Vente> ventes = getVentesDebt(LocalDate.now(), region);
    // double sumSales = Util.sumCreditSales(ventes, taux);
    // screance.setText("$ " + BigDecimal.valueOf(sumSales).setScale(2,
    // RoundingMode.HALF_EVEN).doubleValue());
    // }
    // }
    @Override
    public double dashCardResult(String role, LocalDate d1, LocalDate kesho, String region, double taux) {
        double result = 0;
        if (role.equals(Role.Trader.name()) | role.contains(Role.ALL_ACCESS.name())) {
            double sumSales = VenteDelegate.sumVente(d1, kesho, taux, devise);
            double achat = VenteDelegate.sumCoutAchatArticleVendu(d1, kesho);
            result = sumSales - achat;
        } else {
            double sumSales = VenteDelegate.sumVente(d1, kesho, region, taux);
            double achat = VenteDelegate.sumCoutAchatArticleVendu(d1, kesho, region);
            result = sumSales - achat;
        }
        return result;
    }

    @Override
    public double dashCardDepense(String role, LocalDate d1, LocalDate kesho, String region) {
        double achat;
        if (role.equals(Role.Trader.name()) | role.contains(Role.ALL_ACCESS.name())) {
            achat = VenteDelegate.sumCoutAchatArticleVendu(d1, kesho);
        } else {
            achat = VenteDelegate.sumCoutAchatArticleVendu(d1, kesho, region);
        }
        return achat;
    }

    /**
     * La fonction sert a afficher le graphique pour voir l'evolution du
     * business sur une soit annee
     *
     * @param venteChart UI xy-chart
     * @param serie1     serie de vente
     * @param serie2     serie de cout-acquisition
     * @param serie3     serie de marge
     * @param d1         date debut periode
     * @param d2         date fin periode
     * @param role       role utilisateur
     * @param taux       taux de change
     * @param region     region ou site
     */
    @Override
    public void metrify(XYChart<String, Number> venteChart, String serie1, String serie2, String serie3, LocalDate d1,
            LocalDate d2, String role, String region, String periodName) {
        Executors.newSingleThreadExecutor()
                .submit(() -> {
                    List<Metric> kpis;
                    if (role.equals(Role.Trader.name()) | role.contains(Role.ALL_ACCESS.name())) {
                        kpis = kpi(d1, d2, "%", periodName);
                    } else {
                        kpis = kpi(d1, d2, region, periodName);
                    }
                    Platform.runLater(() -> {
                        venteChart.setLegendVisible(true);
                        venteChart.getData().clear();
                        XYChart.Series serie_vente = new XYChart.Series();
                        XYChart.Series serie_prixderevient = new XYChart.Series();
                        XYChart.Series serie_resultat = new XYChart.Series();
                        serie_vente.setName(serie1);
                        serie_prixderevient.setName(serie2);
                        serie_resultat.setName(serie3);
                        for (Metric kpi : kpis) {
                            LocalDate period = kpi.period();
                            String moix = period.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, Locale.FRANCE);
                            serie_vente.getData().add(new XYChart.Data<>(moix, kpi.chiffreAffaire()));
                            serie_prixderevient.getData().add(new XYChart.Data<>(moix, kpi.coutAchat()));
                            serie_resultat.getData().add(new XYChart.Data<>(moix, kpi.result()));
                        }
                        venteChart.getData().addAll(serie_vente, serie_prixderevient, serie_resultat);
                        for (XYChart.Series<String, Number> serie : venteChart.getData()) {
                            for (XYChart.Data<String, Number> data : serie.getData()) {
                                String text = serie.getName() + "\n" + data.getXValue() + " : "
                                        + formatNumber(data.getYValue().doubleValue()) + " " + devise;
                                Tooltip tooltip = new Tooltip(text);
                                Tooltip.install(data.getNode(), tooltip);
                                data.getNode().setStyle("-fx-background-color: #ff6600, white; -fx-padding: 5px;");
                            }
                        }
                        venteChart.setLegendSide(Side.BOTTOM);
                    });
                });

    }

    private String formatNumber(double value) {
        if (value >= 1_000_000_000) {
            return String.format("%.1fB", value / 1_000_000_000);
        } else if (value >= 1_000_000) {
            return String.format("%.1fM", value / 1_000_000);
        } else if (value >= 1_000) {
            return String.format("%.1fK", value / 1_000);
        } else {
            return Double.toString(value);
        }
    }

    public List<Stocker> findStocksAtLocation(String location) {
        StringBuilder sb = new StringBuilder();
        Query query;
        sb.append("SELECT * FROM stocker s WHERE s.localisation = ? ");
        query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Stocker.class);
        query.setParameter(1, location);
        return query.getResultList();
    }

    public List<Stocker> findStocksAtLocation(String location, String region) {
        StringBuilder sb = new StringBuilder();
        Query query;
        sb.append("SELECT * FROM stocker s WHERE s.localisation = ? AND s.region = ? ");
        query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Stocker.class);
        query.setParameter(1, location);
        query.setParameter(2, region);
        return query.getResultList();
    }

    @Override
    public Double findStockValue(LocalDate date1, LocalDate date2, String region, String context) {
        StringBuilder sb = new StringBuilder();
        sb.append(
                "SELECT SUM(((COALESCE(s.cout_achat,0)*COALESCE(s.final_quantity,0)))) valeur FROM stock_agregate s WHERE s.date BETWEEN ? AND ? AND s.region LIKE ? AND s.context = ?");
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString(), Double.class);
                query.setParameter(1, date1.atStartOfDay());
                query.setParameter(2, date2.atTime(23, 59, 59));
                query.setParameter(3, region);
                query.setParameter(4, context);
                Object obj = query.getSingleResult();
                return obj == null ? 0 : (Double) obj;
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
        query.setParameter(1, date1.atStartOfDay());
        query.setParameter(2, date2.atTime(23, 59, 59));
        query.setParameter(3, region);
        query.setParameter(4, context);
        Object obj = query.getSingleResult();
        return obj == null ? 0 : (Double) obj;
    }

    public List<Aretirer> findRetraitByStatus(String status, String region) {
        StringBuilder sb = new StringBuilder();
        Query query;
        sb.append("SELECT * FROM aretirer s WHERE s.status = ? AND s.region = ? ");
        query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Stocker.class);
        query.setParameter(1, status);
        query.setParameter(2, region);
        return query.getResultList();
    }

    public List<Aretirer> findRetraitByStatus(String status) {
        StringBuilder sb = new StringBuilder();
        Query query;
        sb.append("SELECT * FROM aretirer s WHERE s.status = ? ");
        query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Stocker.class);
        query.setParameter(1, status);
        return query.getResultList();
    }

    public Produit findByCodebarr(String codebar) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM produit l WHERE l.codebar = ?");
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Produit.class);
            query.setParameter(1, codebar);
            return (Produit) query.getSingleResult();
        } catch (NoResultException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public List<Produit> findProduitLike(String prod) {
        StringBuilder sb = new StringBuilder();
        sb.append(
                "SELECT  * FROM produit p WHERE CONCAT(p.codebar,' ',p.nomproduit,' ',p.marque,' ',p.modele,' ',p.taille,' ', p.couleur) LIKE ?");
        try {
            Query q = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Produit.class);
            q.setParameter(1, "%" + prod + "%");
            return q.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<PrixDeVente> findPricesForReq(String reqid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM prix_de_vente p WHERE p.recquisition_id = ? ");
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), PrixDeVente.class);
            query.setParameter(1, reqid);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Vente> getDraftedCarts() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM vente v WHERE v.observation = ? ");
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Vente.class);
            query.setParameter(1, "Drafted");
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Vente getDraftedCart(int saved) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM vente v WHERE v.uid = ? AND v.observation = ? ");
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Vente.class);
            query.setParameter(1, saved);
            query.setParameter(2, "Drafted");
            return (Vente) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public StockAgregate findStockFor(Produit prod, LocalDate today, LocalDate today1) {
        String sql = """
                SELECT
                    SUM(COALESCE(s.initial_quantity,0)),
                    SUM(COALESCE(s.entrees,0)),
                    SUM(COALESCE(s.sorties,0)),
                    SUM(COALESCE(s.final_quantity,0)),
                    SUM(COALESCE(s.expiree,0)),
                    CASE WHEN SUM(COALESCE(s.final_quantity,0)) = 0 THEN 0
                         ELSE SUM(COALESCE(s.cout_achat,0) * COALESCE(s.final_quantity,0)) / SUM(COALESCE(s.final_quantity,0))
                    END,
                    MAX(s.date)
                FROM stock_agregate s
                WHERE s.date BETWEEN ? AND ? AND s.product_id = ?
                """;
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> mapStockAggregate(em.createNativeQuery(sql)
                    .setParameter(1, today.atStartOfDay())
                    .setParameter(2, today1.atTime(LocalTime.of(23, 59, 59)))
                    .setParameter(3, prod.getUid())
                    .getSingleResult(), prod, null));
        }
        Object row = ManagedSessionFactory.getEntityManager().createNativeQuery(sql)
                .setParameter(1, today.atStartOfDay())
                .setParameter(2, today1.atTime(LocalTime.of(23, 59, 59)))
                .setParameter(3, prod.getUid())
                .getSingleResult();
        return mapStockAggregate(row, prod, null);
    }

    @Override
    public StockAgregate findStockFor(Produit prod, LocalDate today, LocalDate otherDay, String region) {
        String sql = """
                SELECT
                    SUM(COALESCE(s.initial_quantity,0)),
                    SUM(COALESCE(s.entrees,0)),
                    SUM(COALESCE(s.sorties,0)),
                    SUM(COALESCE(s.final_quantity,0)),
                    SUM(COALESCE(s.expiree,0)),
                    CASE WHEN SUM(COALESCE(s.final_quantity,0)) = 0 THEN 0
                         ELSE SUM(COALESCE(s.cout_achat,0) * COALESCE(s.final_quantity,0)) / SUM(COALESCE(s.final_quantity,0))
                    END,
                    MAX(s.date)
                FROM stock_agregate s
                WHERE s.date BETWEEN ? AND ? AND s.region LIKE ? AND s.product_id = ?
                """;
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> mapStockAggregate(em.createNativeQuery(sql)
                    .setParameter(1, today.atStartOfDay())
                    .setParameter(2, otherDay.atTime(LocalTime.of(23, 59, 59)))
                    .setParameter(3, region)
                    .setParameter(4, prod.getUid())
                    .getSingleResult(), prod, region));
        }
        Object row = ManagedSessionFactory.getEntityManager().createNativeQuery(sql)
                .setParameter(1, today.atStartOfDay())
                .setParameter(2, otherDay.atTime(LocalTime.of(23, 59, 59)))
                .setParameter(3, region)
                .setParameter(4, prod.getUid())
                .getSingleResult();
        return mapStockAggregate(row, prod, region);
    }

    private StockAgregate mapStockAggregate(Object rowObject, Produit product, String region) {
        if (!(rowObject instanceof Object[] row) || row.length < 7 || row[0] == null) {
            return null;
        }
        StockAgregate aggregate = new StockAgregate();
        aggregate.setProductId(product);
        aggregate.setRegion(region);
        aggregate.setInitialQuantity(((Number) row[0]).doubleValue());
        aggregate.setEntrees(((Number) row[1]).doubleValue());
        aggregate.setSorties(((Number) row[2]).doubleValue());
        aggregate.setFinalQuantity(((Number) row[3]).doubleValue());
        aggregate.setExpiree(((Number) row[4]).doubleValue());
        aggregate.setCoutAchat(((Number) row[5]).doubleValue());
        if (row[6] instanceof Timestamp ts) {
            aggregate.setDate(ts.toLocalDateTime());
        }
        return aggregate;
    }

    // maintenant le vraie rapport
    @Override
    public double chiffreDaffaire(LocalDate dateDebut, LocalDate dateFin, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT SUM(COALESCE(s.total_sale_usd,0)) CA FROM sale_agregate s WHERE s.date BETWEEN ? AND ? AND s.region LIKE ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Double.class);
                    query.setParameter(1, dateDebut.atStartOfDay());
                    query.setParameter(2, dateFin.atTime(23, 59, 59));
                    query.setParameter(3, region);
                    Object rst = query.getSingleResult();
                    return rst == null ? 0 : (Double) rst;
                });
            }

            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Double.class);
            query.setParameter(1, dateDebut.atStartOfDay());
            query.setParameter(2, dateFin.atTime(23, 59, 59));
            query.setParameter(3, region);
            Object rst = query.getSingleResult();
            return rst == null ? 0 : (Double) rst;
        } catch (NoResultException e) {
            return 0;
        }
    }

    @Override
    public double chargeVariable(LocalDate dateDebut, LocalDate dateFin, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT SUM(COALESCE(s.cout_achat_total,0)) CA FROM sale_agregate s WHERE s.date BETWEEN ? AND ? AND s.region LIKE ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Double.class);
                    query.setParameter(1, dateDebut.atStartOfDay());
                    query.setParameter(2, dateFin.atTime(23, 59, 59));
                    query.setParameter(3, region);
                    Object rst = query.getSingleResult();
                    return rst == null ? 0 : (Double) rst;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Double.class);
            query.setParameter(1, dateDebut.atStartOfDay());
            query.setParameter(2, dateFin.atTime(23, 59, 59));
            query.setParameter(3, region);
            Object rst = query.getSingleResult();
            return rst == null ? 0 : (Double) rst;
        } catch (NoResultException e) {
            return 0;
        }
    }

    @Override
    public double chiffreDaffaireParCategory(String category, LocalDate dateDebut, LocalDate dateFin, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT SUM(COALESCE(s.total_sale_usd,0)) CA FROM sale_agregate s WHERE s.date BETWEEN ? AND ? AND s.region LIKE ? AND s.category_id = ? ");

            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Double.class);
                    query.setParameter(1, dateDebut.atStartOfDay());
                    query.setParameter(2, dateFin.atTime(23, 59, 59));
                    query.setParameter(3, region);
                    query.setParameter(4, category);
                    Object rst = query.getSingleResult();
                    return rst == null ? 0 : (Double) rst;
                });
            }

            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Double.class);
            query.setParameter(1, dateDebut.atStartOfDay());
            query.setParameter(2, dateFin.atTime(23, 59, 59));
            query.setParameter(3, region);
            query.setParameter(4, category);
            Object rst = query.getSingleResult();
            return rst == null ? 0 : (Double) rst;
        } catch (NoResultException e) {
            return 0;
        }
    }

    @Override
    public double chiffreDaffaireParProduit(String produit, LocalDate dateDebut, LocalDate dateFin, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    "SELECT SUM(COALESCE(s.total_sale_usd,0)) CA FROM sale_agregate s WHERE s.date BETWEEN ? AND ? AND s.region LIKE ? AND s.product_id = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), Double.class);
                    query.setParameter(1, dateDebut.atStartOfDay());
                    query.setParameter(2, dateFin.atTime(23, 59, 59));
                    query.setParameter(3, region);
                    query.setParameter(4, produit);
                    Object rst = query.getSingleResult();
                    return rst == null ? 0 : (Double) rst;
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), Double.class);
            query.setParameter(1, dateDebut.atStartOfDay());
            query.setParameter(2, dateFin.atTime(23, 59, 59));
            query.setParameter(3, region);
            query.setParameter(4, produit);
            Object rst = query.getSingleResult();
            return rst == null ? 0 : (Double) rst;
        } catch (NoResultException e) {
            return 0;
        }
    }

    @Override
    public List<Metric> kpi(LocalDate dateDebut,
            LocalDate dateFin,
            String region,
            String groupBy) {

        List<Metric> metrics = new ArrayList<>();
        boolean embedded = ManagedSessionFactory.isEmbedded();

        String periodeExpr = buildPeriodeExpression(groupBy, embedded);
        String sql = buildSql(periodeExpr);

        if (embedded) {
            return ManagedSessionFactory
                    .executeRead(em -> executeQuery(em, sql, dateDebut, dateFin, region, groupBy, metrics));
        }

        return executeQuery(
                ManagedSessionFactory.getEntityManager(),
                sql,
                dateDebut,
                dateFin,
                region,
                groupBy,
                metrics);
    }

    private String buildSql(String periodeExpr) {
        return """
                SELECT
                    SUM(COALESCE(s.total_sale_usd,0)) AS CA,
                    SUM(COALESCE(s.cout_achat_total,0)) AS CH,
                    SUM(COALESCE(s.total_sale_usd,0) - COALESCE(s.cout_achat_total,0)) AS RESULT,
                    %s AS periode
                FROM sale_agregate s
                WHERE s.date BETWEEN :start AND :end
                  AND s.region LIKE :region
                GROUP BY periode
                ORDER BY periode ASC
                """.formatted(periodeExpr);
    }

    private String buildPeriodeExpression(String groupBy, boolean embedded) {

        return switch (groupBy.toLowerCase()) {

            case "mensuel" ->
                embedded
                        ? "strftime('%m', s.date/1000, 'unixepoch')"
                        : "MONTH(s.date)";

            case "annuel" ->
                embedded
                        ? "strftime('%Y', s.date/1000, 'unixepoch')"
                        : "YEAR(s.date)";

            default ->
                "s.date";
        };
    }

    private List<Metric> executeQuery(EntityManager em,
            String sql,
            LocalDate dateDebut,
            LocalDate dateFin,
            String region,
            String groupBy,
            List<Metric> metrics) {

        Query q = em.createNativeQuery(sql);
        q.setParameter("start", dateDebut.atStartOfDay());
        q.setParameter("end", dateFin.atTime(23, 59, 59));
        q.setParameter("region", region);

        List<Object[]> rows = q.getResultList();
        for (Object[] r : rows) {
            metrics.add(mapRowToMetric(r, groupBy, dateFin, region));
        }
        return metrics;
    }

    private Metric mapRowToMetric(Object[] r,
            String groupBy,
            LocalDate referenceDate,
            String region) {

        double ca = ((Number) r[0]).doubleValue();
        double ch = ((Number) r[1]).doubleValue();
        double result = ((Number) r[2]).doubleValue();

        LocalDate periode;

        switch (groupBy.toLowerCase()) {

            case "mensuel" -> {
                int month = ((Number) r[3]).intValue();
                YearMonth ym = YearMonth.of(referenceDate.getYear(), month);
                periode = ym.atEndOfMonth(); // ✅ bissextile OK
            }

            case "annuel" -> {
                int year = ((Number) r[3]).intValue();
                periode = LocalDate.of(year, 12, 31);
            }

            default ->
                periode = ((java.sql.Date) r[3]).toLocalDate();
        }

        return new Metric(periode, ca, ch, result, region);
    }

    private List<SaleAgregate> findSaleAgs(LocalDate today, LocalDate today1, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM sale_agregate s "
                    + "WHERE s.date BETWEEN ? AND ? AND s.region LIKE ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), SaleAgregate.class);
                    query.setParameter(1, today.atStartOfDay());
                    query.setParameter(2, today1.atTime(23, 59, 59));
                    query.setParameter(3, region);
                    return query.getResultList();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), SaleAgregate.class);
            query.setParameter(1, today.atStartOfDay());
            query.setParameter(2, today1.atTime(23, 59, 59));
            query.setParameter(3, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void purgerToutMetric(LocalDate date1, LocalDate date2, String region) {
        List<SaleAgregate> ags = findSaleAgs(date1, date2, region);
        for (SaleAgregate ag : ags) {
            deleteMetrics(ag);
        }
    }

    @Override
    public SaleAgregate findSaleReportFor(String prod, LocalDate today, LocalDate today1, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM sale_agregate s "
                    + "WHERE s.date BETWEEN ? AND ? AND s.region LIKE ? AND s.product_id = ? ");
            if (ManagedSessionFactory.isEmbedded()) {
                return ManagedSessionFactory.executeRead(em -> {
                    Query query = em.createNativeQuery(sb.toString(), SaleAgregate.class);
                    query.setParameter(1, today.atStartOfDay());
                    query.setParameter(2, today1.atTime(23, 59, 59));
                    query.setParameter(3, region);
                    query.setParameter(4, prod).setMaxResults(1);
                    return (SaleAgregate) query.getSingleResult();
                });
            }
            Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString(), SaleAgregate.class);
            query.setParameter(1, today.atStartOfDay());
            query.setParameter(2, today1.atTime(23, 59, 59));
            query.setParameter(3, region);
            query.setParameter(4, prod).setMaxResults(1);
            return (SaleAgregate) query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * SaleReport(String category,String codebar, String produit, double
     * quantite,String unite, String devise,double coutAchat, double
     * vente,double marge) {
     */
    @Override
    public List<SaleReport> findSaleReportPerProduct(LocalDate today, LocalDate today1, String region) {

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT c.descritption,p.codebar,CONCAT(p.nomproduit,' ',p.modele,' ',p.taille) as prod,"
                + " SUM(s.quantite) as quantite, m.description, SUM(s.cout_achat_total) as cat, SUM(s.total_sale_usd) as ts, (SUM(s.total_sale_usd)-SUM(s.cout_achat_total)) as marge ")
                .append(" FROM sale_agregate s, produit p, category c, mesure m ")
                .append("WHERE s.date BETWEEN ? AND ? AND s.region LIKE ? AND s.product_id = p.uid"
                        + " AND s.mesure_id=m.uid AND s.category_id=c.uid ")
                .append("GROUP BY s.product_id ");
        List<SaleReport> result = new ArrayList<>();
        double chiffreDaffaire = chiffreDaffaire(today, today1, region);
        double chargeVariable = chargeVariable(today, today1, region);
        double resultat = chiffreDaffaire - chargeVariable;
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString());
                query.setParameter(1, today.atStartOfDay());
                query.setParameter(2, today1.atTime(23, 59, 59));
                query.setParameter(3, region);
                List<Object[]> objs = query.getResultList();

                for (Object[] obj : objs) {
                    String categ = String.valueOf(obj[0]);
                    String codebar = String.valueOf(obj[1]);
                    String produit = String.valueOf(obj[2]);
                    double quantite = Double.parseDouble(String.valueOf(obj[3]));
                    String unite = String.valueOf(obj[4]);
                    double cout = Double.parseDouble(String.valueOf(obj[5]));
                    double vente = Double.parseDouble(String.valueOf(obj[6]));
                    double marge = Double.parseDouble(String.valueOf(obj[7]));
                    SaleReport sr = new SaleReport(categ, codebar, produit, quantite, unite, devise, cout, vente, marge,
                            ((marge / resultat) * 100));
                    result.add(sr);
                }
                return result;
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
        query.setParameter(1, today.atStartOfDay());
        query.setParameter(2, today1.atTime(23, 59, 59));
        query.setParameter(3, region);
        List<Object[]> objs = query.getResultList();
        for (Object[] obj : objs) {
            String categ = String.valueOf(obj[0]);
            String codebar = String.valueOf(obj[1]);
            String produit = String.valueOf(obj[2]);
            double quantite = Double.parseDouble(String.valueOf(obj[3]));
            String unite = String.valueOf(obj[4]);
            double cout = Double.parseDouble(String.valueOf(obj[5]));
            double vente = Double.parseDouble(String.valueOf(obj[6]));
            double marge = Double.parseDouble(String.valueOf(obj[7]));
            SaleReport sr = new SaleReport(categ, codebar, produit, quantite, unite, devise, cout, vente, marge,
                    ((marge / resultat) * 100));
            result.add(sr);
        }
        return result;
    }

    @Override
    public List<SaleReport> findSaleReportPerCategory(LocalDate today, LocalDate today1, String region) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT c.descritption,p.codebar,CONCAT(p.nomproduit,' ',p.modele,' ',p.taille) as prod,"
                + " SUM(s.quantite) quantite,m.description,SUM(s.cout_achat_total) cat,SUM(s.total_sale_usd) ts, (SUM(s.total_sale_usd)-SUM(s.cout_achat_total)) as marge ")
                .append(" FROM sale_agregate s, produit p, category c, mesure m ")
                .append("WHERE s.date BETWEEN ? AND ? AND s.region LIKE ? AND s.product_id = p.uid"
                        + " AND s.mesure_id=m.uid AND s.category_id=c.uid ")
                .append("GROUP BY s.category_id ");
        List<SaleReport> result = new ArrayList<>();
        double chiffreDaffaire = chiffreDaffaire(today, today1, region);
        double chargeVariable = chargeVariable(today, today1, region);
        double resultat = chiffreDaffaire - chargeVariable;
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString());
                query.setParameter(1, today.atStartOfDay());
                query.setParameter(2, today1.atTime(23, 59, 59));
                query.setParameter(3, region);
                List<Object[]> objs = query.getResultList();
                for (Object[] obj : objs) {
                    String categ = String.valueOf(obj[0]);
                    String codebar = String.valueOf(obj[1]);
                    String produit = String.valueOf(obj[2]);
                    double quantite = Double.parseDouble(String.valueOf(obj[3]));
                    String unite = String.valueOf(obj[4]);
                    double cout = Double.parseDouble(String.valueOf(obj[5]));
                    double vente = Double.parseDouble(String.valueOf(obj[6]));
                    double marge = Double.parseDouble(String.valueOf(obj[7]));
                    SaleReport sr = new SaleReport(categ, codebar, produit, quantite, unite, devise, cout, vente, marge,
                            ((marge / resultat) * 100));
                    result.add(sr);
                }
                return result;
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
        query.setParameter(1, today.atStartOfDay());
        query.setParameter(2, today1.atTime(23, 59, 59));
        query.setParameter(3, region);
        List<Object[]> objs = query.getResultList();
        for (Object[] obj : objs) {
            String categ = String.valueOf(obj[0]);
            String codebar = String.valueOf(obj[1]);
            String produit = String.valueOf(obj[2]);
            double quantite = Double.parseDouble(String.valueOf(obj[3]));
            String unite = String.valueOf(obj[4]);
            double cout = Double.parseDouble(String.valueOf(obj[5]));
            double vente = Double.parseDouble(String.valueOf(obj[6]));
            double marge = Double.parseDouble(String.valueOf(obj[7]));
            SaleReport sr = new SaleReport(categ, codebar, produit, quantite, unite, devise, cout, vente, marge,
                    ((marge / resultat) * 100));
            result.add(sr);
        }
        return result;
    }

    @Override
    public List<RecentSale> findRecentSales(String region) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT v.reference,CONCAT(p.nomproduit,' ',p.modele,' ',p.taille) as prod,"
                + " SUM(l.quantite) quantite,m.description, SUM(l.montantusd) total ")
                .append(" FROM ligne_vente l, produit p, vente v, mesure m ")
                .append("WHERE l.product_id = p.uid AND l.mesure_id=m.uid AND l.reference_uid IN ")
                .append("(SELECT t.uid FROM vente t WHERE t.region LIKE ? AND t.observation NOT LIKE ? "
                        + "AND t.observation NOT LIKE ? AND t.observation NOT LIKE ? AND t.observation NOT LIKE ?"
                        + " ORDER BY t.dateVente DESC) ")
                .append("GROUP BY l.product_id ORDER BY v.dateVente DESC ");
        List<RecentSale> result = new ArrayList<>();
        if (ManagedSessionFactory.isEmbedded()) {
            return ManagedSessionFactory.executeRead(em -> {
                Query query = em.createNativeQuery(sb.toString());
                query.setParameter(1, region)
                        .setParameter(2, "DEC%")
                        .setParameter(3, "Cor%")
                        .setParameter(4, "Ajust%")
                        .setParameter(5, "COR%")
                        .setMaxResults(15);
                List<Object[]> objs = query.getResultList();
                for (Object[] obj : objs) {
                    String ref = String.valueOf(obj[0]);
                    String prod = String.valueOf(obj[1]);
                    double quantite = Double.parseDouble(String.valueOf(obj[2]));
                    String unite = String.valueOf(obj[3]);
                    double total = Double.parseDouble(String.valueOf(obj[4]));
                    RecentSale rs = new RecentSale(ref, prod, quantite, unite, total);
                    result.add(rs);
                }
                return result;
            });
        }
        Query query = ManagedSessionFactory.getEntityManager().createNativeQuery(sb.toString());
        query.setParameter(1, region)
                .setParameter(2, "DEC%")
                .setParameter(3, "Cor%")
                .setParameter(4, "Ajust%")
                .setParameter(5, "COR%").setMaxResults(15);
        List<Object[]> objs = query.getResultList();
        for (Object[] obj : objs) {
            String ref = String.valueOf(obj[0]);
            String prod = String.valueOf(obj[1]);
            double quantite = Double.parseDouble(String.valueOf(obj[2]));
            String unite = String.valueOf(obj[3]);
            double total = Double.parseDouble(String.valueOf(obj[4]));
            RecentSale rs = new RecentSale(ref, prod, quantite, unite, total);
            result.add(rs);
        }
        return result;
    }
}
