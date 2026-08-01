/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import utilities.ImageProduit;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;

import data.Abonnement;
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
import data.Taxe;
import data.Taxer;
import data.BulkModel;
import data.Periode;
import data.Depot;
import data.Commande;
import data.CommandeLister;
import data.Matiere;
import data.MatiereSku;
import data.Production;
import data.Repartir;
import data.Imputer;
import data.Entreposer;
import data.Satisfaire;
import data.Refresher;
import data.Permission;
import data.Immobilisation;
import data.Presence;
import data.FingerprintMapping;
import java.time.LocalDate;
import java.time.LocalDateTime;
import data.BaseModel;
import data.core.KazisafeServiceFactory;
import jakarta.json.stream.JsonParser;
import data.Inventaire;
import data.Compter;
import data.Matiere;
import data.Depot;
import data.Immobilisation;
import data.Entreposer;
import data.Imputer;
import data.Repartir;
import data.Production;
import data.MatiereSku;
import data.Presence;
import data.Entreprise;
import data.Employee;
import java.util.prefs.Preferences;

/**
 *
 * @author eroot
 */
public class JsonUtil {

    public static JsonObject jsonify(Object obj) {

        JsonObjectBuilder builder = Json.createObjectBuilder();
        if (obj instanceof Category) {
            try {
                Category category = (Category) obj;
                builder.add("uid", category.getUid() == null ? "" : category.getUid())
                        .add("descritption", category.getDescritption() == null ? "" : category.getDescritption());
            } catch (Exception e) {
                Category category = (Category) obj;
                builder.add("uid", category.getUid() == null ? "" : category.getUid())
                        .add("descritption", "");
            }
        } else if (obj instanceof Produit) {
            try {
                Produit p = (Produit) obj;
                builder.add("uid", p.getUid() == null ? "" : p.getUid())
                        .add("nomProduit", p.getNomProduit() == null ? "" : p.getNomProduit())
                        .add("marque", p.getMarque() == null ? "" : p.getMarque())
                        .add("modele", p.getModele() == null ? "" : p.getModele())
                        .add("couleur", p.getCouleur()==null?"":p.getCouleur())
                        .add("codebar", p.getCodebar() == null ? "" : p.getCodebar())
                        .add("taille", "")
                        .add("categoryId", Json.createObjectBuilder()
                                .add("uid", (p.getCategoryId() == null || p.getCategoryId().getUid() == null) ? 
                                        "" : p.getCategoryId().getUid()).build());
                if (p.getCouleur() != null) {
                    builder.add("couleur", p.getCouleur());
                }
                if (p.getTaille() != null) {
                    builder.add("taille", p.getTaille());
                }
                if (p.getMethodeInventaire() != null) {
                    builder.add("methodeInventaire", p.getMethodeInventaire());
                } else {
                    builder.add("methodeInventaire", "FIFO");
                }
            } catch (Exception e) {
                Produit p = (Produit) obj;
                builder.add("uid", p.getUid() == null ? "" : p.getUid())
                        .add("nomProduit", "")
                        .add("marque", "")
                        .add("modele", "")
                        .add("couleur", "")
                        .add("codebar", "")
                        .add("taille", "")
                        .add("categoryId", Json.createObjectBuilder().add("uid", "").build())
                        .add("methodeInventaire", "FIFO");
            }
        } else if (obj instanceof Mesure) {
            try {
                Mesure ins = (Mesure) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("description", ins.getDescription() == null ? "" : ins.getDescription())
                        .add("quantContenu", ins.getQuantContenu() == null ? 0 : ins.getQuantContenu())
                        .add("produitId", Json.createObjectBuilder()
                                .add("uid", (ins.getProduitId() == null || ins.getProduitId().getUid() == null) ? "" : ins.getProduitId().getUid()).build());
            } catch (Exception e) {
                Mesure ins = (Mesure) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("description", "")
                        .add("quantContenu", 0)
                        .add("produitId", Json.createObjectBuilder().add("uid", "").build());
            }
        } else if (obj instanceof Fournisseur) {
            try {
                Fournisseur ins = (Fournisseur) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("adresse", ins.getAdresse() == null ? "-" : ins.getAdresse())
                        .add("identification", ins.getIdentification() == null ? "-" : ins.getIdentification())
                        .add("nomFourn", ins.getNomFourn() == null ? "-" : ins.getNomFourn());
                if (ins.getPhone() == null) {
                    builder.add("phone", "Non disponible");
                } else {
                    builder.add("phone", ins.getPhone());
                }
            } catch (Exception e) {
                Fournisseur ins = (Fournisseur) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("adresse", "-")
                        .add("identification", "-")
                        .add("nomFourn", "-")
                        .add("phone", "Non disponible");
            }
        } else if (obj instanceof Livraison) {
            try {
                Livraison ins = (Livraison) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("numPiece", ins.getNumPiece() == null ? "" : ins.getNumPiece())
                        .add("observation", ins.getObservation() == null ? "" : ins.getObservation())
                        .add("payed", ins.getPayed() == null ? 0 : ins.getPayed())
                        .add("reduction", ins.getReduction() == null ? 0 : ins.getReduction())
                        .add("reference", ins.getReference() == null ? "-" : ins.getReference())
                        .add("region", ins.getRegion() == null ? "-" : ins.getRegion())
                        .add("remained", ins.getRemained() == null ? 0 : ins.getRemained())
                        .add("topay", ins.getTopay() == null ? 0 : ins.getTopay())
                        .add("toreceive", ins.getToreceive() == null ? 0 : ins.getToreceive())
                        .add("dateLivr", ins.getDateLivr() == null ? ""
                                : ins.getDateLivr().toString())
                        .add("fournId", Json.createObjectBuilder()
                                .add("uid", (ins.getFournId() == null || ins.getFournId().getUid() == null) ? "-" : ins.getFournId().getUid()).build());
                if (ins.getLibelle() != null) {
                    builder.add("libelle", ins.getLibelle());
                } else {
                    builder.add("libelle", "-");
                }
            } catch (Exception e) {
                // Fallback to minimal json if something goes wrong
                Livraison ins = (Livraison) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("numPiece", "")
                        .add("observation", "")
                        .add("payed", 0)
                        .add("reduction", 0)
                        .add("reference", "-")
                        .add("region", "-")
                        .add("remained", 0)
                        .add("topay", 0)
                        .add("toreceive", 0)
                        .add("dateLivr", "-")
                        .add("fournId", Json.createObjectBuilder().add("uid", "-").build())
                        .add("libelle", "-");
            }
        } else if (obj instanceof Stocker) {
            try {
                Stocker ins = (Stocker) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("libelle", ins.getLibelle() == null ? "" : ins.getLibelle())
                        .add("coutAchat", ins.getCoutAchat())
                        .add("observation", ins.getObservation() == null ? "" : ins.getObservation())
                        .add("numlot", ins.getNumlot() == null ? (ins.getDateStocker() == null ? "" : ins.getDateStocker().toString()) : ins.getNumlot())
                        .add("reduction", ins.getReduction())
                        .add("prixAchatTotal", ins.getPrixAchatTotal())
                        .add("region", ins.getRegion() == null ? "" : ins.getRegion())
                        .add("quantite", ins.getQuantite())
                        .add("localisation", ins.getLocalisation() == null ? "" : ins.getLocalisation())
                        .add("stockAlerte", ins.getStockAlerte())
                        .add("dateStocker", ins.getDateStocker() == null ? "" : ins.getDateStocker().toString())
                        .add("livraisId", Json.createObjectBuilder()
                                .add("uid", (ins.getLivraisId() == null || ins.getLivraisId().getUid() == null) ? "" : ins.getLivraisId().getUid()).build())
                        .add("mesureId", Json.createObjectBuilder()
                                .add("uid", (ins.getMesureId() == null || ins.getMesureId().getUid() == null) ? "" : ins.getMesureId().getUid()).build())
                        .add("productId", Json.createObjectBuilder()
                                .add("uid", (ins.getProductId() == null || ins.getProductId().getUid() == null) ? "" : ins.getProductId().getUid()).build());
                if (ins.getDateExpir() != null) {
                    builder.add("dateExpir", ins.getDateExpir().toString());
                }
            } catch (Exception e) {
                Stocker ins = (Stocker) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("libelle", "")
                        .add("coutAchat", 0)
                        .add("observation", "")
                        .add("numlot", "")
                        .add("reduction", 0)
                        .add("prixAchatTotal", 0)
                        .add("region", "")
                        .add("quantite", 0)
                        .add("localisation", "")
                        .add("stockAlerte", 0)
                        .add("dateStocker", "")
                        .add("livraisId", Json.createObjectBuilder().add("uid", "").build())
                        .add("mesureId", Json.createObjectBuilder().add("uid", "").build())
                        .add("productId", Json.createObjectBuilder().add("uid", "").build());
            }
        } else if (obj instanceof Destocker) {
            try {
                Destocker ins = (Destocker) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("libelle", ins.getLibelle() == null ? "" : ins.getLibelle())
                        .add("coutAchat", ins.getCoutAchat())
                        .add("observation", ins.getObservation() == null ? "" : ins.getObservation())
                        .add("numlot", ins.getNumlot() == null ? (ins.getDateDestockage() == null ? "" : ins.getDateDestockage().toString()) : ins.getNumlot())
                        .add("reference", ins.getReference() == null ? "" : ins.getReference())
                        .add("region", ins.getRegion() == null ? "" : ins.getRegion())
                        .add("quantite", ins.getQuantite())
                        .add("destination", ins.getDestination() == null ? "" : ins.getDestination())
                        .add("dateDestockage", ins.getDateDestockage() == null ? "" : ins.getDateDestockage().toString())
                        .add("mesureId", Json.createObjectBuilder()
                                .add("uid", (ins.getMesureId() == null || ins.getMesureId().getUid() == null) ? "" : ins.getMesureId().getUid()).build())
                        .add("productId", Json.createObjectBuilder()
                                .add("uid", (ins.getProductId() == null || ins.getProductId().getUid() == null) ? "" : ins.getProductId().getUid()).build());
            } catch (Exception e) {
                Destocker ins = (Destocker) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("libelle", "")
                        .add("coutAchat", 0)
                        .add("observation", "")
                        .add("numlot", "")
                        .add("reference", "")
                        .add("region", "")
                        .add("quantite", 0)
                        .add("destination", "")
                        .add("dateDestockage", "")
                        .add("mesureId", Json.createObjectBuilder().add("uid", "").build())
                        .add("productId", Json.createObjectBuilder().add("uid", "").build());
            }
        } else if (obj instanceof Recquisition) {
            try {
                Recquisition ins = (Recquisition) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("reference", ins.getReference() == null ? "" : ins.getReference())
                        .add("coutAchat", ins.getCoutAchat())
                        .add("observation", ins.getObservation() == null ? "" : ins.getObservation())
                        .add("numlot", ins.getNumlot() == null ? (ins.getDate() == null ? "" : ins.getDate().toString()) : ins.getNumlot())
                        .add("region", ins.getRegion() == null ? "" : ins.getRegion())
                        .add("quantite", ins.getQuantite())
                        .add("stockAlert", ins.getStockAlert() == null ? 0 : ins.getStockAlert())
                        .add("date", ins.getDate() == null ? "" : ins.getDate().toString())
                        .add("mesureId", Json.createObjectBuilder()
                                .add("uid", (ins.getMesureId() == null || ins.getMesureId().getUid() == null) ? "" : ins.getMesureId().getUid()).build())
                        .add("productId", Json.createObjectBuilder()
                                .add("uid", (ins.getProductId() == null || ins.getProductId().getUid() == null) ? "" : ins.getProductId().getUid()).build());
                if (ins.getDateExpiry() != null) {
                    builder.add("dateExpiry", ins.getDateExpiry().toString());
                }
            } catch (Exception e) {
                Recquisition ins = (Recquisition) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("reference", "")
                        .add("coutAchat", 0)
                        .add("observation", "")
                        .add("numlot", "")
                        .add("region", "")
                        .add("quantite", 0)
                        .add("stockAlert", 0)
                        .add("date", "")
                        .add("mesureId", Json.createObjectBuilder().add("uid", "").build())
                        .add("productId", Json.createObjectBuilder().add("uid", "").build());
            }
        } else if (obj instanceof PrixDeVente) {
            try {
                PrixDeVente ins = (PrixDeVente) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("qmax", ins.getQmax())
                        .add("qmin", ins.getQmin())
                        .add("devise", ins.getDevise() == null ? "" : ins.getDevise())
                        .add("prixUnitaire", ins.getPrixUnitaire())
                        .add("pourcentParCunit", ins.getPourcentParCunit() == null ? 0 : ins.getPourcentParCunit())
                        .add("mesureId", Json.createObjectBuilder()
                                .add("uid", (ins.getMesureId() == null || ins.getMesureId().getUid() == null) ? "" : ins.getMesureId().getUid()).build())
                        .add("recquisitionId", Json.createObjectBuilder()
                                .add("uid", (ins.getRecquisitionId() == null || ins.getRecquisitionId().getUid() == null) ? "" : ins.getRecquisitionId().getUid()).build());
            } catch (Exception e) {
                PrixDeVente ins = (PrixDeVente) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("qmax", 0)
                        .add("qmin", 0)
                        .add("devise", "")
                        .add("prixUnitaire", 0)
                        .add("mesureId", Json.createObjectBuilder().add("uid", "").build())
                        .add("recquisitionId", Json.createObjectBuilder().add("uid", "").build());
            }
        } else if (obj instanceof Client) {
            try {
                Client ins = (Client) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("adresse", ins.getAdresse() == null ? "" : ins.getAdresse())
                        .add("email", ins.getEmail() == null ? "" : ins.getEmail())
                        .add("typeClient", ins.getTypeClient() == null ? "" : ins.getTypeClient())
                        .add("nomClient", ins.getNomClient() == null ? "" : ins.getNomClient())
                        .add("phone", ins.getPhone() == null ? "" : ins.getPhone());
                if (ins.getParentId() != null && ins.getParentId().getUid() != null) {
                    builder.add("parentId", Json.createObjectBuilder()
                            .add("uid", ins.getParentId().getUid()).build());
                } else {
                    builder.add("parentId", Json.createObjectBuilder()
                            .add("uid", ins.getUid() == null ? "" : ins.getUid()).build());
                }
            } catch (Exception e) {
                Client ins = (Client) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("adresse", "")
                        .add("email", "")
                        .add("typeClient", "")
                        .add("nomClient", "")
                        .add("phone", "")
                        .add("parentId", Json.createObjectBuilder()
                                .add("uid", ins.getUid() == null ? "" : ins.getUid()).build());
            }
        } else if (obj instanceof Vente) {
            try {
                Vente ins = (Vente) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid().toString())
                        .add("libelle", ins.getLibelle() == null ? "" : ins.getLibelle())
                        .add("latitude", ins.getLatitude() == null ? 0 : ins.getLatitude())
                        .add("observation", ins.getObservation() == null ? "" : ins.getObservation())
                        .add("longitude", ins.getLongitude() == null ? 0 : ins.getLongitude())
                        .add("montantCdf", ins.getMontantCdf())
                        .add("montantDette", ins.getMontantDette() == null ? 0 : ins.getMontantDette())
                        .add("region", ins.getRegion() == null ? "" : ins.getRegion())
                        .add("montantUsd", ins.getMontantUsd())
                        .add("payment", ins.getPayment() == null ? "" : ins.getPayment())
                        .add("reference", ins.getReference() == null ? "" : ins.getReference())
                        .add("dateVente", ins.getDateVente() == null ? "" : ins.getDateVente().toString())
                        .add("deviseDette", ins.getDeviseDette() == null ? "" : ins.getDeviseDette());

                JsonObjectBuilder jsob = Json.createObjectBuilder();
                if (ins.getClientId() != null) {
                    jsob.add("uid", ins.getClientId().getUid() == null ? "" : ins.getClientId().getUid())
                            .add("adresse", ins.getClientId().getAdresse() == null ? "" : ins.getClientId().getAdresse())
                            .add("email", ins.getClientId().getEmail() == null ? "" : ins.getClientId().getEmail())
                            .add("typeClient", ins.getClientId().getTypeClient() == null ? "" : ins.getClientId().getTypeClient())
                            .add("nomClient", ins.getClientId().getNomClient() == null ? "" : ins.getClientId().getNomClient())
                            .add("phone", ins.getClientId().getPhone() == null ? "" : ins.getClientId().getPhone());

                    Client c = ins.getClientId().getParentId();
                    if (c == null || c.getUid() == null) {
                        jsob.add("parentId", Json.createObjectBuilder().add("uid", ins.getClientId().getUid() == null ? "" : ins.getClientId().getUid()).build());
                    } else {
                        jsob.add("parentId", Json.createObjectBuilder().add("uid", c.getUid()).build());
                    }
                } else {
                    jsob.add("uid", "")
                            .add("adresse", "")
                            .add("email", "")
                            .add("typeClient", "")
                            .add("nomClient", "")
                            .add("phone", "")
                            .add("parentId", Json.createObjectBuilder().add("uid", "").build());
                }
                JsonObject sobj = jsob.build();
                builder.add("clientId", sobj);

                if (ins.getEcheance() != null) {
                    builder.add("echeance", ins.getEcheance().toString());
                }
            } catch (Exception e) {
                Vente ins = (Vente) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid().toString())
                        .add("libelle", "")
                        .add("latitude", 0)
                        .add("observation", "")
                        .add("longitude", 0)
                        .add("montantCdf", 0)
                        .add("montantDette", 0)
                        .add("region", "")
                        .add("montantUsd", 0)
                        .add("payment", "")
                        .add("reference", "")
                        .add("dateVente", "")
                        .add("deviseDette", "")
                        .add("clientId", Json.createObjectBuilder()
                                .add("uid", "")
                                .add("adresse", "")
                                .add("email", "")
                                .add("typeClient", "")
                                .add("nomClient", "")
                                .add("phone", "")
                                .add("parentId", Json.createObjectBuilder().add("uid", "").build()).build());
            }
        } else if (obj instanceof LigneVente) {
            try {
                LigneVente ins = (LigneVente) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid().toString())
                        .add("numlot", ins.getNumlot() == null ? "" : ins.getNumlot())
                        .add("prixUnit", ins.getPrixUnit() == null ? 0 : ins.getPrixUnit())
                        .add("quantite", ins.getQuantite())
                        .add("montantCdf", ins.getMontantCdf())
                        .add("montantUsd", ins.getMontantUsd())
                        .add("coutAchat", ins.getCoutAchat() == null ? 0 : ins.getCoutAchat())
                        .add("productId", Json.createObjectBuilder()
                                .add("uid", (ins.getProductId() == null || ins.getProductId().getUid() == null) ? "" : ins.getProductId().getUid()).build())
                        .add("mesureId", Json.createObjectBuilder()
                                .add("uid", (ins.getMesureId() == null || ins.getMesureId().getUid() == null) ? "" : ins.getMesureId().getUid()).build());

                if (ins.getReference() != null && ins.getReference().getUid() != null) {
                    builder.add("reference", Json.createObjectBuilder()
                            .add("uid", ins.getReference().getUid()).build());
                }
                if (ins.getClientId() != null) {
                    builder.add("clientId", ins.getClientId());
                }
            } catch (Exception e) {
                LigneVente ins = (LigneVente) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid().toString())
                        .add("numlot", "")
                        .add("prixUnit", 0)
                        .add("quantite", 0)
                        .add("montantCdf", 0)
                        .add("montantUsd", 0)
                        .add("productId", Json.createObjectBuilder().add("uid", "").build())
                        .add("mesureId", Json.createObjectBuilder().add("uid", "").build());
            }
        } else if (obj instanceof Traisorerie) {
            try {
                Traisorerie ins = (Traisorerie) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("libelle", ins.getLibelle() == null ? "" : ins.getLibelle())
                        .add("mouvement", ins.getMouvement() == null ? "" : ins.getMouvement())
                        .add("typeTresorerie", ins.getTypeTresorerie() == null ? "" : ins.getTypeTresorerie())
                        .add("montantCdf", ins.getMontantCdf())
                        .add("region", ins.getRegion() == null ? "" : ins.getRegion())
                        .add("montantUsd", ins.getMontantUsd())
                        .add("reference", ins.getReference() == null ? "" : ins.getReference())
                        .add("tresorId", Json.createObjectBuilder()
                                .add("uid", (ins.getTresorId() == null || ins.getTresorId().getUid() == null) ? "" : ins.getTresorId().getUid()).build())
                        .add("date", ins.getDate() == null ? "" : ins.getDate().toString());
            } catch (Exception e) {
                Traisorerie ins = (Traisorerie) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("libelle", "")
                        .add("mouvement", "")
                        .add("typeTresorerie", "")
                        .add("montantCdf", 0)
                        .add("region", "")
                        .add("montantUsd", 0)
                        .add("reference", "")
                        .add("tresorId", Json.createObjectBuilder().add("uid", "").build())
                        .add("date", "");
            }
        } else if (obj instanceof Operation) {
            try {
                Operation ins = (Operation) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("libelle", ins.getLibelle() == null ? "" : ins.getLibelle())
                        .add("mouvement", ins.getMouvement() == null ? "" : ins.getMouvement())
                        .add("imputation", ins.getImputation() == null ? "" : ins.getImputation())
                        .add("montantCdf", ins.getMontantCdf() == null ? 0 : ins.getMontantCdf())
                        .add("region", ins.getRegion() == null ? "" : ins.getRegion())
                        .add("tresorId", Json.createObjectBuilder()
                                .add("uid", (ins.getTresorId() == null || ins.getTresorId().getUid() == null) ? "" : ins.getTresorId().getUid()).build())
                        .add("depenseId", Json.createObjectBuilder()
                                .add("uid", (ins.getDepenseId() == null || ins.getDepenseId().getUid() == null) ? "" : ins.getDepenseId().getUid()).build())
                        .add("montantUsd", ins.getMontantUsd() == null ? 0 : ins.getMontantUsd())
                        .add("referenceOp", ins.getReferenceOp() == null ? "" : ins.getReferenceOp())
                        .add("date", ins.getDate() == null ? "" : ins.getDate().toString());

                JsonObjectBuilder caisseOpBuilder = Json.createObjectBuilder();
                if (ins.getCaisseOpId() != null) {
                    caisseOpBuilder.add("uid", ins.getCaisseOpId().getUid() == null ? "" : ins.getCaisseOpId().getUid())
                            .add("libelle", ins.getCaisseOpId().getLibelle() == null ? "" : ins.getCaisseOpId().getLibelle())
                            .add("mouvement", ins.getCaisseOpId().getMouvement() == null ? "" : ins.getCaisseOpId().getMouvement())
                            .add("typeTresorerie", ins.getCaisseOpId().getTypeTresorerie() == null ? "" : ins.getCaisseOpId().getTypeTresorerie())
                            .add("montantCdf", ins.getCaisseOpId().getMontantCdf())
                            .add("region", ins.getCaisseOpId().getRegion() == null ? "" : ins.getCaisseOpId().getRegion())
                            .add("montantUsd", ins.getCaisseOpId().getMontantUsd())
                            .add("reference", ins.getCaisseOpId().getReference() == null ? "" : ins.getCaisseOpId().getReference())
                            .add("date", ins.getCaisseOpId().getDate() == null ? "" : ins.getCaisseOpId().getDate().toString());
                } else {
                    caisseOpBuilder.add("uid", "")
                            .add("libelle", "")
                            .add("mouvement", "")
                            .add("typeTresorerie", "")
                            .add("montantCdf", 0)
                            .add("region", "")
                            .add("montantUsd", 0)
                            .add("reference", "")
                            .add("date", "");
                }
                builder.add("caisseOpId", caisseOpBuilder.build());
            } catch (Exception e) {
                Operation ins = (Operation) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("libelle", "")
                        .add("mouvement", "")
                        .add("imputation", "")
                        .add("montantCdf", 0)
                        .add("region", "")
                        .add("tresorId", Json.createObjectBuilder().add("uid", "").build())
                        .add("depenseId", Json.createObjectBuilder().add("uid", "").build())
                        .add("montantUsd", 0)
                        .add("referenceOp", "")
                        .add("date", "")
                        .add("caisseOpId", Json.createObjectBuilder()
                                .add("uid", "")
                                .add("libelle", "")
                                .add("mouvement", "")
                                .add("typeTresorerie", "")
                                .add("montantCdf", 0)
                                .add("region", "")
                                .add("montantUsd", 0)
                                .add("reference", "")
                                .add("date", "").build());
            }
        } else if (obj instanceof ImageProduit) {
            try {
                ImageProduit image = (ImageProduit) obj;
                builder.add("idProduit", image.getIdProduit() == null ? "" : image.getIdProduit())
                        .add("imageBase64", image.getImageBase64() == null ? "" : image.getImageBase64());
            } catch (Exception e) {
                ImageProduit image = (ImageProduit) obj;
                builder.add("idProduit", "")
                        .add("imageBase64", "");
            }
        } else if (obj instanceof Aretirer) {
            try {
                Aretirer oper = (Aretirer) obj;
                builder.add("uid", oper.getUid() == null ? "" : oper.getUid())
                        .add("numlot", oper.getNumlot() == null ? "" : oper.getNumlot())
                        .add("prixVente", oper.getPrixVente() == null ? 0 : oper.getPrixVente())
                        .add("quantite", oper.getQuantite() == null ? 0 : oper.getQuantite())
                        .add("referenceVente", oper.getReferenceVente() == null ? "" : oper.getReferenceVente())
                        .add("region", oper.getRegion() == null ? "" : oper.getRegion())
                        .add("status", oper.getStatus() == null ? "" : oper.getStatus())
                        .add("date", oper.getDate() == null ? "" : oper.getDate().toString())
                        .add("clientId", Json.createObjectBuilder()
                                .add("uid", (oper.getClientId() == null || oper.getClientId().getUid() == null) ? "" : oper.getClientId().getUid()).build())
                        .add("ligneVenteId", Json.createObjectBuilder()
                                .add("uid", (oper.getLigneVenteId() == null || oper.getLigneVenteId().getUid() == null) ? "" : oper.getLigneVenteId().getUid().toString()).build())
                        .add("mesureId", Json.createObjectBuilder()
                                .add("uid", (oper.getMesureId() == null || oper.getMesureId().getUid() == null) ? "" : oper.getMesureId().getUid()).build());
            } catch (Exception e) {
                Aretirer oper = (Aretirer) obj;
                builder.add("uid", oper.getUid() == null ? "" : oper.getUid())
                        .add("numlot", "")
                        .add("prixVente", 0)
                        .add("quantite", 0)
                        .add("referenceVente", "")
                        .add("region", "")
                        .add("status", "")
                        .add("date", "")
                        .add("clientId", Json.createObjectBuilder().add("uid", "").build())
                        .add("ligneVenteId", Json.createObjectBuilder().add("uid", "").build())
                        .add("mesureId", Json.createObjectBuilder().add("uid", "").build());
            }
        } else if (obj instanceof ClientAppartenir) {
            try {
                ClientAppartenir oper = (ClientAppartenir) obj;
                builder.add("uid", oper.getUid() == null ? "" : oper.getUid())
                        .add("region", oper.getRegion() == null ? "" : oper.getRegion())
                        .add("date", oper.getDateAppartenir() == null ? "" : oper.getDateAppartenir().toString())
                        .add("clientId", Json.createObjectBuilder()
                                .add("uid", (oper.getClientId() == null || oper.getClientId().getUid() == null) ? "" : oper.getClientId().getUid()).build())
                        .add("clientOrganisationId", Json.createObjectBuilder()
                                .add("uid", (oper.getClientOrganisationId() == null || oper.getClientOrganisationId().getUid() == null) ? "" : oper.getClientOrganisationId().getUid()).build());
            } catch (Exception e) {
                ClientAppartenir oper = (ClientAppartenir) obj;
                builder.add("uid", oper.getUid() == null ? "" : oper.getUid())
                        .add("region", "")
                        .add("date", "")
                        .add("clientId", Json.createObjectBuilder().add("uid", "").build())
                        .add("clientOrganisationId", Json.createObjectBuilder().add("uid", "").build());
            }
        } else if (obj instanceof ClientOrganisation) {
            try {
                ClientOrganisation oper = (ClientOrganisation) obj;
                builder.add("uid", oper.getUid() == null ? "" : oper.getUid())
                        .add("adresse", oper.getAdresse() == null ? "" : oper.getAdresse())
                        .add("boitePostalOrganisation", oper.getBoitePostalOrganisation() == null ? "" : oper.getBoitePostalOrganisation())
                        .add("domaineOrganisation", oper.getDomaineOrganisation() == null ? "" : oper.getDomaineOrganisation())
                        .add("emailOrganisation", oper.getEmailOrganisation() == null ? "" : oper.getEmailOrganisation())
                        .add("region", oper.getRegion() == null ? "" : oper.getRegion())
                        .add("nomOrganisation", oper.getNomOrganisation() == null ? "" : oper.getNomOrganisation())
                        .add("phoneOrganisation", oper.getPhoneOrganisation() == null ? "" : oper.getPhoneOrganisation())
                        .add("rccmOrganisation", oper.getRccmOrganisation() == null ? "" : oper.getRccmOrganisation())
                        .add("websiteOrganisation", oper.getWebsiteOrganisation() == null ? "" : oper.getWebsiteOrganisation());
            } catch (Exception e) {
                ClientOrganisation oper = (ClientOrganisation) obj;
                builder.add("uid", oper.getUid() == null ? "" : oper.getUid())
                        .add("adresse", "")
                        .add("boitePostalOrganisation", "")
                        .add("domaineOrganisation", "")
                        .add("emailOrganisation", "")
                        .add("region", "")
                        .add("nomOrganisation", "")
                        .add("phoneOrganisation", "")
                        .add("rccmOrganisation", "")
                        .add("websiteOrganisation", "");
            }
        } else if (obj instanceof RetourDepot) {
            try {
                RetourDepot oper = (RetourDepot) obj;
                builder.add("uid", oper.getUid() == null ? "" : oper.getUid())
                        .add("coutAchat", oper.getCoutAchat() == null ? 0 : oper.getCoutAchat())
                        .add("localisation", oper.getLocalisation() == null ? "" : oper.getLocalisation())
                        .add("motif", oper.getMotif() == null ? "" : oper.getMotif())
                        .add("numlot", oper.getNumlot() == null ? "" : oper.getNumlot())
                        .add("region", oper.getRegion() == null ? "" : oper.getRegion())
                        .add("quantite", oper.getQuantite() == null ? 0 : oper.getQuantite())
                        .add("regionDest", oper.getRegionDest() == null ? "" : oper.getRegionDest())
                        .add("regionProv", oper.getRegionProv() == null ? "" : oper.getRegionProv())
                        .add("date", oper.getDate() == null ? "" : oper.getDate().toString())
                        .add("destockerId", Json.createObjectBuilder()
                                .add("uid", (oper.getDestockerId() == null || oper.getDestockerId().getUid() == null) ? "" : oper.getDestockerId().getUid()).build())
                        .add("recquisitionId", Json.createObjectBuilder()
                                .add("uid", (oper.getRecquisitionId() == null || oper.getRecquisitionId().getUid() == null) ? "" : oper.getRecquisitionId().getUid()).build())
                        .add("mesureId", Json.createObjectBuilder()
                                .add("uid", (oper.getMesureId() == null || oper.getMesureId().getUid() == null) ? "" : oper.getMesureId().getUid()).build());
            } catch (Exception e) {
                RetourDepot oper = (RetourDepot) obj;
                builder.add("uid", oper.getUid() == null ? "" : oper.getUid())
                        .add("coutAchat", 0)
                        .add("localisation", "")
                        .add("motif", "")
                        .add("numlot", "")
                        .add("region", "")
                        .add("quantite", 0)
                        .add("regionDest", "")
                        .add("regionProv", "")
                        .add("date", "")
                        .add("destockerId", Json.createObjectBuilder().add("uid", "").build())
                        .add("recquisitionId", Json.createObjectBuilder().add("uid", "").build())
                        .add("mesureId", Json.createObjectBuilder().add("uid", "").build());
            }
        } else if (obj instanceof RetourMagasin) {
            try {
                RetourMagasin oper = (RetourMagasin) obj;
                builder.add("uid", oper.getUid() == null ? "" : oper.getUid())
                        .add("prixVente", oper.getPrixVente() == null ? 0 : oper.getPrixVente())
                        .add("referenceVente", oper.getReferenceVente() == null ? "" : oper.getReferenceVente())
                        .add("motif", oper.getMotif() == null ? "" : oper.getMotif())
                        .add("region", oper.getRegion() == null ? "" : oper.getRegion())
                        .add("quantite", oper.getQuantite() == null ? 0 : oper.getQuantite())
                        .add("date", oper.getDate() == null ? "" : oper.getDate().toString())
                        .add("clientId", Json.createObjectBuilder()
                                .add("uid", (oper.getClientId() == null || oper.getClientId().getUid() == null) ? "" : oper.getClientId().getUid()).build())
                        .add("ligneVenteId", Json.createObjectBuilder()
                                .add("uid", (oper.getLigneVenteId() == null || oper.getLigneVenteId().getUid() == null) ? "" : oper.getLigneVenteId().getUid().toString()).build())
                        .add("mesureId", Json.createObjectBuilder()
                                .add("uid", (oper.getMesureId() == null || oper.getMesureId().getUid() == null) ? "" : oper.getMesureId().getUid()).build());
            } catch (Exception e) {
                RetourMagasin oper = (RetourMagasin) obj;
                builder.add("uid", oper.getUid() == null ? "" : oper.getUid())
                        .add("prixVente", 0)
                        .add("referenceVente", "")
                        .add("motif", "")
                        .add("region", "")
                        .add("quantite", 0)
                        .add("date", "")
                        .add("clientId", Json.createObjectBuilder().add("uid", "").build())
                        .add("ligneVenteId", Json.createObjectBuilder().add("uid", "").build())
                        .add("mesureId", Json.createObjectBuilder().add("uid", "").build());
            }
        } else if (obj instanceof Abonnement) {
            try {
                Abonnement ab = (Abonnement) obj;
                builder.add("uid", ab.getUid() == null ? "" : ab.getUid())
                        .add("devise", ab.getDevise() == null ? "" : ab.getDevise())
                        .add("etat", ab.getEtat() == null ? "" : ab.getEtat())
                        .add("montant", ab.getMontant())
                        .add("nombreOperation", ab.getNombreOperation())
                        .add("typeAbonnement", ab.getTypeAbonnement() == null ? "" : ab.getTypeAbonnement())
                        .add("dateAbonnement", ab.getDateAbonnement() == null ? "" : ab.getDateAbonnement().toString());
            } catch (Exception e) {
                Abonnement ab = (Abonnement) obj;
                builder.add("uid", ab.getUid() == null ? "" : ab.getUid())
                        .add("devise", "")
                        .add("etat", "")
                        .add("montant", 0)
                        .add("nombreOperation", 0)
                        .add("typeAbonnement", "")
                        .add("dateAbonnement", "");
            }
        } else if (obj instanceof Facture) {
            try {
                Facture bill = (Facture) obj;
                builder.add("uid", bill.getUid() == null ? "" : bill.getUid())
                        .add("numero", bill.getNumero() == null ? "" : bill.getNumero())
                        .add("payedamount", bill.getPayedamount() == null ? 0 : bill.getPayedamount())
                        .add("region", bill.getRegion() == null ? "" : bill.getRegion())
                        .add("status", bill.getStatus() == null ? "" : bill.getStatus())
                        .add("totalamount", bill.getTotalamount() == null ? 0 : bill.getTotalamount())
                        .add("organisId", Json.createObjectBuilder()
                                .add("uid", (bill.getOrganisId() == null || bill.getOrganisId().getUid() == null) ? "" : bill.getOrganisId().getUid()).build())
                        .add("startDate", bill.getStartDate() == null ? "" : bill.getStartDate().toString())
                        .add("endDate", bill.getEndDate() == null ? "" :bill.getEndDate().toString());
            } catch (Exception e) {
                Facture bill = (Facture) obj;
                builder.add("uid", bill.getUid() == null ? "" : bill.getUid())
                        .add("numero", "")
                        .add("payedamount", 0)
                        .add("region", "")
                        .add("status", "")
                        .add("totalamount", 0)
                        .add("organisId", Json.createObjectBuilder().add("uid", "").build())
                        .add("startDate", "")
                        .add("endDate", "");
            }
        } else if (obj instanceof Depense) {
            try {
                Depense bill = (Depense) obj;
                builder.add("uid", bill.getUid() == null ? "" : bill.getUid())
                        .add("nomDepense", bill.getNomDepense() == null ? "" : bill.getNomDepense())
                        .add("region", bill.getRegion() == null ? "" : bill.getRegion());
            } catch (Exception e) {
                Depense bill = (Depense) obj;
                builder.add("uid", bill.getUid() == null ? "" : bill.getUid())
                        .add("nomDepense", "")
                        .add("region", "");
            }
        } else if (obj instanceof CompteTresor) {
            try {
                CompteTresor bill = (CompteTresor) obj;
                builder.add("uid", bill.getUid() == null ? "" : bill.getUid())
                        .add("bankName", bill.getBankName() == null ? "-" : bill.getBankName())
                        .add("intitule", bill.getIntitule() == null ? "-" : bill.getIntitule())
                        .add("numeroCompte", bill.getNumeroCompte() == null ? "-" : bill.getNumeroCompte())
                        .add("region", bill.getRegion() == null ? "-" : bill.getRegion())
                        .add("soldeMinimum", bill.getSoldeMinimum() == null ? 0 : bill.getSoldeMinimum())
                        .add("typeCompte", bill.getTypeCompte() == null ? "-" : bill.getTypeCompte());
            } catch (Exception e) {
                CompteTresor bill = (CompteTresor) obj;
                builder.add("uid", bill.getUid() == null ? "" : bill.getUid())
                        .add("bankName", "-")
                        .add("intitule", "-")
                        .add("numeroCompte", "-")
                        .add("region", "-")
                        .add("soldeMinimum", 0)
                        .add("typeCompte", "-");
            }
        } else if (obj instanceof Matiere) {
            try {
                Matiere ins = (Matiere) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("matiereName", ins.getMatiereName() == null ? "" : ins.getMatiereName())
                        .add("typeMatiere", ins.getTypeMatiere() == null ? "" : ins.getTypeMatiere())
                        .add("perissable", ins.getPerissable() ? 1 : 0)
                        .add("region", ins.getRegion() == null ? "" : ins.getRegion());
            } catch (Exception e) {
                Matiere ins = (Matiere) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("matiereName", "")
                        .add("typeMatiere", "")
                        .add("perissable", 0)
                        .add("region", "");
            }
        } else if (obj instanceof Depot) {
            try {
                Depot ins = (Depot) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("nomDepot", ins.getNomDepot() == null ? "" : ins.getNomDepot())
                        .add("dimension", ins.getDimension() == null ? "" : ins.getDimension())
                        .add("typeDepot", ins.getTypeDepot() == null ? "" : ins.getTypeDepot())
                        .add("region", ins.getRegion() == null ? "" : ins.getRegion());
            } catch (Exception e) {
                Depot ins = (Depot) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("nomDepot", "")
                        .add("dimension", "")
                        .add("typeDepot", "")
                        .add("region", "");
            }
        } else if (obj instanceof Immobilisation) {
            try {
                Immobilisation ins = (Immobilisation) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("libelle", ins.getLibelle() == null ? "" : ins.getLibelle())
                        .add("categorie", ins.getCategorie() == null ? "" : ins.getCategorie())
                        .add("region", ins.getRegion() == null ? "" : ins.getRegion())
                        .add("valeurOrigineUsd", ins.getValeurOrigineUsd() == null ? 0 : ins.getValeurOrigineUsd())
                        .add("valeurResiduelleUsd", ins.getValeurResiduelleUsd() == null ? 0 : ins.getValeurResiduelleUsd())
                        .add("dureeAmortissementMois", ins.getDureeAmortissementMois() == null ? 12 : ins.getDureeAmortissementMois())
                        .add("actif", ins.getActif() ? 1 : 0);
            } catch (Exception e) {
                Immobilisation ins = (Immobilisation) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("libelle", "")
                        .add("categorie", "")
                        .add("region", "")
                        .add("valeurOrigineUsd", 0)
                        .add("valeurResiduelleUsd", 0)
                        .add("dureeAmortissementMois", 12)
                        .add("actif", 1);
            }
        } else if (obj instanceof Inventaire) {
            try {
                Inventaire ins = (Inventaire) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("etat", ins.getEtat() == null ? "" : ins.getEtat())
                        .add("comment", ins.getComment() == null ? "" : ins.getComment())
                        .add("codeInventaire", ins.getCodeInventaire() == null ? "" : ins.getCodeInventaire())
                        .add("region", ins.getRegion() == null ? "" : ins.getRegion())
                        .add("valeurTotal", ins.getValeurTotal())
                        .add("valeurTotalEcart", ins.getValeurTotalEcart() == null ? 0 : ins.getValeurTotalEcart());
            } catch (Exception e) {
                Inventaire ins = (Inventaire) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("etat", "")
                        .add("comment", "")
                        .add("codeInventaire", "")
                        .add("region", "")
                        .add("valeurTotal", 0)
                        .add("valeurTotalEcart", 0);
            }
        } else if (obj instanceof Entreposer) {
            try {
                Entreposer ins = (Entreposer) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("date", ins.getDate() == null ? "" : ins.getDate().toString())
                        .add("numlot", ins.getNumlot() == null ? "" : ins.getNumlot())
                        .add("quantite", ins.getQuantite())
                        .add("comment", ins.getComment() == null ? "" : ins.getComment())
                        .add("region", ins.getRegion() == null ? "" : ins.getRegion())
                        .add("qualite", ins.getQualite() == null ? "" : ins.getQualite())
                        .add("cout", ins.getCout())
                        .add("devise", ins.getDevise() == null ? "" : ins.getDevise())
                        .add("niveauFabrication", ins.getNiveauFabrication() == null ? "" : ins.getNiveauFabrication())
                        .add("depotId", Json.createObjectBuilder().add("uid", (ins.getDepotId() == null || ins.getDepotId().getUid() == null) ? "" : ins.getDepotId().getUid()).build())
                        .add("livraisonId", Json.createObjectBuilder().add("uid", (ins.getLivraisonId() == null || ins.getLivraisonId().getUid() == null) ? "" : ins.getLivraisonId().getUid()).build())
                        .add("matiereId", Json.createObjectBuilder().add("uid", (ins.getMatiereId() == null || ins.getMatiereId().getUid() == null) ? "" : ins.getMatiereId().getUid()).build())
                        .add("skuId", Json.createObjectBuilder().add("uid", (ins.getSkuId() == null || ins.getSkuId().getUid() == null) ? "" : ins.getSkuId().getUid()).build())
                        .add("mesureId", Json.createObjectBuilder().add("uid", (ins.getMesureId() == null || ins.getMesureId().getUid() == null) ? "" : ins.getMesureId().getUid()).build())
                        .add("productionId", Json.createObjectBuilder().add("uid", (ins.getProductionId() == null || ins.getProductionId().getUid() == null) ? "" : ins.getProductionId().getUid()).build());
            } catch (Exception e) {
                Entreposer ins = (Entreposer) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("date", "")
                        .add("numlot", "")
                        .add("quantite", 0)
                        .add("comment", "")
                        .add("region", "")
                        .add("qualite", "")
                        .add("cout", 0)
                        .add("devise", "")
                        .add("niveauFabrication", "")
                        .add("depotId", Json.createObjectBuilder().add("uid", "").build())
                        .add("livraisonId", Json.createObjectBuilder().add("uid", "").build())
                        .add("matiereId", Json.createObjectBuilder().add("uid", "").build())
                        .add("skuId", Json.createObjectBuilder().add("uid", "").build())
                        .add("mesureId", Json.createObjectBuilder().add("uid", "").build())
                        .add("productionId", Json.createObjectBuilder().add("uid", "").build());
            }
        } else if (obj instanceof Imputer) {
            try {
                Imputer ins = (Imputer) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("date", ins.getDate() == null ? "" : ins.getDate().toString())
                        .add("montant", ins.getMontant() == null ? 0 : ins.getMontant())
                        .add("devise", ins.getDevise() == null ? "" : ins.getDevise())
                        .add("percent", ins.getPercent() == null ? 0 : ins.getPercent())
                        .add("region", ins.getRegion() == null ? "" : ins.getRegion())
                        .add("operationId", Json.createObjectBuilder().add("uid", (ins.getOperationId() == null || ins.getOperationId().getUid() == null) ? "" : ins.getOperationId().getUid()).build())
                        .add("productionId", Json.createObjectBuilder().add("uid", (ins.getProductionId() == null || ins.getProductionId().getUid() == null) ? "" : ins.getProductionId().getUid()).build());
            } catch (Exception e) {
                Imputer ins = (Imputer) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("date", "")
                        .add("montant", 0)
                        .add("devise", "")
                        .add("percent", 0)
                        .add("region", "")
                        .add("operationId", Json.createObjectBuilder().add("uid", "").build())
                        .add("productionId", Json.createObjectBuilder().add("uid", "").build());
            }
        } else if (obj instanceof Repartir) {
            try {
                Repartir ins = (Repartir) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("coutAchat", ins.getCoutAchat() == null ? 0 : ins.getCoutAchat())
                        .add("devise", ins.getDevise() == null ? "" : ins.getDevise())
                        .add("quantite", ins.getQuantite() == null ? 0 : ins.getQuantite())
                        .add("date", ins.getDate() == null ? "" : ins.getDate().toString())
                        .add("region", ins.getRegion() == null ? "" : ins.getRegion())
                        .add("numlot", ins.getNumlot() == null ? "" : ins.getNumlot())
                        .add("matiereId", Json.createObjectBuilder().add("uid", (ins.getMatiereId() == null || ins.getMatiereId().getUid() == null) ? "" : ins.getMatiereId().getUid()).build())
                        .add("skuId", Json.createObjectBuilder().add("uid", (ins.getSkuId() == null || ins.getSkuId().getUid() == null) ? "" : ins.getSkuId().getUid()).build())
                        .add("productionId", Json.createObjectBuilder().add("uid", (ins.getProductionId() == null || ins.getProductionId().getUid() == null) ? "" : ins.getProductionId().getUid()).build());
            } catch (Exception e) {
                Repartir ins = (Repartir) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("coutAchat", 0)
                        .add("devise", "")
                        .add("quantite", 0)
                        .add("date", "")
                        .add("region", "")
                        .add("numlot", "")
                        .add("matiereId", Json.createObjectBuilder().add("uid", "").build())
                        .add("skuId", Json.createObjectBuilder().add("uid", "").build())
                        .add("productionId", Json.createObjectBuilder().add("uid", "").build());
            }
        } else if (obj instanceof Production) {
            try {
                Production ins = (Production) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("numlot", ins.getNumlot() == null ? "" : ins.getNumlot())
                        .add("etat", ins.getEtat() == null ? "" : ins.getEtat())
                        .add("comment", ins.getComment() == null ? "" : ins.getComment())
                        .add("region", ins.getRegion() == null ? "" : ins.getRegion())
                        .add("quantitePrevu", ins.getQuantitePrevu() == null ? 0 : ins.getQuantitePrevu())
                        .add("qualitePrevu", ins.getQualitePrevu() == null ? "" : ins.getQualitePrevu())
                        .add("mesureId", Json.createObjectBuilder().add("uid", (ins.getMesureId() == null || ins.getMesureId().getUid() == null) ? "" : ins.getMesureId().getUid()).build())
                        .add("produitId", Json.createObjectBuilder().add("uid", (ins.getProduitId() == null || ins.getProduitId().getUid() == null) ? "" : ins.getProduitId().getUid()).build());
            } catch (Exception e) {
                Production ins = (Production) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("numlot", "")
                        .add("etat", "")
                        .add("comment", "")
                        .add("region", "")
                        .add("quantitePrevu", 0)
                        .add("qualitePrevu", "")
                        .add("mesureId", Json.createObjectBuilder().add("uid", "").build())
                        .add("produitId", Json.createObjectBuilder().add("uid", "").build());
            }
        } else if (obj instanceof MatiereSku) {
            try {
                MatiereSku ins = (MatiereSku) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("nomSku", ins.getNomSku() == null ? "" : ins.getNomSku())
                        .add("quantContenuSku", ins.getQuantContenuSku())
                        .add("region", ins.getRegion() == null ? "" : ins.getRegion())
                        .add("matiereId", Json.createObjectBuilder().add("uid", (ins.getMatiere() == null || ins.getMatiere().getUid() == null) ? "" : ins.getMatiere().getUid()).build());
            } catch (Exception e) {
                MatiereSku ins = (MatiereSku) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("nomSku", "")
                        .add("quantContenuSku", 0)
                        .add("region", "")
                        .add("matiereId", Json.createObjectBuilder().add("uid", "").build());
            }
        } else if (obj instanceof Compter) {
            try {
                Compter ins = (Compter) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("region", ins.getRegion() == null ? "" : ins.getRegion())
                        .add("numlot", ins.getNumlot() == null ? "" : ins.getNumlot())
                        .add("quantite", ins.getQuantite())
                        .add("coutAchat", ins.getCoutAchat())
                        .add("quantiteTheorik", ins.getQuantiteTheorik() == null ? 0 : ins.getQuantiteTheorik())
                        .add("ecart", ins.getEcart() == null ? 0 : ins.getEcart())
                        .add("observation", ins.getObservation() == null ? "" : ins.getObservation())
                        .add("inventaireId", Json.createObjectBuilder().add("uid", (ins.getInventaireId() == null || ins.getInventaireId().getUid() == null) ? "" : ins.getInventaireId().getUid()).build())
                        .add("mesureId", Json.createObjectBuilder().add("uid", (ins.getMesureId() == null || ins.getMesureId().getUid() == null) ? "" : ins.getMesureId().getUid()).build())
                        .add("productId", Json.createObjectBuilder().add("uid", (ins.getProductId() == null || ins.getProductId().getUid() == null) ? "" : ins.getProductId().getUid()).build());
            } catch (Exception e) {
                Compter ins = (Compter) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("region", "")
                        .add("numlot", "")
                        .add("quantite", 0)
                        .add("coutAchat", 0)
                        .add("quantiteTheorik", 0)
                        .add("ecart", 0)
                        .add("observation", "")
                        .add("inventaireId", Json.createObjectBuilder().add("uid", "").build())
                        .add("mesureId", Json.createObjectBuilder().add("uid", "").build())
                        .add("productId", Json.createObjectBuilder().add("uid", "").build());
            }
        } else if (obj instanceof Presence) {
            try {
                Presence ins = (Presence) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("agentId", ins.getAgentId() == null ? "" : ins.getAgentId())
                        .add("agentNom", ins.getAgentNom() == null ? "" : ins.getAgentNom())
                        .add("agentPrenom", ins.getAgentPrenom() == null ? "" : ins.getAgentPrenom())
                        .add("timestamp", ins.getTimestamp() == null ? "" : ins.getTimestamp().toString())
                        .add("typePresence", ins.getTypePresence() == null ? "" : ins.getTypePresence())
                        .add("fingerprintHash", ins.getFingerprintHash() == null ? "" : ins.getFingerprintHash())
                        .add("region", ins.getRegion() == null ? "" : ins.getRegion())
                        .add("entreprise", ins.getEntreprise() == null ? "" : ins.getEntreprise());
            } catch (Exception e) {
                Presence ins = (Presence) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("agentId", "")
                        .add("agentNom", "")
                        .add("agentPrenom", "")
                        .add("timestamp", "")
                        .add("typePresence", "")
                        .add("fingerprintHash", "")
                        .add("region", "")
                        .add("entreprise", "");
            }
        } else if (obj instanceof Entreprise) {
            try {
                Entreprise ins = (Entreprise) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("nomEntreprise", ins.getNomEntreprise() == null ? "" : ins.getNomEntreprise())
                        .add("identification", ins.getIdentification() == null ? "" : ins.getIdentification())
                        .add("typeIdentification", ins.getTypeIdentification() == null ? "" : ins.getTypeIdentification())
                        .add("adresse", ins.getAdresse() == null ? "" : ins.getAdresse())
                        .add("website", ins.getWebsite() == null ? "" : ins.getWebsite())
                        .add("email", ins.getEmail() == null ? "" : ins.getEmail())
                        .add("category", ins.getCategory() == null ? "" : ins.getCategory())
                        .add("idNat", ins.getIdNat() == null ? "" : ins.getIdNat())
                        .add("numeroImpot", ins.getNumeroImpot() == null ? "" : ins.getNumeroImpot())
                        .add("phones", ins.getPhones() == null ? "" : ins.getPhones())
                        .add("latitude", ins.getLatitude() == null ? 0 : ins.getLatitude())
                        .add("longitude", ins.getLongitude() == null ? 0 : ins.getLongitude())
                        .add("dateCreation", ins.getDateCreation() == null ? "" : ins.getDateCreation().toString());
            } catch (Exception e) {
                Entreprise ins = (Entreprise) obj;
                builder.add("uid", ins.getUid() == null ? "" : ins.getUid())
                        .add("nomEntreprise", "")
                        .add("identification", "")
                        .add("typeIdentification", "")
                        .add("adresse", "")
                        .add("website", "")
                        .add("email", "")
                        .add("category", "")
                        .add("idNat", "")
                        .add("numeroImpot", "")
                        .add("phones", "")
                        .add("latitude", 0)
                        .add("longitude", 0)
                        .add("dateCreation", "");
            }
        } else if (obj instanceof Employee) {
            try {
                Employee ins = (Employee) obj;
                builder.add("userId", ins.getUserId() == null ? "" : ins.getUserId())
                        .add("engagementId", ins.getEngagementId() == null ? "" : ins.getEngagementId())
                        .add("region", ins.getRegion() == null ? "" : ins.getRegion())
                        .add("poste", ins.getPoste() == null ? "" : ins.getPoste())
                        .add("entreprise", ins.getEntreprise() == null ? "" : ins.getEntreprise())
                        .add("nom", ins.getNom() == null ? "" : ins.getNom())
                        .add("prenom", ins.getPrenom() == null ? "" : ins.getPrenom())
                        .add("phone", ins.getPhone() == null ? "" : ins.getPhone())
                        .add("fingerprintHash", ins.getFingerprintHash() == null ? "" : ins.getFingerprintHash())
                        .add("revoquee", ins.isRevoquee() ? 1 : 0);
            } catch (Exception e) {
                Employee ins = (Employee) obj;
                builder.add("userId", "")
                        .add("engagementId", "")
                        .add("region", "")
                        .add("poste", "")
                        .add("entreprise", "")
                        .add("nom", "")
                        .add("prenom", "")
                        .add("phone", "")
                        .add("fingerprintHash", "")
                        .add("revoquee", 0);
            }
        }

        return builder.build();
    }

    /**
     * Parse de {@code LocalDateTime} null-safe : retourne {@code null} si la
     * propriété est absente, {@code JsonValue.NULL} ou non parsable, au lieu
     * de lever une exception (les payloads downsync peuvent contenir
     * {@code "deletedAt": null}, {@code "updatedAt": null}, etc.).
     */
    private static LocalDateTime safeLocalDateTime(JsonObject json, String name) {
        if (json == null || json.isNull(name)) {
            return null;
        }
        try {
            return LocalDateTime.parse(json.getString(name));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parse de {@code LocalDate} null-safe : retourne {@code null} si la
     * propriété est absente, {@code JsonValue.NULL} ou non parsable.
     */
    private static LocalDate safeLocalDate(JsonObject json, String name) {
        if (json == null || json.isNull(name)) {
            return null;
        }
        try {
            return LocalDate.parse(json.getString(name));
        } catch (Exception e) {
            return null;
        }
    }

    public static Object objectify(String message) {
        JsonReader reader = Json.createReader(new StringReader(message));
        JsonObject json = reader.readObject();
        
        // First, check if there's a "type" field for reliable type detection
        if (json.containsKey("type") && !json.isNull("type")) {
            String type = json.getString("type");
            try {
                Tables table = Tables.valueOf(type);
                ObjectMapper mapper = KazisafeServiceFactory.mapper();
                switch (table) {
                    case CATEGORY -> {
                        return mapper.readValue(message, Category.class);
                    }
                    case PRODUIT -> {
                        return mapper.readValue(message, Produit.class);
                    }
                    case MESURE -> {
                        return mapper.readValue(message, Mesure.class);
                    }
                    case FOURNISSEUR -> {
                        return mapper.readValue(message, Fournisseur.class);
                    }
                    case LIVRAISON -> {
                        return mapper.readValue(message, Livraison.class);
                    }
                    case STOCKER -> {
                        return mapper.readValue(message, Stocker.class);
                    }
                    case DESTOCKER -> {
                        return mapper.readValue(message, Destocker.class);
                    }
                    case RECQUISITION -> {
                        return mapper.readValue(message, Recquisition.class);
                    }
                    case PRIXDEVENTE -> {
                        return mapper.readValue(message, PrixDeVente.class);
                    }
                    case CLIENT -> {
                        return mapper.readValue(message, Client.class);
                    }
                    case VENTE -> {
                        return mapper.readValue(message, Vente.class);
                    }
                    case LIGNEVENTE -> {
                        return mapper.readValue(message, LigneVente.class);
                    }
                    case TAXE -> {
                        return mapper.readValue(message, Taxe.class);
                    }
                    case TAXER -> {
                        return mapper.readValue(message, Taxer.class);
                    }
                    case ARETIRER -> {
                        return mapper.readValue(message, Aretirer.class);
                    }
                    case COMPTETRESOR -> {
                        return mapper.readValue(message, CompteTresor.class);
                    }
                    case TRAISORERIE -> {
                        return mapper.readValue(message, Traisorerie.class);
                    }
                    case DEPENSE -> {
                        return mapper.readValue(message, Depense.class);
                    }
                    case OPERATION -> {
                        return mapper.readValue(message, Operation.class);
                    }
                    case INVENTORY -> {
                        return mapper.readValue(message, Inventaire.class);
                    }
                    case COMPTER -> {
                        return mapper.readValue(message, Compter.class);
                    }
                    case CLIENTAPPARTENIR -> {
                        return mapper.readValue(message, ClientAppartenir.class);
                    }
                    case CLIENTORGANISATION -> {
                        return mapper.readValue(message, ClientOrganisation.class);
                    }
                    case RETOURMAGASIN -> {
                        return mapper.readValue(message, RetourMagasin.class);
                    }
                    case RETOURDEPOT -> {
                        return mapper.readValue(message, RetourDepot.class);
                    }
                    case FACTURE -> {
                        return mapper.readValue(message, Facture.class);
                    }
                    case ABONNEMENT -> {
                        return mapper.readValue(message, Abonnement.class);
                    }
                    case BULKMODEL -> {
                        return mapper.readValue(message, BulkModel.class);
                    }
                    case PERIODE -> {
                        return mapper.readValue(message, Periode.class);
                    }
                    case DEPOT -> {
                        return mapper.readValue(message, Depot.class);
                    }
                    case COMMANDE -> {
                        return mapper.readValue(message, Commande.class);
                    }
                    case COMMANDELIST -> {
                        return mapper.readValue(message, CommandeLister.class);
                    }
                    case MATIERE -> {
                        return mapper.readValue(message, Matiere.class);
                    }
                    case MATIERESKU -> {
                        return mapper.readValue(message, MatiereSku.class);
                    }
                    case PRODUCTION -> {
                        return mapper.readValue(message, Production.class);
                    }
                    case REPARTIR -> {
                        return mapper.readValue(message, Repartir.class);
                    }
                    case IMPUTER -> {
                        return mapper.readValue(message, Imputer.class);
                    }
                    case ENTREPOSER -> {
                        return mapper.readValue(message, Entreposer.class);
                    }
                    case SATISFAIRE -> {
                        return mapper.readValue(message, Satisfaire.class);
                    }
                    case REFRESH -> {
                        return mapper.readValue(message, Refresher.class);
                    }
                    case PERMISSION -> {
                        return mapper.readValue(message, Permission.class);
                    }
                    case IMMOBILISATION -> {
                        return mapper.readValue(message, Immobilisation.class);
                    }
                    case PRESENCE -> {
                        return mapper.readValue(message, Presence.class);
                    }
                    case FINGERPRINTMAPPING -> {
                        return mapper.readValue(message, FingerprintMapping.class);
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(JsonUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        // Fall back to field-based detection if no type field or deserialization failed
        if (json.containsKey("descritption")) {
            Category cat = new Category(json.getString("uid"));
            cat.setDescritption(json.getString("descritption"));
            return cat;
        } else if (json.containsKey("codebar")) {
            Produit p = new Produit(json.getString("uid"));
            p.setCodebar(json.getString("codebar"));
            p.setNomProduit(json.getString("nomProduit"));
            p.setMarque(json.getString("marque"));
            p.setModele(json.getString("modele"));
            p.setCouleur(json.getString("couleur"));
            p.setCodebar(json.getString("codebar"));
            p.setTaille(json.getString("taille"));
            p.setMethodeInventaire(json.getString("methodeInventaire"));
            p.setCategoryId(new Category(json.getJsonObject("categoryId").getString("uid")));
            return p;
        } else if (json.containsKey("quantContenu") && !json.containsKey("mesureId")) {
            Mesure m = new Mesure(json.getString("uid"));
            m.setDescription(json.getString("description"));
            m.setQuantContenu(json.getJsonNumber("quantContenu").doubleValue());
            JsonObject jso = json.getJsonObject("produitId");
            Produit p = new Produit(jso.getString("uid"));
            m.setProduitId(p);
            return m;
        } else if (json.containsKey("nomFourn") && !json.containsKey("fournId")) {
            Fournisseur ins = new Fournisseur(json.getString("uid"));
            ins.setAdresse(json.getString("adresse"));
            ins.setIdentification(json.getString("identification"));
            ins.setNomFourn(json.getString("nomFourn"));
            ins.setPhone(json.getString("phone"));
            return ins;
        } else if (json.containsKey("dateLivr") && !json.containsKey("livraisId")) {
            Livraison ins = new Livraison(json.getString("uid"));
            ins.setLibelle(json.containsKey("libelle") ? json.getString("libelle") : "");
            ins.setNumPiece(json.getString("numPiece"));
            ins.setObservation(json.containsKey("observation") ? json.getString("observation") : "");
            ins.setPayed(json.getJsonNumber("payed").doubleValue());
            ins.setReduction(json.getJsonNumber("reduction").doubleValue());
            ins.setReference(json.getString("reference"));
            ins.setRegion(json.getString("region"));
            ins.setRemained(json.getJsonNumber("remained").doubleValue());
            ins.setTopay(json.getJsonNumber("topay").doubleValue());
            ins.setToreceive(json.getJsonNumber("toreceive").doubleValue());
            ins.setDateLivr(safeLocalDate(json, "dateLivr"));
            JsonObject jso = json.getJsonObject("fournId");
            Fournisseur fssr = new Fournisseur(jso.getString("uid"));
            ins.setFournId(fssr);
            return ins;
        } else if (json.containsKey("dateStocker")) {
            Stocker ins = new Stocker(json.getString("uid"));
            ins.setLibelle(json.containsKey("libelle") ? json.getString("libelle") : "");
            ins.setRegion(json.getString("region"));
            ins.setObservation(json.containsKey("observation") ? json.getString("observation") : "");
            ins.setNumlot(json.getString("numlot"));
            ins.setLocalisation(json.getString("localisation"));
            ins.setCoutAchat(json.getJsonNumber("coutAchat").doubleValue());
            ins.setReduction(json.getJsonNumber("reduction").doubleValue());
            ins.setPrixAchatTotal(json.getJsonNumber("prixAchatTotal").doubleValue());
            ins.setQuantite(json.getJsonNumber("quantite").doubleValue());
            ins.setStockAlerte(json.getJsonNumber("stockAlerte").doubleValue());
            ins.setDateExpir(safeLocalDate(json, "dateExpir"));
            ins.setDateStocker(safeLocalDateTime(json, "dateStocker"));
            JsonObject jso1 = json.getJsonObject("livraisId");
            Livraison livr = new Livraison(jso1.getString("uid"));
            ins.setLivraisId(livr);
            JsonObject jso2 = json.getJsonObject("mesureId");
            Mesure m = new Mesure(jso2.getString("uid"));
            ins.setMesureId(m);
            JsonObject jso3 = json.getJsonObject("productId");
            Produit pro = new Produit(jso3.getString("uid"));
            ins.setProductId(pro);
            return ins;
        } else if (json.containsKey("dateDestockage")) {
            Destocker ins = new Destocker();
            ins.setUid(json.getString("uid"));
            ins.setLibelle(json.containsKey("libelle") ? json.getString("libelle") : "");
            ins.setCoutAchat(json.getJsonNumber("coutAchat").doubleValue());
            ins.setObservation(json.containsKey("observation") ? json.getString("observation") : "");
            ins.setNumlot(json.getString("numlot"));
            ins.setReference(json.getString("reference"));
            ins.setRegion(json.getString("region"));
            ins.setQuantite(json.getJsonNumber("quantite").doubleValue());
            ins.setDestination(json.getString("destination"));
            ins.setDateDestockage(safeLocalDateTime(json, "dateDestockage"));
            JsonObject jso1 = json.getJsonObject("mesureId");
            Mesure mz = new Mesure();
            mz.setUid(jso1.getString("uid"));
            ins.setMesureId(mz);
            JsonObject jso3 = json.getJsonObject("productId");
            Produit pro = new Produit(jso3.getString("uid"));
            ins.setProductId(pro);
            return ins;
        } else if (json.containsKey("stockAlert")) {
            Recquisition ins = new Recquisition(json.getString("uid"));
            ins.setReference(json.getString("reference"));
            ins.setObservation(json.containsKey("observation") ? json.getString("observation") : "");
            ins.setNumlot(json.getString("numlot"));
            ins.setRegion(json.getString("region"));
            ins.setQuantite(json.getJsonNumber("quantite").doubleValue());
            ins.setCoutAchat(json.getJsonNumber("coutAchat").doubleValue());
            ins.setStockAlert(json.getJsonNumber("stockAlert").doubleValue());
            ins.setDateExpiry(safeLocalDate(json, "dateExpiry"));
            ins.setDate(safeLocalDateTime(json, "date"));
            JsonObject jso1 = json.getJsonObject("mesureId");
            Mesure mz = new Mesure();
            mz.setUid(jso1.getString("uid"));
            ins.setMesureId(mz);
            JsonObject jso3 = json.getJsonObject("productId");
            Produit pro = new Produit(jso3.getString("uid"));
            ins.setProductId(pro);
            return ins;
        } else if (json.containsKey("qmax") && json.containsKey("prixUnitaire")) {
            PrixDeVente ins = new PrixDeVente(json.getString("uid"));
            ins.setQmax(json.getJsonNumber("qmax").doubleValue());
            ins.setQmin(json.getJsonNumber("qmin").doubleValue());
            ins.setDevise(json.getString("devise"));
            ins.setPrixUnitaire(json.getJsonNumber("prixUnitaire").doubleValue());
            ins.setPourcentParCunit(json.containsKey("pourcentParCunit") ? json.getJsonNumber("pourcentParCunit").doubleValue() : 0);
            JsonObject jso1 = json.getJsonObject("mesureId");
            Mesure mz = new Mesure(jso1.getString("uid"));
            ins.setMesureId(mz);
            JsonObject jso2 = json.getJsonObject("recquisitionId");
            Recquisition req = new Recquisition(jso2.getString("uid"));
            ins.setRecquisitionId(req);
            return ins;
        } else if (json.containsKey("typeClient") && !json.containsKey("clientId")) {
            Client ins = new Client();
            ins.setUid(json.getString("uid"));
            ins.setAdresse(json.getString("adresse"));
            ins.setEmail(json.getString("email"));
            ins.setTypeClient(json.getString("typeClient"));
            ins.setNomClient(json.getString("nomClient"));
            JsonObject oo = json.getJsonObject("parentId");
            if (oo != null) {
                ins.setParentId(new Client(oo.getString("uid")));
            } else {
                ins.setParentId(ins);
            }
            ins.setPhone(json.getString("phone"));
            return ins;
        } else if (json.containsKey("dateVente")) {
            Vente ins = new Vente();
            ins.setUid(json.getJsonNumber("uid").intValue());
            ins.setLibelle(json.getString("libelle"));
            ins.setLatitude(json.getJsonNumber("latitude").doubleValue());
            ins.setObservation(json.getString("observation"));
            ins.setLongitude(json.getJsonNumber("longitude").doubleValue());
            ins.setMontantCdf(json.getJsonNumber("montantCdf").doubleValue());
            ins.setMontantDette(json.getJsonNumber("montantDette").doubleValue());
            ins.setRegion(json.getString("region"));
            ins.setMontantUsd(json.getJsonNumber("montantUsd").doubleValue());
            ins.setPayment(json.getString("payment"));
            ins.setReference(json.getString("reference"));
            ins.setEcheance(safeLocalDate(json, "echeance"));
            ins.setDateVente(safeLocalDateTime(json, "dateVente"));
            ins.setDeviseDette(json.getString("deviseDette"));
            JsonObject jso = json.getJsonObject("clientId");
            Client clt = new Client();
            clt.setUid(jso.getString("uid"));
            clt.setAdresse(jso.getString("adresse"));
            clt.setEmail(jso.getString("email"));
            clt.setTypeClient(jso.getString("typeClient"));
            clt.setNomClient(jso.getString("nomClient"));
            clt.setPhone(jso.getString("phone"));
            JsonObject oo = jso.getJsonObject("parentId");
            if (oo != null) {
                clt.setParentId(new Client(oo.getString("uid")));
            } else {
                clt.setParentId(clt);
            }
            ins.setClientId(clt);
            return ins;
        } else if (json.containsKey("prixUnit")) {
            LigneVente ins = new LigneVente();
            ins.setUid(json.getJsonNumber("uid").longValue());
            ins.setClientId(json.getString("clientId"));
            ins.setNumlot(json.getString("numlot"));
            ins.setPrixUnit(json.getJsonNumber("prixUnit").doubleValue());
            ins.setQuantite(json.getJsonNumber("quantite").doubleValue());
            ins.setMontantCdf(json.getJsonNumber("montantCdf").doubleValue());
            ins.setMontantUsd(json.getJsonNumber("montantUsd").doubleValue());
            ins.setCoutAchat(json.containsKey("coutAchat") ? json.getJsonNumber("coutAchat").doubleValue() : 0);
            JsonObject jso3 = json.getJsonObject("productId");
            Produit pro = new Produit(jso3.getString("uid"));
            ins.setProductId(pro);
            JsonObject jso = json.getJsonObject("mesureId");
            Mesure m = new Mesure();
            m.setUid(jso.getString("uid"));
            ins.setMesureId(m);
            JsonObject jso1 = json.getJsonObject("reference");
            Vente v = new Vente(jso1.getJsonNumber("uid").intValue());
            ins.setReference(v);
            return ins;
        } else if (json.containsKey("typeTresorerie") && !json.containsKey("caisseOpId")) {
            Traisorerie ins = new Traisorerie();
            ins.setUid(json.getString("uid"));
            ins.setLibelle(json.getString("libelle"));
            ins.setMouvement(json.getString("mouvement"));
            ins.setTypeTresorerie(json.getString("typeTresorerie"));
            ins.setMontantCdf(json.getJsonNumber("montantCdf").doubleValue());
            ins.setRegion(json.getString("region"));
            if (json.containsKey("tresorId")) {
                ins.setTresorId(new CompteTresor(json.getJsonObject("tresorId").getString("uid")));
            }
            ins.setMontantUsd(json.getJsonNumber("montantUsd").doubleValue());
            ins.setReference(json.getString("reference"));
            ins.setDate(safeLocalDateTime(json, "date"));
            return ins;
        } else if (json.containsKey("imputation")) {
            Operation ins = new Operation();
            ins.setUid(json.getString("uid"));
            ins.setLibelle(json.getString("libelle"));
            ins.setMouvement(json.getString("mouvement"));
            ins.setImputation(json.getString("imputation"));
            ins.setMontantCdf(json.getJsonNumber("montantCdf").doubleValue());
            ins.setRegion(json.getString("region"));
            ins.setMontantUsd(json.getJsonNumber("montantUsd").doubleValue());
            ins.setReferenceOp(json.getString("referenceOp"));
            if (json.containsKey("tresorId")) {
                ins.setTresorId(new CompteTresor(json.getJsonObject("tresorId").getString("uid")));
            }
            if (json.containsKey("depenseId")) {
                ins.setDepenseId(new Depense(json.getJsonObject("depenseId").getString("uid")));
            }
            ins.setDate(safeLocalDateTime(json, "date"));
            JsonObject jso = json.getJsonObject("caisseOpId");
            Traisorerie t = new Traisorerie();
            t.setUid(jso.getString("uid"));
            t.setLibelle(jso.getString("libelle"));
            t.setMouvement(jso.getString("mouvement"));
            t.setTypeTresorerie(jso.getString("typeTresorerie"));
            t.setMontantCdf(jso.getJsonNumber("montantCdf").doubleValue());
            t.setRegion(jso.getString("region"));
            t.setMontantUsd(jso.getJsonNumber("montantUsd").doubleValue());
            t.setReference(jso.getString("reference"));
            t.setDate(safeLocalDateTime(jso, "date"));
            ins.setCaisseOpId(t);
            return ins;
        } else if (json.containsKey("imageBase64")) {
            ImageProduit image = new ImageProduit();
            image.setIdProduit(json.getString("idProduit"));
            image.setImageBase64(json.getString("imageBase64"));
            return image;
        } else if (json.containsKey("status")) {
            Aretirer oper = new Aretirer();
            oper.setUid(json.getString("uid"));
            oper.setNumlot(json.getString("numlot"));
            oper.setPrixVente(json.getJsonNumber("prixVente").doubleValue());
            oper.setQuantite(json.getJsonNumber("quantite").doubleValue());
            oper.setReferenceVente(json.getString("referenceVente"));
            oper.setRegion(json.getString("region"));
            oper.setStatus(json.getString("status"));
            oper.setDate(safeLocalDateTime(json, "date"));
            JsonObject jsoc = json.getJsonObject("clientId");
            Client clt = new Client(jsoc.getString("uid"));
            oper.setClientId(clt);
            JsonObject jsol = json.getJsonObject("ligneVenteId");
            oper.setLigneVenteId(new LigneVente(jsol.getJsonNumber("uid").longValue()));
            JsonObject jsom = json.getJsonObject("mesureId");
            oper.setMesureId(new Mesure(jsom.getString("uid")));
            return oper;
        } else if (json.containsKey("clientOrganisationId")) {
            ClientAppartenir oper = new ClientAppartenir();
            oper.setUid(json.getString("uid"));
            oper.setRegion(json.getString("region"));

            oper.setDateAppartenir(safeLocalDate(json, "date"));

            JsonObject jsoc = json.getJsonObject("clientId");
            Client clt = new Client(jsoc.getString("uid"));
            oper.setClientId(clt);
            JsonObject jsoo = json.getJsonObject("clientOrganisationId");
            ClientOrganisation clto = new ClientOrganisation(jsoo.getString("uid"));
            oper.setClientOrganisationId(clto);
            return oper;
        } else if (json.containsKey("boitePostalOrganisation")) {
            ClientOrganisation oper = new ClientOrganisation();
            oper.setUid(json.getString("uid"));
            oper.setRegion(json.getString("region"));
            oper.setAdresse(json.getString("adresse"));
            oper.setBoitePostalOrganisation(json.getString("boitePostalOrganisation"));
            oper.setDomaineOrganisation(json.getString("domaineOrganisation"));
            oper.setEmailOrganisation(json.getString("emailOrganisation"));
            oper.setNomOrganisation(json.getString("nomOrganisation"));
            oper.setPhoneOrganisation(json.getString("phoneOrganisation"));
            oper.setRccmOrganisation(json.getString("rccmOrganisation"));
            oper.setWebsiteOrganisation(json.getString("websiteOrganisation"));
            return oper;
        } else if (json.containsKey("regionProv")) {
            RetourDepot oper = new RetourDepot();
            oper.setUid(json.getString("uid"));
            oper.setRegion(json.getString("region"));
            oper.setCoutAchat(json.getJsonNumber("coutAchat").doubleValue());
            oper.setLocalisation(json.getString("localisation"));
            oper.setMotif(json.getString("motif"));
            oper.setNumlot(json.getString("numlot"));
            oper.setQuantite(json.getJsonNumber("quantite").doubleValue());
            oper.setRegionDest(json.getString("regionDest"));
            oper.setRegionProv(json.getString("regionProv"));
            oper.setDate(safeLocalDateTime(json, "date"));
            JsonObject jo = json.getJsonObject("destockerId");
            oper.setDestockerId(new Destocker(jo.getString("uid")));
            JsonObject job = json.getJsonObject("recquisitionId");
            oper.setRecquisitionId(new Recquisition(job.getString("uid")));
            JsonObject jso = json.getJsonObject("mesureId");
            oper.setMesureId(new Mesure(jso.getString("uid")));
            return oper;
        } else if (json.containsKey("referenceVente") && !json.containsKey("status")) {
            RetourMagasin oper = new RetourMagasin();
            oper.setUid(json.getString("uid"));
            oper.setRegion(json.getString("region"));
            oper.setPrixVente(json.getJsonNumber("prixVente").doubleValue());
            oper.setReferenceVente(json.getString("referenceVente"));
            oper.setMotif(json.getString("motif"));
            oper.setQuantite(json.getJsonNumber("quantite").doubleValue());
            oper.setDate(safeLocalDateTime(json, "date"));
            JsonObject jo = json.getJsonObject("ligneVenteId");
            oper.setLigneVenteId(new LigneVente(jo.getJsonNumber("uid").longValue()));
            JsonObject job = json.getJsonObject("recquisitionId");
            oper.setClientId(new Client(job.getString("clientId")));
            JsonObject jso = json.getJsonObject("mesureId");
            oper.setMesureId(new Mesure(jso.getString("uid")));
            return oper;
        } else if (json.containsKey("typeAbonnemnt") || json.containsKey("typeAbonnement")) {
            Abonnement ab = new Abonnement();
            ab.setUid(json.getString("uid"));

            ab.setDateAbonnement(safeLocalDateTime(json, "dateAbonnement"));

            ab.setDevise(json.getString("devise"));
            ab.setEtat(json.getString("etat"));
            ab.setMontant(json.getJsonNumber("montant").doubleValue());
            ab.setNombreOperation(json.getJsonNumber("nombreOperation").doubleValue());
            ab.setTypeAbonnement(json.getString("typeAbonnement"));
            return ab;
        } else if (json.containsKey("startDate")) {
            Facture f = new Facture();
            f.setUid(json.getString("uid"));

            f.setStartDate(safeLocalDate(json, "startDate"));
            f.setEndDate(safeLocalDate(json, "endDate"));

            f.setNumero(json.getString("numero"));
            f.setOrganisId(new ClientOrganisation(json.getJsonObject("organisId").getString("uid")));
            f.setPayedamount(json.getJsonNumber("payedamount").doubleValue());
            f.setRegion(json.getString("region"));
            f.setStatus(json.getString("status"));
            f.setTotalamount(json.getJsonNumber("totalamount").doubleValue());
            return f;
        } else if (json.containsKey("nomDepense")) {
            Depense bill = new Depense(json.getString("uid"));
            bill.setNomDepense(json.getString("nomDepense"));
            bill.setRegion(json.getString("region"));
            return bill;
        } else if (json.containsKey("bankName")) {
            CompteTresor bill = new CompteTresor(json.getString("uid"));
            bill.setBankName(json.getString("bankName"));
            bill.setIntitule(json.getString("intitule"));
            bill.setNumeroCompte(json.getString("numeroCompte"));
            bill.setRegion(json.getString("region"));
            bill.setSoldeMinimum(json.getJsonNumber("soldeMinimum").doubleValue());
            bill.setTypeCompte(json.getString("typeCompte"));
            return bill;
        }

        return null;
    }

    public static BaseModel toBaseModelObject(String s) {
        System.out.println("incoming "+s);
        String type = readType(s);
        System.out.println("Type result : "+type);
        try {
            if (type != null) {
                Tables table = Tables.valueOf(type);
                ObjectMapper mapper = KazisafeServiceFactory.mapper();
                switch (table) {
                    case CATEGORY -> {
                        Category rstc = mapper.readValue(s, Category.class);
                        System.out.println("Category recu et bien deserialized "+rstc.getDescritption());
                        return rstc;
                    }
                    case PRODUIT -> {
                        Produit p = mapper.readValue(s, Produit.class);
                        System.out.println("Deserialization de produit reussi : "+p.getNomProduit());
                        return p;
                    }
                    case MESURE -> {
                        Mesure m = mapper.readValue(s, Mesure.class);
                        return m;
                    }
                    case FOURNISSEUR -> {
                        Fournisseur ins = mapper.readValue(s, Fournisseur.class);
                        return ins;
                    }
                    case LIVRAISON -> {
                        Livraison livrz = mapper.readValue(s,Livraison.class);
                        return livrz;
                    }
                    case STOCKER -> {
                        Stocker stok = mapper.readValue(s, Stocker.class);
                        return stok;
                    }
                    case DESTOCKER -> {
                        Destocker destok = mapper.readValue(s, Destocker.class);
                        return destok;
                    }
                    case RECQUISITION -> {
                        Recquisition recq = mapper.readValue(s, Recquisition.class);
                        return recq;
                    }
                    case PRIXDEVENTE -> {
                        PrixDeVente pxv = mapper.readValue(s, PrixDeVente.class);
                        return pxv;
                    }
                    case CLIENT -> {
                        Client client = mapper.readValue(s, Client.class);
                        return client;
                    }
                    case VENTE -> {
                        Vente vente = mapper.readValue(s, Vente.class);
                        return vente;
                    }
                    case LIGNEVENTE -> {
                        LigneVente lignv = mapper.readValue(s, LigneVente.class);
                        return lignv;
                    }
                    case TAXE -> {
                        return mapper.readValue(s, Taxe.class);
                    }
                    case TAXER -> {
                        return mapper.readValue(s, Taxer.class);
                    }
                    case ARETIRER -> {
                        Aretirer aretir = mapper.readValue(s, Aretirer.class);
                        return aretir;
                    }
                    case COMPTETRESOR -> {
                        CompteTresor bill = mapper.readValue(s, CompteTresor.class);
                        return bill;
                    }
                    case TRAISORERIE -> {
                        Traisorerie tres = mapper.readValue(s,Traisorerie.class);
                        return tres;
                    }
                    case DEPENSE -> {
                        Depense dep = mapper.readValue(s, Depense.class);
                        return dep;
                    }
                    case OPERATION -> {
                        Operation operation =mapper.readValue(s,Operation.class);
                        return operation;
                    }
                    case INVENTORY -> {
                        Inventaire inventaire =mapper.readValue(s,Inventaire.class);
                        System.out.println("Inventaire from jsonUtil "+inventaire);
                        return inventaire;
                    }
                    case COMPTER -> {
                        Compter compter =mapper.readValue(s,Compter.class);
                        return compter;
                    }
                    case CLIENTAPPARTENIR -> {
                        ClientAppartenir clientAppartenir = mapper.readValue(s, ClientAppartenir.class);
                        return clientAppartenir;
                    }
                    case CLIENTORGANISATION -> {
                        ClientOrganisation clientOrganisation = mapper.readValue(s, ClientOrganisation.class);
                        return clientOrganisation;
                    }
                    case RETOURMAGASIN -> {
                        RetourMagasin retourMagasin = mapper.readValue(s, RetourMagasin.class);
                        return retourMagasin;
                    }
                    case RETOURDEPOT -> {
                        RetourDepot retourDepot = mapper.readValue(s, RetourDepot.class);
                        return retourDepot;
                    }
                    case FACTURE -> {
                        Facture facture = mapper.readValue(s, Facture.class);
                        return facture;
                    }
                    case ABONNEMENT -> {
                        return mapper.readValue(s, Abonnement.class);
                    }
                    case BULKMODEL -> {
                        return mapper.readValue(s, BulkModel.class);
                    }
                    case PERIODE -> {
                        return mapper.readValue(s, Periode.class);
                    }
                    case DEPOT -> {
                        return mapper.readValue(s, Depot.class);
                    }
                    case MATIERE -> {
                        return mapper.readValue(s, Matiere.class);
                    }
                    case MATIERESKU -> {
                        return mapper.readValue(s, MatiereSku.class);
                    }
                    case PRODUCTION -> {
                        return mapper.readValue(s, Production.class);
                    }
                    case REPARTIR -> {
                        return mapper.readValue(s, Repartir.class);
                    }
                    case IMPUTER -> {
                        return mapper.readValue(s, Imputer.class);
                    }
                    case ENTREPOSER -> {
                        return mapper.readValue(s, Entreposer.class);
                    }
                    case REFRESH -> {
                        return mapper.readValue(s, Refresher.class);
                    }
                    case PERMISSION -> {
                        return mapper.readValue(s, Permission.class);
                    }
                    case IMMOBILISATION -> {
                        return mapper.readValue(s, Immobilisation.class);
                    }
                    case PRESENCE -> {
                        Presence presence = mapper.readValue(s, Presence.class);
                        return presence;
                    }
                    case FINGERPRINTMAPPING -> {
                        return mapper.readValue(s, FingerprintMapping.class);
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(JsonUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private static boolean isObject(String obj) {
        StringReader sreader = new StringReader(obj);
        JsonParser parser = Json.createParser(sreader);
        if (parser.hasNext()) {
            JsonParser.Event evt = parser.next();
            if (evt == JsonParser.Event.START_OBJECT) {
                return true;
            }
        }
        return false;
    }

    public static String readType(String message) {
        if (isObject(message)) {
            JsonReader reader = Json.createReader(new StringReader(message));
            JsonObject json = reader.readObject();
            if (json.containsKey("type")) {
                return json.getString("type");
            }
        }
        return null;
    }
   
}
