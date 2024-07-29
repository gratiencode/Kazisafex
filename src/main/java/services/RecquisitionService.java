/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.RecquisitionStorage;
import com.endeleya.kazisafex.PosController;
import data.Destocker;
import delegates.MesureDelegate;
import delegates.ProduitDelegate;
import delegates.StockerDelegate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TemporalType;
import data.Mesure;
import data.Produit;
import data.Recquisition;
import data.Stocker;
import data.PrixDeVente;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tools.Constants;
import tools.ListViewItem;
import tools.Rupture;

/**
 *
 * @author eroot
 */
public class RecquisitionService implements RecquisitionStorage {

    EntityManager em;

    public RecquisitionService() {
        em = JpaUtil.getEntityManagerFactory().createEntityManager();
    }

    @Override
    public Recquisition createRecquisition(Recquisition cat) {
        EntityTransaction tx = em.getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        em.persist(cat);
        tx.commit();
        return cat;
    }

    @Override
    public Recquisition updateRecquisition(Recquisition cat) {
        EntityTransaction tx = em.getTransaction();
        if (!tx.isActive()) {
            tx.begin();
        }
        em.merge(cat);
        tx.commit();
        return cat;
    }

    @Override
    public void deleteRecquisition(Recquisition cat) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        em.remove(em.merge(cat));
        etr.commit();
    }

    @Override
    public Recquisition findRecquisition(String catId) {
        return em.find(Recquisition.class, catId);
    }

    @Override
    public List<Recquisition> findRecquisitions() {
        try {
            Query query = em.createNamedQuery("Recquisition.findAll");
            if (query == null) {
                return new ArrayList<>();
            }
            List<Recquisition> rst = query.getResultList();
            return rst == null ? new ArrayList<>() : rst;
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Recquisition> findRecquisitions(int start, int max) {
        try {
            Query query = em.createNamedQuery("Recquisition.findAll");
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Recquisition> findRecquisitionByProduit(String objId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition s WHERE s.product_id = ? ");
            Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, objId);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Long getCount() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT COUNT(*) FROM recquisition");
            return (Long) em.createNativeQuery(sb.toString()).getSingleResult();
        } catch (NoResultException e) {
            return 0L;
        }
    }

    @Override
    public List<Recquisition> findRecquisitionByProduit(String objId, String lot) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition s WHERE s.product_id = ? AND s.numlot = ? ");
            Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, objId);
            query.setParameter(2, lot);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }  //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Recquisition> findRecquisitionByProduitRegion(String uid, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition s WHERE s.product_id = ? AND s.region = ? ");
            Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, uid);
            query.setParameter(2, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Recquisition> findDescSortedByDateForProduit(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition s WHERE s.product_id = ? ORDER BY s.date DESC");
            Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, uid);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Recquisition> toFefoOrdering(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition s WHERE s.product_id = ? ORDER BY s.dateExpiry ASC");
            Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, uid);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Recquisition> toFifoOrdering(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition s WHERE s.product_id = ? ORDER BY s.date ASC");
            Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, uid);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Recquisition> toLifoOrdering(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition s WHERE s.product_id = ? ORDER BY s.date DESC");
            Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, uid);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Recquisition> findRecquisitionByProduit(String uid, String numlot, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition s WHERE s.product_id = ? AND s.numlot = ?  AND s.region = ? ");
            Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, uid);
            query.setParameter(2, numlot);
            query.setParameter(3, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Recquisition> findByDateExpInterval(Date time, Date darg) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition s WHERE s.dateExpiry BETWEEN ? AND ?");
            Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, time, TemporalType.DATE);
            query.setParameter(2, darg, TemporalType.DATE);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Object[]> findGoods() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT pipe.uid, pipe.product_id, pipe.mesure_id,p.nomproduit,p.marque,p.modele,p.taille,(cal/pipe.quantcontenu) as q,pipe.description,(pipe.cta*pipe.quantcontenu),pipe.numlot,pipe.dateExpiry FROM ")
                .append("(SELECT A.uid,(A.ta - IFNULL(B.tb,0)) as cal,A.cta,A.mesure_id, A.product_id,A.numlot,A.quantcontenu,A.description,A.dateExpiry FROM ")
                .append("(SELECT r.uid,SUM(r.quantite*m.quantcontenu) as ta,r.mesure_id,(r.coutAchat/m.quantcontenu) as cta,m.quantcontenu,m.description, r.product_id,r.numlot,r.dateExpiry FROM recquisition r, mesure m WHERE r.mesure_id=m.uid GROUP BY r.product_id) as A LEFT OUTER JOIN ")
                .append("(SELECT SUM(l.quantite*n.quantcontenu) as tb, l.product_id,l.numlot FROM ligne_vente l, mesure n WHERE l.mesure_id=n.uid GROUP BY l.product_id) as B")
                .append(" ON A.product_id=B.product_id AND A.numlot=B.numlot) as pipe, produit p WHERE pipe.product_id=p.uid ");
        try {
            Query query = em.createNativeQuery(sb.toString());
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Object[]> findGoodsFromRegion(String region) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT pipe.uid, pipe.product_id, pipe.mesure_id,p.nomproduit,p.marque,p.modele,p.taille,(pipe.pieces/pipe.quantcontenu) as q,pipe.description,(pipe.cta*pipe.quantcontenu),pipe.numlot,pipe.dateExpiry FROM ")
                .append("(SELECT A.uid,(A.ta-IFNULL(B.tb,0)) as pieces,A.cta,A.mesure_id, A.product_id,A.numlot,A.quantcontenu,A.description,A.dateExpiry FROM ")
                .append("(SELECT r.uid,SUM(r.quantite*m.quantcontenu) as ta,r.mesure_id,(r.coutAchat/m.quantcontenu) as cta,m.quantcontenu,m.description, r.product_id,r.numlot,r.dateExpiry FROM recquisition r, mesure m WHERE r.mesure_id=m.uid AND r.region = ? GROUP BY r.product_id) as A LEFT OUTER JOIN ")
                .append("(SELECT SUM(l.quantite*n.quantcontenu) as tb, l.product_id,l.numlot FROM ligne_vente l, mesure n WHERE l.mesure_id=n.uid GROUP BY l.product_id) as B")
                .append(" ON A.product_id=B.product_id AND A.numlot=B.numlot) as pipe, produit p WHERE pipe.product_id=p.uid ");
        try {
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Object[]> findGoodsCategorized(String cat) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT pipe.uid, pipe.product_id, pipe.mesure_id,p.nomproduit,p.marque,p.modele,p.taille,(pipe.pieces/pipe.quantcontenu) as q,pipe.description,pipe.coutAchat,pipe.numlot,pipe.dateExpiry FROM ")
                .append("(SELECT A.uid,(A.ta-IFNULL(B.tb,0)) as pieces,A.coutAchat,A.mesure_id, A.product_id,A.numlot,A.quantcontenu,A.description,A.dateExpiry FROM ")
                .append("(SELECT r.uid,SUM(r.quantite*m.quantcontenu) as ta,r.mesure_id,r.coutAchat,m.quantcontenu,m.description, r.product_id,r.numlot,r.dateExpiry FROM recquisition r, mesure m WHERE r.mesure_id=m.uid GROUP BY r.product_id,numlot) as A LEFT OUTER JOIN ")
                .append("(SELECT SUM(l.quantite*n.quantcontenu) as tb, l.product_id,l.numlot FROM ligne_vente l, mesure n WHERE l.mesure_id=n.uid GROUP BY l.product_id,l.numlot) as B")
                .append(" ON A.product_id=B.product_id AND A.numlot=B.numlot) as pipe, produit p WHERE pipe.product_id=p.uid AND pipe.pieces > 0 AND p.categoryid_uid = ? ");
        try {
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, cat);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Object[]> findGoodsCategorized(String cat, String region) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT pipe.uid, pipe.product_id, pipe.mesure_id,p.nomproduit,p.marque,p.modele,p.taille,(pipe.pieces/pipe.quantcontenu) as q,pipe.description,pipe.coutAchat,pipe.numlot,pipe.dateExpiry FROM ")
                .append("(SELECT A.uid,(A.ta-IFNULL(B.tb,0)) as pieces,A.coutAchat,A.mesure_id, A.product_id,A.numlot,A.quantcontenu,A.description,A.dateExpiry FROM ")
                .append("(SELECT r.uid,SUM(r.quantite*m.quantcontenu) as ta,r.mesure_id,r.coutAchat,m.quantcontenu,m.description, r.product_id,r.numlot,r.dateExpiry FROM recquisition r, mesure m WHERE r.mesure_id=m.uid AND r.region = ? GROUP BY r.product_id,numlot) as A LEFT OUTER JOIN ")
                .append("(SELECT SUM(l.quantite*n.quantcontenu) as tb, l.product_id,l.numlot FROM ligne_vente l, mesure n WHERE l.mesure_id=n.uid GROUP BY l.product_id,l.numlot) as B")
                .append(" ON A.product_id=B.product_id AND A.numlot=B.numlot) as pipe, produit p WHERE pipe.product_id=p.uid AND pipe.pieces > 0 AND p.categoryid_uid = ? ");
        try {
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, region);
            query.setParameter(2, cat);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }

    }

    @Override
    public List<Recquisition> findRecquisitions(String region) {
        try {
            Query query = em.createNamedQuery("Recquisition.findByRegion");
            query.setParameter("region", region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public double sumByProduitWithLotInUnit(String idpro, String lot) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(s.quantite*m.quantcontenu) as q FROM recquisition s,mesure m WHERE s.product_id = ? AND s.numlot = ? AND s.mesure_id=m.uid");
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, idpro);
            query.setParameter(2, lot);
            Double d = (Double) query.getSingleResult();
            return d == null ? 0 : d;
        } catch (NoResultException e) {
            return 0;
        }
    }

    @Override
    public Recquisition addToTransaction(Recquisition r) {
        em.persist(r);
        return r;
    }

    @Override
    public void startTransaction() {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
    }

    @Override
    @Deprecated(forRemoval = true)
    public void commitTransaction() {

    }

    @Override
    public List<Recquisition> findByReference(String ref) {
        try {
            Query query = em.createNamedQuery("Recquisition.findByReference");
            query.setParameter("reference", ref);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Recherche une recquisition par produit et par reference
     *
     * @param uid le uid du produit
     * @param ref reference du destockage
     * @return
     */
    @Override
    public List<Recquisition> findByReference(String uid, String ref) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition s WHERE s.product_id = ? AND s.reference = ? ");
            Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, uid);
            query.setParameter(2, ref);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Retourne le stock disponible en piece pour un produit donne en argument
     *
     * @param uid
     * @return le stock en piece
     */
    @Override
    public double findRemainedInMagasinFor(String uid) {
        double entree = sumRecqusition(uid);
        double sortie = sumLignevente(uid);
        return (entree - sortie);
    }

    private double sumRecqusition(String proId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(r.quantite*m.quantcontenu) e FROM recquisition r,mesure m WHERE r.product_id = ? AND r.mesure_id=m.uid ");
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, proId);
            Double dos = (Double) query.getSingleResult();
            return dos == null ? 0 : dos;
        } catch (NoResultException e) {
            return 0;
        }
    }

    private double sumLignevente(String proId) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(r.quantite*m.quantcontenu) s FROM ligne_vente r,mesure m WHERE r.product_id = ? AND r.mesure_id=m.uid ");
            Query query = SafeConnectionFactory.getEntityManager().createNativeQuery(sb.toString());
            query.setParameter(1, proId);
            Double rst = (Double) query.getSingleResult();
            return rst == null ? 0 : rst;
        } catch (NoResultException e) {
            return 0;
        }
    }

    private double sumRecqusitionFrom(String proId, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(r.quantite*m.quantcontenu) e FROM recquisition r,mesure m WHERE r.product_id = ? AND r.region = ? AND r.mesure_id=m.uid ");
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, proId);
            query.setParameter(2, region);
            Double dos = (Double) query.getSingleResult();
            return dos == null ? 0 : dos;
        } catch (NoResultException e) {
            return 0;
        }
    }
    
    private double sumBatchedRecqusitionFrom(String proId, String lot) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(r.quantite*m.quantcontenu) e FROM recquisition r,mesure m WHERE r.product_id = ? AND r.numlot = ? AND r.mesure_id=m.uid ");
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, proId);
            query.setParameter(2, lot);
            Double dos = (Double) query.getSingleResult();
            return dos == null ? 0 : dos;
        } catch (NoResultException e) {
            return 0;
        }
    }

    private double sumLigneventeFrom(String proId, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(r.quantite*m.quantcontenu) s FROM ligne_vente r,mesure m WHERE r.product_id = ? AND r.mesure_id=m.uid AND r.reference_uid IN (SELECT v.uid FROM vente v WHERE v.region = ? ) ");
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, proId);
            query.setParameter(2, region);
            Double rst = (Double) query.getSingleResult();
            return rst == null ? 0 : rst;
        } catch (NoResultException e) {
            return 0;
        }
    }
    
    private double sumBatchedLigneventeFrom(String proId, String lot) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(r.quantite*m.quantcontenu) s FROM ligne_vente r,mesure m WHERE r.product_id = ? AND r.mesure_id=m.uid AND r.numlot = ? ");
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, proId);
            query.setParameter(2, lot);
            Double rst = (Double) query.getSingleResult();
            return rst == null ? 0 : rst;
        } catch (NoResultException e) {
            return 0;
        }
    }

    private double sumRecqusitionByLotFrom(String proId, String numlot, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(r.quantite*m.quantcontenu) e FROM recquisition r,mesure m WHERE r.product_id = ? AND r.region = ? AND r.mesure_id=m.uid  AND r.numlot = ?");
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, proId);
            query.setParameter(2, region);
            query.setParameter(3, numlot);
            Double dos = (Double) query.getSingleResult();
            return dos == null ? 0 : dos;
        } catch (NoResultException e) {
            return 0;
        }
    }

    private double sumLigneventeByLotFrom(String proId, String lot, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(r.quantite*m.quantcontenu) s FROM ligne_vente r,mesure m WHERE r.product_id = ? AND r.mesure_id=m.uid AND r.numlot = ? AND r.reference_uid IN (SELECT v.uid FROM vente v WHERE v.region = ? ) ");
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, proId);
            query.setParameter(2, lot);
            query.setParameter(3, region);
            Double rst = (Double) query.getSingleResult();
            return rst == null ? 0 : rst;
        } catch (NoResultException e) {
            return 0;
        }
    }

    private double sumRecqusitionByLotFrom(String proId, String numlot) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(r.quantite*m.quantcontenu) e FROM recquisition r,mesure m WHERE r.product_id = ? AND r.mesure_id=m.uid  AND r.numlot = ?");
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, proId);
            query.setParameter(2, numlot);
            Double dos = (Double) query.getSingleResult();
            return dos == null ? 0 : dos;
        } catch (NoResultException e) {
            return 0;
        }
    }

    private double sumLigneventeByLotFrom(String proId, String lot) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(r.quantite*m.quantcontenu) s FROM ligne_vente r,mesure m WHERE r.product_id = ? AND r.mesure_id=m.uid AND r.numlot = ? ");
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, proId);
            query.setParameter(2, lot);
            Double rst = (Double) query.getSingleResult();
            return rst == null ? 0 : rst;
        } catch (NoResultException e) {
            return 0;
        }
    }

    /**
     * Retourne le stock disponible en piece, cad en unite a partir d'une region
     * fourni en argument pour un produit
     *
     * @param uid
     * @param region
     * @return le stock en piece
     */
    @Override
    public double findRemainedInMagasinFor(String uid, String region) {
        double entree = sumRecqusitionFrom(uid, region);
        double sortie = sumLigneventeFrom(uid, region);
        return (entree - sortie);
    }

    @Override
    public List<Rupture> findStockEnRupture() {
        List<Rupture> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT r.product_id, r.mesure_id, r.stockAlert, r.dateExpiry, r.date, r.region, r.coutachat FROM recquisition r GROUP BY r.product_id ORDER BY r.date DESC ");
        try {
            Query query = em.createNativeQuery(sb.toString());
            List<Object[]> datas = query.getResultList();
            for (Object[] data : datas) {
                Rupture r = new Rupture();
                String uidP = String.valueOf(data[0]);
                Produit pro = ProduitDelegate.findProduit(uidP);
                r.setProduit(pro);
                Mesure mezr = MesureDelegate.findMesure(String.valueOf(data[1]));
                r.setMesure(mezr);
                r.setRegion(String.valueOf(data[5]));
                r.setUnitprice(Double.parseDouble(String.valueOf(data[6])));
                if (!Objects.isNull(data[4])) {
                    try {
                        String expr = String.valueOf(data[4]);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Date dt = sdf.parse(String.valueOf(expr));
                        r.setDate(sdf.format(dt));
                    } catch (ParseException ex) {
                        Logger.getLogger(PosController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                Double alrt = Double.valueOf(String.valueOf(data[2] == null ? "0" : data[2]));
                r.setAlert(alrt);
                List<Stocker> prox = StockerDelegate.findDescSortedByDateStock(uidP);
                String loc = (prox.isEmpty() ? "N/A" : prox.get(0).getLocalisation());
                r.setLocalisation(loc);
                r.setSelect(false);
                double alrpc = (alrt == null ? 0 : alrt) * mezr.getQuantContenu();
                double rstpc = findRemainedInMagasinFor(uidP);
                if (rstpc <= alrpc || r.isSelect()) {
                    r.setQuant(BigDecimal.valueOf(rstpc / mezr.getQuantContenu()).setScale(2, RoundingMode.HALF_EVEN).doubleValue());
                    result.add(r);
                    //add to result list
                }
            }
            return result;
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Rupture> findStockEnRupture(String region) {
        List<Rupture> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT r.product_id, r.mesure_id, r.stockAlert, r.dateExpiry, r.date, r.region, r.coutachat FROM recquisition r WHERE r.region = ? GROUP BY r.product_id ORDER BY r.date DESC ");
        try {
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, region);
            List<Object[]> datas = query.getResultList();
            for (Object[] data : datas) {
                Rupture r = new Rupture();
                String uidP = String.valueOf(data[0]);
                Produit pro = ProduitDelegate.findProduit(uidP);
                r.setProduit(pro);
                Mesure mezr = MesureDelegate.findMesure(String.valueOf(data[1]));
                r.setMesure(mezr);
                r.setRegion(String.valueOf(data[5]));
                r.setUnitprice(Double.valueOf(String.valueOf(data[6])));
                if (!Objects.isNull(data[4])) {
                    try {
                        String expr = String.valueOf(data[4]);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
                        Date dt = sdf.parse(String.valueOf(expr));
                        r.setDate(sdf.format(dt));
                    } catch (ParseException ex) {
                        Logger.getLogger(PosController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                Double alrt = Double.valueOf(String.valueOf(data[2] == null ? "0" : data[2]));
                r.setAlert(alrt);
                List<Stocker> prox = StockerDelegate.findDescSortedByDateStock(uidP);
                String loc = (prox.isEmpty() ? "N/A" : prox.get(0).getLocalisation());
                r.setLocalisation(loc);
                r.setSelect(false);
                double alrpc = (alrt == null ? 0 : alrt) * mezr.getQuantContenu();
                double rstpc = findRemainedInMagasinFor(uidP);
                if (rstpc <= alrpc || r.isSelect()) {
                    r.setQuant(BigDecimal.valueOf(rstpc / mezr.getQuantContenu()).setScale(2, RoundingMode.HALF_EVEN).doubleValue());
                    result.add(r);
                    //add to result list
                }
            }
            return result;
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * La quantity en piece ici est a diviser par contenu mesure
     *
     * @param region
     * @return
     */
    @Override
    public List<Recquisition> findRecquisitionByRegionGroupBylot(String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT s.uid, s.dateexpiry, s.date, s.region, s.numlot, s.stockalert,SUM(s.quantite*m.quantcontenu) as quantite ,s.reference, s.observation,s.coutachat,s.mesure_id,s.product_id FROM recquisition s, mesure m WHERE s.mesure_id=m.uid AND s.region = ? GROUP BY s.product_id, s.numlot");
            Query query = em.createNativeQuery(sb.toString(), Recquisition.class);

            query.setParameter(1, region);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Recquisition> findRecquisitionGroupByLot() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT s.uid, s.dateexpiry, s.date, s.region, s.numlot, s.stockalert,SUM(s.quantite*m.quantcontenu) as quantite ,s.reference, s.observation,s.coutachat,"
                    + "s.mesure_id,s.product_id FROM recquisition s, mesure m WHERE s.mesure_id=m.uid GROUP BY s.product_id, s.numlot");
            Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Recquisition> findRecquisitionByRegionGroupBylot(Date debut, Date fin, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT s.uid, s.dateexpiry, s.date, s.region, s.numlot, s.stockalert,SUM(s.quantite*m.quantcontenu) as quantite ,s.reference, s.observation,"
                    + "s.coutachat,s.mesure_id,s.product_id FROM recquisition s, mesure m WHERE s.mesure_id=m.uid AND s.region = ? AND s.date BETWEEN ? AND ? GROUP BY s.product_id, s.numlot");
            Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, region);
            query.setParameter(2, debut, TemporalType.DATE).setParameter(3, fin, TemporalType.DATE);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Recquisition> findRecquisitionGroupByLot(Date debut, Date fin) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT s.uid, s.dateexpiry, s.date, s.region, s.numlot, s.stockalert,SUM(s.quantite*m.quantcontenu) as quantite ,s.reference, s.observation,s.coutachat,"
                    + "s.mesure_id,s.product_id FROM recquisition s, mesure m WHERE s.mesure_id=m.uid AND s.date BETWEEN ? AND ? GROUP BY s.product_id, s.numlot");
            Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, debut, TemporalType.DATE);
            query.setParameter(2, fin, TemporalType.DATE);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public double findRemainedInMagasinByLot(String puid, String numlot) {
        double en = sumRecqusitionByLotFrom(puid, numlot);
        double so = sumLigneventeByLotFrom(puid, numlot);
        return en - so;
    }

    @Override
    public double findRemainedInMagasinByLot(String puid, String numlot, String region) {
        double en = sumRecqusitionByLotFrom(puid, numlot, region);
        double so = sumLigneventeByLotFrom(puid, numlot, region);
        return en - so;
    }

    @Override
    public double sumByProduit(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(r.quantite*m.quantcontenu) e FROM recquisition r,mesure m WHERE r.product_id = ? AND r.mesure_id=m.uid  ");
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, uid);
            Double d = (Double) query.getSingleResult();
            return d == null ? 0 : d;
        } catch (NoResultException e) {
            return 0;
        }
    }

    @Override
    public double sumByProduit(String uid, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(r.quantite*m.quantcontenu) e FROM recquisition r,mesure m WHERE r.product_id = ? AND r.mesure_id=m.uid AND r.region = ? ");
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, uid);
            query.setParameter(2, region);
            Double d = (Double) query.getSingleResult();
            return d == null ? 0 : d;
        } catch (NoResultException e) {
            return 0;
        }
    }

    @Override
    public List<Recquisition> mergeSet(Set<Recquisition> bulk) {
        return null;
    }

    @Override
    public List<Recquisition> findByReference(String ref, String uid, String numlot) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition s WHERE s.product_id = ? AND s.reference = ? AND s.numlot = ? ");
            Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, uid).setParameter(2, ref).setParameter(3, numlot);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public double sumByProduit(String idpro, Date d1, Date d2) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(r.quantite*m.quantcontenu) e FROM recquisition r,mesure m WHERE r.product_id = ? AND r.mesure_id=m.uid  AND date BETWEEN ? AND ?");
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, idpro);
            query.setParameter(2, d1, TemporalType.DATE);
            query.setParameter(3, d2, TemporalType.DATE);
            Object dbl = query.getSingleResult();
            return dbl == null ? 0 : Double.parseDouble(String.valueOf(dbl));
        } catch (NoResultException e) {
            return 0;
        }
    }

    @Override
    public double sumByProduit(String idpro, Date d1, Date d2, String region) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT SUM(r.quantite*m.quantcontenu) e FROM recquisition r,mesure m WHERE r.product_id = ? AND r.mesure_id=m.uid AND date BETWEEN ? AND ? AND region = ? ");
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, idpro);
            query.setParameter(2, d1, TemporalType.DATE);
            query.setParameter(3, d2, TemporalType.DATE);
            query.setParameter(4, region);
            Object dbl = query.getSingleResult();
            return dbl == null ? 0 : Double.parseDouble(String.valueOf(dbl));
        } catch (NoResultException e) {
            return 0;
        }
    }

    @Override
    public List<ListViewItem> populate() {
        List<ListViewItem> result = new ArrayList<>();
        List<Produit> products = getProduits();
        if(products==null){
            return null;
        }
        if (products.isEmpty()) {
            return null;
        }
        System.out.println("All produits size "+products.size());
        for (Produit product : products) {
            double pieces = findRemainedInMagasinFor(product.getUid());
            if (pieces <= 0) {
                continue;
            }
            Recquisition r = getTheRightRecquisition(product.getUid());
            if (r == null) {
                continue;
            }
            List<PrixDeVente> prices = findPricesFor(r.getUid());
            if (prices.isEmpty()) {
                continue;
            }
            System.out.println("All continues pass...");
            Mesure mesure = r.getMesureId();
            double resteEnMesure = pieces / mesure.getQuantContenu();
            ListViewItem item = new ListViewItem();
            item.setQuantiteRestant(resteEnMesure);
            item.setMesureAchat(mesure);
            item.setCoutAchat(r.getCoutAchat());
            item.setNumlot(r.getNumlot());
            item.setPeremption(r.getDateExpiry());
            item.setProduit(product);
            item.setPurchasePrice(r.getCoutAchat());
            
            PrixDeVente ptiprix = prices.get(0);
            Mesure detailMesure = ptiprix.getMesureId();
            item.setMesureDetail(detailMesure);
            item.setDetailPrice(ptiprix.getPrixUnitaire());
            if (prices.size() > 1) {
                PrixDeVente grprix = prices.get(1);
                Mesure grosMesure = grprix.getMesureId();
                item.setMesureGros(grosMesure);
                item.setSalePrice(grprix.getPrixUnitaire());
            } else {
                item.setMesureGros(detailMesure);
                item.setSalePrice(ptiprix.getPrixUnitaire());
            }
            result.add(item);
        }
        return result;
    }

    private List<Produit> getProduits() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM produit ");
            Query query = em.createNativeQuery(sb.toString(), Produit.class);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    private Recquisition getTheRightRecquisition(String uid) {
        String meth = Constants.getStringPref("meth", "fifo");
        if (meth.equals("ppps")) {
            List<Recquisition> fefo = toFefoOrdering(uid);
            if (fefo.isEmpty()) {
                return null;
            }
            return fefo.get(0);
        } else if (meth.equals("fifo")) {
            List<Recquisition> fifo = toFifoOrdering(uid);
            if (fifo.isEmpty()) {
                return null;
            }
            return fifo.get(0);
        } else if (meth.equals("lifo")) {
            List<Recquisition> lifo = toLifoOrdering(uid);
            if (lifo.isEmpty()) {
                return null;
            }
            return lifo.get(0);
        }
        return null;
    }

    private List<PrixDeVente> findPricesFor(String uid) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM prix_de_vente WHERE recquisition_id = ? AND q_min <= ? ORDER BY prix_unitaire ASC");
            Query query = em.createNativeQuery(sb.toString(), PrixDeVente.class);
            query.setParameter(1, uid);
            query.setParameter(2, 1);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Recquisition findCustomized(String uid, String numlot, String ref, Date dateStocker) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("SELECT * FROM recquisition WHERE product_id = ? AND numlot = ? AND reference = ? AND date = ?");
            Query query = em.createNativeQuery(sb.toString(), Recquisition.class);
            query.setParameter(1, uid);
            query.setParameter(2, numlot);
            query.setParameter(3, ref);
            query.setParameter(4, dateStocker, TemporalType.DATE);
            List<Recquisition> dtks = query.getResultList();
            if (dtks.isEmpty()) {
                return null;
            }
            return dtks.get(0);
        } catch (NoResultException e) {
            return null;
        }
    }
    
    @Override
    public double findRemainedInMagasinForBatched(String uid,String numlot) {
        double entree = sumBatchedRecqusitionFrom(uid, numlot);
        double sortie = sumBatchedLigneventeFrom(uid, numlot);
        return (entree - sortie);
    }

}
