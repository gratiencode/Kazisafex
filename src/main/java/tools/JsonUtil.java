/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import utilities.ImageProduit;
import java.io.StringReader;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
//import javax.json.Json;
//import javax.json.JsonObject;
//import javax.json.JsonObjectBuilder;
//import javax.json.JsonReader;
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
import java.time.LocalDate;

/**
 *
 * @author eroot
 */
public class JsonUtil {

    public static JsonObject jsonify(Object obj) {

        JsonObjectBuilder builder = Json.createObjectBuilder();
        if (obj instanceof Category) {
            Category category = (Category) obj;
            builder.add("uid", category.getUid())
                    .add("descritption", category.getDescritption());
            System.err.println("JSONFYING CATEGORY");
        } else if (obj instanceof Produit) {
            Produit p = (Produit) obj;
            if (p.getCodebar() == null) {
                return null;
            }
            builder.add("uid", p.getUid())
                    .add("nomProduit", p.getNomProduit() == null ? "" : p.getNomProduit())
                    .add("marque", p.getMarque() == null ? "" : p.getMarque())
                    .add("modele", p.getModele() == null ? "" : p.getModele())
                    .add("couleur", "")
                    .add("codebar", p.getCodebar() == null ? "" : p.getCodebar())
                    .add("taille", "")
                    .add("categoryId", Json.createObjectBuilder()
                            .add("uid", p.getCategoryId().getUid()).build());
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
        } else if (obj instanceof Mesure) {
            Mesure ins = (Mesure) obj;
            builder.add("uid", ins.getUid())
                    .add("description", ins.getDescription())
                    .add("quantContenu", ins.getQuantContenu())
                    .add("produitId", Json.createObjectBuilder()
                            .add("uid", ins.getProduitId().getUid()).build());

        } else if (obj instanceof Fournisseur) {
            Fournisseur ins = (Fournisseur) obj;
            builder.add("uid", ins.getUid())
                    .add("adresse", ins.getAdresse() == null ? "-" : ins.getAdresse())
                    .add("identification", ins.getIdentification() == null ? "-" : ins.getIdentification())
                    .add("nomFourn", ins.getNomFourn() == null ? "-" : ins.getNomFourn());
            if (ins.getPhone() == null) {
                builder.add("phone", "Non disponible");
            } else {
                builder.add("phone", ins.getPhone());
            }
        } else if (obj instanceof Livraison) {
            Livraison ins = (Livraison) obj;
            builder.add("uid", ins.getUid())
                    .add("numPiece", ins.getNumPiece() == null ? "" : ins.getNumPiece())
                    .add("observation", ins.getObservation() == null ? "" : ins.getObservation())
                    .add("payed", ins.getPayed() == null ? 0 : ins.getPayed())
                    .add("reduction", ins.getReduction() == null ? 0 : ins.getReduction())
                    .add("reference", ins.getReference() == null ? "-" : ins.getReference())
                    .add("region", ins.getRegion() == null ? "-" : ins.getRegion())
                    .add("remained", ins.getRemained() == null ? 0 : ins.getRemained())
                    .add("topay", ins.getTopay() == null ? 0 : ins.getTopay())
                    .add("toreceive", ins.getToreceive() == null ? 0 : ins.getToreceive())
                    .add("dateLivr", ins.getDateLivr() == null ? "-"
                            : Constants.dateFormater.format(ins.getDateLivr()))
                    .add("fournId", Json.createObjectBuilder()
                            .add("uid", ins.getFournId() == null ? "-" : ins.getFournId().getUid()).build());
            if (ins.getLibelle() != null) {
                builder.add("libelle", ins.getLibelle());
            } else {
                builder.add("libelle", "-");
            }
        } else if (obj instanceof Stocker) {
            Stocker ins = (Stocker) obj;
            if (ins.getProductId() != null) {
                builder.add("uid", ins.getUid())
                        .add("libelle", ins.getLibelle() == null ? "" : ins.getLibelle())
                        .add("coutAchat", ins.getCoutAchat())
                        .add("observation", ins.getObservation() == null ? "" : ins.getObservation())
                        .add("numlot", ins.getNumlot() == null ? Constants.TIMESTAMPED_FORMAT.format(ins.getDateStocker()) : ins.getNumlot())
                        .add("reduction", ins.getReduction())
                        .add("prixAchatTotal", ins.getPrixAchatTotal())
                        .add("region", ins.getRegion())
                        .add("quantite", ins.getQuantite())
                        .add("localisation", ins.getLocalisation() == null ? "" : ins.getLocalisation())
                        .add("stockAlerte", ins.getStockAlerte())
                        .add("dateStocker", Constants.Datetime.format(ins.getDateStocker()))
                        .add("livraisId", Json.createObjectBuilder()
                                .add("uid", ins.getLivraisId().getUid()).build())
                        .add("mesureId", Json.createObjectBuilder()
                                .add("uid", ins.getMesureId().getUid()).build())
                        .add("productId", Json.createObjectBuilder()
                                .add("uid", ins.getProductId().getUid()).build());
                if (ins.getDateExpir() != null) {
                    builder.add("dateExpir", Constants.dateFormater.format(ins.getDateExpir()));
                }
            }
        } else if (obj instanceof Destocker) {
            Destocker ins = (Destocker) obj;
            builder.add("uid", ins.getUid())
                    .add("libelle", ins.getLibelle() == null ? "" : ins.getLibelle())
                    .add("coutAchat", ins.getCoutAchat())
                    .add("observation", ins.getObservation() == null ? "" : ins.getObservation())
                    .add("numlot", ins.getNumlot() == null ? Constants.TIMESTAMPED_FORMAT.format(ins.getDateDestockage()) : ins.getNumlot())
                    .add("reference", ins.getReference())
                    .add("region", ins.getRegion())
                    .add("quantite", ins.getQuantite())
                    .add("destination", ins.getDestination())
                    .add("dateDestockage", Constants.Datetime.format(ins.getDateDestockage()))
                    .add("mesureId", Json.createObjectBuilder()
                            .add("uid", ins.getMesureId().getUid()).build())
                    .add("productId", Json.createObjectBuilder()
                            .add("uid", ins.getProductId().getUid()).build());

        } else if (obj instanceof Recquisition) {

            Recquisition ins = (Recquisition) obj;
            builder.add("uid", ins.getUid())
                    .add("reference", ins.getReference())
                    .add("coutAchat", ins.getCoutAchat())
                    .add("observation", ins.getObservation() == null ? "" : ins.getObservation())
                    .add("numlot", ins.getNumlot() == null ? Constants.TIMESTAMPED_FORMAT.format(ins.getDate()) : ins.getNumlot())
                    .add("region", ins.getRegion())
                    .add("quantite", ins.getQuantite())
                    .add("stockAlert", ins.getStockAlert() == null ? 0 : ins.getStockAlert())
                    .add("date", Constants.Datetime.format(ins.getDate()))
                    .add("mesureId", Json.createObjectBuilder()
                            .add("uid", ins.getMesureId().getUid()).build())
                    .add("productId", Json.createObjectBuilder()
                            .add("uid", ins.getProductId().getUid()).build());
            if (ins.getDateExpiry() != null) {
                builder.add("dateExpiry", Constants.dateFormater.format(ins.getDateExpiry()));
            }
        } else if (obj instanceof PrixDeVente) {
            PrixDeVente ins = (PrixDeVente) obj;
            builder.add("uid", ins.getUid())
                    .add("qmax", ins.getQmax())
                    .add("qmin", ins.getQmin())
                    .add("devise", ins.getDevise())
                    .add("prixUnitaire", ins.getPrixUnitaire());
            builder.add("mesureId", Json.createObjectBuilder()
                    .add("uid", ins.getMesureId().getUid()));
            builder.add("recquisitionId", Json.createObjectBuilder()
                    .add("uid", ins.getRecquisitionId().getUid()).build());
        } else if (obj instanceof Client) {
            Client ins = (Client) obj;
            builder.add("uid", ins.getUid())
                    .add("adresse", ins.getAdresse())
                    .add("email", ins.getEmail())
                    .add("typeClient", ins.getTypeClient())
                    .add("nomClient", ins.getNomClient())
                    .add("phone", ins.getPhone());
            if (ins.getParentId() != null) {
                builder.add("parentId", Json.createObjectBuilder()
                        .add("uid", ins.getParentId().getUid()).build());
            } else {
                builder.add("parentId", Json.createObjectBuilder()
                        .add("uid", ins.getUid()).build());
            }
        } else if (obj instanceof Vente) {
            Vente ins = (Vente) obj;
            System.out.println(" TYPE ENTRANT " + ins.getClass().getName());
            builder.add("uid", ins.getUid())
                    .add("libelle", ins.getLibelle() == null ? "" : ins.getLibelle())
                    .add("latitude", ins.getLatitude())
                    .add("observation", ins.getObservation() == null ? "" : ins.getObservation())
                    .add("longitude", ins.getLongitude())
                    .add("montantCdf", ins.getMontantCdf())
                    .add("montantDette", ins.getMontantDette() == null ? 0 : ins.getMontantDette())
                    .add("region", ins.getRegion())
                    .add("montantUsd", ins.getMontantUsd())
                    .add("payment", ins.getPayment())
                    .add("reference", ins.getReference())
                    .add("dateVente", Constants.Datetime.format(ins.getDateVente()))
                    .add("deviseDette", ins.getDeviseDette());
            JsonObjectBuilder jsob = Json.createObjectBuilder();

            jsob.add("uid", ins.getClientId().getUid())
                    .add("adresse", ins.getClientId().getAdresse())
                    .add("email", ins.getClientId().getEmail())
                    .add("typeClient", ins.getClientId().getTypeClient())
                    .add("nomClient", ins.getClientId().getNomClient())
                    .add("phone", ins.getClientId().getPhone());

            Client c = ins.getClientId().getParentId();
            if (c == null) {
                jsob.add("parentId", Json.createObjectBuilder().add("uid", ins.getClientId().getUid()).build());
            } else {
                jsob.add("parentId", Json.createObjectBuilder().add("uid", c.getUid()).build());
            }
            JsonObject sobj = jsob.build();
            builder.add("clientId", sobj);
            if (ins.getEcheance() != null) {
                builder.add("echeance", Constants.dateFormater.format(ins.getEcheance()));
            }
            System.out.println(" VENTE synchrozzzzzzzzzzzzzzzz.............................sssssss.ssssssss.s.s.....ds.sssssssssss.s.XXXXXXXXXXXXX");
        } else if (obj instanceof LigneVente) {
            LigneVente ins = (LigneVente) obj;
            builder.add("uid", ins.getUid())
                    .add("numlot", ins.getNumlot())
                    .add("prixUnit", ins.getPrixUnit())
                    .add("quantite", ins.getQuantite())
                    .add("montantCdf", ins.getMontantCdf())
                    .add("montantUsd", ins.getMontantUsd())
                    .add("productId", Json.createObjectBuilder()
                            .add("uid", ins.getProductId().getUid()).build())
                    .add("mesureId", Json.createObjectBuilder()
                            .add("uid", ins.getMesureId().getUid()).build());
//            System.out.println("REF== " + ins.getReference().getUid());

            if (ins.getReference() != null) {
                builder.add("reference", Json.createObjectBuilder()
                        .add("uid", ins.getReference().getUid()).build());
            }
            if (ins.getClientId() != null) {
                builder.add("clientId", ins.getClientId());
            }
        } else if (obj instanceof Traisorerie) {
            Traisorerie ins = (Traisorerie) obj;
            builder.add("uid", ins.getUid())
                    .add("libelle", ins.getLibelle())
                    .add("mouvement", ins.getMouvement())
                    .add("typeTresorerie", ins.getTypeTresorerie())
                    .add("montantCdf", ins.getMontantCdf())
                    .add("region", ins.getRegion())
                    .add("montantUsd", ins.getMontantUsd())
                    .add("reference", ins.getReference())
                    .add("tresorId", Json.createObjectBuilder().add("uid", ins.getTresorId() == null ? "" : ins.getTresorId().getUid()))
                    .add("date", Constants.Datetime.format(ins.getDate()));
        } else if (obj instanceof Operation) {
            Operation ins = (Operation) obj;
            builder.add("uid", ins.getUid())
                    .add("libelle", ins.getLibelle())
                    .add("mouvement", ins.getMouvement())
                    .add("imputation", ins.getImputation())
                    .add("montantCdf", ins.getMontantCdf())
                    .add("region", ins.getRegion())
                    .add("tresorId", Json.createObjectBuilder().add("uid", ins.getTresorId() == null ? "" : ins.getTresorId().getUid()))
                    .add("depenseId", Json.createObjectBuilder().add("uid", ins.getDepenseId() == null ? "" : ins.getDepenseId().getUid()))
                    .add("montantUsd", ins.getMontantUsd())
                    .add("referenceOp", ins.getReferenceOp())
                    .add("date", Constants.Datetime.format(ins.getDate()))
                    .add("caisseOpId", Json.createObjectBuilder()
                            .add("uid", ins.getCaisseOpId().getUid())
                            .add("libelle", ins.getCaisseOpId().getLibelle())
                            .add("mouvement", ins.getCaisseOpId().getMouvement())
                            .add("typeTresorerie", ins.getCaisseOpId().getTypeTresorerie())
                            .add("montantCdf", ins.getCaisseOpId().getMontantCdf())
                            .add("region", ins.getCaisseOpId().getRegion())
                            .add("montantUsd", ins.getCaisseOpId().getMontantUsd())
                            .add("reference", ins.getCaisseOpId().getReference())
                            .add("date", Constants.Datetime.format(ins.getCaisseOpId().getDate())).build());

        } else if (obj instanceof ImageProduit) {
            ImageProduit image = (ImageProduit) obj;
            builder.add("idProduit", image.getIdProduit())
                    .add("imageBase64", image.getImageBase64());
        } else if (obj instanceof Aretirer) {
            Aretirer oper = (Aretirer) obj;
            builder.add("uid", oper.getUid())
                    .add("numlot", oper.getNumlot())
                    .add("prixVente", oper.getPrixVente())
                    .add("quantite", oper.getQuantite())
                    .add("referenceVente", oper.getReferenceVente())
                    .add("region", oper.getRegion())
                    .add("status", oper.getStatus())
                    .add("date", Constants.Datetime.format(oper.getDate()))
                    .add("clientId", Json.createObjectBuilder().add("uid", oper.getClientId().getUid()).build())
                    .add("ligneVenteId", Json.createObjectBuilder().add("uid", oper.getLigneVenteId().getUid()).build())
                    .add("mesureId", Json.createObjectBuilder().add("uid", oper.getMesureId().getUid()).build());

        } else if (obj instanceof ClientAppartenir) {
            ClientAppartenir oper = (ClientAppartenir) obj;
            builder.add("uid", oper.getUid())
                    .add("region", oper.getRegion())
                    .add("date", Constants.Datetime.format(oper.getDateAppartenir()))
                    .add("clientId", Json.createObjectBuilder().add("uid", oper.getClientId().getUid()).build())
                    .add("clientOrganisationId", Json.createObjectBuilder().add("uid", oper.getClientOrganisationId().getUid()).build());
        } else if (obj instanceof ClientOrganisation) {
            ClientOrganisation oper = (ClientOrganisation) obj;
            builder.add("uid", oper.getUid())
                    .add("adresse", oper.getAdresse())
                    .add("boitePostalOrganisation", oper.getBoitePostalOrganisation())
                    .add("domaineOrganisation", oper.getDomaineOrganisation())
                    .add("emailOrganisation", oper.getEmailOrganisation())
                    .add("region", oper.getRegion())
                    .add("nomOrganisation", oper.getNomOrganisation())
                    .add("phoneOrganisation", oper.getPhoneOrganisation())
                    .add("rccmOrganisation", oper.getRccmOrganisation())
                    .add("websiteOrganisation", oper.getWebsiteOrganisation());
        } else if (obj instanceof RetourDepot) {
            RetourDepot oper = (RetourDepot) obj;
            builder.add("uid", oper.getUid())
                    .add("coutAchat", oper.getCoutAchat())
                    .add("localisation", oper.getLocalisation())
                    .add("motif", oper.getMotif())
                    .add("numlot", oper.getNumlot())
                    .add("region", oper.getRegion())
                    .add("quantite", oper.getQuantite())
                    .add("regionDest", oper.getRegionDest())
                    .add("regionProv", oper.getRegionProv())
                    .add("date", Constants.Datetime.format(oper.getDate()))
                    .add("destockerId", Json.createObjectBuilder().add("uid", oper.getDestockerId().getUid()).build())
                    .add("recquisitionId", Json.createObjectBuilder().add("uid", oper.getRecquisitionId().getUid()).build())
                    .add("mesureId", Json.createObjectBuilder().add("uid", oper.getMesureId().getUid()).build());
        } else if (obj instanceof RetourMagasin) {
            RetourMagasin oper = (RetourMagasin) obj;
            builder.add("uid", oper.getUid())
                    .add("prixVente", oper.getPrixVente())
                    .add("referenceVente", oper.getReferenceVente())
                    .add("motif", oper.getMotif())
                    .add("region", oper.getRegion())
                    .add("quantite", oper.getQuantite())
                    .add("date", Constants.Datetime.format(oper.getDate()))
                    .add("clientId", Json.createObjectBuilder().add("uid", oper.getClientId().getUid()).build())
                    .add("ligneVenteId", Json.createObjectBuilder().add("uid", oper.getLigneVenteId().getUid()).build())
                    .add("mesureId", Json.createObjectBuilder().add("uid", oper.getMesureId().getUid()).build());
        } else if (obj instanceof Abonnement) {
            Abonnement ab = (Abonnement) obj;
            builder.add("uid", ab.getUid())
                    .add("devise", ab.getDevise())
                    .add("etat", ab.getEtat())
                    .add("montant", ab.getMontant())
                    .add("nombreOperation", ab.getNombreOperation())
                    .add("typeAbonnement", ab.getTypeAbonnement())
                    .add("dateAbonnement", Constants.Datetime.format(ab.getDateAbonnement()));
        } else if (obj instanceof Facture) {
            Facture bill = (Facture) obj;
            builder.add("uid", bill.getUid())
                    .add("numero", bill.getNumero())
                    .add("payedamount", bill.getPayedamount())
                    .add("region", bill.getRegion())
                    .add("status", bill.getStatus())
                    .add("totalamount", bill.getTotalamount())
                    .add("organisId", Json.createObjectBuilder().add("uid", bill.getOrganisId().getUid()).build())
                    .add("startDate", Constants.DATE_HEURE_FORMAT.format(bill.getStartDate()))
                    .add("endDate", Constants.DATE_HEURE_FORMAT.format(bill.getEndDate()));
        } else if (obj instanceof Depense) {
            Depense bill = (Depense) obj;
            builder.add("uid", bill.getUid())
                    .add("nomDepense", bill.getNomDepense())
                    .add("region", bill.getRegion());
        } else if (obj instanceof CompteTresor) {
            CompteTresor bill = (CompteTresor) obj;
            builder.add("uid", bill.getUid())
                    .add("bankName", bill.getBankName() == null ? "-" : bill.getBankName())
                    .add("intitule", bill.getIntitule() == null ? "-" : bill.getIntitule())
                    .add("numeroCompte", bill.getNumeroCompte() == null ? "-" : bill.getNumeroCompte())
                    .add("region", bill.getRegion() == null ? "-" : bill.getRegion())
                    .add("soldeMinimum", bill.getSoldeMinimum() == null ? 0 : bill.getSoldeMinimum())
                    .add("typeCompte", bill.getTypeCompte() == null ? "-" : bill.getTypeCompte());
        }

        return builder.build();
    }

    public static Object objectify(String message) {
        JsonReader reader = Json.createReader(new StringReader(message));
        JsonObject json = reader.readObject();
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
            String s = json.getString("dateLivr");
            if (!s.isEmpty()) {
                ins.setDateLivr(LocalDate.parse(s));
            }
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
            try {
                if (json.containsKey("dateExpir")) {
                    String dateE = json.getString("dateExpir");
                    ins.setDateExpir(dateE == null ? null : Constants.dateFormater.parse(dateE));
                }
                ins.setDateStocker(Constants.Datetime.parse(json.getString("dateStocker")));
            } catch (ParseException ex) {
                Logger.getLogger(JsonUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
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
            ins.setDateDestockage(Constants.Datetime.parse(json.getString("dateDestockage")));
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
            try {
                if (json.containsKey("dateExpiry")) {
                    String dateE = json.getString("dateExpiry");
                    if (!dateE.isEmpty() && !dateE.equalsIgnoreCase("null")) {
                        ins.setDateExpiry(Constants.dateFormater.parse(dateE));
                    }
                }
                ins.setDate(Constants.Datetime.parse(json.getString("date")));
            } catch (ParseException ex) {
                Logger.getLogger(JsonUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
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
            try {
                if (json.containsKey("echeance")) {
                    ins.setEcheance(Constants.dateFormater.parse(json.getString("echeance")));
                }
                ins.setDateVente(Constants.dateFormater.parse(json.getString("dateVente")));
            } catch (ParseException ex) {
                Logger.getLogger(JsonUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
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
            ins.setDate(Constants.Datetime.parse(json.getString("date")));
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
            ins.setDate(Constants.Datetime.parse(json.getString("date")));
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
            t.setDate(Constants.Datetime.parse(jso.getString("date")));
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

            try {
                oper.setDate(Constants.dateFormater.parse(json.getString("date")));
            } catch (ParseException ex) {
                Logger.getLogger(JsonUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
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
            try {
                oper.setDateAppartenir(Constants.dateFormater.parse(json.getString("date")));
            } catch (ParseException ex) {
                Logger.getLogger(JsonUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
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
            try {
                oper.setDate(Constants.dateFormater.parse(json.getString("date")));
            } catch (ParseException ex) {
                Logger.getLogger(JsonUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
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
            try {
                oper.setDate(Constants.dateFormater.parse(json.getString("date")));
            } catch (ParseException ex) {
                Logger.getLogger(JsonUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
            JsonObject jo = json.getJsonObject("ligneVenteId");
            oper.setLigneVenteId(new LigneVente(jo.getJsonNumber("uid").longValue()));
            JsonObject job = json.getJsonObject("recquisitionId");
            oper.setClientId(new Client(job.getString("clientId")));
            JsonObject jso = json.getJsonObject("mesureId");
            oper.setMesureId(new Mesure(jso.getString("uid")));
            return oper;
        } else if (json.containsKey("typeAbonnemnt")) {
            Abonnement ab = new Abonnement();
            ab.setUid(json.getString("uid"));
            try {
                ab.setDateAbonnement(Constants.dateFormater.parse(json.getString("dateAbonnement")));
            } catch (ParseException ex) {
                Logger.getLogger(JsonUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
            ab.setDevise(json.getString("devise"));
            ab.setEtat(json.getString("etat"));
            ab.setMontant(json.getJsonNumber("montant").doubleValue());
            ab.setNombreOperation(json.getJsonNumber("nombreOperation").doubleValue());
            ab.setTypeAbonnement(json.getString("typeAbonnement"));
            return ab;
        } else if (json.containsKey("startDate")) {
            Facture f = new Facture();
            f.setUid(json.getString("uid"));
            try {
                f.setStartDate(Constants.dateFormater.parse(json.getString("startDate")));
                f.setStartDate(Constants.dateFormater.parse(json.getString("endDate")));
            } catch (ParseException ex) {
                Logger.getLogger(JsonUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
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

}
