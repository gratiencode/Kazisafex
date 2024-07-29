/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tools;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import jakarta.persistence.EntityManager;

import jakarta.persistence.EntityManagerFactory;

import jakarta.persistence.EntityTransaction;
import data.BaseModel;
import data.Category;
import data.Client;
import data.ClientAppartenir;
import data.ClientOrganisation;
import data.CompteTresor;
import data.Depense;
import data.Destocker;
import data.Fournisseur;
import data.LigneVente;
import data.Livraison;
import data.Mesure;
import data.Operation;
import data.PrixDeVente;
import data.Produit;
import data.Recquisition;
import data.RetourMagasin;
import data.Stocker;
import data.Traisorerie;
import data.Vente;
import jakarta.persistence.EntityExistsException;
import java.util.prefs.Preferences;
import static tools.Tables.ARETIRER;
import static tools.Tables.CATEGORY;
import static tools.Tables.CLIENT;
import static tools.Tables.CLIENTAPPARTENIR;
import static tools.Tables.CLIENTORGANISATION;
import static tools.Tables.COMPTETRESOR;
import static tools.Tables.DEPENSE;
import static tools.Tables.DESTOCKER;
import static tools.Tables.FACTURE;
import static tools.Tables.FOURNISSEUR;
import static tools.Tables.LIGNEVENTE;
import static tools.Tables.LIVRAISON;
import static tools.Tables.MESURE;
import static tools.Tables.OPERATION;
import static tools.Tables.PRIXDEVENTE;
import static tools.Tables.PRODUIT;
import static tools.Tables.RECQUISITION;
import static tools.Tables.REFRESH;
import static tools.Tables.RETOURDEPOT;
import static tools.Tables.RETOURMAGASIN;
import static tools.Tables.STOCKER;
import static tools.Tables.TRAISORERIE;
import static tools.Tables.VENTE;

/**
 *
 * @author endeleya
 */
public class PersisterTask implements Runnable {

    int MAX_RETRY = 5;
    private final EntityManagerFactory entityManagerFactory;
    private final Set<BaseModel> childEntities;
    private final CountDownLatch latch;
    

    public PersisterTask(EntityManagerFactory entityManagerFactory, Set<BaseModel> childEntities, CountDownLatch latch) {
        this.entityManagerFactory = entityManagerFactory;
        this.childEntities = childEntities;
        this.latch = latch;
    }

    @Override
    public void run() {
        int retries = 0;
        
        boolean success = false;
        while (retries < MAX_RETRY && !success) {
            EntityManager em = entityManagerFactory.createEntityManager();
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                for (BaseModel model : childEntities) {
                    String type = model.getType();
                    if (type.isEmpty()) {
                        continue;
                    }
                   
                    switch (type) {
                        case "CATEGORY" -> {
                            Category c = (Category) model;
                            Category ct = em.find(Category.class, c.getUid());
                            if (ct != null) {
                                em.merge(c);
                            } else {
                                em.persist(c);
//                                em.merge(Util.createJournal(c.getUid(), c, true));
                            }

                        }
                        case "PRODUIT" -> {

                            Produit p = (Produit) model;

                            Produit pt = em.find(Produit.class, p.getUid());
                            Category categ = em.find(Category.class, p.getCategoryId().getUid());
                            if (categ != null) {
                                if (pt == null) {
                                    em.persist(p);
//                                    em.merge(Util.createJournal(p.getUid(), p, true));
                                } else {
                                    em.merge(p);
//                                    em.merge(Util.createJournal(p.getUid(), p, true));
                                }

                            }

                        }

                        case "MESURE" -> {

                            Mesure mz = (Mesure) model;

                            // Vérifier l'existence du parent (Department)
                            Produit pr = em.find(Produit.class, mz.getProduitId().getUid());
                            if (pr != null) {
                                Mesure mex = em.find(Mesure.class, mz.getUid());
                                if (mex != null) {
                                    em.merge(mz);
//                                    em.merge(Util.createJournal(mz.getUid(), mz, true));
                                } else {
                                    em.persist(mz);
//                                    em.merge(Util.createJournal(mz.getUid(), mz, true));
                                }
                            }

                        }
                        case "FOURNISSEUR" -> {

                            Fournisseur mz = (Fournisseur) model;

                           Fournisseur exist = em.find(Fournisseur.class, mz.getUid());
                                if(exist!=null){
                                em.merge(mz);
                                }else{
                                em.persist(mz);
                                }

                        }
                        case "LIVRAISON" -> {

                            Livraison mz = (Livraison) model;
                            // Vérifier l'existence du parent (Department)
                            Fournisseur pr = em.find(Fournisseur.class, mz.getFournId().getUid());
                            if (pr != null) {
                                Livraison exist = em.find(Livraison.class, mz.getUid());
                                if (exist != null) {
                                    em.merge(mz);
                                } else {
                                    em.persist(mz);
                                }
                            }

                        }
                        case "STOCKER" -> {

                            Stocker mz = (Stocker) model;

                            // Vérifier l'existence du parent (Department)
                            Produit pr = em.find(Produit.class, mz.getProductId().getUid());
                            Mesure m = em.find(Mesure.class, mz.getMesureId().getUid());
                            Livraison l = em.find(Livraison.class, mz.getLivraisId().getUid());
                            if (pr != null && m != null && l != null) {
                                Stocker exist = em.find(Stocker.class, mz.getUid());
                                if (exist != null) {
                                    em.merge(mz);
                                } else {
                                    em.persist(mz);
                                }
                            }

                        }
                        case "DESTOCKER" -> {

                            Destocker mz = (Destocker) model;

                            // Vérifier l'existence du parent (Department)
                            Produit pr = em.find(Produit.class, mz.getProductId().getUid());
                            Mesure m = em.find(Mesure.class, mz.getMesureId().getUid());
                            if (pr != null && m != null) {
                                Destocker exist = em.find(Destocker.class, mz.getUid());
                                if (exist != null) {
                                    em.merge(mz);
                                } else {
                                    em.persist(mz);
                                }
                            } else {

                            }

                        }
                        case "RECQUISITION" -> {
                            Recquisition mz = (Recquisition) model;
                            // Vérifier l'existence du parent (Department)
                            Produit pr = em.find(Produit.class, mz.getProductId().getUid());
                            Mesure m = em.find(Mesure.class, mz.getMesureId().getUid());
                            if (pr != null && m != null) {
                                Recquisition exist = em.find(Recquisition.class, mz.getUid());
                                if (exist != null) {
                                    em.merge(mz);
                                } else {
                                    em.persist(mz);
                                }
                            }
                        }
                        case "PRIXDEVENTE" -> {
                            PrixDeVente mz = (PrixDeVente) model;
                            // Vérifier l'existence du parent (Department)
                            Recquisition pr = em.find(Recquisition.class, mz.getRecquisitionId().getUid());
                            Mesure m = em.find(Mesure.class, mz.getMesureId().getUid());
                            if (pr != null && m != null) {
                                PrixDeVente exist = em.find(PrixDeVente.class, mz.getUid());
                                if (exist != null) {
                                    em.merge(mz);
                                } else {
                                    em.persist(mz);
                                }
                            }
                        }
                        case "CLIENT" -> {

                            Client mz = (Client) model;
                            if (mz.getUid() != null) {
                                Client exist = em.find(Client.class, mz.getUid());
                                if (exist != null) {
                                    em.merge(mz);
                                } else {
                                    em.persist(mz);
                                }
                            }
                        }
                        case "CLIENTORGANISATION" -> {

                            ClientOrganisation mz = (ClientOrganisation) model;

                            ClientOrganisation exist = em.find(ClientOrganisation.class, mz.getUid());
                            if (exist != null) {
                                em.merge(mz);
                            } else {
                                em.persist(mz);
                            }

                        }
                        case "CLIENTAPPARTENIR" -> {

                            ClientAppartenir mz = (ClientAppartenir) model;

                            // Vérifier l'existence du parent (Department)
                            ClientOrganisation pr = em.find(ClientOrganisation.class, mz.getClientOrganisationId().getUid());
                            Client m = em.find(Client.class, mz.getClientId().getUid());
                            if (pr != null && m != null) {
                                ClientAppartenir exist = em.find(ClientAppartenir.class, mz.getUid());
                                if (exist != null) {
                                    em.merge(mz);
                                } else {
                                    em.persist(mz);
                                }
                            }

                        }
                        case "VENTE" -> {

                            Vente mz = (Vente) model;

                            // Vérifier l'existence du parent (Department)
                            Client pr = em.find(Client.class, mz.getClientId().getUid());
                            if (pr != null) {
                                Vente exist = em.find(Vente.class, mz.getUid());
                                if (exist != null) {
                                    em.merge(mz);
                                } else {
                                    em.persist(mz);
                                }
                            }

                        }
                        case "LIGNEVENTE" -> {

                            LigneVente mz = (LigneVente) model;

                            // Vérifier l'existence du parent (Department)
                            Produit pr = em.find(Produit.class, mz.getProductId().getUid());
                            Mesure m = em.find(Mesure.class, mz.getMesureId().getUid());
                            Vente v = em.find(Vente.class, mz.getReference().getUid());
                            if (pr != null && m != null && v != null) {
                                LigneVente exist = em.find(LigneVente.class, mz.getUid());
                                if (exist != null) {
                                    em.merge(mz);
                                } else {
                                    em.persist(mz);
                                }
                            }

                        }
                        case "COMPTETRESOR" -> {
                            CompteTresor mz = (CompteTresor) model;
                            CompteTresor exist = em.find(CompteTresor.class, mz.getUid());
                            if (exist != null) {
                                em.merge(mz);
                            } else {
                                em.persist(mz);
                            }

                        }
                        case "TRAISORERIE" -> {

                            Traisorerie mz = (Traisorerie) model;
                            // Vérifier l'existence du parent (Department)
                            CompteTresor pr = em.find(CompteTresor.class, mz.getTresorId().getUid());
                            if (pr != null) {
                                Traisorerie exist = em.find(Traisorerie.class, mz.getUid());
                                if (exist != null) {
                                    em.merge(mz);
                                } else {
                                    em.persist(mz);
                                }
                            }

                        }
                        case "DEPENSE" -> {

                            Depense mz = (Depense) model;
                            Depense exist = em.find(Depense.class, mz.getUid());
                            if (exist != null) {
                                em.merge(mz);
                            } else {
                                em.persist(mz);
                            }

                        }
                        case "OPERATION" -> {
                            Operation mz = (Operation) model;
                            // Vérifier l'existence du parent (Department)
                            Depense pr = em.find(Depense.class, mz.getDepenseId().getUid());
                            CompteTresor ct = em.find(CompteTresor.class, mz.getTresorId().getUid());
                            Traisorerie tr = em.find(Traisorerie.class, mz.getTresorId().getUid());
                            if (pr != null && ct != null && tr != null) {
                                Operation exist = em.find(Operation.class, mz.getUid());
                                if (exist != null) {
                                    em.merge(mz);
                                } else {
                                    em.persist(mz);
                                }
                            }

                        }
                        case "RETOURDEPOT" -> {

                        }
                        case "RETOURMAGASIN" -> {
                            RetourMagasin mz = (RetourMagasin) model;
                            RetourMagasin exist = em.find(RetourMagasin.class, mz.getUid());
                            if (exist != null) {
                                em.merge(mz);
                            } else {
                                em.persist(mz);
                            }
                        }
                        case "ARETIRER" -> {

                        }
                        case "FACTURE" -> {

                        }
                        case "REFRESH" -> {

                        }

                    }
                }
                tx.commit();
                success = true;
            } catch (Exception e) {
                if (tx.isActive()) {
                    tx.rollback();
                }
                if (isDeadlockException(e)) {
                    retries++;
                    System.out.println("Dead lock detecte " + retries + " fois, reessaie en cours.... ");
                } else if (isUniqueConstraintViolation(e)) {
                    System.out.println("Doublon dans les donnees saut executed");
                    break;
                } else if (isEntityExistException(e)) {
                    break;
                }else if (isIllegalStateException(e)) {
                    break;
                } else {
                    e.printStackTrace();
                    break;
                }
            } finally {
                em.close();
            }
        }
        latch.countDown();
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

    private boolean isEntityExistException(Exception e) {
        Throwable x = e;
        while (x != null) {
            if (x instanceof EntityExistsException) {
                return true;
            }
            x = x.getCause();
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
    
    private boolean isIllegalStateException(Exception e) {
        Throwable t = e;
        while (t != null) {
            if (t instanceof IllegalStateException) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }
}
