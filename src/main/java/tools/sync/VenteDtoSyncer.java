package tools.sync;

import data.Client;
import data.CompteTresor;
import data.LigneVente;
import data.Mesure;
import data.Produit;
import data.Traisorerie;
import data.Vente;
import data.dto.ClientUpsertDto;
import data.dto.CompteTresorUpsertDto;
import data.dto.LigneVenteUpsertDto;
import data.dto.TraisorerieUpsertDto;
import data.dto.VenteUpsertDto;
import data.network.Kazisafe;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import delegates.MesureDelegate;
import delegates.ProduitDelegate;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Response;
import tools.SyncLogger;

/**
 * Synchronisation asynchrone (asymetrique) d'une vente via les endpoints DTO du
 * serveur :
 *
 * <ul>
 * <li>le {@code POST /v1/ventes/x-sync/dto} pousse l'entete {@link VenteUpsertDto}
 * (uid a plat + FKs + champs scalaires) ;</li>
 * <li>puis un {@code PATCH /v1/lignevente/x-sync/dto} par {@link LigneVenteUpsertDto}
 * rejoue chaque ligne en reference du uid de la vente.</li>
 * </ul>
 *
 * Chaque mutation passe par {@link UpsyncEngine} : si le serveur refuse parce
 * qu'un parent manque (client/produit/mesure...), l'auto-healing
 * ({@link MissingParentHealer}) rejoue la mutation dans le meme flux. L'entete
 * est poussee en premier pour que les lignes puissent resoudre leur vente.
 *
 * Les trois flux d'enregistrement d'une vente (facturation en direct,
 * brouillon hors-ligne, compact) ainsi que la synchronisation de masse en
 * partagent une seule implementation, ce qui evite la duplication du
 * mapping / repli / healing.
 */
public final class VenteDtoSyncer {

    private static final String CTX = "VenteDtoSyncer";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final UpsyncEngine engine;

    public VenteDtoSyncer() {
        this(new UpsyncEngine());
    }

    public VenteDtoSyncer(UpsyncEngine engine) {
        this.engine = engine;
    }

    /**
     * Pousse l'entete de la vente puis toutes ses lignes via les endpoints DTO.
     *
     * @param kazisafe client Retrofit
     * @param vente    entete (clientUid lowercase en {@code clientId})
     * @param transactionId identifiant de transaction (peut etre null)
     * @param lignes   lignes de vente (info produit/mesure + reference au uid
     *                 de la vente); elles utilisent ZERO objet metier cote
     *                 serveur, seuls les uid plat sont envoyes.
     * @return la {@link Response} de l'entete de vente (200 si la vente et ses
     *         lignes ont pu etre poussees)
     * @throws IOException si une ligne ne peut etre synchronisee apres
     *                     auto-healing (le flux externe relance alors via son
     *                     gestionnaire de retry, l'entete etant idempotente).
     */
    public Response<Vente> pushSale(Kazisafe kazisafe, Vente vente, String transactionId,
            List<LigneVente> lignes) throws IOException {
        if (kazisafe == null || vente == null) {
            return null;
        }
        VenteUpsertDto header = SyncMappers.toVenteDto(vente, transactionId);
        Response<Vente> headerResp = engine.execute(kazisafe,
                () -> kazisafe.syncSaleDto(header), CTX + ".header", 3);
        if (headerResp == null || !headerResp.isSuccessful()) {
            return headerResp;
        }
        if (lignes == null || lignes.isEmpty()) {
            return headerResp;
        }
        for (LigneVente lv : lignes) {
            LigneVenteUpsertDto ligneDto = SyncMappers.toLigneVenteDto(lv);
            if (ligneDto == null || ligneDto.getVenteUid() == null || ligneDto.getVenteUid().isBlank()) {
                SyncLogger.getInstance().logMessage(CTX,
                        "ligne " + (lv == null ? "?" : lv.getUid())
                                + " ignoree (uid vente absent)");
                continue;
            }
            Response<LigneVente> ligneResp = engine.execute(kazisafe,
                    () -> kazisafe.syncLigneVenteDto(ligneDto), CTX + ".line", 3);
            if (ligneResp == null || !ligneResp.isSuccessful()) {
                int code = ligneResp == null ? -1 : ligneResp.code();
                String body = "";
                try {
                    body = ligneResp.errorBody() != null ? ligneResp.errorBody().string() : "";
                } catch (Exception ignored) {
                    SyncLogger.getInstance().log(ignored, CTX + ".bodyLine");
                }
                SyncLogger.getInstance().logMessage(CTX,
                        "ligne " + ligneDto.getUid() + " rejetee (http " + code + ") body=" + body
                                + " produitUid=" + ligneDto.getProduitUid()
                                + " mesureUid=" + ligneDto.getMesureUid()
                                + " venteUid=" + ligneDto.getVenteUid());
                throw new IOException("Ligne de vente non synchronisee (http " + code + ")");
            }
            migrateLigneCopy(kazisafe, ligneDto, ligneResp.body());
        }
        return headerResp;
    }

    /**
     * Consomme la copie cross-tenant eventuelle effectuee par le serveur sur
     * {@code PATCH /v1/lignevente/x-sync/dto} : quand un produit/mesure de la
     * ligne appartient a un autre tenant, le serveur le COPIe dans le tenant
     * courant sous un NOUVEAU uid et renvoie le mapping dans le champ
     * {@code payload} de la reponse ligne
     * ({@code {"status":"created","replacement":{"oldProd":"newProd"},
     * "measureUids":{"oldMes":"newMes"}}}). On duplique alors localement le
     * produit et ses mesures sous les nouveaux uids (la reponse ligne ne porte
     * que des stubs) puis on migre les foreign keys locales vers les nouveaux
     * uids via {@link ProductUidMigrator}.
     */
    private void migrateLigneCopy(Kazisafe kazisafe, LigneVenteUpsertDto dto, LigneVente returned) {
        if (kazisafe == null || dto == null || returned == null) {
            return;
        }
        String oldProdUid = dto.getProduitUid();
        String oldMesureUid = dto.getMesureUid();
        if (oldProdUid == null || oldProdUid.isBlank()) {
            return;
        }
        Map<String, String> measureUids = new HashMap<>();
        String newProdUid = returned.getProductId() != null ? returned.getProductId().getUid() : null;
        String newMesureUid = returned.getMesureId() != null ? returned.getMesureId().getUid() : null;
        String payload = returned.getPayload();
        if (payload != null && !payload.isBlank()) {
            try {
                JsonNode root = objectMapper.readTree(payload);
                JsonNode repl = root.path("replacement");
                if (repl.isObject()) {
                    JsonNode nu = repl.get(oldProdUid);
                    if (nu != null && nu.isTextual() && !nu.asText().isBlank()) {
                        newProdUid = nu.asText();
                    }
                }
                JsonNode mu = root.path("measureUids");
                if (mu.isObject()) {
                    mu.fields().forEachRemaining(en -> {
                        String v = en.getValue() != null ? en.getValue().asText() : null;
                        if (v != null && !v.isBlank()) {
                            measureUids.put(en.getKey(), v);
                        }
                    });
                }
            } catch (Exception e) {
                SyncLogger.getInstance().log(e, CTX + ".ligneCopy.payload");
            }
        }
        if (newProdUid == null || newProdUid.isBlank() || newProdUid.equals(oldProdUid)) {
            SyncLogger.getInstance().logMessage(CTX,
                    "ligne " + dto.getUid() + " pas de copie produit cross-tenant (produit " + oldProdUid + ")");
            return;
        }
        persistLocalProductCopy(oldProdUid, newProdUid);
        if (measureUids.isEmpty() && oldMesureUid != null && newMesureUid != null
                && !newMesureUid.equals(oldMesureUid)) {
            measureUids.put(oldMesureUid, newMesureUid);
        }
        for (Map.Entry<String, String> en : measureUids.entrySet()) {
            persistLocalMesureCopy(en.getKey(), en.getValue(), newProdUid);
        }
        boolean ok = ProductUidMigrator.migrate(oldProdUid, newProdUid, measureUids);
        SyncLogger.getInstance().logMessage(CTX,
                "copie cross-tenant ligne " + dto.getUid() + " produit " + oldProdUid + " -> "
                        + newProdUid + " mesures=" + measureUids.size() + " migrated=" + ok);
    }

    /**
     * Duplique localement le produit (sous le nouvel uid) quand il n'existe pas
     * deja : la reponse ligne ne renvoie que le stub {@code Produit(uid)}, on
     * recopie donc les champs scalaires de l'ancien produit en base locale.
     */
    private void persistLocalProductCopy(String oldProdUid, String newProdUid) {
        try {
            if (newProdUid == null || ProduitDelegate.findProduit(newProdUid) != null) {
                return;
            }
            Produit old = ProduitDelegate.findProduit(oldProdUid);
            if (old == null) {
                SyncLogger.getInstance().logMessage(CTX,
                        "produit local " + oldProdUid + " introuvable, copie locale ignoree");
                return;
            }
            Produit copy = new Produit(newProdUid);
            copy.setCodebar(old.getCodebar());
            copy.setNomProduit(old.getNomProduit());
            copy.setMarque(old.getMarque());
            copy.setModele(old.getModele());
            copy.setTaille(old.getTaille());
            copy.setCouleur(old.getCouleur());
            copy.setMethodeInventaire(old.getMethodeInventaire());
            copy.setDateCreation(old.getDateCreation());
            copy.setCategoryId(old.getCategoryId());
            copy.setImage(old.getImage());
            ProduitDelegate.saveProduit(copy);
            SyncLogger.getInstance().logMessage(CTX,
                    "copie locale produit " + oldProdUid + " -> " + newProdUid + " persiste");
        } catch (Exception e) {
            SyncLogger.getInstance().log(e, CTX + ".ligneCopy.productCopy");
        }
    }

    /**
     * Duplique localement une mesure sous son nouvel uid (meme champ que
     * {@link #persistLocalProductCopy}) puisqu'aucune donnee de mesure n'est
     * portee par la reponse ligne.
     */
    private void persistLocalMesureCopy(String oldMesureUid, String newMesureUid, String prodUid) {
        try {
            if (oldMesureUid == null || newMesureUid == null || MesureDelegate.findMesure(newMesureUid) != null) {
                return;
            }
            Mesure old = MesureDelegate.findMesure(oldMesureUid);
            if (old == null) {
                SyncLogger.getInstance().logMessage(CTX,
                        "mesure locale " + oldMesureUid + " introuvable, copie locale ignoree");
                return;
            }
            Mesure copy = new Mesure(newMesureUid);
            copy.setDescription(old.getDescription());
            copy.setQuantContenu(old.getQuantContenu());
            if (prodUid != null) {
                copy.setProduitId(new Produit(prodUid));
            }
            MesureDelegate.saveMesure(copy);
            SyncLogger.getInstance().logMessage(CTX,
                    "copie locale mesure " + oldMesureUid + " -> " + newMesureUid + " persiste");
        } catch (Exception e) {
            SyncLogger.getInstance().log(e, CTX + ".ligneCopy.mesureCopy");
        }
    }

    /**
     * Variante pratique : garantit que le uid du client (uniquement) est pose
     * sur l'entete avant le mapping, en repli sur le client passe a part.
     */
    public Response<Vente> pushSale(Kazisafe kazisafe, Vente vente, Client client,
            String transactionId, List<LigneVente> lignes) throws IOException {
        if (vente != null && vente.getClientId() == null && client != null) {
            vente.setClientId(client);
        }
        return pushSale(kazisafe, vente, transactionId, lignes);
    }

    /**
     * Pousse un client via {@code PATCH /v1/clients/x-sync/dto}.
     */
    public boolean pushClient(Kazisafe kazisafe, Client client) throws IOException {
        if (kazisafe == null || client == null || client.getUid() == null) {
            return false;
        }
        ClientUpsertDto dto = SyncMappers.toClientDto(client);
        Response<Client> rep = engine.execute(kazisafe,
                () -> kazisafe.syncClientDto(dto), CTX + ".client", 3);
        boolean ok = rep != null && rep.isSuccessful();
        SyncLogger.getInstance().logMessage(CTX,
                "client " + client.getUid() + (ok ? " pousse" : " rejete (http " + (rep == null ? -1 : rep.code()) + ")"));
        return ok;
    }

    /**
     * Pousse un compte de tresorerie via {@code PATCH /v1/tresor/x-sync/dto}.
     */
    public boolean pushCompteTresor(Kazisafe kazisafe, CompteTresor ct) throws IOException {
        if (kazisafe == null || ct == null || ct.getUid() == null) {
            return false;
        }
        CompteTresorUpsertDto dto = SyncMappers.toCompteTresorDto(ct);
        Response<CompteTresor> rep = engine.execute(kazisafe,
                () -> kazisafe.syncCompteTresorDto(dto), CTX + ".compteTresor", 3);
        boolean ok = rep != null && rep.isSuccessful();
        SyncLogger.getInstance().logMessage(CTX,
                "compteTresor " + ct.getUid() + (ok ? " pousse" : " rejete (http " + (rep == null ? -1 : rep.code()) + ")"));
        return ok;
    }

    /**
     * Pousse une operation de tresorerie via
     * {@code PATCH /v1/traisorerie/x-sync/dto}. La dependance vers le compte de
     * tresorerie ({@code tresorUid}) est auto-healee par {@link UpsyncEngine} si
     * le compte manque cote serveur (code 412 {@literal COMPTETRESOR}).
     */
    public boolean pushTraisorerie(Kazisafe kazisafe, Traisorerie tr) throws IOException {
        if (kazisafe == null || tr == null || tr.getUid() == null) {
            return false;
        }
        TraisorerieUpsertDto dto = SyncMappers.toTraisorerieDto(tr);
        if (dto.getTresorUid() == null || dto.getTresorUid().isBlank()) {
            SyncLogger.getInstance().logMessage(CTX,
                    "traisorerie " + tr.getUid() + " ignoree (compte tresor absent)");
            return false;
        }
        Response<Traisorerie> rep = engine.execute(kazisafe,
                () -> kazisafe.syncTraisorerieDto(dto), CTX + ".traisorerie", 3);
        boolean ok = rep != null && rep.isSuccessful();
        SyncLogger.getInstance().logMessage(CTX,
                "traisorerie " + tr.getUid() + (ok ? " poussee" : " rejetee (http " + (rep == null ? -1 : rep.code()) + ")"));
        return ok;
    }
}
