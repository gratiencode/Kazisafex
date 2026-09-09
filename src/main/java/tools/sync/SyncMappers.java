package tools.sync;

import data.Category;
import data.Client;
import data.CompteTresor;
import data.Destocker;
import data.Fournisseur;
import data.LigneVente;
import data.Livraison;
import data.Mesure;
import data.PrixDeVente;
import data.Produit;
import data.Recquisition;
import data.Stocker;
import data.Traisorerie;
import data.Vente;
import data.dto.CategoryUpsertDto;
import data.dto.ClientUpsertDto;
import data.dto.CompteTresorUpsertDto;
import data.dto.DestockerUpsertDto;
import data.dto.FournisseurUpsertDto;
import data.dto.LigneVenteUpsertDto;
import data.dto.LivraisonUpsertDto;
import data.dto.MesureUpsertDto;
import data.dto.PrixDeVenteUpsertDto;
import data.dto.ProduitUpsertDto;
import data.dto.RecquisitionUpsertDto;
import data.dto.StockerUpsertDto;
import data.dto.TraisorerieUpsertDto;
import data.dto.VenteUpsertDto;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Mappers entity -&gt; DTO upsync asymetrique. Toutes les classe sont statiques
 * et sans etat : la date est normalisee en String ISO pour traverser les
 * plateformes (Jackson JavaFX, Gson Android, JSON-B serveur) sans ambigurite.
 */
public final class SyncMappers {

    private SyncMappers() {
    }

    static String fmt(LocalDateTime value) {
        return value == null ? null : value.toString();
    }

    static String fmt(LocalDate value) {
        return value == null ? null : value.toString();
    }

    static String nz(Number value) {
        return value == null ? null : String.valueOf(value);
    }

    public static ClientUpsertDto toClientDto(Client c) {
        if (c == null) {
            return null;
        }
        ClientUpsertDto dto = new ClientUpsertDto();
        dto.setUid(c.getUid());
        dto.setNomClient(c.getNomClient());
        dto.setPhone(c.getPhone());
        dto.setEmail(c.getEmail());
        dto.setAdresse(c.getAdresse());
        dto.setTypeClient(c.getTypeClient());
        dto.setParentUid(c.getParentId() != null ? c.getParentId().getUid() : null);
        dto.setUpdatedAt(fmt(c.getUpdatedAt()));
        return dto;
    }

    public static ProduitUpsertDto toProduitDto(Produit p) {
        if (p == null) {
            return null;
        }
        ProduitUpsertDto dto = new ProduitUpsertDto();
        dto.setUid(p.getUid());
        dto.setCodebar(p.getCodebar());
        dto.setNomProduit(p.getNomProduit());
        dto.setMarque(p.getMarque());
        dto.setModele(p.getModele());
        dto.setTaille(p.getTaille());
        dto.setCouleur(p.getCouleur());
        dto.setMethodeInventaire(p.getMethodeInventaire());
        dto.setCategoryUid(p.getCategoryId() != null ? p.getCategoryId().getUid() : null);
        dto.setDateCreation(fmt(p.getDateCreation()));
        dto.setUpdatedAt(fmt(p.getUpdatedAt()));
        return dto;
    }

    public static MesureUpsertDto toMesureDto(Mesure m) {
        if (m == null) {
            return null;
        }
        MesureUpsertDto dto = new MesureUpsertDto();
        dto.setUid(m.getUid());
        dto.setDescription(m.getDescription());
        dto.setQuantContenu(m.getQuantContenu());
        dto.setProduitUid(m.getProduitId() != null ? m.getProduitId().getUid() : null);
        dto.setUpdatedAt(fmt(m.getUpdatedAt()));
        return dto;
    }

    public static CategoryUpsertDto toCategoryDto(Category c) {
        if (c == null) {
            return null;
        }
        CategoryUpsertDto dto = new CategoryUpsertDto();
        dto.setUid(c.getUid());
        dto.setDescritption(c.getDescritption());
        dto.setUpdatedAt(fmt(c.getUpdatedAt()));
        return dto;
    }

    public static FournisseurUpsertDto toFournisseurDto(Fournisseur f) {
        if (f == null) {
            return null;
        }
        FournisseurUpsertDto dto = new FournisseurUpsertDto();
        dto.setUid(f.getUid());
        dto.setNomFourn(f.getNomFourn());
        dto.setAdresse(f.getAdresse());
        dto.setIdentification(f.getIdentification());
        dto.setPhone(f.getPhone());
        dto.setUpdatedAt(fmt(f.getUpdatedAt()));
        return dto;
    }

    public static CompteTresorUpsertDto toCompteTresorDto(CompteTresor t) {
        if (t == null) {
            return null;
        }
        CompteTresorUpsertDto dto = new CompteTresorUpsertDto();
        dto.setUid(t.getUid());
        dto.setIntitule(t.getIntitule());
        dto.setTypeCompte(t.getTypeCompte());
        dto.setNumeroCompte(t.getNumeroCompte());
        dto.setBankName(t.getBankName());
        dto.setRegion(t.getRegion());
        dto.setSoldeMinimum(t.getSoldeMinimum());
        dto.setUpdatedAt(fmt(t.getUpdatedAt()));
        return dto;
    }

    public static TraisorerieUpsertDto toTraisorerieDto(Traisorerie t) {
        if (t == null) {
            return null;
        }
        TraisorerieUpsertDto dto = new TraisorerieUpsertDto();
        dto.setUid(t.getUid());
        dto.setTresorUid(t.getTresorId() != null ? t.getTresorId().getUid() : null);
        dto.setReference(t.getReference());
        dto.setRegion(t.getRegion());
        dto.setDate(fmt(t.getDate()));
        dto.setLibelle(t.getLibelle());
        dto.setMontantUsd(t.getMontantUsd());
        dto.setMontantCdf(t.getMontantCdf());
        dto.setSoldeCdf(t.getSoldeCdf());
        dto.setSoldeUsd(t.getSoldeUsd());
        dto.setMouvement(t.getMouvement());
        dto.setTypeTresorerie(t.getTypeTresorerie());
        dto.setUpdatedAt(fmt(t.getUpdatedAt()));
        return dto;
    }

    public static VenteUpsertDto toVenteDto(Vente v) {
        return toVenteDto(v, null);
    }

    public static VenteUpsertDto toVenteDto(Vente v, String transactionId) {
        if (v == null) {
            return null;
        }
        VenteUpsertDto dto = new VenteUpsertDto();
        dto.setUid(v.getUid() == null ? null : String.valueOf(v.getUid()));
        dto.setClientUid(v.getClientId() != null ? v.getClientId().getUid() : null);
        dto.setReference(v.getReference());
        dto.setRegion(v.getRegion());
        dto.setDateVente(fmt(v.getDateVente()));
        dto.setMontantUsd(v.getMontantUsd());
        dto.setMontantCdf(v.getMontantCdf());
        dto.setMontantDette(v.getMontantDette());
        dto.setDeviseDette(v.getDeviseDette());
        dto.setPayment(v.getPayment());
        dto.setLibelle(v.getLibelle());
        dto.setObservation(v.getObservation());
        dto.setEcheance(fmt(v.getEcheance()));
        dto.setLatitude(v.getLatitude());
        dto.setLongitude(v.getLongitude());
        dto.setTransactionId(transactionId);
        dto.setUpdatedAt(fmt(v.getUpdatedAt()));
        return dto;
    }

    public static LigneVenteUpsertDto toLigneVenteDto(LigneVente lv) {
        if (lv == null) {
            return null;
        }
        LigneVenteUpsertDto dto = new LigneVenteUpsertDto();
        dto.setUid(lv.getUid() == null ? null : String.valueOf(lv.getUid()));
        dto.setVenteUid(lv.getReference() != null && lv.getReference().getUid() != null
                ? String.valueOf(lv.getReference().getUid()) : null);
        dto.setClientUid(lv.getClientId());
        dto.setProduitUid(lv.getProductId() != null ? lv.getProductId().getUid() : null);
        dto.setMesureUid(lv.getMesureId() != null ? lv.getMesureId().getUid() : null);
        dto.setQuantite(lv.getQuantite());
        dto.setMontantUsd(lv.getMontantUsd());
        dto.setMontantCdf(lv.getMontantCdf());
        dto.setPrixUnit(lv.getPrixUnit());
        dto.setCoutAchat(lv.getCoutAchat());
        dto.setNumlot(lv.getNumlot());
        dto.setUpdatedAt(fmt(lv.getUpdatedAt()));
        return dto;
    }

    public static LivraisonUpsertDto toLivraisonDto(Livraison l) {
        if (l == null) {
            return null;
        }
        LivraisonUpsertDto dto = new LivraisonUpsertDto();
        dto.setUid(l.getUid());
        dto.setFournisseurUid(l.getFournId() != null ? l.getFournId().getUid() : null);
        dto.setNumPiece(l.getNumPiece());
        dto.setDateLivr(fmt(l.getDateLivr()));
        dto.setReference(l.getReference());
        dto.setLibelle(l.getLibelle());
        dto.setRegion(l.getRegion());
        dto.setObservation(l.getObservation());
        dto.setReduction(l.getReduction());
        dto.setTopay(l.getTopay());
        dto.setPayed(l.getPayed());
        dto.setRemained(l.getRemained());
        dto.setToreceive(l.getToreceive());
        dto.setUpdatedAt(fmt(l.getUpdatedAt()));
        return dto;
    }

    public static StockerUpsertDto toStockerDto(Stocker s) {
        if (s == null) {
            return null;
        }
        StockerUpsertDto dto = new StockerUpsertDto();
        dto.setUid(s.getUid());
        dto.setLivraisonUid(s.getLivraisId() != null ? s.getLivraisId().getUid() : null);
        dto.setProduitUid(s.getProductId() != null ? s.getProductId().getUid() : null);
        dto.setMesureUid(s.getMesureId() != null ? s.getMesureId().getUid() : null);
        dto.setRegion(s.getRegion());
        dto.setDateStocker(fmt(s.getDateStocker()));
        dto.setDateExpir(fmt(s.getDateExpir()));
        dto.setCoutAchat(s.getCoutAchat());
        dto.setReduction(s.getReduction());
        dto.setStockAlerte(s.getStockAlerte());
        dto.setQuantite(s.getQuantite());
        dto.setPrixAchatTotal(s.getPrixAchatTotal());
        dto.setPrixVenteEstime(s.getPrixVenteEstime());
        dto.setNumlot(s.getNumlot());
        dto.setLibelle(s.getLibelle());
        dto.setLocalisation(s.getLocalisation());
        dto.setObservation(s.getObservation());
        dto.setUpdatedAt(fmt(s.getUpdatedAt()));
        return dto;
    }

    public static DestockerUpsertDto toDestockerDto(Destocker d) {
        if (d == null) {
            return null;
        }
        DestockerUpsertDto dto = new DestockerUpsertDto();
        dto.setUid(d.getUid());
        dto.setProduitUid(d.getProductId() != null ? d.getProductId().getUid() : null);
        dto.setMesureUid(d.getMesureId() != null ? d.getMesureId().getUid() : null);
        dto.setRegion(d.getRegion());
        dto.setDateDestockage(fmt(d.getDateDestockage()));
        dto.setReference(d.getReference());
        dto.setDestination(d.getDestination());
        dto.setCoutAchat(d.getCoutAchat());
        dto.setQuantite(d.getQuantite());
        dto.setLibelle(d.getLibelle());
        dto.setNumlot(d.getNumlot());
        dto.setObservation(d.getObservation());
        dto.setUpdatedAt(fmt(d.getUpdatedAt()));
        return dto;
    }

    public static RecquisitionUpsertDto toRecquisitionDto(Recquisition r) {
        if (r == null) {
            return null;
        }
        RecquisitionUpsertDto dto = new RecquisitionUpsertDto();
        dto.setUid(r.getUid());
        dto.setProduitUid(r.getProductId() != null ? r.getProductId().getUid() : null);
        dto.setMesureUid(r.getMesureId() != null ? r.getMesureId().getUid() : null);
        dto.setRegion(r.getRegion());
        dto.setDate(fmt(r.getDate()));
        dto.setReference(r.getReference());
        dto.setObservation(r.getObservation());
        dto.setQuantite(r.getQuantite());
        dto.setCoutAchat(r.getCoutAchat());
        dto.setDateExpiry(fmt(r.getDateExpiry()));
        dto.setStockAlert(r.getStockAlert());
        dto.setNumlot(r.getNumlot());
        dto.setUpdatedAt(fmt(r.getUpdatedAt()));
        return dto;
    }

    public static PrixDeVenteUpsertDto toPrixDeVenteDto(PrixDeVente pv) {
        if (pv == null) {
            return null;
        }
        PrixDeVenteUpsertDto dto = new PrixDeVenteUpsertDto();
        dto.setUid(pv.getUid());
        dto.setMesureUid(pv.getMesureId() != null ? pv.getMesureId().getUid() : null);
        dto.setRecquisitionUid(pv.getRecquisitionId() != null ? pv.getRecquisitionId().getUid() : null);
        dto.setQmin(pv.getQmin());
        dto.setQmax(pv.getQmax());
        dto.setPrixUnitaire(pv.getPrixUnitaire());
        dto.setDevise(pv.getDevise());
        dto.setPourcentParCunit(pv.getPourcentParCunit());
        dto.setUpdatedAt(fmt(pv.getUpdatedAt()));
        return dto;
    }
}