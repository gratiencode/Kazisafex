/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tools;

import data.Client;
import data.CompteTresor;
import data.LigneVente;
import data.Vente;
import data.dto.SyncErrorResponse;
import data.dto.UpsyncErrorParser;
import data.network.Kazisafe;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import retrofit2.Response;
import tools.sync.MissingParentHealer;
import tools.sync.VenteDtoSyncer;

/**
 * Synchronisation resiliente du brouillon de vente.
 *
 * Le corps d'erreur est desormais structure ({@link SyncErrorResponse}) et
 * l'auto-healing des parents manquants est delegue a
 * {@link MissingParentHealer} (DTO asymetriques + repli legacy), avant rejeu
 * via {@link SyncRetryHandler}.
 */
public final class SaleSyncHelper {

    private static final int MAX_SALE_RETRY = 9;

    private SaleSyncHelper() {
    }

    public static void tryToSaveSale(final Kazisafe kazisafe,
                                     final String transaction,
                                     final CompteTresor ct,
                                     final Client client,
                                     final Vente vente,
                                     final List<LigneVente> lignes) {
        if (kazisafe == null || vente == null || lignes == null || lignes.isEmpty()) {
            return;
        }
        Executors.newCachedThreadPool().submit(() -> {
            if (!Util.isInternetAndBaseApiReachable()) {
                System.out.println("SaleSyncHelper: pas de reseau, brouillon " + vente.getUid()
                        + " remontera via l'outbox");
                return;
            }
            try {
                SyncRetryHandler.retryVoid("Vente", String.valueOf(vente.getUid()), () -> {
                    Response<Vente> rep = saveVenteByHttp(kazisafe, vente, client, ct, transaction, lignes);
                    if (rep == null) {
                        throw new Exception("Réponse null");
                    }
                    int code = rep.code();
                    System.out.println("SaleSyncHelper: reponse http brouillon " + code);
                    switch (code) {
                        case 200 -> {
                            System.out.println("SaleSyncHelper: brouillon " + vente.getUid()
                                    + " synchronise avec succes");
                            return;
                        }
                        case 417 -> {
                            SyncErrorResponse err = parseError(rep);
                            if (err != null && (err.getMissingUid() == null || err.getMissingUid().isBlank())
                                    && client != null) {
                                err.setMissingUid(client.getUid());
                            }
                            if (!heal(kazisafe, err)) {
                                System.out.println("SaleSyncHelper: client manquant introuvable, retry");
                            }
                        }
                        case 412 -> {
                            SyncErrorResponse err = parseError(rep);
                            if (err != null && (err.getMissingUid() == null || err.getMissingUid().isBlank())
                                    && ct != null) {
                                err.setMissingUid(ct.getUid());
                            }
                            if (!heal(kazisafe, err)) {
                                System.out.println("SaleSyncHelper: tresor manquant introuvable, retry");
                            }
                        }
                        case 460 -> {
                            SyncErrorResponse err = parseError(rep);
                            if (err == null || !heal(kazisafe, err)) {
                                healProduitsOf(kazisafe, lignes);
                            }
                        }
                        case 461 -> {
                            SyncErrorResponse err = parseError(rep);
                            if (err == null || !heal(kazisafe, err)) {
                                healProduitsOf(kazisafe, lignes);
                            }
                        }
                        case 403 -> {
                            String body403 = "";
                            try { body403 = rep.errorBody() != null ? rep.errorBody().string() : ""; } catch (Exception ignored) { SyncLogger.getInstance().log(ignored, "SaleSyncHelper.tryToSaveSale.parse403"); }
                            System.out.println("SaleSyncHelper: 403 Forbidden brouillon " + vente.getUid() + " body=" + body403 + " — abandon, permission/tenant");
                            return;
                        }
                        case 422 -> {
                            System.out.println("SaleSyncHelper: brouillon " + vente.getUid()
                                    + " — liste de lignes vide (422), abandon");
                            return;
                        }
                        case 400, 409 -> {
                            System.out.println("SaleSyncHelper: fallback " + code + " — pousse tous les produits");
                            healProduitsOf(kazisafe, lignes);
                        }
                        case 402 -> {
                            System.out.println("SaleSyncHelper: abonnement expire (402) — abandon");
                            return;
                        }
                        case 500, 502, 503, 504 -> throw new Exception("Erreur serveur, code=" + code);
                        default -> System.out.println("SaleSyncHelper: brouillon " + vente.getUid()
                                + " non traite, code=" + code + " — retry");
                    }
                    throw new Exception("Vente non enregistrée, code=" + code + " — retry apres sync dependance");
                }, MAX_SALE_RETRY);
            } catch (Exception e) {
                SyncLogger.getInstance().log(e, "SaleSyncHelper.tryToSaveSale");
                System.out.println("SaleSyncHelper: erreur sync brouillon " + vente.getUid() + ": " + e.getMessage());
            }
        });
    }

    public static Response<Vente> saveVenteByHttp(Kazisafe kazisafe, Vente vente, Client client,
            CompteTresor tresor, String transaction, List<LigneVente> venteItems) throws IOException {
        if (kazisafe == null || vente == null || !Util.isInternetAndBaseApiReachable()) {
            return null;
        }
        Vente slim = payloadVente(vente, tresor);
        if (slim.getClientId() == null && client != null) {
            slim.setClientId(client);
        }
        return new VenteDtoSyncer().pushSale(kazisafe, slim, client, transaction, venteItems);
    }

    private static SyncErrorResponse parseError(Response<Vente> rep) {
        try {
            String body = rep.errorBody() != null ? rep.errorBody().string() : "";
            SyncLogger.getInstance().logMessage("SaleSyncHelper.parseError",
                    "http " + rep.code() + " body=" + body);
            return UpsyncErrorParser.parse(body);
        } catch (Exception e) {
            SyncLogger.getInstance().log(e, "SaleSyncHelper.parseError");
            return null;
        }
    }

    private static boolean heal(Kazisafe kazisafe, SyncErrorResponse err) {
        try {
            return err != null && MissingParentHealer.getInstance().heal(kazisafe, err);
        } catch (Exception e) {
            SyncLogger.getInstance().log(e, "SaleSyncHelper.heal");
            return false;
        }
    }

    private static void healProduitsOf(Kazisafe kazisafe, List<LigneVente> lignes) {
        MissingParentHealer healer = MissingParentHealer.getInstance();
        for (LigneVente ligne : lignes) {
            if (ligne.getProductId() == null || ligne.getProductId().getUid() == null) {
                continue;
            }
            SyncErrorResponse err = new SyncErrorResponse();
            err.setMissingType("PRODUIT");
            err.setMissingUid(ligne.getProductId().getUid());
            try {
                healer.heal(kazisafe, err);
            } catch (Exception e) {
                SyncLogger.getInstance().log(e, "SaleSyncHelper.healProduitsOf");
            }
        }
    }

    private static Vente payloadVente(Vente source, CompteTresor tresor) {
        if (tresor != null) {
            return source;
        }
        // Brouillon sans caisse (Drafted) : pas d'opération de trésorerie créée.
        // Il est donc poussé comme une vente à crédit. L'entête porte le total dans
        // UNE SEULE devise (convention legacy) : les lecteurs d'autres terminaux
        // recombinent le total via usd + cdf/taux (ou cdf + usd*taux), renseigner les
        // deux montants ferait donc DOUBLER le montant affiché/imprimé.
        Vente copy = new Vente(source.getUid());
        copy.setReference(source.getReference());
        copy.setLibelle(source.getLibelle());
        copy.setObservation(source.getObservation());
        copy.setDateVente(source.getDateVente());
        copy.setLatitude(source.getLatitude());
        copy.setLongitude(source.getLongitude());
        copy.setRegion(source.getRegion());
        copy.setClientId(source.getClientId());
        double totalUsd = CurrencyConverter.legacyUsdFromStorage(source.getMontantUsd(), source.getMontantCdf());
        double cdf = source.getMontantCdf();
        double usd = source.getMontantUsd();
        boolean cdfOperative = cdf > 0 && usd <= 0;
        copy.setMontantCdf(cdfOperative ? CurrencyConverter.round(cdf) : 0d);
        copy.setMontantUsd(cdfOperative ? 0d : CurrencyConverter.round(totalUsd));
        copy.setMontantDette(totalUsd > 0 ? totalUsd : source.getMontantDette());
        copy.setDeviseDette(CurrencyConverter.USD);
        copy.setEcheance(source.getEcheance());
        copy.setPayment(Constants.PAYEMENT_CREDIT);
        return copy;
    }
}