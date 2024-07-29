/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tools;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
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
import data.Journal;
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
import java.util.prefs.Preferences;

/**
 *
 * @author endeleya
 */
public class LoggingTask implements Runnable {

    int MAX_RETRY = 5;
    private final EntityManagerFactory entityManagerFactory;
    private final Set<BaseModel> childEntityIds;
    private final CountDownLatch latch;
  

    public LoggingTask(EntityManagerFactory entityManagerFactory, Set<BaseModel> childEntityIds, CountDownLatch latch) {
        this.entityManagerFactory = entityManagerFactory;
        this.childEntityIds = childEntityIds;
        this.latch = latch;
        
    }

    @Override
    public void run() {
        try {
            latch.await(); // Wait for MergeEntitiesTask to complete
            int retries = 0;
            boolean success = false;
         
            while (retries < MAX_RETRY && !success) {
                EntityManager em = entityManagerFactory.createEntityManager();
                EntityTransaction tx = em.getTransaction();
                try {
                    long id = System.currentTimeMillis();
                    String selectSql = "SELECT * FROM journal WHERE uid = :uid";
                    Query selectQuery = em.createNativeQuery(selectSql, Journal.class);
                    selectQuery.setParameter("uid", id);
                    List<Journal> results = selectQuery.getResultList();
                    if (results.isEmpty()) {
                        tx.begin();
                        for (BaseModel model : childEntityIds) {
                            
                            Journal j;
                            List<Journal> is1;
                            switch (model) {
                                case Vente v -> {
                                    j = Util.createJournalWithId(id, v.getUid(), model, true);
                                    is1 = checkUnicity(em, v.getUid(), model.getAction());
                                }
                                case LigneVente l -> {
                                    j = Util.createJournalWithId(id, l.getUid(), model, true);
                                    is1 = checkUnicity(em, l.getUid(), model.getAction());
                                }
                                default -> {
                                    String uid = getUid(model);
                                    j = Util.createJournalWithId(id, uid, model, true);
                                    is1 = checkUnicity(em, uid, model.getAction());
                                }
                            }
                            if (is1.isEmpty()) {
                                em.merge(j);
                            }
                        }

                        tx.commit();
                    }
                    success = true;
                } catch (Exception e) {
                    if (tx.isActive()) {
                        tx.rollback();
                    }
                    if (isDeadlockException(e)) {
                        retries++;
                        System.out.println("Deadlock trouve dans journal, recommencement....");
                    } else if (isUniqueConstraintViolation(e)) {
                        System.err.println("Doublon Unique detecte, dans journal saut...");
                        break;
                    } else {
                        e.printStackTrace();
                        break;
                    }
                } finally {
                    em.close();
                }

            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private List<Journal> checkUnicity(EntityManager em, String uid, String action) {
        try {
            Query Q = em.createNativeQuery("SELECT * FROM journal WHERE stringuid = ? AND intuid = ? AND longuid = ? AND actionname = ?", Journal.class);
            Q.setParameter(1, uid);
            Q.setParameter(2, 0).setParameter(3, 0);
            Q.setParameter(4, action);
            return Q.getResultList();
        } catch (jakarta.persistence.NoResultException e) {
            return null;
        }

    }

    private List<Journal> checkUnicity(EntityManager em, int uid, String action) {
        try {
            Query Q = em.createNativeQuery("SELECT * FROM journal WHERE stringuid = ? AND intuid = ? AND longuid = ? AND actionname = ?", Journal.class);
            Q.setParameter(1, "SAVED");
            Q.setParameter(2, uid);
            Q.setParameter(3, 0).setParameter(4, action);
            return Q.getResultList();
        } catch (jakarta.persistence.NoResultException e) {
            return null;
        }

    }

    private List<Journal> checkUnicity(EntityManager em, long uid, String action) {
        try {
            Query Q = em.createNativeQuery("SELECT * FROM journal WHERE stringuid = ? AND intuid = ? AND longuid = ? AND actionname = ?", Journal.class);
            Q.setParameter(1, "SAVED");
            Q.setParameter(2, 0);
            Q.setParameter(3, uid).setParameter(4, action);
            return Q.getResultList();
        } catch (jakarta.persistence.NoResultException e) {
            return null;
        }

    }

    private String getUid(BaseModel obj) {
        if (obj instanceof Category) {
            Category ins = (Category) obj;
            return ins.getUid();
        } else if (obj instanceof Produit ins) {
            return ins.getUid();
        } else if (obj instanceof Mesure ins) {
            return ins.getUid();
        } else if (obj instanceof Fournisseur ins) {
            return ins.getUid();
        } else if (obj instanceof Livraison ins) {
            return ins.getUid();
        } else if (obj instanceof Stocker ins) {
            return ins.getUid();
        } else if (obj instanceof Destocker ins) {
            return ins.getUid();
        } else if (obj instanceof Recquisition ins) {
            return ins.getUid();
        } else if (obj instanceof PrixDeVente ins) {
            return ins.getUid();
        } else if (obj instanceof Client ins) {
            return ins.getUid();
        } else if (obj instanceof ClientAppartenir ins) {
            return ins.getUid();
        } else if (obj instanceof ClientOrganisation ins) {
            return ins.getUid();
        } else if (obj instanceof RetourMagasin ins) {
            return ins.getUid();
        } else if (obj instanceof RetourDepot ins) {
            return ins.getUid();
        } else if (obj instanceof Aretirer ins) {
            return ins.getUid();
        } else if (obj instanceof CompteTresor ins) {
            return ins.getUid();
        } else if (obj instanceof Depense ins) {
            return ins.getUid();
        } else if (obj instanceof Traisorerie ins) {
            return ins.getUid();
        } else if (obj instanceof Operation ins) {
            return ins.getUid();
        } else if (obj instanceof Facture ins) {
            return ins.getUid();
        }
        return null;
    }

    private boolean isDeadlockException(Exception e) {
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause.getMessage().contains("Deadlock found when trying to get lock")) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    private boolean isUniqueConstraintViolation(Exception e) {
        Throwable t = e;
        while (t != null) {
            if (t instanceof SQLIntegrityConstraintViolationException) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }

}
