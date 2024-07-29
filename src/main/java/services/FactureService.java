/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package services;

import IServices.FactureStorage;
import delegates.MesureDelegate;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TemporalType;
import data.Facture;
import data.Mesure;
import tools.Constants;
import utilities.Relevee;

/**
 *
 * @author eroot
 */
public class FactureService implements FactureStorage{

    EntityManager em;

    public FactureService() {
        em=JpaUtil.getEntityManagerFactory().createEntityManager();
    }

    @Override
    public Facture createFacture(Facture cat) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        em.merge(cat);
        etr.commit();
        return cat;
    }

    @Override
    public Facture updateFacture(Facture cat) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        em.merge(cat);
        etr.commit();
        return cat;
    }

    @Override
    public void deleteFacture(Facture cat) {
        EntityTransaction etr = em.getTransaction();
        if (!etr.isActive()) {
            etr.begin();
        }
        em.remove(em.merge(cat));
        etr.commit();
    }

    @Override
    public Facture findFacture(String catId) {
         return em.find(Facture.class, catId);
    }
    
     @Override
    public Long getCount() {
       try{
           StringBuilder sb=new StringBuilder();
           sb.append("SELECT COUNT(*) FROM facture");
           return (Long) em.createNativeQuery(sb.toString()).getSingleResult();
       }catch(NoResultException e){
           return 0L;
       }
    }

    @Override
    public List<Facture> findFactures() {
        try{
            Query query=em.createNamedQuery("Facture.findAll");
            return query.getResultList();
        }catch(NoResultException e){
            return null;
        }
    }

   
    @Override
    public List<Facture> findFactures(int start, int max) {
        try{
            Query query=em.createNamedQuery("Facture.findAll");
            query.setFirstResult(start);
            query.setMaxResults(max);
            return query.getResultList();
        }catch(NoResultException e){
            return null;
        }
    }

    @Override
    public List<Relevee> findReleveeInterval(String orgId, Date d1, Date d2) {
        List<Relevee> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT v.dateVente,v.libelle,c.nom_client,p.nomproduit,k.nom_client,l.quantite,l.prixunit,(l.quantite*l.prixunit) as tot, c.parent_id,v.observation, m.uid "
                + " FROM vente v,client c,ligne_vente l,client k,client_appartenir a,client_organisation o,produit p, mesure m"
                + " WHERE v.clientid_uid=c.uid AND c.parent_id=k.uid AND c.parent_id=a.client_id AND l.product_id=p.uid AND l.mesure_id=m.uid"
                + " AND a.client_organisation_id=o.uid AND l.reference_uid=v.uid AND o.uid = ? AND v.datevente BETWEEN ? AND ?");

        try {
            Query query = em.createNativeQuery(sb.toString());
            query.setParameter(1, orgId)
                    .setParameter(2, d1, TemporalType.TIMESTAMP)
                    .setParameter(3, d2, TemporalType.TIMESTAMP);
            List<Object[]> objs = query.getResultList();

            for (Object[] obj : objs) {
                Relevee r = new Relevee();
                try {
                    r.setDate(Constants.DATE_HEURE_FORMAT.parse(String.valueOf(obj[0])));
                } catch (ParseException ex) {
                    Logger.getLogger(JpaStorage.class.getName()).log(Level.SEVERE, null, ex);
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
    public List<Facture> findOrgaBills(String uid) {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append("SELECT * FROM facture f WHERE f.organis_id = ?");
            Query query = em.createNativeQuery(sb.toString(), Facture.class);
            query.setParameter(1, uid);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Facture> findUpaidBillsFor(String uid) {
       StringBuilder sb = new StringBuilder();
        try {
            sb.append("SELECT * FROM facture f WHERE f.organis_id = ? AND (f.status = ? OR f.status = ?) ");
            Query query = em.createNativeQuery(sb.toString(), Facture.class);
            query.setParameter(1, uid);
            query.setParameter(2, Constants.BILL_STATUS_UNPAID);
            query.setParameter(3, Constants.BILL_STATUS_INPAYMENT);
            return query.getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }


    @Override
    public List<Facture> findBillingInInterval(Date date1, Date date2) {
        try{
        StringBuilder sb=new StringBuilder();
        sb.append("SELECT * FROM facture f WHERE f.start_date BETWEEN ? AND ? ");
           Query query = em.createNativeQuery(sb.toString(), Facture.class);
            query.setParameter(1, date1, TemporalType.DATE);
            query.setParameter(2, date2, TemporalType.DATE);
            return query.getResultList();
        }catch(NoResultException e){
            return null;
        }
    }

    @Override
    public List<Facture> mergeSet(Set<Facture> bulk) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
            
}
