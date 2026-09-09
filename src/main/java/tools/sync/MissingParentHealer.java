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
import data.ProduitHelper;
import data.Recquisition;
import data.Stocker;
import data.Traisorerie;
import data.Vente;
import data.dto.SyncErrorResponse;
import data.network.Kazisafe;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import delegates.CategoryDelegate;
import delegates.ClientDelegate;
import delegates.CompteTresorDelegate;
import delegates.DestockerDelegate;
import delegates.FournisseurDelegate;
import delegates.LigneVenteDelegate;
import delegates.LivraisonDelegate;
import delegates.MesureDelegate;
import delegates.PrixDeVenteDelegate;
import delegates.ProduitDelegate;
import delegates.RecquisitionDelegate;
import delegates.StockerDelegate;
import delegates.TraisorerieDelegate;
import delegates.VenteDelegate;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import retrofit2.Response;
import tools.SyncLogger;

/**
 * Auto-healing des parents manquants (MISSING_PARENT).
 *
 * Quand le serveur refuse une mutation enfant (vente, ligne, stock, prix...)
 * parce qu'un parent n'existe pas encore, ce moteur:
 *  1. recupere le parent dans la base locale via les delegates,
 *  2. pousse d'abord ses propres parents (recursif, ordre topologique),
 *  3. repousse le parent : DTO upsync asymetrique d'abord, puis repli
 *     (fallback) sur les endpoints legacy tant que le serveur n'a pas les
 *     nouveaux endpoints /dto,
 *  4. renvoie true pour que l'appelant rejoue l'enfant dans le meme flux.
 *
 * Gardes anti-recursion: profondeur maximale {@link #MAX_DEPTH} + cache ThreadLocal
 * des parents deja pousses avec succes dans la chaine courante.
 */
public final class MissingParentHealer {

    private static final int MAX_DEPTH = 5;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ThreadLocal<Set<String>> PUSHED = new ThreadLocal<Set<String>>() {
        @Override
        protected Set<String> initialValue() {
            return new HashSet<>();
        }
    };

    private static final MissingParentHealer INSTANCE = new MissingParentHealer();

    private MissingParentHealer() {
    }

    public static MissingParentHealer getInstance() {
        return INSTANCE;
    }

    /**
     * @return true si le parent manquant a ete pousse (ou deja pousse dans cette
     * chaine), false si rien n'a pu etre fait (parent introuvable localement,
     * type non gerable, echec reseau).
     */
    public boolean heal(Kazisafe kazisafe, SyncErrorResponse error) {
        if (kazisafe == null || error == null || !error.isMissingParent()) {
            return false;
        }
        PUSHED.get().clear();
        return healByType(kazisafe, error.getMissingType(), error.getMissingUid(), error, 0);
    }

    /**
     * Pousse directement un client deja en memoire (brouillon de vente cree a
     * l'ecran avant persistance locale), sans lookup delegate. Repli utilise
     * quand {@link #heal} echoue a retrouver le client dans la base locale.
     */
    public boolean healClientEntity(Kazisafe kazisafe, Client c) {
        if (kazisafe == null || c == null) {
            return false;
        }
        PUSHED.get().clear();
        if (c.getParentId() != null && !alreadyPushed(c.getParentId().getUid())) {
            healClient(kazisafe, c.getParentId().getUid(), 1);
        }
        if (alreadyPushed(c.getUid())) {
            return true;
        }
        return pushClient(kazisafe, c) && markPushed("CLIENT", c.getUid());
    }

    /**
     * Pousse directement un compte de tresorerie deja en memoire (brouillon de
     * vente sans caisse persistee localement). Repli de {@link #heal}.
     */
    public boolean healCompteTresorEntity(Kazisafe kazisafe, CompteTresor ct) {
        if (kazisafe == null || ct == null) {
            return false;
        }
        PUSHED.get().clear();
        if (alreadyPushed(ct.getUid())) {
            return true;
        }
        return pushCompteTresor(kazisafe, ct) && markPushed("COMPTETRESOR", ct.getUid());
    }

    // ---------------------------------------------------------------------
    // resolution + dispatch
    // ---------------------------------------------------------------------

    private boolean healByType(Kazisafe kazisafe, String type, String uid, SyncErrorResponse ctx, int depth) {
        if (depth > MAX_DEPTH) {
            SyncLogger.getInstance().logMessage(context(type, uid),
                    "profondeur max atteinte, healing abandonne (ref circulaire possible)");
            return true;
        }
        if (uid == null || uid.isBlank() || "null".equals(uid)) {
            return resolveViaContext(kazisafe, type, ctx, depth);
        }
        switch (type == null ? "" : type) {
            case "CLIENT" -> {
                return healClient(kazisafe, uid, depth);
            }
            case "PRODUIT" -> {
                return healProduit(kazisafe, uid, depth);
            }
            case "MESURE" -> {
                return healMesure(kazisafe, uid, depth);
            }
            case "COMPTETRESOR" -> {
                return healCompteTresor(kazisafe, uid, depth);
            }
            case "CATEGORY" -> {
                return healCategory(kazisafe, uid, depth);
            }
            case "FOURNISSEUR" -> {
                return healFournisseur(kazisafe, uid, depth);
            }
            case "TRAISORERIE" -> {
                return healTraisorerie(kazisafe, uid, depth);
            }
            case "VENTE" -> {
                return healVente(kazisafe, uid, depth);
            }
            case "LIGNEVENTE" -> {
                return healLigneVente(kazisafe, uid, depth);
            }
            case "LIVRAISON" -> {
                return healLivraison(kazisafe, uid, depth);
            }
            case "STOCKER" -> {
                return healStocker(kazisafe, uid, depth);
            }
            case "DESTOCKER" -> {
                return healDestocker(kazisafe, uid, depth);
            }
            case "RECQUISITION" -> {
                return healRecquisition(kazisafe, uid, depth);
            }
            case "PRIXDEVENTE" -> {
                return healPrixDeVente(kazisafe, uid, depth);
            }
            default -> {
                SyncLogger.getInstance().logMessage(context(type, uid),
                        "type de parent non gerable, retry sans healing");
                return false;
            }
        }
    }

    /** Les erreurs de vente peuvent nommer la ligne au lieu du produit. */
    private boolean resolveViaContext(Kazisafe kazisafe, String type, SyncErrorResponse ctx, int depth) {
        String produitUid = ctx != null ? ctx.getProduitUid() : null;
        if (produitUid != null && !produitUid.isBlank() && "PRODUIT".equals(type)) {
            return healProduit(kazisafe, produitUid, depth);
        }
        String ligneUid = ctx != null ? ctx.getLigneUid() : null;
        if (ligneUid != null && !ligneUid.isBlank()) {
            try {
                LigneVente ligne = LigneVenteDelegate.findLigneVente(Long.parseLong(ligneUid));
                if (ligne != null && ligne.getProductId() != null) {
                    if ("PRODUIT".equals(type)) {
                        return healProduit(kazisafe, ligne.getProductId().getUid(), depth);
                    }
                    if ("MESURE".equals(type) && ligne.getMesureId() != null) {
                        return healMesure(kazisafe, ligne.getMesureId().getUid(), depth);
                    }
                }
            } catch (Exception e) {
                SyncLogger.getInstance().log(e, "MissingParentHealer.resolveViaContext");
            }
        }
        SyncLogger.getInstance().logMessage(context(type, null), "pas d'uid de parent exploitable");
        return false;
    }

    // ---------------------------------------------------------------------
    // healers par type
    // ---------------------------------------------------------------------

    private boolean healClient(Kazisafe kazisafe, String uid, int depth) {
        Client c = ClientDelegate.findClient(uid);
        if (c == null) {
            SyncLogger.getInstance().logMessage(context("CLIENT", uid), "introuvable localement");
            return false;
        }
        if (c.getParentId() != null && !alreadyPushed(c.getParentId().getUid())) {
            healClient(kazisafe, c.getParentId().getUid(), depth + 1);
        }
        if (alreadyPushed(uid)) {
            return true;
        }
        return pushClient(kazisafe, c) && markPushed("CLIENT", uid);
    }

    private boolean healProduit(Kazisafe kazisafe, String uid, int depth) {
        Produit p = ProduitDelegate.findProduit(uid);
        if (p == null) {
            SyncLogger.getInstance().logMessage(context("PRODUIT", uid), "introuvable localement");
            return false;
        }
        if (p.getCategoryId() != null && !alreadyPushed(p.getCategoryId().getUid())) {
            healCategory(kazisafe, p.getCategoryId().getUid(), depth + 1);
        }
        if (alreadyPushed(uid)) {
            return true;
        }
        boolean ok = pushProduit(kazisafe, p) && markPushed("PRODUIT", uid);
        if (ok) {
            pushMesuresOf(kazisafe, p, depth);
        }
        return ok;
    }

    private void pushMesuresOf(Kazisafe kazisafe, Produit p, int depth) {
        try {
            List<Mesure> mesures = p.getMesureList() != null && !p.getMesureList().isEmpty()
                    ? p.getMesureList() : MesureDelegate.findMesureByProduit(p.getUid());
            if (mesures == null) {
                return;
            }
            for (Mesure m : mesures) {
                if (m == null || alreadyPushed(m.getUid())) {
                    continue;
                }
                if (pushMesure(kazisafe, m)) {
                    markPushed("MESURE", m.getUid());
                }
            }
        } catch (Exception e) {
            SyncLogger.getInstance().log(e, "MissingParentHealer.pushMesuresOf");
        }
    }

    private boolean healMesure(Kazisafe kazisafe, String uid, int depth) {
        Mesure m = MesureDelegate.findMesure(uid);
        if (m == null) {
            SyncLogger.getInstance().logMessage(context("MESURE", uid), "introuvable localement");
            return false;
        }
        if (m.getProduitId() != null && !alreadyPushed(m.getProduitId().getUid())) {
            healProduit(kazisafe, m.getProduitId().getUid(), depth + 1);
        }
        if (alreadyPushed(uid)) {
            return true;
        }
        if (m.getDescription() == null || m.getDescription().isBlank()) {
            try {
                retrofit2.Response<Mesure> full = kazisafe.syncMesureFull(uid).execute();
                if (full.isSuccessful() && full.body() != null) {
                    SyncLogger.getInstance().logMessage(context("MESURE", uid),
                            "full downsync avant repush, description="
                                    + full.body().getDescription());
                    m = full.body();
                }
            } catch (Exception e) {
                SyncLogger.getInstance().log(e, context("MESURE", uid) + ".fullDownsync");
            }
            MesureDelegate.saveMesure(m);
            retrofit2.Response<Mesure> rep = null;
            try {
                rep = kazisafe
                        .syncMesureDto(SyncMappers.toMesureDto(m)).execute();
            } catch (IOException e) {
                SyncLogger.getInstance().log(e, context("MESURE", uid) + ".repush");
            }
            if (rep != null && rep.isSuccessful() && rep.body() != null
                    && (rep.body().getDescription() == null || rep.body().getDescription().isBlank())) {
                SyncLogger.getInstance().logMessage(context("MESURE", uid),
                        "repush ok mais description toujours null, fallback description=Piece");
                m.setDescription("Piece");
                MesureDelegate.saveMesure(m);
                return pushMesure(kazisafe, m) && markPushed("MESURE", uid);
            }
            if (rep == null || !rep.isSuccessful()) {
                SyncLogger.getInstance().logMessage(context("MESURE", uid),
                        "repush echoue" + (rep == null ? "" : " http " + rep.code())
                                + ", fallback description=Piece");
                m.setDescription("Piece");
                MesureDelegate.saveMesure(m);
                return pushMesure(kazisafe, m) && markPushed("MESURE", uid);
            }
            pushMesureMigrate(rep.body(), uid, context("MESURE", uid));
            return true && markPushed("MESURE", uid);
        }
        return pushMesure(kazisafe, m) && markPushed("MESURE", uid);
    }

    private boolean healCompteTresor(Kazisafe kazisafe, String uid, int depth) {
        CompteTresor ct = CompteTresorDelegate.findCompteTresor(uid);
        if (ct == null) {
            SyncLogger.getInstance().logMessage(context("COMPTETRESOR", uid), "introuvable localement");
            return false;
        }
        if (alreadyPushed(uid)) {
            return true;
        }
        return pushCompteTresor(kazisafe, ct) && markPushed("COMPTETRESOR", uid);
    }

    private boolean healCategory(Kazisafe kazisafe, String uid, int depth) {
        Category cat = CategoryDelegate.findCategory(uid);
        if (cat == null) {
            SyncLogger.getInstance().logMessage(context("CATEGORY", uid), "introuvable localement");
            return false;
        }
        if (alreadyPushed(uid)) {
            return true;
        }
        return pushCategory(kazisafe, cat) && markPushed("CATEGORY", uid);
    }

    private boolean healFournisseur(Kazisafe kazisafe, String uid, int depth) {
        Fournisseur f = FournisseurDelegate.findFournisseur(uid);
        if (f == null) {
            SyncLogger.getInstance().logMessage(context("FOURNISSEUR", uid), "introuvable localement");
            return false;
        }
        if (alreadyPushed(uid)) {
            return true;
        }
        return pushFournisseur(kazisafe, f) && markPushed("FOURNISSEUR", uid);
    }

    private boolean healTraisorerie(Kazisafe kazisafe, String uid, int depth) {
        Traisorerie t = TraisorerieDelegate.findTraisorerie(uid);
        if (t == null) {
            SyncLogger.getInstance().logMessage(context("TRAISORERIE", uid), "introuvable localement");
            return false;
        }
        if (t.getTresorId() != null && !alreadyPushed(t.getTresorId().getUid())) {
            healCompteTresor(kazisafe, t.getTresorId().getUid(), depth + 1);
        }
        if (alreadyPushed(uid)) {
            return true;
        }
        return pushTraisorerie(kazisafe, t) && markPushed("TRAISORERIE", uid);
    }

    private boolean healVente(Kazisafe kazisafe, String uid, int depth) {
        try {
            Vente v = VenteDelegate.findVente(Integer.parseInt(uid));
            if (v == null) {
                SyncLogger.getInstance().logMessage(context("VENTE", uid), "introuvable localement");
                return false;
            }
            if (v.getClientId() != null && !alreadyPushed(v.getClientId().getUid())) {
                healClient(kazisafe, v.getClientId().getUid(), depth + 1);
            }
            if (alreadyPushed(uid)) {
                return true;
            }
            return pushVente(kazisafe, v) && markPushed("VENTE", uid);
        } catch (Exception e) {
            SyncLogger.getInstance().log(e, "MissingParentHealer.healVente.parseUid");
            return false;
        }
    }

    private boolean healLigneVente(Kazisafe kazisafe, String uid, int depth) {
        try {
            LigneVente lv = LigneVenteDelegate.findLigneVente(Long.parseLong(uid));
            if (lv == null) {
                SyncLogger.getInstance().logMessage(context("LIGNEVENTE", uid), "introuvable localement");
                return false;
            }
            if (lv.getReference() != null && !alreadyPushed(lv.getReference().getUid().toString())) {
                healVente(kazisafe, lv.getReference().getUid().toString(), depth + 1);
            }
            if (lv.getProductId() != null && !alreadyPushed(lv.getProductId().getUid())) {
                healProduit(kazisafe, lv.getProductId().getUid(), depth + 1);
            }
            if (lv.getMesureId() != null && !alreadyPushed(lv.getMesureId().getUid())) {
                healMesure(kazisafe, lv.getMesureId().getUid(), depth + 1);
            }
            if (alreadyPushed(uid)) {
                return true;
            }
            return pushLigneVente(kazisafe, lv) && markPushed("LIGNEVENTE", uid);
        } catch (Exception e) {
            SyncLogger.getInstance().log(e, "MissingParentHealer.healLigneVente.parseUid");
            return false;
        }
    }

    private boolean healLivraison(Kazisafe kazisafe, String uid, int depth) {
        Livraison l = LivraisonDelegate.findLivraison(uid);
        if (l == null) {
            SyncLogger.getInstance().logMessage(context("LIVRAISON", uid), "introuvable localement");
            return false;
        }
        if (l.getFournId() != null && !alreadyPushed(l.getFournId().getUid())) {
            healFournisseur(kazisafe, l.getFournId().getUid(), depth + 1);
        }
        if (alreadyPushed(uid)) {
            return true;
        }
        return pushLivraison(kazisafe, l) && markPushed("LIVRAISON", uid);
    }

    private boolean healStocker(Kazisafe kazisafe, String uid, int depth) {
        Stocker s = StockerDelegate.findStocker(uid);
        if (s == null) {
            SyncLogger.getInstance().logMessage(context("STOCKER", uid), "introuvable localement");
            return false;
        }
        if (s.getLivraisId() != null && !alreadyPushed(s.getLivraisId().getUid())) {
            healLivraison(kazisafe, s.getLivraisId().getUid(), depth + 1);
        }
        if (s.getProductId() != null && !alreadyPushed(s.getProductId().getUid())) {
            healProduit(kazisafe, s.getProductId().getUid(), depth + 1);
        }
        if (s.getMesureId() != null && !alreadyPushed(s.getMesureId().getUid())) {
            healMesure(kazisafe, s.getMesureId().getUid(), depth + 1);
        }
        if (alreadyPushed(uid)) {
            return true;
        }
        return pushStocker(kazisafe, s) && markPushed("STOCKER", uid);
    }

    private boolean healDestocker(Kazisafe kazisafe, String uid, int depth) {
        Destocker d = DestockerDelegate.findDestocker(uid);
        if (d == null) {
            SyncLogger.getInstance().logMessage(context("DESTOCKER", uid), "introuvable localement");
            return false;
        }
        if (d.getProductId() != null && !alreadyPushed(d.getProductId().getUid())) {
            healProduit(kazisafe, d.getProductId().getUid(), depth + 1);
        }
        if (d.getMesureId() != null && !alreadyPushed(d.getMesureId().getUid())) {
            healMesure(kazisafe, d.getMesureId().getUid(), depth + 1);
        }
        if (alreadyPushed(uid)) {
            return true;
        }
        return pushDestocker(kazisafe, d) && markPushed("DESTOCKER", uid);
    }

    private boolean healRecquisition(Kazisafe kazisafe, String uid, int depth) {
        Recquisition r = RecquisitionDelegate.findRecquisition(uid);
        if (r == null) {
            SyncLogger.getInstance().logMessage(context("RECQUISITION", uid), "introuvable localement");
            return false;
        }
        if (r.getProductId() != null && !alreadyPushed(r.getProductId().getUid())) {
            healProduit(kazisafe, r.getProductId().getUid(), depth + 1);
        }
        if (r.getMesureId() != null && !alreadyPushed(r.getMesureId().getUid())) {
            healMesure(kazisafe, r.getMesureId().getUid(), depth + 1);
        }
        if (alreadyPushed(uid)) {
            return true;
        }
        return pushRecquisition(kazisafe, r) && markPushed("RECQUISITION", uid);
    }

    private boolean healPrixDeVente(Kazisafe kazisafe, String uid, int depth) {
        PrixDeVente pv = PrixDeVenteDelegate.findPrixDeVente(uid);
        if (pv == null) {
            SyncLogger.getInstance().logMessage(context("PRIXDEVENTE", uid), "introuvable localement");
            return false;
        }
        if (pv.getRecquisitionId() != null && !alreadyPushed(pv.getRecquisitionId().getUid())) {
            healRecquisition(kazisafe, pv.getRecquisitionId().getUid(), depth + 1);
        }
        if (pv.getMesureId() != null && !alreadyPushed(pv.getMesureId().getUid())) {
            healMesure(kazisafe, pv.getMesureId().getUid(), depth + 1);
        }
        if (alreadyPushed(uid)) {
            return true;
        }
        return pushPrixDeVente(kazisafe, pv) && markPushed("PRIXDEVENTE", uid);
    }

    // ---------------------------------------------------------------------
    // push : DTO d'abord, puis repli legacy tant que le serveur ne propose pas
    // ---------------------------------------------------------------------

    private boolean pushClient(Kazisafe kazisafe, Client c) {
        String ctxt = "MissingParentHealer.pushClient";
        if (push(kazisafe, ctxt + ".dto", kazisafe.syncClientDto(SyncMappers.toClientDto(c)))) {
            return true;
        }
        try {
            String parentUid = c.getParentId() != null ? c.getParentId().getUid() : null;
            Response<Client> legacy = kazisafe.saveByForm(c.getUid(), c.getNomClient(), c.getPhone(),
                    c.getTypeClient(), c.getEmail(), c.getAdresse(), parentUid).execute();
            if (legacy.isSuccessful()) {
                SyncLogger.getInstance().logMessage(ctxt, "repli legacy saveByForm OK");
                return true;
            }
            SyncLogger.getInstance().logMessage(ctxt, "repli legacy echec http " + legacy.code());
            return false;
        } catch (Exception e) {
            SyncLogger.getInstance().log(e, ctxt + ".legacy");
            return false;
        }
    }

    private boolean pushProduit(Kazisafe kazisafe, Produit p) {
        String ctxt = "MissingParentHealer.pushProduit";
        if (pushProduitDto(kazisafe, p)) {
            return true;
        }
        try {
            byte[] imageBytes = p.getImage();
            if (imageBytes == null) {
                try (InputStream is = MissingParentHealer.class.getResourceAsStream("/icons/gallery.png")) {
                    if (is != null) {
                        imageBytes = is.readAllBytes();
                    }
                } catch (Exception ignored) {
                    SyncLogger.getInstance().log(ignored, ctxt + ".defaultImage");
                }
            }
            String base64Image = imageBytes != null ? Base64.getEncoder().encodeToString(imageBytes) : "";
            ProduitHelper ph = new ProduitHelper();
            ph.setUid(p.getUid());
            ph.setCategoryId(p.getCategoryId() != null ? p.getCategoryId().getUid() : null);
            ph.setCodebar(p.getCodebar());
            ph.setCouleur(p.getCouleur());
            ph.setMarque(p.getMarque());
            ph.setModele(p.getModele());
            ph.setNomProduit(p.getNomProduit());
            ph.setImage(base64Image.isEmpty() ? null : "data:image/jpeg;base64," + base64Image);
            ph.setTaille(p.getTaille());
            ph.setMethodeInventaire(p.getMethodeInventaire());
            ph.setMesureList(MesureDelegate.findMesureByProduit(p.getUid()));
            Response<Produit> legacy = kazisafe.saveLite(ph).execute();
            if (legacy.isSuccessful()) {
                SyncLogger.getInstance().logMessage(ctxt, "repli legacy saveLite OK");
                return true;
            }
            SyncLogger.getInstance().logMessage(ctxt, "repli legacy echec http " + legacy.code());
            return false;
        } catch (Exception e) {
            SyncLogger.getInstance().log(e, ctxt + ".legacy");
            return false;
        }
    }

    /**
     * Pousse le produit via {@code /produits/x-sync/dto}. Si le serveur estime
     * que le produit appartient a un autre tenant, il cree une COPIE sous un
     * NOUVEAU uid et renvoie le mapping (ancien uid produit -> nouveau uid, et
     * ancienUidMesure -> nouveauUidMesure) dans le champ {@code payload} de la
     * reponse. On persiste alors le nouveau produit localement et on migre les
     * foreign keys locales (product_id/produit_id/mesure_id) de l'ancien uid
     * vers le nouveau, puis on hard-delete l'ancien produit et ses anciennes
     * mesures ({@link ProductUidMigrator}).
     */
    private boolean pushProduitDto(Kazisafe kazisafe, Produit p) {
        String ctxt = "MissingParentHealer.pushProduitDto";
        String oldUid = p != null ? p.getUid() : null;
        try {
            retrofit2.Response<Produit> rep = kazisafe
                    .syncProduitDto(SyncMappers.toProduitDto(p))
                    .execute();
            if (!rep.isSuccessful()) {
                String body = rep.errorBody() != null ? rep.errorBody().string() : "";
                SyncLogger.getInstance().logMessage(ctxt,
                        "produit " + oldUid + " rejete (http " + rep.code() + ") body=" + body);
                return false;
            }
            Produit returned = rep.body();
            if (returned == null || returned.getUid() == null || returned.getUid().isBlank()
                    || returned.getUid().equals(oldUid)) {
                SyncLogger.getInstance().logMessage(ctxt,
                        "produit " + oldUid + " pousse (pas de copie cross-tenant)");
                return true;
            }
            String newUid = returned.getUid();
            if (ProduitDelegate.findProduit(newUid) == null && returned.getNomProduit() != null) {
                try {
                    ProduitDelegate.saveProduit(returned);
                    SyncLogger.getInstance().logMessage(ctxt, "nouveau produit persiste localement " + newUid);
                } catch (Exception e) {
                    SyncLogger.getInstance().log(e, ctxt + ".persistNew");
                }
            }
            Map<String, String> measureUids = new HashMap<>();
            String payload = returned.getPayload();
            if (payload != null && !payload.isBlank()) {
                try {
                    JsonNode root = objectMapper.readTree(payload);
                    JsonNode mu = root.path("measureUids");
                    if (mu.isObject()) {
                        mu.fields().forEachRemaining(en -> {
                            String v = en.getValue().asText();
                            if (v != null && !v.isBlank()) {
                                measureUids.put(en.getKey(), v);
                            }
                        });
                    }
                } catch (Exception e) {
                    SyncLogger.getInstance().log(e, ctxt + ".parsePayload");
                }
            }
            SyncLogger.getInstance().logMessage(ctxt,
                    "copie cross-tenant produit " + oldUid + " -> " + newUid
                            + " mesures=" + measureUids.size() + " payload=" + payload);
            ProductUidMigrator.migrate(oldUid, newUid, measureUids);
            markPushed("PRODUIT", oldUid);
            return true;
        } catch (Exception e) {
            SyncLogger.getInstance().log(e, ctxt);
            return false;
        }
    }



    private boolean pushMesure(Kazisafe kazisafe, Mesure m) {
        String ctxt = "MissingParentHealer.pushMesure";
        String oldUid = m != null ? m.getUid() : null;
        try {
            retrofit2.Response<Mesure> rep = kazisafe
                    .syncMesureDto(SyncMappers.toMesureDto(m))
                    .execute();
            if (rep.isSuccessful() && rep.body() != null) {
                pushMesureMigrate(rep.body(), oldUid, ctxt);
                return true;
            }
            SyncLogger.getInstance().logMessage(ctxt,
                    "mesure " + oldUid + " rejetee (http " + rep.code() + ") body="
                            + (rep.errorBody() != null ? rep.errorBody().string() : ""));
        } catch (Exception e) {
            SyncLogger.getInstance().log(e, ctxt + ".dto");
        }
        try {
            Response<Mesure> legacy = kazisafe.saveMesure(m).execute();
            if (legacy.isSuccessful()) {
                SyncLogger.getInstance().logMessage(ctxt, "repli legacy saveMesure OK");
                return true;
            }
            SyncLogger.getInstance().logMessage(ctxt, "repli legacy echec http " + legacy.code());
            return false;
        } catch (Exception e) {
            SyncLogger.getInstance().log(e, ctxt + ".legacy");
            return false;
        }
    }

    /**
     * Consomme le {@code payload} d'une copie cross-tenant de MESURE : le serveur
     * renvoie {@code {"status":"created","replacement":{"ancienUid":"nouveauUid"}}}
     * (et/ou le nouvel uid dans {@code getUid()}). Quand le uid a change, on migre
     * la FK locale vers le nouveau uid via {@link ProductUidMigrator#migrateMesure}.
     */
    private void pushMesureMigrate(Mesure returned, String oldUid, String ctxt) {
        if (returned == null || oldUid == null || oldUid.isBlank()) {
            return;
        }
        String newUid = returned.getUid();
        String payload = returned.getPayload();
        if (payload != null && !payload.isBlank()) {
            try {
                com.fasterxml.jackson.databind.JsonNode root =
                        data.core.KazisafeServiceFactory.mapper().readTree(payload);
                com.fasterxml.jackson.databind.JsonNode repl = root.path("replacement");
                if (repl.isObject()) {
                    com.fasterxml.jackson.databind.JsonNode nu = repl.get(oldUid);
                    if (nu != null && nu.isTextual() && !nu.asText().isBlank()) {
                        newUid = nu.asText();
                    }
                }
            } catch (Exception e) {
                SyncLogger.getInstance().log(e, ctxt + ".payload");
            }
        }
        if (newUid == null || newUid.isBlank() || newUid.equals(oldUid)) {
            SyncLogger.getInstance().logMessage(ctxt, "mesure " + oldUid + " pas de copie cross-tenant");
            return;
        }
        tools.sync.ProductUidMigrator.migrateMesure(oldUid, newUid);
        SyncLogger.getInstance().logMessage(ctxt,
                "copie cross-tenant mesure " + oldUid + " -> " + newUid);
    }

    private boolean pushCompteTresor(Kazisafe kazisafe, CompteTresor ct) {
        String ctxt = "MissingParentHealer.pushCompteTresor";
        if (push(kazisafe, ctxt + ".dto", kazisafe.syncCompteTresorDto(SyncMappers.toCompteTresorDto(ct)))) {
            return true;
        }
        try {
            Response<CompteTresor> legacy = kazisafe.saveCompteTresorByForm(ct.getUid(), ct.getBankName(),
                    ct.getIntitule(), ct.getSoldeMinimum() != null ? ct.getSoldeMinimum() : 0d,
                    ct.getNumeroCompte(), ct.getRegion(), ct.getTypeCompte()).execute();
            if (legacy.isSuccessful()) {
                SyncLogger.getInstance().logMessage(ctxt, "repli legacy saveCompteTresorByForm OK");
                return true;
            }
            SyncLogger.getInstance().logMessage(ctxt, "repli legacy echec http " + legacy.code());
            return false;
        } catch (Exception e) {
            SyncLogger.getInstance().log(e, ctxt + ".legacy");
            return false;
        }
    }

    private boolean pushCategory(Kazisafe kazisafe, Category cat) {
        String ctxt = "MissingParentHealer.pushCategory";
        if (push(kazisafe, ctxt + ".dto", kazisafe.syncCategoryDto(SyncMappers.toCategoryDto(cat)))) {
            return true;
        }
        try {
            Response<okhttp3.ResponseBody> legacy = kazisafe.saveCategory(cat).execute();
            if (legacy.isSuccessful()) {
                SyncLogger.getInstance().logMessage(ctxt, "repli legacy saveCategory OK");
                return true;
            }
            SyncLogger.getInstance().logMessage(ctxt, "repli legacy echec http " + legacy.code());
            return false;
        } catch (Exception e) {
            SyncLogger.getInstance().log(e, ctxt + ".legacy");
            return false;
        }
    }

    private boolean pushFournisseur(Kazisafe kazisafe, Fournisseur f) {
        String ctxt = "MissingParentHealer.pushFournisseur";
        if (push(kazisafe, ctxt + ".dto", kazisafe.syncFournisseurDto(SyncMappers.toFournisseurDto(f)))) {
            return true;
        }
        try {
            Response<Fournisseur> legacy = kazisafe.saveSupplier(f).execute();
            if (legacy.isSuccessful()) {
                SyncLogger.getInstance().logMessage(ctxt, "repli legacy saveSupplier OK");
                return true;
            }
            SyncLogger.getInstance().logMessage(ctxt, "repli legacy echec http " + legacy.code());
            return false;
        } catch (Exception e) {
            SyncLogger.getInstance().log(e, ctxt + ".legacy");
            return false;
        }
    }

    private boolean pushTraisorerie(Kazisafe kazisafe, Traisorerie t) {
        return push(kazisafe, "MissingParentHealer.pushTraisorerie",
                kazisafe.syncTraisorerieDto(SyncMappers.toTraisorerieDto(t)));
    }

    private boolean pushVente(Kazisafe kazisafe, Vente v) {
        return push(kazisafe, "MissingParentHealer.pushVente",
                kazisafe.syncSaleDto(SyncMappers.toVenteDto(v)));
    }

    private boolean pushLigneVente(Kazisafe kazisafe, LigneVente lv) {
        return push(kazisafe, "MissingParentHealer.pushLigneVente",
                kazisafe.syncLigneVenteDto(SyncMappers.toLigneVenteDto(lv)));
    }

    private boolean pushLivraison(Kazisafe kazisafe, Livraison l) {
        return push(kazisafe, "MissingParentHealer.pushLivraison",
                kazisafe.syncLivraisonDto(SyncMappers.toLivraisonDto(l)));
    }

    private boolean pushStocker(Kazisafe kazisafe, Stocker s) {
        return push(kazisafe, "MissingParentHealer.pushStocker",
                kazisafe.syncStockerDto(SyncMappers.toStockerDto(s)));
    }

    private boolean pushDestocker(Kazisafe kazisafe, Destocker d) {
        return push(kazisafe, "MissingParentHealer.pushDestocker",
                kazisafe.syncDestockerDto(SyncMappers.toDestockerDto(d)));
    }

    private boolean pushRecquisition(Kazisafe kazisafe, Recquisition r) {
        return push(kazisafe, "MissingParentHealer.pushRecquisition",
                kazisafe.syncRecquisitionDto(SyncMappers.toRecquisitionDto(r)));
    }

    private boolean pushPrixDeVente(Kazisafe kazisafe, PrixDeVente pv) {
        return push(kazisafe, "MissingParentHealer.pushPrixDeVente",
                kazisafe.syncPrixDeVenteDto(SyncMappers.toPrixDeVenteDto(pv)));
    }

    // ---------------------------------------------------------------------
    // utilitaires
    // ---------------------------------------------------------------------

    private boolean push(Kazisafe kazisafe, String context, retrofit2.Call<?> call) {
        try {
            Response<?> rep = call.execute();
            if (rep.isSuccessful()) {
                SyncLogger.getInstance().logMessage(context,
                        "parent pousse avec succes (http " + rep.code() + ")");
                return true;
            }
            String body = rep.errorBody() != null ? rep.errorBody().string() : "";
            SyncLogger.getInstance().logMessage(context,
                    "poussage parent rejete (http " + rep.code() + ") body=" + body);
            return false;
        } catch (IOException e) {
            SyncLogger.getInstance().log(e, context);
            return false;
        }
    }

    private boolean markPushed(String type, String uid) {
        PUSHED.get().add(type + ":" + uid);
        return true;
    }

    private boolean alreadyPushed(String uid) {
        if (uid == null) {
            return true;
        }
        for (String key : PUSHED.get()) {
            if (key.endsWith(":" + uid)) {
                return true;
            }
        }
        return false;
    }

    private static String context(String type, String uid) {
        return "MissingParentHealer." + type + (uid != null ? "(" + uid + ")" : "");
    }
}