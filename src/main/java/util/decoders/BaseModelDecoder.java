/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util.decoders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.stream.JsonParser;
import jakarta.websocket.DecodeException;
import jakarta.websocket.Decoder;
import jakarta.websocket.EndpointConfig;
import data.Abonnement;
import data.Aretirer;
import data.BaseModel;
import data.BulkModel;
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
import data.Refresher;
import data.RetourDepot;
import data.RetourMagasin;
import data.Stocker;
import data.Traisorerie;
import data.Vente;
import java.time.LocalDate;
import tools.JsonUtil;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class BaseModelDecoder implements Decoder.Text<BaseModel> {

    HashMap<String, Object> vmap;

    public BaseModelDecoder() {
    }
    

    @Override
    public BaseModel decode(String s) throws DecodeException {
       // System.out.println("REcept "+s);
        if (willDecode(s)) {
            String type = String.valueOf(vmap.get("type"));
            Tables table = Tables.valueOf(type);
            switch (table) {
                case CATEGORY:
                    Category rstc = new Category(String.valueOf(vmap.get("uid")),String.valueOf(vmap.get("descritption")));
                    rstc.setAction(String.valueOf(vmap.get("action")));
                    rstc.setCount(Long.parseLong(String.valueOf(vmap.get("count"))));
                    rstc.setCounter(Long.valueOf(String.valueOf(vmap.get("counter"))));
                    rstc.setPriority(Integer.valueOf(String.valueOf(vmap.get("priority"))));
                    rstc.setType(String.valueOf(vmap.get("type")));
                    rstc.setPayload(String.valueOf(vmap.get("payload")));
                    rstc.setFrom(String.valueOf(vmap.get("from")));
                    return rstc;
                case PRODUIT:
                    Produit p = new Produit(String.valueOf(vmap.get("uid")));
                    p.setCodebar(String.valueOf(vmap.get("codebar")));
                    p.setNomProduit(String.valueOf(vmap.get("nomProduit")));
                    p.setMarque(String.valueOf(vmap.get("marque")));
                    p.setModele(String.valueOf(vmap.get("modele")));
                    p.setCouleur(String.valueOf(vmap.get("couleur")));
                    p.setTaille(String.valueOf(vmap.get("taille")));
                    p.setMethodeInventaire(String.valueOf(vmap.get("methodeInventaire")));
                    p.setCategoryId((Category) vmap.get("categoryId"));

                    p.setAction(String.valueOf(vmap.get("action")));
                    p.setCount(Long.valueOf(String.valueOf(vmap.get("count"))));
                    p.setPriority(Integer.valueOf(String.valueOf(vmap.get("priority"))));
                    p.setCounter(Long.valueOf(String.valueOf(vmap.get("counter"))));
                    p.setType(String.valueOf(vmap.get("type")));
                    p.setPayload(String.valueOf(vmap.get("payload")));
                    p.setFrom(String.valueOf(vmap.get("from")));
                    return p;
                case MESURE:
                    Mesure m = new Mesure(String.valueOf(vmap.get("uid")));
                    m.setDescription(String.valueOf(vmap.get("description")));
                    m.setQuantContenu((Double) vmap.get("quantContenu"));
                    m.setProduitId((Produit) vmap.get("produitId"));

                    m.setAction(String.valueOf(vmap.get("action")));
                    m.setCount(Long.valueOf(String.valueOf(vmap.get("count"))));
                    m.setPriority(Integer.valueOf(String.valueOf(vmap.get("priority"))));
                    m.setCounter(Long.valueOf(String.valueOf(vmap.get("counter"))));
                    m.setType(String.valueOf(vmap.get("type")));
                    m.setPayload(String.valueOf(vmap.get("payload")));
                    m.setFrom(String.valueOf(vmap.get("from")));
                    return m;
                case FOURNISSEUR:
                    Fournisseur ins = new Fournisseur(String.valueOf(vmap.get("uid")));
                    ins.setAdresse(String.valueOf(vmap.get("adresse")));
                    ins.setIdentification(String.valueOf(vmap.get("identification")));
                    ins.setNomFourn(String.valueOf(vmap.get("nomFourn")));
                    ins.setPhone(String.valueOf(vmap.get("phone")));

                    ins.setAction(String.valueOf(vmap.get("action")));
                    ins.setCount(Long.parseLong(String.valueOf(vmap.get("count"))));
                    ins.setPriority(Integer.valueOf(String.valueOf(vmap.get("priority"))));
                    ins.setCounter(Long.valueOf(String.valueOf(vmap.get("counter"))));
                    ins.setType(String.valueOf(vmap.get("type")));
                    ins.setPayload(String.valueOf(vmap.get("payload")));
                    ins.setFrom(String.valueOf(vmap.get("from")));
                    return ins;
                case LIVRAISON:
                    Livraison livrz = new Livraison(String.valueOf(vmap.get("uid")));
                    livrz.setLibelle(String.valueOf(vmap.get("libelle")));
                    livrz.setNumPiece(String.valueOf(vmap.get("numPiece")));
                    livrz.setObservation(String.valueOf(vmap.get("observation")));
                    livrz.setPayed((Double) vmap.get("payed"));
                    livrz.setReduction((Double) vmap.get("reduction"));
                    livrz.setReference(String.valueOf(vmap.get("reference")));
                    livrz.setRegion(String.valueOf(vmap.get("region")));
                    livrz.setRemained((Double) vmap.get("remained"));
                    livrz.setTopay((Double) vmap.get("topay"));
                    livrz.setToreceive((Double) vmap.get("toreceive"));
                    livrz.setDateLivr(LocalDate.parse(String.valueOf(vmap.get("dateLivr"))));
                    livrz.setFournId((Fournisseur) vmap.get("fournId"));

                    livrz.setAction(String.valueOf(vmap.get("action")));
                    livrz.setCount(Long.parseLong(String.valueOf(vmap.get("count"))));
                    livrz.setCounter(Long.parseLong(String.valueOf(vmap.get("counter"))));
                    livrz.setPriority(Integer.parseInt(String.valueOf(vmap.get("priority"))));
                    livrz.setType(String.valueOf(vmap.get("type")));
                    livrz.setPayload(String.valueOf(vmap.get("payload")));
                    livrz.setFrom(String.valueOf(vmap.get("from")));
                    return livrz;
                case STOCKER:
                    Stocker stok = new Stocker(String.valueOf(vmap.get("uid")));
                    stok.setLibelle(String.valueOf(vmap.get("libelle")));
                    stok.setRegion(String.valueOf(vmap.get("region")));
                    stok.setObservation(String.valueOf(vmap.get("observation")));
                    stok.setNumlot(String.valueOf(vmap.get("numlot")));
                    stok.setLocalisation(String.valueOf(vmap.get("localisation")));
                    stok.setCoutAchat((Double) vmap.get("coutAchat"));
                    stok.setReduction((Double) vmap.get("reduction"));
                    stok.setPrixAchatTotal((Double) vmap.get("prixAchatTotal"));
                    stok.setQuantite((Double) vmap.get("quantite"));
                    stok.setStockAlerte((Double) vmap.get("stockAlerte"));
                    Object obz = vmap.get("dateExpir");
                    if (obz != null) {
                        stok.setDateExpir((Date) obz);
                    }
                    stok.setDateStocker((Date) vmap.get("dateStocker"));
                    stok.setLivraisId((Livraison) vmap.get("livraisId"));
                    stok.setMesureId((Mesure) vmap.get("mesureId"));
                    stok.setProductId((Produit) vmap.get("productId"));

                    stok.setAction(String.valueOf(vmap.get("action")));
                    stok.setCount(Long.parseLong(String.valueOf(vmap.get("count"))));
                    stok.setCounter(Long.valueOf(String.valueOf(vmap.get("counter"))));
                    stok.setPriority(Integer.valueOf(String.valueOf(vmap.get("priority"))));
                    stok.setType(String.valueOf(vmap.get("type")));
                    stok.setPayload(String.valueOf(vmap.get("payload")));
                    stok.setFrom(String.valueOf(vmap.get("from")));
                    return stok;
                case DESTOCKER:
                    Destocker destok = new Destocker();
                    destok.setUid(String.valueOf(vmap.get("uid")));
                    destok.setLibelle(String.valueOf(vmap.get("libelle")));
                    destok.setCoutAchat((Double) vmap.get("coutAchat"));
                    destok.setObservation(String.valueOf(vmap.get("observation")));
                    destok.setNumlot(String.valueOf(vmap.get("numlot")));
                    destok.setReference(String.valueOf(vmap.get("reference")));
                    destok.setRegion(String.valueOf(vmap.get("region")));
                    destok.setQuantite((Double) vmap.get("quantite"));
                    destok.setDestination(String.valueOf(vmap.get("destination")));
                    destok.setDateDestockage((Date) vmap.get("dateDestockage"));
                    destok.setMesureId((Mesure) vmap.get("mesureId"));
                    destok.setProductId((Produit) vmap.get("productId"));

                    destok.setAction(String.valueOf(vmap.get("action")));
                    destok.setCount(Long.valueOf(String.valueOf(vmap.get("count"))));
                    destok.setCounter(Long.valueOf(String.valueOf(vmap.get("counter"))));
                    destok.setPriority(Integer.valueOf(String.valueOf(vmap.get("priority"))));
                    destok.setType(String.valueOf(vmap.get("type")));
                    destok.setPayload(String.valueOf(vmap.get("payload")));
                    destok.setFrom(String.valueOf(vmap.get("from")));
                    return destok;

                case RECQUISITION:
                    Recquisition recq = new Recquisition(String.valueOf(vmap.get("uid")));
                    recq.setReference(String.valueOf(vmap.get("reference")));
                    recq.setObservation(String.valueOf(vmap.get("observation")));
                    recq.setNumlot(String.valueOf(vmap.get("numlot")));
                    recq.setRegion(String.valueOf(vmap.get("region")));
                    recq.setQuantite((Double) vmap.get("quantite"));
                    recq.setCoutAchat((Double) vmap.get("coutAchat"));
                    recq.setStockAlert((Double) vmap.get("stockAlert"));
                    Object de = vmap.get("dateExpiry");
                    if (de != null) {
                        recq.setDateExpiry((Date) de);
                    }
                    recq.setDate((Date) vmap.get("date"));
                    recq.setMesureId((Mesure) vmap.get("mesureId"));
                    recq.setProductId((Produit) vmap.get("productId"));

                    recq.setAction(String.valueOf(vmap.get("action")));
                    recq.setCount(Long.valueOf(String.valueOf(vmap.get("count"))));
                    recq.setPriority(Integer.valueOf(String.valueOf(vmap.get("priority"))));
                    recq.setCounter(Long.valueOf(String.valueOf(vmap.get("counter"))));
                    recq.setType(String.valueOf(vmap.get("type")));
                    recq.setPayload(String.valueOf(vmap.get("payload")));
                    recq.setFrom(String.valueOf(vmap.get("from")));
                    return recq;
                case PRIXDEVENTE:
                    PrixDeVente pxv = new PrixDeVente(String.valueOf(vmap.get("uid")));
                    pxv.setQmax((Double) vmap.get("qmax"));
                    pxv.setQmin((Double) vmap.get("qmin"));
                    pxv.setDevise(String.valueOf(vmap.get("devise")));
                    pxv.setPrixUnitaire((Double) vmap.get("prixUnitaire"));
                    pxv.setMesureId((Mesure) vmap.get("mesureId"));
                    pxv.setRecquisitionId((Recquisition) vmap.get("recquisitionId"));

                    pxv.setAction(String.valueOf(vmap.get("action")));
                    pxv.setCount(Long.valueOf(String.valueOf(vmap.get("count"))));
                    pxv.setPriority(Integer.valueOf(String.valueOf(vmap.get("priority"))));
                    pxv.setCounter(Long.valueOf(String.valueOf(vmap.get("counter"))));
                    pxv.setType(String.valueOf(vmap.get("type")));
                    pxv.setPayload(String.valueOf(vmap.get("payload")));
                    pxv.setFrom(String.valueOf(vmap.get("from")));
                    return pxv;
                case CLIENT:
                    Client client = new Client();
                    client.setUid(String.valueOf(vmap.get("uid")));
                    client.setAdresse(String.valueOf(vmap.get("adresse")));
                    client.setEmail(String.valueOf(vmap.get("email")));
                    client.setTypeClient(String.valueOf(vmap.get("typeClient")));
                    client.setNomClient(String.valueOf(vmap.get("nomClient")));
                    client.setParentId((Client) vmap.get("parentId"));
                    client.setPhone(String.valueOf(vmap.get("phone")));

                    client.setAction(String.valueOf(vmap.get("action")));
                    client.setCount(Long.valueOf(String.valueOf(vmap.get("count"))));
                    client.setPriority(Integer.valueOf(String.valueOf(vmap.get("priority"))));
                    client.setCounter(Long.valueOf(String.valueOf(vmap.get("counter"))));
                    client.setType(String.valueOf(vmap.get("type")));
                    client.setPayload(String.valueOf(vmap.get("payload")));
                    client.setFrom(String.valueOf(vmap.get("from")));
                    return client;
                case CLIENTAPPARTENIR:
                    ClientAppartenir oper = new ClientAppartenir();
                    oper.setUid(String.valueOf(vmap.get("uid")));
                    oper.setRegion(String.valueOf(vmap.get("region")));
                    oper.setDateAppartenir((Date) vmap.get("date"));
                    oper.setClientId((Client) vmap.get("clientId"));
                    oper.setClientOrganisationId((ClientOrganisation) vmap.get("clientOrganisationId"));

                    oper.setAction(String.valueOf(vmap.get("action")));
                    oper.setCount(Long.valueOf(String.valueOf(vmap.get("count"))));
                    oper.setPriority(Integer.valueOf(String.valueOf(vmap.get("priority"))));
                    oper.setCounter(Long.valueOf(String.valueOf(vmap.get("counter"))));
                    oper.setType(String.valueOf(vmap.get("type")));
                    oper.setPayload(String.valueOf(vmap.get("payload")));
                    oper.setFrom(String.valueOf(vmap.get("from")));
                    return oper;

                case CLIENTORGANISATION:
                    ClientOrganisation clio = new ClientOrganisation();
                    clio.setUid(String.valueOf(vmap.get("uid")));
                    clio.setRegion(String.valueOf(vmap.get("region")));
                    clio.setAdresse(String.valueOf(vmap.get("adresse")));
                    clio.setBoitePostalOrganisation(String.valueOf(vmap.get("boitePostalOrganisation")));
                    clio.setDomaineOrganisation(String.valueOf(vmap.get("domaineOrganisation")));
                    clio.setEmailOrganisation(String.valueOf(vmap.get("emailOrganisation")));
                    clio.setNomOrganisation(String.valueOf(vmap.get("nomOrganisation")));
                    clio.setPhoneOrganisation(String.valueOf(vmap.get("phoneOrganisation")));
                    clio.setRccmOrganisation(String.valueOf(vmap.get("rccmOrganisation")));
                    clio.setWebsiteOrganisation(String.valueOf(vmap.get("websiteOrganisation")));

                    clio.setAction(String.valueOf(vmap.get("action")));
                    clio.setCount(Long.valueOf(String.valueOf(vmap.get("count"))));
                    clio.setPriority(Integer.valueOf(String.valueOf(vmap.get("priority"))));
                    clio.setCounter(Long.valueOf(String.valueOf(vmap.get("counter"))));
                    clio.setType(String.valueOf(vmap.get("type")));
                    clio.setPayload(String.valueOf(vmap.get("payload")));
                    clio.setFrom(String.valueOf(vmap.get("from")));
                    return clio;
                case VENTE:
                    Vente vente = new Vente();
                    vente.setUid((Integer) vmap.get("uid"));
                    vente.setLibelle(String.valueOf(vmap.get("libelle")));
                    vente.setLatitude((Double) vmap.get("latitude"));
                    vente.setObservation(String.valueOf(vmap.get("observation")));
                    vente.setLongitude((Double) vmap.get("longitude"));
                    vente.setMontantCdf((Double) vmap.get("montantCdf"));
                    vente.setMontantDette((Double) vmap.get("montantDette"));
                    vente.setRegion(String.valueOf(vmap.get("region")));
                    vente.setMontantUsd((Double) vmap.get("montantUsd"));
                    vente.setPayment(String.valueOf(vmap.get("payment")));
                    vente.setReference(String.valueOf(vmap.get("reference")));
                    Object ech = vmap.get("echeance");
                    if (ech != null) {
                        vente.setEcheance((Date) ech);
                    }
                    vente.setDateVente((Date) vmap.get("dateVente"));
                    vente.setDeviseDette(String.valueOf(vmap.get("deviseDette")));
                    vente.setClientId((Client) vmap.get("clientId"));

                    vente.setAction(String.valueOf(vmap.get("action")));
                    vente.setCount(Long.valueOf(String.valueOf(vmap.get("count"))));
                    vente.setPriority(Integer.valueOf(String.valueOf(vmap.get("priority"))));
                    vente.setCounter(Long.valueOf(String.valueOf(vmap.get("counter"))));
                    vente.setType(String.valueOf(vmap.get("type")));
                    vente.setPayload(String.valueOf(vmap.get("payload")));
                    vente.setFrom(String.valueOf(vmap.get("from")));
                    return vente;
                case LIGNEVENTE:
                    LigneVente lignv = new LigneVente();
                    lignv.setUid((Long) vmap.get("uid"));
                    lignv.setClientId(String.valueOf(vmap.get("clientId")));
                    lignv.setNumlot(String.valueOf(vmap.get("numlot")));
                    lignv.setPrixUnit((Double) vmap.get("prixUnit"));
                    lignv.setQuantite((Double) vmap.get("quantite"));
                    lignv.setMontantCdf((Double) vmap.get("montantCdf"));
                    lignv.setMontantUsd((Double) vmap.get("montantUsd"));

                    lignv.setProductId((Produit) vmap.get("productId"));
                    lignv.setMesureId((Mesure) vmap.get("mesureId"));
                    lignv.setReference((Vente) vmap.get("reference"));

                    lignv.setAction(String.valueOf(vmap.get("action")));
                    lignv.setCount(Long.valueOf(String.valueOf(vmap.get("count"))));
                    lignv.setPriority(Integer.valueOf(String.valueOf(vmap.get("priority"))));
                    lignv.setCounter(Long.valueOf(String.valueOf(vmap.get("counter"))));
                    lignv.setType(String.valueOf(vmap.get("type")));
                    lignv.setPayload(String.valueOf(vmap.get("payload")));
                    lignv.setFrom(String.valueOf(vmap.get("from")));
                    return lignv;
                case FACTURE:
                    Facture f = new Facture();
                    f.setUid(String.valueOf(vmap.get("uid")));

                    f.setStartDate((Date) vmap.get("startDate"));
                    f.setStartDate((Date) vmap.get("endDate"));

                    f.setNumero(String.valueOf(vmap.get("numero")));
                    f.setOrganisId((ClientOrganisation) vmap.get("organisId"));
                    f.setPayedamount((Double) vmap.get("payedamount"));
                    f.setRegion(String.valueOf(vmap.get("region")));
                    f.setStatus(String.valueOf(vmap.get("status")));
                    f.setTotalamount((Double) vmap.get("totalamount"));
                    f.setAction(String.valueOf(vmap.get("action")));
                    f.setPriority(Integer.valueOf(String.valueOf(vmap.get("priority"))));
                    f.setCount(Long.valueOf(String.valueOf(vmap.get("count"))));
                    f.setCounter(Long.valueOf(String.valueOf(vmap.get("counter"))));
                    f.setType(String.valueOf(vmap.get("type")));
                    f.setPayload(String.valueOf(vmap.get("payload")));
                    f.setFrom(String.valueOf(vmap.get("from")));
                    return f;
                case ARETIRER:
                    Aretirer aretir = new Aretirer();
                    aretir.setUid(String.valueOf(vmap.get("uid")));
                    aretir.setNumlot(String.valueOf(vmap.get("numlot")));
                    aretir.setPrixVente((Double) vmap.get("prixVente"));
                    aretir.setQuantite((Double) vmap.get("quantite"));
                    aretir.setReferenceVente(String.valueOf(vmap.get("referenceVente")));
                    aretir.setRegion(String.valueOf(vmap.get("region")));
                    aretir.setStatus(String.valueOf(vmap.get("status")));
                    aretir.setDate((Date) vmap.get("date"));
                    aretir.setClientId((Client) vmap.get("clientId"));
                    aretir.setLigneVenteId((LigneVente) vmap.get("ligneVenteId"));
                    aretir.setMesureId((Mesure) vmap.get("mesureId"));
                    aretir.setAction(String.valueOf(vmap.get("action")));
                    aretir.setCount(Long.valueOf(String.valueOf(vmap.get("count"))));
                    aretir.setPriority(Integer.valueOf(String.valueOf(vmap.get("priority"))));
                    aretir.setCounter(Long.valueOf(String.valueOf(vmap.get("counter"))));
                    aretir.setType(String.valueOf(vmap.get("type")));
                    aretir.setPayload(String.valueOf(vmap.get("payload")));
                    aretir.setFrom(String.valueOf(vmap.get("from")));
                    return aretir;
                case RETOURMAGASIN:
                    RetourMagasin rtrmag = new RetourMagasin();
                    rtrmag.setUid(String.valueOf(vmap.get("uid")));
                    rtrmag.setRegion(String.valueOf(vmap.get("region")));
                    rtrmag.setPrixVente((Double) vmap.get("prixVente"));
                    rtrmag.setReferenceVente(String.valueOf(vmap.get("referenceVente")));
                    rtrmag.setMotif(String.valueOf(vmap.get("motif")));
                    rtrmag.setQuantite((Double) vmap.get("quantite"));
                    rtrmag.setDate((Date) vmap.get("date"));
                    rtrmag.setLigneVenteId((LigneVente) vmap.get("ligneVenteId"));
                    rtrmag.setClientId((Client) vmap.get("recquisitionId"));
                    rtrmag.setMesureId((Mesure) vmap.get("mesureId"));

                    rtrmag.setAction(String.valueOf(vmap.get("action")));
                    rtrmag.setCount(Long.parseLong(String.valueOf(vmap.get("count"))));
                    rtrmag.setPriority(Integer.valueOf(String.valueOf(vmap.get("priority"))));
                    rtrmag.setCounter(Long.valueOf(String.valueOf(vmap.get("counter"))));
                    rtrmag.setType(String.valueOf(vmap.get("type")));
                    rtrmag.setPayload(String.valueOf(vmap.get("payload")));
                    rtrmag.setFrom(String.valueOf(vmap.get("from")));
                    return rtrmag;
                case RETOURDEPOT:
                    RetourDepot rtrdep = new RetourDepot();
                    rtrdep.setUid(String.valueOf(vmap.get("uid")));
                    rtrdep.setRegion(String.valueOf(vmap.get("region")));
                    rtrdep.setCoutAchat((Double) vmap.get("coutAchat"));
                    rtrdep.setLocalisation(String.valueOf(vmap.get("localisation")));
                    rtrdep.setMotif(String.valueOf(vmap.get("motif")));
                    rtrdep.setNumlot(String.valueOf(vmap.get("numlot")));
                    rtrdep.setQuantite((Double) vmap.get("quantite"));
                    rtrdep.setRegionDest(String.valueOf(vmap.get("regionDest")));
                    rtrdep.setRegionProv(String.valueOf(vmap.get("regionProv")));
                    rtrdep.setDate((Date) vmap.get("date"));
                    rtrdep.setDestockerId((Destocker) vmap.get("destockerId"));
                    rtrdep.setRecquisitionId((Recquisition) vmap.get("recquisitionId"));
                    rtrdep.setMesureId((Mesure) vmap.get("mesureId"));

                    rtrdep.setAction(String.valueOf(vmap.get("action")));
                    rtrdep.setCount(Long.parseLong(String.valueOf(vmap.get("count"))));
                    rtrdep.setPriority(Integer.parseInt(String.valueOf(vmap.get("priority"))));
                    rtrdep.setCounter(Long.parseLong(String.valueOf(vmap.get("counter"))));
                    rtrdep.setType(String.valueOf(vmap.get("type")));
                    rtrdep.setPayload(String.valueOf(vmap.get("payload")));
                    rtrdep.setFrom(String.valueOf(vmap.get("from")));
                    return rtrdep;
                case COMPTETRESOR:
                    CompteTresor bill = new CompteTresor(String.valueOf(vmap.get("uid")));
                    bill.setBankName(String.valueOf(vmap.get("bankName")));
                    bill.setIntitule(String.valueOf(vmap.get("intitule")));
                    bill.setNumeroCompte(String.valueOf(vmap.get("numeroCompte")));
                    bill.setRegion(String.valueOf(vmap.get("region")));
                    bill.setSoldeMinimum((Double) vmap.get("soldeMinimum"));
                    bill.setTypeCompte(String.valueOf(vmap.get("typeCompte")));

                    bill.setAction(String.valueOf(vmap.get("action")));
                    bill.setCount(Long.parseLong(String.valueOf(vmap.get("count"))));
                    bill.setPriority(Integer.parseInt(String.valueOf(vmap.get("priority"))));
                    bill.setCounter(Long.parseLong(String.valueOf(vmap.get("counter"))));
                    bill.setType(String.valueOf(vmap.get("type")));
                    bill.setPayload(String.valueOf(vmap.get("payload")));
                    bill.setFrom(String.valueOf(vmap.get("from")));
                    return bill;
                case BULKMODEL:
                    String datax = String.valueOf(vmap.get("object"));
                     ObjectMapper obm = new ObjectMapper();
            
                try {
                    List<Object> objes = obm.readValue(datax, List.class);
                    BulkModel bm = new BulkModel();
                    bm.setType(type);
                    bm.setModels(objes);
                    return bm;
                } catch (JsonProcessingException ex) {
                    Logger.getLogger(BaseModelDecoder.class.getName()).log(Level.SEVERE, null, ex);
                } 

            
                  
                case TRAISORERIE:
                    Traisorerie tres = new Traisorerie();
                    tres.setUid(String.valueOf(vmap.get("uid")));
                    tres.setLibelle(String.valueOf(vmap.get("libelle")));
                    tres.setMouvement(String.valueOf(vmap.get("mouvement")));
                    tres.setTypeTresorerie(String.valueOf(vmap.get("typeTresorerie")));
                    tres.setMontantCdf((Double) vmap.get("montantCdf"));
                    tres.setRegion(String.valueOf(vmap.get("region")));
                    Object ctrz = vmap.get("tresorId");
                    if (ctrz != null) {
                        tres.setTresorId((CompteTresor) ctrz);
                    }
                    tres.setMontantUsd((Double) vmap.get("montantUsd"));
                    tres.setReference(String.valueOf(vmap.get("reference")));
                    tres.setDate((Date) vmap.get("date"));
                    tres.setAction(String.valueOf(vmap.get("action")));
                    tres.setCount(Long.parseLong(String.valueOf(vmap.get("count"))));
                    tres.setPriority(Integer.parseInt(String.valueOf(vmap.get("priority"))));
                    tres.setCounter(Long.parseLong(String.valueOf(vmap.get("counter"))));
                    tres.setType(String.valueOf(vmap.get("type")));
                    tres.setPayload(String.valueOf(vmap.get("payload")));
                    tres.setFrom(String.valueOf(vmap.get("from")));
                    return tres;
                case DEPENSE:
                    Depense dep = new Depense(String.valueOf(vmap.get("uid")));
                    dep.setNomDepense(String.valueOf(vmap.get("nomDepense")));
                    dep.setRegion(String.valueOf(vmap.get("region")));

                    dep.setAction(String.valueOf(vmap.get("action")));
                    dep.setCount(Long.valueOf(String.valueOf(vmap.get("count"))));
                    dep.setPriority(Integer.valueOf(String.valueOf(vmap.get("priority"))));
                    dep.setCounter(Long.valueOf(String.valueOf(vmap.get("counter"))));
                    dep.setType(String.valueOf(vmap.get("type")));
                    dep.setPayload(String.valueOf(vmap.get("payload")));
                    dep.setFrom(String.valueOf(vmap.get("from")));
                    return dep;
                case OPERATION:
                    Operation operation = new Operation();
                    operation.setUid(String.valueOf(vmap.get("uid")));
                    operation.setLibelle(String.valueOf(vmap.get("libelle")));
                    operation.setMouvement(String.valueOf(vmap.get("mouvement")));
                    operation.setImputation(String.valueOf(vmap.get("imputation")));
                    operation.setMontantCdf((Double) vmap.get("montantCdf"));
                    operation.setRegion(String.valueOf(vmap.get("region")));
                    operation.setMontantUsd((Double) vmap.get("montantUsd"));
                    operation.setReferenceOp(String.valueOf(vmap.get("referenceOp")));
                    Object cotrz = vmap.get("tresorId");
                    if (cotrz != null) {
                        operation.setTresorId((CompteTresor) cotrz);
                    }
                    Object deps = vmap.get("depenseId");
                    if (deps != null) {
                        operation.setDepenseId((Depense) deps);
                    }
                    operation.setDate((Date) vmap.get("date"));
                    operation.setCaisseOpId((Traisorerie) vmap.get("caisseOpId"));
                    operation.setAction(String.valueOf(vmap.get("action")));
                    operation.setCount(Long.valueOf(String.valueOf(vmap.get("count"))));
                    operation.setPriority(Integer.valueOf(String.valueOf(vmap.get("priority"))));
                    operation.setCounter(Long.valueOf(String.valueOf(vmap.get("counter"))));
                    operation.setType(String.valueOf(vmap.get("type")));
                    operation.setPayload(String.valueOf(vmap.get("payload")));
                    operation.setFrom(String.valueOf(vmap.get("from")));
                    return operation;
                case REFRESH:
                    Refresher r = new Refresher();
                    r.setTarget(String.valueOf(vmap.get("target")));
                    r.setAction(String.valueOf(vmap.get("action")));
                    r.setCount(Long.parseLong(String.valueOf(vmap.get("count"))));
                    r.setPriority(Integer.parseInt(String.valueOf(vmap.get("priority"))));
                    r.setCounter(Long.parseLong(String.valueOf(vmap.get("counter"))));
                    r.setType(String.valueOf(vmap.get("type")));
                    r.setPayload(String.valueOf(vmap.get("payload")));
                    r.setFrom(String.valueOf(vmap.get("from")));
                    return r;
                case ABONNEMENT:
                    Abonnement abx = new Abonnement();
                    abx.setAction(String.valueOf(vmap.get("action")));
                    abx.setCount(Long.parseLong(String.valueOf(vmap.get("count"))));
                    abx.setPriority(Integer.parseInt(String.valueOf(vmap.get("priority"))));
                    abx.setCounter(Long.parseLong(String.valueOf(vmap.get("counter"))));
                    abx.setType(String.valueOf(vmap.get("type")));
                    abx.setDateAbonnement((Date) vmap.get("dateAbonnement"));
                    abx.setDevise("USD");
                    abx.setEtat(String.valueOf(vmap.get("etat")));
                    abx.setAgent(String.valueOf(vmap.get("agent")));
                    abx.setNombreOperation(Double.parseDouble(String.valueOf(vmap.get("nombreOperation"))));
                    abx.setTypeAbonnement(String.valueOf(vmap.get("typeAbonnement")));
                    abx.setPayload(String.valueOf(vmap.get("payload")));
                    abx.setFrom(String.valueOf(vmap.get("from")));
                    return abx;

            }
        }
        return null;
    }

    private boolean isObject(String obj) {
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

    @Override
    public boolean willDecode(String message) {
        boolean result = false;
     //   System.out.println("incoming messg " + message);
        if (isObject(message)) {
            JsonReader reader = Json.createReader(new StringReader(message));
            JsonObject json = reader.readObject();
            if (json.containsKey("type")) {
                String type = json.getString("type");
                if (type.equals(Tables.CATEGORY.name())) {
                    vmap.put("uid", json.getString("uid"));
                    vmap.put("descritption", json.getString("descritption"));
                    vmap.put("type", json.getString("type"));
                    vmap.put("action", json.getString("action"));
                    vmap.put("count", json.getJsonNumber("count").longValue());
                    vmap.put("counter", json.getJsonNumber("counter").longValue());
                    if (json.containsKey("priority")) {
                        vmap.put("priority", json.getJsonNumber("priority").intValue());
                    } else {
                        vmap.put("priority", 0);
                    }
                    if (json.containsKey("payload")) {
                        vmap.put("payload", json.getString("payload"));
                    }
                    if (json.containsKey("from")) {
                        vmap.put("from", json.getString("from"));
                    }
                    result = true;
                } else if (type.equals(Tables.PRODUIT.name())) {
                    vmap.put("uid", json.getString("uid"));
                    vmap.put("codebar", json.getString("codebar"));
                    vmap.put("nomProduit", json.getString("nomProduit"));
                    vmap.put("marque", json.getString("marque"));
                    vmap.put("modele", json.getString("modele"));
                    vmap.put("couleur", json.getString("couleur"));
                    vmap.put("taille", json.getString("taille"));
                    vmap.put("methodeInventaire", json.getString("methodeInventaire"));

                    vmap.put("categoryId", new Category(json.getJsonObject("categoryId").getString("uid")));
                    vmap.put("type", json.getString("type"));
                    vmap.put("action", json.getString("action"));
                    vmap.put("count", json.getJsonNumber("count").longValue());
                    vmap.put("counter", json.getJsonNumber("counter").longValue());
                    if (json.containsKey("priority")) {
                        vmap.put("priority", json.getJsonNumber("priority").intValue());
                    } else {
                        vmap.put("priority", 0);
                    }
                    if (json.containsKey("payload")) {
                        vmap.put("payload", json.getString("payload"));
                    }
                    if (json.containsKey("from")) {
                        vmap.put("from", json.getString("from"));
                    }
                    result = true;
                } else if (type.equals(Tables.MESURE.name())) {

                    vmap.put("uid", json.getString("uid"));
                    vmap.put("description", json.getString("description"));
                    vmap.put("quantContenu", json.getJsonNumber("quantContenu").doubleValue());
                    JsonObject jso = json.getJsonObject("produitId");
                    Produit p = new Produit(jso.getString("uid"));
                    vmap.put("produitId", p);

                    vmap.put("type", json.getString("type"));
                    vmap.put("action", json.getString("action"));
                    vmap.put("count", json.getJsonNumber("count").longValue());
                    vmap.put("counter", json.getJsonNumber("counter").longValue());
                    if (json.containsKey("priority")) {
                        vmap.put("priority", json.getJsonNumber("priority").intValue());
                    } else {
                        vmap.put("priority", 0);
                    }
                    if (json.containsKey("payload")) {
                        vmap.put("payload", json.getString("payload"));
                    }
                    if (json.containsKey("from")) {
                        vmap.put("from", json.getString("from"));
                    }
                    result = true;
                } else if (type.equals(Tables.FOURNISSEUR.name())) {

                    vmap.put("uid", json.getString("uid"));
                    vmap.put("adresse", json.getString("adresse"));
                    vmap.put("identification", json.getString("identification"));
                    vmap.put("nomFourn", json.getString("nomFourn"));
                    vmap.put("phone", json.getString("phone"));

                    vmap.put("type", json.getString("type"));
                    vmap.put("action", json.getString("action"));
                    vmap.put("count", json.getJsonNumber("count").longValue());
                    vmap.put("counter", json.getJsonNumber("counter").longValue());
                    if (json.containsKey("priority")) {
                        vmap.put("priority", json.getJsonNumber("priority").intValue());
                    } else {
                        vmap.put("priority", 0);
                    }
                    if (json.containsKey("payload")) {
                        vmap.put("payload", json.getString("payload"));
                    }
                    if (json.containsKey("from")) {
                        vmap.put("from", json.getString("from"));
                    }
                    result = true;
                } else if (type.equals(Tables.LIVRAISON.name())) {
                    vmap.put("uid", json.getString("uid"));
                    vmap.put("libelle", json.containsKey("libelle") ? json.getString("libelle") : "");
                    vmap.put("numPiece", json.getString("numPiece"));
                    vmap.put("observation", json.containsKey("observation") ? json.getString("observation") : "");
                    vmap.put("payed", json.getJsonNumber("payed").doubleValue());
                    vmap.put("reduction", json.getJsonNumber("reduction").doubleValue());
                    vmap.put("reference", json.getString("reference"));
                    vmap.put("region", json.getString("region"));
                    vmap.put("remained", json.getJsonNumber("remained").doubleValue());
                    vmap.put("topay", json.getJsonNumber("topay").doubleValue());
                    vmap.put("toreceive", json.getJsonNumber("toreceive").doubleValue());
                    String s = json.getString("dateLivr");
                    if (!s.isEmpty()) {
                        vmap.put("dateLivr", s);
                    }
                    JsonObject jso = json.getJsonObject("fournId");
                    Fournisseur fssr = new Fournisseur(jso.getString("uid"));
                    vmap.put("fournId", fssr);

                    vmap.put("type", json.getString("type"));
                    vmap.put("action", json.getString("action"));
                    vmap.put("count", json.getJsonNumber("count").longValue());
                    vmap.put("counter", json.getJsonNumber("counter").longValue());
                    if (json.containsKey("priority")) {
                        vmap.put("priority", json.getJsonNumber("priority").intValue());
                    } else {
                        vmap.put("priority", 0);
                    }
                    if (json.containsKey("payload")) {
                        vmap.put("payload", json.getString("payload"));
                    }
                    if (json.containsKey("from")) {
                        vmap.put("from", json.getString("from"));
                    }
                    result = true;
                } else if (type.equals(Tables.STOCKER.name())) {

                    vmap.put("uid", json.getString("uid"));
                    vmap.put("libelle", json.containsKey("libelle") ? json.getString("libelle") : "");
                    vmap.put("region", json.getString("region"));
                    vmap.put("observation", json.containsKey("observation") ? json.getString("observation") : "");
                    vmap.put("numlot", json.getString("numlot"));
                    vmap.put("localisation", json.getString("localisation"));
                    vmap.put("coutAchat", json.getJsonNumber("coutAchat").doubleValue());
                    vmap.put("reduction", json.getJsonNumber("reduction").doubleValue());
                    vmap.put("prixAchatTotal", json.getJsonNumber("prixAchatTotal").doubleValue());
                    vmap.put("quantite", json.getJsonNumber("quantite").doubleValue());
                    vmap.put("stockAlerte", json.getJsonNumber("stockAlerte").doubleValue());
                    try {
                        if (json.containsKey("dateExpir")) {
                            String dateE = json.getString("dateExpir");
                            if (dateE != null && !dateE.isEmpty()) {
                                vmap.put("dateExpir", tools.Constants.DATE_ONLY_FORMAT.parse(dateE));
                            }
                        }
                        vmap.put("dateStocker", tools.Constants.DATE_HEURE_FORMAT.parse(json.getString("dateStocker")));
                    } catch (ParseException ex) {
                    }
                    JsonObject jso1 = json.getJsonObject("livraisId");
                    Livraison livr = new Livraison(jso1.getString("uid"));
                    vmap.put("livraisId", livr);
                    JsonObject jso2 = json.getJsonObject("mesureId");
                    Mesure m = new Mesure(jso2.getString("uid"));
                    vmap.put("mesureId", m);
                    JsonObject jso3 = json.getJsonObject("productId");
                    Produit pro = new Produit(jso3.getString("uid"));
                    vmap.put("productId", pro);

                    vmap.put("type", json.getString("type"));
                    vmap.put("action", json.getString("action"));
                    vmap.put("count", json.getJsonNumber("count").longValue());
                    vmap.put("counter", json.getJsonNumber("counter").longValue());
                    if (json.containsKey("priority")) {
                        vmap.put("priority", json.getJsonNumber("priority").intValue());
                    } else {
                        vmap.put("priority", 0);
                    }
                    if (json.containsKey("payload")) {
                        vmap.put("payload", json.getString("payload"));
                    }
                    if (json.containsKey("from")) {
                        vmap.put("from", json.getString("from"));
                    }
                    result = true;
                } else if (type.equals(Tables.DESTOCKER.name())) {

                    vmap.put("uid", json.getString("uid"));
                    vmap.put("libelle", json.containsKey("libelle") ? json.getString("libelle") : "");
                    vmap.put("coutAchat", json.getJsonNumber("coutAchat").doubleValue());
                    vmap.put("observation", json.containsKey("observation") ? json.getString("observation") : "");
                    vmap.put("numlot", json.getString("numlot"));
                    vmap.put("reference", json.getString("reference"));
                    vmap.put("region", json.getString("region"));
                    vmap.put("quantite", json.getJsonNumber("quantite").doubleValue());
                    vmap.put("destination", json.getString("destination"));
                    try {
                        vmap.put("dateDestockage", tools.Constants.DATE_HEURE_FORMAT.parse(json.getString("dateDestockage")));
                    } catch (ParseException ex) {
                        Logger.getLogger(BaseModelDecoder.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    JsonObject jso1 = json.getJsonObject("mesureId");
                    Mesure mz = new Mesure();
                    mz.setUid(jso1.getString("uid"));
                    vmap.put("mesureId", mz);
                    JsonObject jso3 = json.getJsonObject("productId");
                    Produit pro = new Produit(jso3.getString("uid"));
                    vmap.put("productId", pro);

                    vmap.put("type", json.getString("type"));
                    vmap.put("action", json.getString("action"));
                    vmap.put("count", json.getJsonNumber("count").longValue());
                    vmap.put("counter", json.getJsonNumber("counter").longValue());
                    if (json.containsKey("priority")) {
                        vmap.put("priority", json.getJsonNumber("priority").intValue());
                    } else {
                        vmap.put("priority", 0);
                    }
                    if (json.containsKey("payload")) {
                        vmap.put("payload", json.getString("payload"));
                    }
                    if (json.containsKey("from")) {
                        vmap.put("from", json.getString("from"));
                    }
                    result = true;
                } else if (type.equals(Tables.RECQUISITION.name())) {
                    vmap.put("uid", json.getString("uid"));

                    vmap.put("reference", json.getString("reference"));
                    vmap.put("observation", json.containsKey("observation") ? json.getString("observation") : "");
                    vmap.put("numlot", json.getString("numlot"));
                    vmap.put("region", json.getString("region"));
                    vmap.put("quantite", json.getJsonNumber("quantite").doubleValue());
                    vmap.put("coutAchat", json.getJsonNumber("coutAchat").doubleValue());
                    vmap.put("stockAlert", json.getJsonNumber("stockAlert").doubleValue());
                    try {
                        if (json.containsKey("dateExpiry")) {
                            String dateE = json.getString("dateExpiry");
                            if (!dateE.isEmpty() && !dateE.equalsIgnoreCase("null")) {
                                vmap.put("dateExpiry", tools.Constants.DATE_ONLY_FORMAT.parse(dateE));
                            }
                        }
                        vmap.put("date", tools.Constants.DATE_HEURE_FORMAT.parse(json.getString("date")));
                    } catch (ParseException ex) {

                    }
                    JsonObject jso1 = json.getJsonObject("mesureId");
                    Mesure mz = new Mesure();
                    mz.setUid(jso1.getString("uid"));
                    vmap.put("mesureId", mz);
                    JsonObject jso3 = json.getJsonObject("productId");
                    Produit pro = new Produit(jso3.getString("uid"));
                    vmap.put("productId", pro);

                    vmap.put("type", json.getString("type"));
                    vmap.put("action", json.getString("action"));
                    vmap.put("count", json.getJsonNumber("count").longValue());
                    vmap.put("counter", json.getJsonNumber("counter").longValue());
                    if (json.containsKey("priority")) {
                        vmap.put("priority", json.getJsonNumber("priority").intValue());
                    } else {
                        vmap.put("priority", 0);
                    }
                    if (json.containsKey("payload")) {
                        vmap.put("payload", json.getString("payload"));
                    }
                    if (json.containsKey("from")) {
                        vmap.put("from", json.getString("from"));
                    }
                    result = true;
                } else if (type.equals(Tables.PRIXDEVENTE.name())) {
                    vmap.put("uid", json.getString("uid"));

                    vmap.put("qmax", json.getJsonNumber("qmax").doubleValue());
                    vmap.put("qmin", json.getJsonNumber("qmin").doubleValue());
                    vmap.put("devise", json.getString("devise"));
                    vmap.put("prixUnitaire", json.getJsonNumber("prixUnitaire").doubleValue());
                    JsonObject jso1 = json.getJsonObject("mesureId");
                    Mesure mz = new Mesure(jso1.getString("uid"));
                    vmap.put("mesureId", mz);
                    JsonObject jso2 = json.getJsonObject("recquisitionId");
                    Recquisition req = new Recquisition(jso2.getString("uid"));
                    vmap.put("recquisitionId", req);

                    vmap.put("type", json.getString("type"));
                    vmap.put("action", json.getString("action"));
                    vmap.put("count", json.getJsonNumber("count").longValue());
                    vmap.put("counter", json.getJsonNumber("counter").longValue());
                    if (json.containsKey("priority")) {
                        vmap.put("priority", json.getJsonNumber("priority").intValue());
                    } else {
                        vmap.put("priority", 0);
                    }
                    if (json.containsKey("payload")) {
                        vmap.put("payload", json.getString("payload"));
                    }
                    if (json.containsKey("from")) {
                        vmap.put("from", json.getString("from"));
                    }
                    result = true;
                } else if (type.equals(Tables.CLIENT.name())) {
                    vmap.put("uid", json.getString("uid"));

                    vmap.put("adresse", json.getString("adresse"));
                    vmap.put("email", json.getString("email"));
                    vmap.put("typeClient", json.getString("typeClient"));
                    vmap.put("nomClient", json.getString("nomClient"));
                    JsonObject oo = json.getJsonObject("parentId");
                    if (oo != null) {
                        vmap.put("parentId", new Client(oo.getString("uid")));
                    } else {
                        vmap.put("parentId", json.getString("uid"));
                    }
                    vmap.put("phone", json.getString("phone"));

                    vmap.put("type", json.getString("type"));
                    vmap.put("action", json.getString("action"));
                    vmap.put("count", json.getJsonNumber("count").longValue());
                    vmap.put("counter", json.getJsonNumber("counter").longValue());
                    if (json.containsKey("priority")) {
                        vmap.put("priority", json.getJsonNumber("priority").intValue());
                    } else {
                        vmap.put("priority", 0);
                    }
                    if (json.containsKey("payload")) {
                        vmap.put("payload", json.getString("payload"));
                    }
                    if (json.containsKey("from")) {
                        vmap.put("from", json.getString("from"));
                    }
                    result = true;
                } else if (type.equals(Tables.VENTE.name())) {
                    vmap.put("uid", json.getJsonNumber("uid").intValue());
                    vmap.put("libelle", json.getString("libelle"));
                    vmap.put("latitude", json.getJsonNumber("latitude").doubleValue());
                    vmap.put("observation", json.getString("observation"));
                    vmap.put("longitude", json.getJsonNumber("longitude").doubleValue());
                    vmap.put("montantCdf", json.getJsonNumber("montantCdf").doubleValue());
                    vmap.put("montantDette", json.getJsonNumber("montantDette").doubleValue());
                    vmap.put("region", json.getString("region"));
                    vmap.put("montantUsd", json.getJsonNumber("montantUsd").doubleValue());
                    vmap.put("payment", json.getString("payment"));
                    vmap.put("reference", json.getString("reference"));
                    DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                    DateFormat format2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    try {
                        if (json.containsKey("echeance")) {
                            vmap.put("echeance", format.parse(json.getString("echeance")));
                        }
                        vmap.put("dateVente", format2.parse(json.getString("dateVente")));
                    } catch (ParseException ex) {
                    }
                    vmap.put("deviseDette", json.getString("deviseDette"));
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
                    vmap.put("clientId", clt);

                    vmap.put("type", json.getString("type"));
                    vmap.put("action", json.getString("action"));
                    vmap.put("count", json.getJsonNumber("count").longValue());
                    vmap.put("counter", json.getJsonNumber("counter").longValue());
                    if (json.containsKey("priority")) {
                        vmap.put("priority", json.getJsonNumber("priority").intValue());
                    } else {
                        vmap.put("priority", 0);
                    }
                    if (json.containsKey("payload")) {
                        vmap.put("payload", json.getString("payload"));
                    }
                    if (json.containsKey("from")) {
                        vmap.put("from", json.getString("from"));
                    }
                    result = true;
                } else if (type.equals(Tables.CLIENTORGANISATION.name())) {
                    vmap.put("uid", json.getString("uid"));

                    vmap.put("region", json.getString("region"));
                    vmap.put("adresse", json.getString("adresse"));
                    vmap.put("boitePostalOrganisation", json.getString("boitePostalOrganisation"));
                    vmap.put("domaineOrganisation", json.getString("domaineOrganisation"));
                    vmap.put("emailOrganisation", json.getString("emailOrganisation"));
                    vmap.put("nomOrganisation", json.getString("nomOrganisation"));
                    vmap.put("phoneOrganisation", json.getString("phoneOrganisation"));
                    vmap.put("rccmOrganisation", json.getString("rccmOrganisation"));
                    vmap.put("websiteOrganisation", json.getString("websiteOrganisation"));

                    vmap.put("type", json.getString("type"));
                    vmap.put("action", json.getString("action"));
                    vmap.put("count", json.getJsonNumber("count").longValue());
                    vmap.put("counter", json.getJsonNumber("counter").longValue());
                    if (json.containsKey("priority")) {
                        vmap.put("priority", json.getJsonNumber("priority").intValue());
                    } else {
                        vmap.put("priority", 0);
                    }
                    if (json.containsKey("payload")) {
                        vmap.put("payload", json.getString("payload"));
                    }
                    if (json.containsKey("from")) {
                        vmap.put("from", json.getString("from"));
                    }
                    result = true;
                } else if (type.equals(Tables.CLIENTAPPARTENIR.name())) {
                    vmap.put("uid", json.getString("uid"));
                    vmap.put("region", json.getString("region"));
                    try {
                        vmap.put("date", tools.Constants.dateFormater.parse(json.getString("date")));
                    } catch (ParseException ex) {
                        Logger.getLogger(JsonUtil.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    JsonObject jsoc = json.getJsonObject("clientId");
                    Client clt = new Client(jsoc.getString("uid"));
                    vmap.put("clientId", clt);
                    JsonObject jsoo = json.getJsonObject("clientOrganisationId");
                    ClientOrganisation clto = new ClientOrganisation(jsoo.getString("uid"));
                    vmap.put("clientOrganisationId", clto);
                    vmap.put("type", json.getString("type"));
                    vmap.put("action", json.getString("action"));
                    vmap.put("count", json.getJsonNumber("count").longValue());
                    vmap.put("counter", json.getJsonNumber("counter").longValue());
                    if (json.containsKey("priority")) {
                        vmap.put("priority", json.getJsonNumber("priority").intValue());
                    } else {
                        vmap.put("priority", 0);
                    }
                    if (json.containsKey("payload")) {
                        vmap.put("payload", json.getString("payload"));
                    }
                    if (json.containsKey("from")) {
                        vmap.put("from", json.getString("from"));
                    }
                    result = true;
                } else if (type.equals(Tables.LIGNEVENTE.name())) {
                    vmap.put("uid", json.getJsonNumber("uid").longValue());
                    vmap.put("clientId", json.getString("clientId"));
                    vmap.put("numlot", json.getString("numlot"));
                    vmap.put("prixUnit", json.getJsonNumber("prixUnit").doubleValue());
                    vmap.put("quantite", json.getJsonNumber("quantite").doubleValue());
                    vmap.put("montantCdf", json.getJsonNumber("montantCdf").doubleValue());
                    vmap.put("montantUsd", json.getJsonNumber("montantUsd").doubleValue());
                    JsonObject jso3 = json.getJsonObject("productId");
                    Produit pro = new Produit(jso3.getString("uid"));
                    vmap.put("productId", pro);
                    JsonObject jso = json.getJsonObject("mesureId");
                    Mesure m = new Mesure();
                    m.setUid(jso.getString("uid"));
                    vmap.put("mesureId", m);
                    JsonObject jso1 = json.getJsonObject("reference");
                    Vente v = new Vente(jso1.getJsonNumber("uid").intValue());
                    vmap.put("reference", v);

                    vmap.put("type", json.getString("type"));
                    vmap.put("action", json.getString("action"));
                    vmap.put("count", json.getJsonNumber("count").longValue());
                    vmap.put("counter", json.getJsonNumber("counter").longValue());
                    if (json.containsKey("priority")) {
                        vmap.put("priority", json.getJsonNumber("priority").intValue());
                    } else {
                        vmap.put("priority", 0);
                    }
                    if (json.containsKey("payload")) {
                        vmap.put("payload", json.getString("payload"));
                    }
                    if (json.containsKey("from")) {
                        vmap.put("from", json.getString("from"));
                    }
                    result = true;
                } else if (type.equals(Tables.RETOURMAGASIN.name())) {
                    vmap.put("uid", json.getString("uid"));

                    vmap.put("region", json.getString("region"));
                    vmap.put("prixVente", json.getJsonNumber("prixVente").doubleValue());
                    vmap.put("referenceVente", json.getString("referenceVente"));
                    vmap.put("motif", json.getString("motif"));
                    vmap.put("quantite", json.getJsonNumber("quantite").doubleValue());
                    try {
                        vmap.put("date", tools.Constants.dateFormater.parse(json.getString("date")));
                    } catch (ParseException ex) {
                        Logger.getLogger(JsonUtil.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    JsonObject jo = json.getJsonObject("ligneVenteId");
                    vmap.put("ligneVenteId", new LigneVente(jo.getJsonNumber("uid").longValue()));
                    JsonObject job = json.getJsonObject("recquisitionId");
                    vmap.put("clientId", new Client(job.getString("clientId")));
                    JsonObject jso = json.getJsonObject("mesureId");
                    vmap.put("mesureId", new Mesure(jso.getString("uid")));

                    vmap.put("type", json.getString("type"));
                    vmap.put("action", json.getString("action"));
                    vmap.put("count", json.getJsonNumber("count").longValue());
                    vmap.put("counter", json.getJsonNumber("counter").longValue());
                    if (json.containsKey("priority")) {
                        vmap.put("priority", json.getJsonNumber("priority").intValue());
                    } else {
                        vmap.put("priority", 0);
                    }
                    if (json.containsKey("payload")) {
                        vmap.put("payload", json.getString("payload"));
                    }
                    if (json.containsKey("from")) {
                        vmap.put("from", json.getString("from"));
                    }
                    result = true;
                } else if (type.equals(Tables.RETOURDEPOT.name())) {
                    vmap.put("uid", json.getString("uid"));

                    vmap.put("region", json.getString("region"));
                    vmap.put("coutAchat", json.getJsonNumber("coutAchat").doubleValue());
                    vmap.put("localisation", json.getString("localisation"));
                    vmap.put("motif", json.getString("motif"));
                    vmap.put("numlot", json.getString("numlot"));
                    vmap.put("quantite", json.getJsonNumber("quantite").doubleValue());
                    vmap.put("regionDest", json.getString("regionDest"));
                    vmap.put("regionProv", json.getString("regionProv"));
                    try {
                        vmap.put("date", tools.Constants.dateFormater.parse(json.getString("date")));
                    } catch (ParseException ex) {
                        Logger.getLogger(JsonUtil.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    JsonObject jo = json.getJsonObject("destockerId");
                    vmap.put("destockerId", new Destocker(jo.getString("uid")));
                    JsonObject job = json.getJsonObject("recquisitionId");
                    vmap.put("recquisitionId", new Recquisition(job.getString("uid")));
                    JsonObject jso = json.getJsonObject("mesureId");
                    vmap.put("mesureId", new Mesure(jso.getString("uid")));

                    vmap.put("type", json.getString("type"));
                    vmap.put("action", json.getString("action"));
                    vmap.put("count", json.getJsonNumber("count").longValue());
                    vmap.put("counter", json.getJsonNumber("counter").longValue());
                    if (json.containsKey("priority")) {
                        vmap.put("priority", json.getJsonNumber("priority").intValue());
                    } else {
                        vmap.put("priority", 0);
                    }
                    if (json.containsKey("payload")) {
                        vmap.put("payload", json.getString("payload"));
                    }
                    if (json.containsKey("from")) {
                        vmap.put("from", json.getString("from"));
                    }
                    result = true;
                } else if (type.equals(Tables.COMPTETRESOR.name())) {
                    vmap.put("uid", json.getString("uid"));
                    vmap.put("bankName", json.getString("bankName"));
                    vmap.put("intitule", json.getString("intitule"));
                    vmap.put("numeroCompte", json.getString("numeroCompte"));
                    vmap.put("region", json.getString("region"));
                    vmap.put("soldeMinimum", json.getJsonNumber("soldeMinimum").doubleValue());
                    vmap.put("typeCompte", json.getString("typeCompte"));
                    vmap.put("type", json.getString("type"));
                    vmap.put("action", json.getString("action"));
                    vmap.put("count", json.getJsonNumber("count").longValue());
                    vmap.put("counter", json.getJsonNumber("counter").longValue());
                    if (json.containsKey("priority")) {
                        vmap.put("priority", json.getJsonNumber("priority").intValue());
                    } else {
                        vmap.put("priority", 0);
                    }
                    if (json.containsKey("payload")) {
                        vmap.put("payload", json.getString("payload"));
                    }
                    if (json.containsKey("from")) {
                        vmap.put("from", json.getString("from"));
                    }
                    result = true;
                } else if (type.equals(Tables.TRAISORERIE.name())) {
                    vmap.put("uid", json.getString("uid"));
                    vmap.put("libelle", json.getString("libelle"));
                    vmap.put("mouvement", json.getString("mouvement"));
                    vmap.put("typeTresorerie", json.getString("typeTresorerie"));
                    vmap.put("montantCdf", json.getJsonNumber("montantCdf").doubleValue());
                    vmap.put("region", json.getString("region"));
                    if (json.containsKey("tresorId")) {
                        vmap.put("tresorId", new CompteTresor(json.getJsonObject("tresorId").getString("uid")));
                    }
                    vmap.put("montantUsd", json.getJsonNumber("montantUsd").doubleValue());
                    vmap.put("reference", json.getString("reference"));
                    DateFormat DATE_HEURE = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    try {
                        vmap.put("date", DATE_HEURE.parse(json.getString("date")));
                    } catch (ParseException ex) {
                        Logger.getLogger(BaseModelDecoder.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    vmap.put("type", json.getString("type"));
                    vmap.put("action", json.getString("action"));
                    vmap.put("count", json.getJsonNumber("count").longValue());
                    vmap.put("counter", json.getJsonNumber("counter").longValue());
                    if (json.containsKey("priority")) {
                        vmap.put("priority", json.getJsonNumber("priority").intValue());
                    } else {
                        vmap.put("priority", 0);
                    }
                    if (json.containsKey("payload")) {
                        vmap.put("payload", json.getString("payload"));
                    }
                    if (json.containsKey("from")) {
                        vmap.put("from", json.getString("from"));
                    }
                    result = true;
                } else if (type.equals(Tables.DEPENSE.name())) {
                    vmap.put("uid", json.getString("uid"));
                    vmap.put("nomDepense", json.getString("nomDepense"));
                    vmap.put("region", json.getString("region"));
                    vmap.put("type", json.getString("type"));
                    vmap.put("action", json.getString("action"));
                    vmap.put("count", json.getJsonNumber("count").longValue());
                    vmap.put("counter", json.getJsonNumber("counter").longValue());
                    if (json.containsKey("priority")) {
                        vmap.put("priority", json.getJsonNumber("priority").intValue());
                    } else {
                        vmap.put("priority", 0);
                    }
                    if (json.containsKey("payload")) {
                        vmap.put("payload", json.getString("payload"));
                    }
                    if (json.containsKey("from")) {
                        vmap.put("from", json.getString("from"));
                    }
                    result = true;
                } else if (type.equals(Tables.OPERATION.name())) {
                    vmap.put("uid", json.getString("uid"));
                    vmap.put("libelle", json.getString("libelle"));
                    vmap.put("mouvement", json.getString("mouvement"));
                    vmap.put("imputation", json.getString("imputation"));
                    vmap.put("montantCdf", json.getJsonNumber("montantCdf").doubleValue());
                    vmap.put("region", json.getString("region"));
                    vmap.put("montantUsd", json.getJsonNumber("montantUsd").doubleValue());
                    vmap.put("referenceOp", json.getString("referenceOp"));
                    if (json.containsKey("tresorId")) {
                        vmap.put("tresorId", new CompteTresor(json.getJsonObject("tresorId").getString("uid")));
                    }
                    if (json.containsKey("depenseId")) {
                        vmap.put("depenseId", new Depense(json.getJsonObject("depenseId").getString("uid")));
                    }
                    try {
                        vmap.put("date", tools.Constants.DATE_HEURE_FORMAT.parse(json.getString("date")));
                    } catch (ParseException ex) {
                        Logger.getLogger(BaseModelDecoder.class.getName()).log(Level.SEVERE, null, ex);
                    }
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
                    try {
                        t.setDate(tools.Constants.DATE_HEURE_FORMAT.parse(jso.getString("date")));
                    } catch (ParseException ex) {
                        Logger.getLogger(BaseModelDecoder.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    vmap.put("caisseOpId", t);
                    vmap.put("type", json.getString("type"));
                    vmap.put("action", json.getString("action"));
                    vmap.put("count", json.getJsonNumber("count").longValue());
                    vmap.put("counter", json.getJsonNumber("counter").longValue());
                    if (json.containsKey("priority")) {
                        vmap.put("priority", json.getJsonNumber("priority").intValue());
                    } else {
                        vmap.put("priority", 0);
                    }
                    if (json.containsKey("payload")) {
                        vmap.put("payload", json.getString("payload"));
                    }
                    if (json.containsKey("from")) {
                        vmap.put("from", json.getString("from"));
                    }
                    result = true;
                } else if (type.equals(Tables.ARETIRER.name())) {
                    vmap.put("uid", json.getString("uid"));
                    vmap.put("numlot", json.getString("numlot"));
                    vmap.put("prixVente", json.getJsonNumber("prixVente").doubleValue());
                    vmap.put("quantite", json.getJsonNumber("quantite").doubleValue());
                    vmap.put("referenceVente", json.getString("referenceVente"));
                    vmap.put("region", json.getString("region"));
                    vmap.put("status", json.getString("status"));
                    try {
                        vmap.put("date", tools.Constants.DATE_HEURE_FORMAT.parse(json.getString("date")));
                    } catch (ParseException ex) {
                        Logger.getLogger(JsonUtil.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    JsonObject jsoc = json.getJsonObject("clientId");
                    Client clt = new Client(jsoc.getString("uid"));
                    vmap.put("clientId", clt);
                    JsonObject jsol = json.getJsonObject("ligneVenteId");
                    vmap.put("ligneVenteId", new LigneVente(jsol.getJsonNumber("uid").longValue()));
                    JsonObject jsom = json.getJsonObject("mesureId");
                    vmap.put("mesureId", new Mesure(jsom.getString("uid")));

                    vmap.put("type", json.getString("type"));
                    vmap.put("action", json.getString("action"));
                    vmap.put("count", json.getJsonNumber("count").longValue());
                    vmap.put("counter", json.getJsonNumber("counter").longValue());
                    if (json.containsKey("priority")) {
                        vmap.put("priority", json.getJsonNumber("priority").intValue());
                    } else {
                        vmap.put("priority", 0);
                    }
                    if (json.containsKey("payload")) {
                        vmap.put("payload", json.getString("payload"));
                    }
                    if (json.containsKey("from")) {
                        vmap.put("from", json.getString("from"));
                    }
                    result = true;
                } else if (type.equals(Tables.FACTURE.name())) {
                    vmap.put("uid", json.getString("uid"));
                    try {
                        vmap.put("startDate", tools.Constants.DATE_ONLY_FORMAT.parse(json.getString("startDate")));

                        vmap.put("endDate", tools.Constants.DATE_ONLY_FORMAT.parse(json.getString("endDate")));
                    } catch (ParseException ex) {
                    }
                    vmap.put("numero", json.getString("numero"));
                    vmap.put("organisId", new ClientOrganisation(json.getJsonObject("organisId").getString("uid")));
                    vmap.put("payedamount", json.getJsonNumber("payedamount").doubleValue());
                    vmap.put("region", json.getString("region"));
                    vmap.put("status", json.getString("status"));
                    vmap.put("totalamount", json.getJsonNumber("totalamount").doubleValue());
                    vmap.put("type", json.getString("type"));
                    vmap.put("action", json.getString("action"));
                    vmap.put("count", json.getJsonNumber("count").longValue());
                    vmap.put("counter", json.getJsonNumber("counter").longValue());
                    if (json.containsKey("priority")) {
                        vmap.put("priority", json.getJsonNumber("priority").intValue());
                    } else {
                        vmap.put("priority", 0);
                    }
                    if (json.containsKey("payload")) {
                        vmap.put("payload", json.getString("payload"));
                    }
                    if (json.containsKey("from")) {
                        vmap.put("from", json.getString("from"));
                    }
                    result = true;
                } else if (type.equals(Tables.ABONNEMENT.name())) {
                    System.err.println("<<ABX>>");
                    vmap.put("etat", json.getString("etat"));
                    vmap.put("nombreOperation", json.getJsonNumber("nombreOperation").doubleValue());
                    vmap.put("typeAbonnement", json.getString("typeAbonnement"));
                    SimpleDateFormat DATE_ONLY = new SimpleDateFormat("yyyy-MM-dd");
                    try {
                        vmap.put("dateAbonnement", DATE_ONLY.parse(json.getString("dateAbonnement")));
                    } catch (ParseException ex) {
                        Logger.getLogger(BaseModelDecoder.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    vmap.put("agent", json.getString("agent"));
                    vmap.put("status", json.getString("status"));
                    vmap.put("type", json.getString("type"));
                    vmap.put("action", json.getString("action"));
                    vmap.put("count", json.getJsonNumber("count").longValue());
                    vmap.put("counter", json.getJsonNumber("counter").longValue());
                    if (json.containsKey("priority")) {
                        vmap.put("priority", json.getJsonNumber("priority").intValue());
                    } else {
                        vmap.put("priority", 0);
                    }
                    if (json.containsKey("payload")) {
                        vmap.put("payload", json.getString("payload"));
                    }
                    if (json.containsKey("from")) {
                        vmap.put("from", json.getString("from"));
                    }
                    result = true;
                }
            }
        } else {
            if (!message.isEmpty()) {
                vmap.put("type", data.helpers.Tables.BULKMODEL.name());
                vmap.put("object", message);
                result = true;
            }

        }
        return result;
    }

    @Override
    public void init(EndpointConfig config) {
        vmap = new HashMap<>();
    }

    @Override
    public void destroy() {

    }

   
}
