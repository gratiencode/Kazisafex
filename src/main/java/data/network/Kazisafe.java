package data.network;

import data.ProduitHelper;
import java.util.List;
import data.*;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import data.helpers.Checked;
import data.helpers.Credentials;
import data.helpers.NetCash;
import data.helpers.Resultat;
import data.helpers.TokenRefreshed;
import data.helpers.SoldeDetteClient;
import data.helpers.SubscriptionPayment;

import data.helpers.Token;
import data.helpers.VuStock;
import data.helpers.LoginWebResult;
import data.helpers.VueAbonnement;
import data.finance.BilanReport;
import data.finance.CompteResultatReport;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.PUT;
import tools.ProductMarshalAdapter;
import tools.WebResult;

public interface Kazisafe {

        @Multipart
        @POST("user/signup")
        Call<User> singUp(@Part okhttp3.MultipartBody.Part var1, @Part("file_desc") RequestBody var2,
                        @Part("email") RequestBody var3, @Part("nom") RequestBody var4,
                        @Part("prenom") RequestBody var5, @Part("phone") RequestBody var6,
                        @Part("password") RequestBody var7);

        @Multipart
        @PATCH("user/{uid}/update")
        Call<User> updateProfile(@Part okhttp3.MultipartBody.Part var1, @Part("file_desc") RequestBody var2,
                        @Part("email") RequestBody var3, @Part("nom") RequestBody var4,
                        @Part("prenom") RequestBody var5, @Part("phone") RequestBody var6,
                        @Part("password") RequestBody var7, @Path("uid") String var8);

        @PATCH("user/{uid}/recover/pswd/{reinit}")
        Call<User> resetPassword(@Path("uid") String var1, @Path("reinit") String var2);

        @PATCH("user/{uid}/recover-it/pswd/{reinit}")
        Call<User> reinitPassword(@Path("uid") String var1, @Path("reinit") String var2);

        @GET("user/{uid}/picturestore/photo")
        Call<ResponseBody> downloadUserPhoto(@Path("uid") String var1);

        @GET("user/{uid}/securely/picturestore/photo")
        Call<ResponseBody> downloadUserPhotoSecurely(@Path("uid") String var1);

        @Multipart
        @POST("organization/save/point")
        Call<Entreprise> createEntreprise(@Part okhttp3.MultipartBody.Part var1,
                        @Part("identification") RequestBody var2,
                        @Part("type_ident") RequestBody var3,
                        @Part("adresse") RequestBody var4,
                        @Part("category") RequestBody category,
                        @Part("nom_ese") RequestBody nomEntreprise,
                        @Part("latitude") RequestBody latitude,
                        @Part("longitude") RequestBody longitude,
                        @Part("date_creat") RequestBody dateCreation,
                        @Part("email") RequestBody email,
                        @Part("website") RequestBody web,
                        @Part("userId") RequestBody userId,
                        @Part("privilege") RequestBody privilege,
                        @Part("region") RequestBody region,
                        @Part("descript") RequestBody engDescription,
                        @Part("username") RequestBody username);

        @Multipart
        @PATCH("organization/{uid}/update")
        Call<ResponseBody> updateEntreprise(
                        @Part okhttp3.MultipartBody.Part var1,
                        @Part("identification") RequestBody var7,
                        @Part("type_ident") RequestBody var9,
                        @Part("adresse") RequestBody var5,
                        @Part("category") RequestBody var8,
                        @Part("nom_ese") RequestBody var4,
                        @Part("latitude") RequestBody var10,
                        @Part("longitude") RequestBody var11,
                        @Part("email") RequestBody var3,
                        @Part("website") RequestBody var6,
                        @Part("impot") RequestBody numero_impot,
                        @Part("idnat") RequestBody idnat,
                        @Part("phones") RequestBody phone,
                        @Path("uid") String var12);

        @GET("organization/{uid}/download/logo")
        Call<ResponseBody> downloadLogo(@Path("uid") String var1);

        @Multipart
        @POST("produit/save/point")
        Call<List<Mesure>> createProduit(
                        @Part okhttp3.MultipartBody.Part var1,
                        @Part("uid") RequestBody var2,
                        @Part("codebar") RequestBody var3,
                        @Part("modeinv") RequestBody var4,
                        @Part("marque") RequestBody var5,
                        @Part("modele") RequestBody var6,
                        @Part("nom_produit") RequestBody var7,
                        @Part("taille") RequestBody var8,
                        @Part("couleur") RequestBody var9,
                        @Part("categoryid") RequestBody var10,
                        @Part List<okhttp3.MultipartBody.Part> var11,
                        @Part("entr") RequestBody var12);

        @Multipart
        @PATCH("produit/{uid}/update")
        Call<List<Mesure>> updateProduit(
                        @Part okhttp3.MultipartBody.Part var1,
                        @Part("file_desc") RequestBody var2,
                        @Part("codebar") RequestBody var3,
                        @Part("modeinv") RequestBody var4,
                        @Part("marque") RequestBody var5,
                        @Part("modele") RequestBody var6,
                        @Part("nom_produit") RequestBody var7,
                        @Part("taille") RequestBody var8,
                        @Part("couleur") RequestBody var9,
                        @Part("categoryid") RequestBody var10,
                        @Part List<okhttp3.MultipartBody.Part> var11,
                        @Part("entr") RequestBody var12,
                        @Path("uid") String var13);

        @GET("produit/{uid}/image")
        Call<ResponseBody> downloadProductImage(@Path("uid") String var1);

        @GET("produit/{uid}/show")
        Call<Produit> refreshProduit(@Path("uid") String var1);

        @POST("produit/upload/{entr}")
        Call<String> uploadProduit(@Path("entr") String entr, @Body Produit lug);

        @GET("produit/show/all/{entr}")
        Call<List<Produit>> getProducts(@Path("entr") String entr);

        @PATCH("produit/save/web/point")
        Call<Produit> saveLite(@Body ProduitHelper produx);

        @GET("subscription/me/active")
        Call<List<Abonnement>> getAbonnements();

        @PATCH("subscription/{uid}/x0e243c99e250e30ab/u00901/x501/{state}/{entr}")
        Call<ResponseBody> notifySubscribed(@Path("uid") String path, @Path("state") String state,
                        @Path("entr") String entr);

        @GET("params/{key}/param/view")
        Call<Parametre> getParameter(@Path("key") String var1);

        @GET("params/sync/icount/{table}/{entrep}")
        Call<ResponseBody> verifySync(@Path("table") String var1, @Path("entrep") String ese);

        @POST("subscription/save/point")
        Call<SubscriptionPayment> getAbonnementPayment(@Body SubscriptionPayment var1);

        @GET("repport/{rccm}/{date1}/{date2}")
        Call<VueAbonnement> showAbonnement(@Path("rccm") String var1, @Path("date1") String var2,
                        @Path("date2") String var3);

        @GET("v1/finance/immobilisations")
        Call<List<Immobilisation>> getImmobilisations(@Query("region") String region);

        @POST("v1/finance/immobilisations")
        Call<Immobilisation> createImmobilisation(@Body Immobilisation immobilisation);

        @POST("v1/finance/immobilisations/sync")
        Call<WebResult> syncImmobilisations(@Body List<Immobilisation> immobilisations);

        @GET("v1/finance/immobilisations/sync/missed/{since}")
        Call<List<Immobilisation>> syncMissedImmobilisations(@Path("since") String since);

        @POST("v1/agent/presence/sync")
        Call<WebResult> syncPresences(@Body List<Presence> presences);

        @GET("v1/agent/presence/sync/missed/{since}")
        Call<List<Presence>> syncMissedPresences(@Path("since") String since);

        @GET("v1/agent/presence")
        Call<List<Presence>> getPresences(@Query("agent") String agent,
                        @Query("from") String from,
                        @Query("to") String to,
                        @Query("limit") Integer limit);

        @GET("v1/agent/presence/{uid}")
        Call<Presence> getPresence(@Path("uid") String uid);

        @POST("v1/agent/presence")
        Call<Presence> createPresence(@Body Presence presence);

        @PATCH("v1/agent/presence/{uid}")
        Call<Presence> updatePresence(@Path("uid") String uid, @Body Presence presence);

        @DELETE("v1/agent/presence/{uid}")
        Call<ResponseBody> deletePresence(@Path("uid") String uid);

        @GET("v1/finance/bilan")
        Call<BilanReport> getBilan(@Query("dateDebut") String dateDebut, @Query("dateFin") String dateFin,
                        @Query("region") String region);

        @GET("v1/finance/compte-resultat")
        Call<CompteResultatReport> getCompteResultat(@Query("dateDebut") String dateDebut,
                        @Query("dateFin") String dateFin, @Query("region") String region);

        @GET("subscription/valide/check/login/{entr}")
        Call<Abonnement> checkSigninValidation(@Path("entr") String euid);

        // @GET("subscription/{uid}/show")
        // Call<Abonnement> getAbonnement(@Path("uid") String var1);
        @POST("auth/tr2")
        Call<String> signin(@Body Credentials var1);

        @POST("auth/auth0/web/signin")
        Call<LoginWebResult> desktopSignin(@Body Credentials var1);

        @POST("auth/auth0/signin")
        Call<ResponseBody> login(@Body Credentials var1);

        @POST("auth/refresh")
        Call<TokenRefreshed> refreshToken(@Body Token var1);

        @POST("auth/refresh")
        Call<TokenRefreshed> refreshTokenWithHeader(@Header("Authorization") String authorization, @Body Token var1);

        @GET("organization/check/{ident}")
        Call<Entreprise> getEntreprise(@Path("ident") String var1);

        @GET("organization/infolite/search/{uid}")
        Call<Entreprise> getEntrepriseInfo(@Path("uid") String var1);

        @GET("organization/newcomp/check/{ident}")
        Call<Entreprise> lookEntreprise(@Path("ident") String var1);

        @GET("categories/show/all")
        @Deprecated
        Call<List<Category>> getCategories();

        @GET("subscription/{uid}/show")
        Call<Abonnement> checkSubscription(@Path("uid") String uid);

        @POST("categories/allcats")
        Call<List<Category>> getCategories(@Body Entreprise entr);

        @GET("categories/show/all/{entr}")
        Call<List<Category>> getCategoriesByEuid(@Path("entr") String entr);

        @GET("categories/show/by/name/{nom_cat}")
        Call<List<Category>> getByName(@Path("nom_cat") String var1);

        @GET("categories/{name}/show")
        Call<Category> showCategory(@Path("name") String var1);

        @POST("categories/save/point")
        Call<ResponseBody> saveCategory(@Body Category var1);

        @GET("categories/{name}/update")
        Call<Category> updateCategory(@Path("name") String var1);

        @DELETE("categories/{name}/update")
        Call<Void> deleteCat(@Path("name") String var1);

        @GET("destockage/out/interval/{date1}/{date2}")
        Call<List<Destocker>> getDestockers(@Path("date1") String var1, @Path("date2") String var2);

        @GET("destockage/reccent")
        Call<List<Destocker>> refreshDestockers();

        @GET("destockage/{uid}/show")
        Call<Destocker> getDestocker(@Path("uid") String var1);

        @POST("destockage/save/point")
        Call<Destocker> saveDestocker(@Body Destocker var1);

        @GET("destockage/dest/recq/{ref}/{prod}/{entr}")
        Call<Destocker> getDestockageByRef(@Path("ref") String var1, @Path("prod") String prod8,
                        @Path("entr") String entr);

        @FormUrlEncoded
        @PATCH("destockage/x-sync")
        Call<Destocker> syncDestocker(@Field("uid") String uid,
                        @Field("datedestok") String date,
                        @Field("reference") String reference,
                        @Field("destination") String destination,
                        @Field("region") String region,
                        @Field("coutAch") String coutAch,
                        @Field("quantite") String quantite,
                        @Field("libelle") String libelle,
                        @Field("observation") String observation,
                        @Field("mesureId") String mesureId,
                        @Field("productId") String productId,
                        @Field("numlot") String numlot);

        @PATCH("destockage/{uid}/update")
        Call<Destocker> updateDestockage(@Body Destocker var1);

        @GET("destockage/all/destock/{entr}")
        Call<List<Destocker>> getDestockages(@Path("entr") String entr);

        @GET("destockage/filtered/destock/{entr}")
        Call<List<Destocker>> getFilteredDestockages(@Path("entr") String entr);

        @POST("destockage/unstorages")
        Call<List<Destocker>> getDestockages(@Body Entreprise ent);

        @DELETE("destockage/{uid}/delete")
        Call<Void> deleteDestockage(@Path("uid") String var1);

        @POST("engagee/form/update")
        Call<Employee> affectAgent(@Query("id") String var1,
                        @Query("userId") String var2,
                        @Query("entrepid") String var3,
                        @Query("privilege") String var4,
                        @Query("blocked") String var5,
                        @Query("region") String var6,
                        @Query("descript") String var7,
                        @Query("username") String var8,
                        @Query("date") long var9);

        @GET("engagee/show/regions")
        Call<List<String>> getRegions();

        @GET("engagee/show/employees/{entr}")
        Call<List<Employee>> findEmployees(@Path("entr") String entr);

        @DELETE("engagee/{uid}/update")
        Call<Void> deleteEngager(@Path("uid") String var1);

        @POST("clients/save")
        Call<Client> saveClient(@Body Client var1);

        @PATCH("clients/x-sync")
        @FormUrlEncoded
        Call<Client> saveByForm(@Field("uid") String uid,
                        @Field("nomCli") String nomCli,
                        @Field("phone") String phone,
                        @Field("typeCli") String typeCli,
                        @Field("email") String email,
                        @Field("adresse") String adresse,
                        @Field("parentId") String parentId);

        @PATCH("clients/{name}/update")
        Call<Client> updateclient(@Body Client var1, @Path("name") String var2);

        @GET("clients/search_all")
        Call<List<Client>> getClients();

        @GET("clients/search/all/{nom}")
        Call<List<Client>> getAllClientByValue(@Path("nom") String var1);

        @GET("organization/show/all")
        Call<List<Entreprise>> showAllEntreprise();

        @GET("organization/{uid}/show")
        Call<Entreprise> showEntreprise(@Path("uid") String var1);

        @GET("supplier/mysuppliers/{name}")
        Call<List<Fournisseur>> showSuppliersByName(@Path("name") String var1);

        @POST("supplier/save/point")
        Call<Fournisseur> saveSupplier(@Body Fournisseur var1);

        @PATCH("supplier/{uid}/update")
        Call<Fournisseur> updateForunisseur(@Body Fournisseur var1, @Path("uid") String var2);

        @DELETE("supplier/{uid}/delete")
        Call<Void> deleteForunisseur(@Path("uid") String var1);

        @GET("supplier/search/all/{nom}")
        Call<List<Fournisseur>> getSuppliersByValue(@Path("nom") String var1);

        @GET("supplier/{uid}/show")
        Call<Fournisseur> showSupplier(@Path("uid") String var1);

        @GET("lignevente/{uid}")
        Call<LigneVente> showLigneVente(@Path("uid") String var1);

        @GET("lignevente/sales/items")
        Call<List<LigneVente>> getVenteItems();

        @GET("lignevente/sales/items/pour/{entr}")
        Call<List<LigneVente>> getSaleItems(@Path("entr") String var1);

        @GET("lignevente/bill/items/{vid}")
        Call<List<LigneVente>> getItemsFor(@Path("vid") String var1);

        @GET("lignevente/for/sale/{vid}")
        Call<List<LigneVente>> getSalesItems(@Path("vid") String var1);

        @GET("lignevente/show/reccent/{vid}")
        Call<List<LigneVente>> refreshSaleItemsFor(@Path("vid") int vid);

        @POST("lignevente/save/point")
        Call<LigneVente> saveLigneVente(@Body List<LigneVente> var1);

        @POST("lignevente/one/save/point")
        Call<LigneVente> saveOneLigneVente(@Body LigneVente var1);

        @GET("livraison/{uid}/show")
        Call<Livraison> showLivraison(@Path("uid") String var1);

        @GET("livraison/reccent500")
        Call<List<Livraison>> refreshLivraison();

        @POST("livraison/save/point")
        Call<Livraison> saveLivraison(@Body Livraison var1);

        @PATCH("livraison/{uid}/update")
        Call<Livraison> updateLivraison(@Path("uid") String var1, @Body Livraison var2);

        @DELETE("livraison/{uid}/delete")
        Call<Void> deleteLivraison(@Path("uid") String var1);

        @FormUrlEncoded
        @PATCH("livraison/x-sync")
        Call<Livraison> syncDelivery(@Field("uid") String uid,
                        @Field("npiece") String numPiece,
                        @Field("dateLivr") String dateLivr,
                        @Field("reference") String reference,
                        @Field("libelle") String libelle,
                        @Field("reduction") String reduction,
                        @Field("observation") String observation,
                        @Field("topay") String topay,
                        @Field("payed") String payed,
                        @Field("ramained") String remained,
                        @Field("toreceive") String toreceive,
                        @Field("fournId") String fournId);

        @GET("livraison/fournisseur/{id}")
        Call<List<Livraison>> showLivraisons4FournId(@Path("id") String var1);

        @GET("livraison/all/fourniss/name/{fss}")
        Call<List<Livraison>> showLivraisons4FournName(@Path("fss") String var1);

        @GET("livraison/show/all/{entr}")
        Call<List<Livraison>> showMyLivraisons(@Path("entr") String entr);

        @GET("mesures/show/all/for/produit/{codebar}")
        Call<List<Mesure>> getMesureForProduct(@Path("codebar") String var1);

        @POST("mesures/save/list/point")
        Call<List<Mesure>> saveMesurList(@Body List<Mesure> var1);

        @GET("mesures/show/for/product/{uidpro}")
        Call<List<Mesure>> refreshMesureFor(@Path("uidpro") String uidPro);

        @POST("mesures/syncdown/{entr}")
        Call<List<Mesure>> syncDownMesures(@Body List<String> prodIds, @Path("entr") String entrep);

        @GET("mesures/show/all/for/produit/loc/{rccm}/{codebar}/{region}")
        Call<List<Mesure>> getMesureForProductInRegion(@Path("rccm") String var1, @Path("region") String var2,
                        @Path("codebar") String var3);

        @POST("mesures/save/point")
        Call<Mesure> saveMesure(@Body Mesure var1);

        @GET("mesures/{uid}/show")
        Call<Mesure> showMesure(@Path("uid") String var1);

        @PATCH("mesures/{uid}/update")
        Call<Mesure> updateMesure(@Path("uid") String var1, @Body Mesure var2);

        @GET("mesures/show/all/my/mesures/{entr}")
        Call<List<Mesure>> getAllMesures(@Path("entr") String entrep);

        @GET("modules/update-check")
        Call<data.Module> checkUpdates();

        @POST("notification/news/event")
        Call<Void> notifyEvent(@Query("id") String var1, @Query("sender") String var2, @Query("receiver") String var3,
                        @Query("region") String var4, @Query("contenu") String var5, @Query("facture") String var6);

        @GET("charges/show/all/for/region/{region}/period/{date1}/{date2}")
        Call<List<Operation>> getExpenseForRegionInPeriod(@Path("region") String var1, @Path("date1") String var2,
                        @Path("date2") String var3);

        @GET("charges/show/all/regs/for/period/{date1}/{date2}")
        Call<List<Operation>> getExpensesForAllInPeriod(@Path("date1") String var1, @Path("date2") String var2);

        @GET("charges/show/all/ops/for/region/{region}")
        Call<List<Operation>> getExpenseForRegion(@Path("region") String var1);

        @GET("charges/show/all/sums/for/region/{region}/{date1}/{date2}")
        Call<List<Operation>> getSumExpenseForRegionInPeriod(@Path("region") String var1, @Path("date1") String var2,
                        @Path("date2") String var3);

        @GET("charges/show/all/sums/for/period/{date1}/{date2}")
        Call<List<Operation>> getSumExpensesForAllInPeriod(@Path("date1") String var1, @Path("date2") String var2);

        @POST("charges/save/point")
        Call<Operation> saveOperation(@Body Operation var1);

        @GET("charges/show/allops")
        @Deprecated
        Call<List<Operation>> getMyOperations();

        @GET("charges/show/allops/{entr}")
        Call<List<Operation>> getOperations(@Path("entr") String entr);

        @POST("charges/operations")
        Call<List<Operation>> getOperations(@Body Entreprise entr);

        @PATCH("charges/{uid}/update")
        Call<Operation> updateOperation(@Path("uid") String var1, @Body Operation var2);

        @GET("charges/{uid}/show")
        Call<Operation> showOperation(@Path("uid") String var1);

        @GET("charges/{uid}/delete")
        Call<Void> deleteOperation(@Path("uid") String var1);

        /**
         *
         * @param mesure mesure du produit
         * @param requis idrecquisiion
         * @param qmin   quantite minimum
         * @param qmax   quantite maximum
         * @param devise devise
         * @param pvu    prix de vente unitaire
         * @return
         */
        @POST("prices/save/point")
        Call<PVUnit> savePrixVente(
                        @Query("mesr") String mesure,
                        @Query("recq") String requis,
                        @Query("qmin") String qmin,
                        @Query("qmax") String qmax,
                        @Query("devz") String devise,
                        @Query("uprice") String pvu);

        @POST("prices/save/jpoint")
        Call<PrixDeVente> savePrice(@Body PrixDeVente prixdevente);

        @GET("prices/all/prix/for/{entr}")
        Call<List<PVUnit>> showPricesForReq(@Path("entr") String var1);

        @GET("prices/all/prix/for/{entr}")
        Call<List<PrixDeVente>> getPrices(@Path("entr") String var1);

        @GET("prices/show/quant/produit/{codebar}/{quant}/{mesure_name}")
        Call<PrixDeVente> getPrixDeventeForProduct(@Path("codebar") String var1, @Path("mesure_name") String var2,
                        @Path("quant") String var3);

        @GET("supplier/show/for/{entr}")
        Call<List<Fournisseur>> getSupplier(@Path("entr") String entr);

        @GET("prices/{uid}/show")
        Call<PrixDeVente> showPrixDeVente(@Path("uid") String var1);

        @GET("prices/show/all")
        Call<List<PrixDeVente>> showPrices();

        @GET("prices/syncdown/{entr}")
        Call<List<PrixDeVente>> syncPricesDown(@Body List<String> reqIds, @Path("entr") String entr);

        @GET("prices/for/recq/{idrq}")
        Call<List<PrixDeVente>> syncPrices(@Path("idrq") String req);

        @PATCH("prices/{uid}/update")
        Call<PrixDeVente> updatePrixDeVente(@Path("uid") String var1, @Body PrixDeVente var2);

        @DELETE("charges/{uid}/delete")
        Call<Void> deletePrixDeVente(@Path("uid") String var1);

        @GET("produit/{codebar}/show")
        Call<Produit> showProduit(@Path("codebar") String var1);

        @POST("produit/save/endpoint")
        Call<Produit> saveProduit(@Body Produit var1);

        @PATCH("produit/{codebar}/update")
        Call<Produit> updateProduit(@Path("codebar") String var1, @Body Produit var2);

        @DELETE("produit/{uid}")
        Call<Void> deleteProduit(@Path("uid") String var1);

        @GET("produit/show/one/codebar/{codebar}")
        Call<Produit> scanProduit(@Path("codebar") String var1);

        @GET("produit/show/one/name/{prod}")
        Call<Produit> findProduitByName(@Path("prod") String var1);

        @POST("produit/articles")
        Call<List<Produit>> allProduit(@Body Entreprise entr);

        @POST("req/save/point")
        Call<Recquisition> saveRecquisition(@Body Recquisition var1);

        @POST("req/save/pooled/point")
        Call<Pool> savePooledData(@Body Pool var1);

        @POST("req/dataset/input")
        Call<InputSet> saveLiteInput(@Body InputSet var1);

        @POST("req/show/all")
        Call<List<Recquisition>> showAllRecquisitionForMyCompany(@Body Entreprise entr);

        @GET("req/show/all/{entr}")
        Call<List<Recquisition>> getRecquisitions(@Path("entr") String entr);

        @GET("req/reccent500")
        Call<List<Recquisition>> refreshRecquisitions();

        @GET("req/show/product/codebar/{codebar}")
        Call<List<Recquisition>> showRecqForCodebar(@Path("codebar") String var1);

        @PATCH("req/upd/all/prices/{region}/{percent}")
        Call<String> updatePricesWithPercentForRegion(@Path("region") String var1, @Path("percent") String var2);

        @PATCH("req/upd/all/prices/allregion/{percent}")
        Call<String> updatePricesWithPercent(@Path("percent") String var1);

        @GET("req/{uid}/show")
        Call<Recquisition> showRecquisition(@Path("uid") String var1);

        @PATCH("req/{uid}/update")
        Call<Recquisition> updateRecquisition(@Path("uid") String var1, @Body Recquisition var2);

        @DELETE("req/{uid}/delete")
        Call<Void> deleteRecquisition(@Path("uid") String var1);

        @FormUrlEncoded
        @PATCH("req/x-sync")
        Call<Recquisition> syncRecquisition(@Field("uid") String uid,
                        @Field("dateReq") String dateReq,
                        @Field("observation") String observation,
                        @Field("reference") String reference,
                        @Field("quantite") String quantite,
                        @Field("coutAch") String coutAchat,
                        @Field("dateExp") String dateExp,
                        @Field("alerte") String alerte,
                        @Field("mesureId") String mesureId,
                        @Field("productId") String productId,
                        @Field("region") String region,
                        @Field("numlot") String numlot);

        @GET("stocks/inventaire/{date1}/{date2}")
        Call<List<VuStock>> showInventaireStrockAllProduit(@Path("date1") String var1, @Path("date2") String var2);

        @GET("stocks/show/reccent/{livid}")
        Call<List<Stocker>> refreshStocksFor(@Path("livid") String livrid);

        @POST("stocks/storein/save/point")
        Call<Stocker> saveStockage(@Body Stocker var1);

        @GET("stocks/show/all")
        Call<List<Stocker>> getAllMyStock();

        @GET("stocks/show/all/{entr}")
        Call<List<Stocker>> getStockages(@Path("entr") String entr);

        @POST("stocks/storage")
        Call<List<Stocker>> getStorages(@Body Entreprise entr);

        @DELETE("stocks/{uid}/delete")
        Call<Void> deleteStocker(@Path("uid") String var1);

        @POST("stocks/storeout/save/point")
        Call<Destocker> saveDestockage(@Body Destocker var1);

        @GET("stocks/fiche/{produit}/{date1}/{date2}")
        Call<List<VuStock>> showMouvementStockProduit(@Path("produit") String var1, @Path("date1") String var2,
                        @Path("date2") String var3);

        @FormUrlEncoded
        @PATCH("stocks/x-sync")
        Call<Stocker> syncStockage(@Field("uid") String uid,
                        @Field("datestok") String date,
                        @Field("coutAchat") String coutAch,
                        @Field("dateExp") String dateExp,
                        @Field("stockAlert") String alerte,
                        @Field("quantite") String quantite,
                        @Field("libelle") String libelle,
                        @Field("localisation") String local,
                        @Field("region") String region,
                        @Field("prixAchatTot") String prixAchTot,
                        @Field("observation") String observation,
                        @Field("livraisonId") String livraisonId,
                        @Field("mesureId") String mesureId,
                        @Field("productId") String productId,
                        @Field("numlot") String numlot);

        @POST("taxews/save/point")
        Call<Taxe> saveTaxe(@Body Taxe var1);

        @GET("taxews/read/retrieve/All")
        Call<List<Taxe>> showAllTaxes();

        @GET("taxews/show/taxes/for/{nom}")
        Call<List<Taxer>> showAllPaidTaxe(@Path("nom") String var1);

        @GET("taxews/show/{descr}")
        Call<Taxe> showTaxeByName(@Path("descr") String var1);

        @GET("taxews/{uid}/show")
        Call<Taxe> showTaxe(@Path("uid") String var1);

        @PATCH("taxews/{uid}/update")
        Call<Taxe> showTaxe(@Path("uid") String var1, @Body Taxe var2);

        @DELETE("taxews/{uid}/delete")
        Call<Void> deleteTaxe(@Path("uid") String var1);

        @POST("taxer/save/point")
        Call<Taxer> saveTaxer(@Body Taxer var1);

        @GET("taxer/{uid}")
        Call<Taxer> showTaxer(@Path("uid") String var1);

        @GET("taxer/show/all")
        Call<List<Taxer>> showAllTaxer();

        @GET("taxer/show/all/{entr}")
        Call<List<Taxer>> getTaxers(@Path("entr") String entr);

        @DELETE("taxer/{uid}")
        Call<Void> deleteTaxer(@Path("uid") String var1);

        @GET("traisorerie/show/in/all")
        Call<List<Traisorerie>> showAugmentationCaisse();

        @GET("traisorerie/show/out/all")
        Call<List<Traisorerie>> showDimunitionCaisse();

        @GET("traisorerie/show/all/mine")
        @Deprecated
        Call<List<Traisorerie>> getAllMyTresorMouvs();

        @GET("traisorerie/show/all/mine/{entr}")
        Call<List<Traisorerie>> getTraisories(@Path("entr") String entr);

        @GET("traisorerie/show/in/all/{entr}")
        Call<List<Traisorerie>> showAugmentationCaisse(@Path("entr") String entr);

        @POST("traisorerie/mouvements")
        Call<List<Traisorerie>> getTraisories(@Body Entreprise entr);

        @POST("traisorerie/save/point")
        Call<Traisorerie> saveTraisorerie(@Body Traisorerie var1);

        @PATCH("traisorerie/{uid}/update")
        Call<Traisorerie> updateTraisorerie(@Path("uid") String var1, @Body Traisorerie var2);

        @DELETE("traisorerie/{uid}/delete")
        Call<Void> deleteTraisorerie(@Path("uid") String var1);

        @GET("traisorerie/show/region/{region}/{date1}/{date2}")
        Call<List<Traisorerie>> showTresorParTypeParRegion(@Path("region") String var1, @Path("date1") String var2,
                        @Path("date2") String var3);

        @GET("traisorerie/show/entree/region/{region}/{date1}/{date2}")
        Call<List<Traisorerie>> showEntreeParTypeParRegion(@Path("region") String var1, @Path("date1") String var2,
                        @Path("date2") String var3);

        @GET("traisorerie/show/sortie/region/{region}/{date1}/{date2}")
        Call<List<Traisorerie>> showSortieParTypeParRegion(@Path("region") String var1, @Path("date1") String var2,
                        @Path("date2") String var3);

        @GET("traisorerie/etat/{region}/{type}")
        Call<NetCash> showTreasuryStatus(@Path("region") String var1, @Path("type") String var2);

        @GET("traisorerie/show/enks/all/{date1}/{date2}")
        Call<List<Traisorerie>> allEntreeCaisse(@Path("date1") String var1, @Path("date2") String var2);

        @GET("traisorerie/showVente/dks/all/{date1}/{date2}")
        Call<List<Traisorerie>> allSortieCaisse(@Path("date1") String var1, @Path("date2") String var2);

        @PATCH("traisorerie/x-sync")
        Call<Traisorerie> saveCash(@Body Traisorerie tr);

        @GET("user/show/any/{phone}")
        Call<User> showByPhone(@Path("phone") String var1);

        @GET("user/show/userBy/{phone}")
        Call<User> showUserByPhone(@Path("phone") String var1);

        @GET("user/securely/show/userBy/{phone}")
        Call<User> showUserByPhoneSecurely(@Path("phone") String var1);

        @GET("user/{uid}/show/profile")
        Call<User> showUser(@Path("uid") String var1);

        @GET("user/search/any/{value}")
        Call<List<User>> searchUser(@Path("value") String var1);

        @POST("subscription/bonus/save/point")
        Call<Abonnement> saveFirstSubscription(@Body Abonnement var1);

        @GET("user/myaccount/confirm/{email}/{code}/{lang}")
        Call<String> confirmByMail(@Path("email") String var1, @Path("code") String var2, @Path("lang") String lang);

        @GET("user/validate/by/sms/{phone}/{code}/{lang}")
        Call<String> validateBySms(@Path("phone") String var1, @Path("code") String var2, @Path("lang") String lang);

        @GET("ventes/sales/show/{date1}/{date2}")
        Call<List<Vente>> showVentes(@Path("date1") String var1, @Path("date2") String var2);

        @GET("ventes/client/allvente/{phone}")
        Call<List<Vente>> showVenteForCLient(@Path("phone") String var1);

        @GET("ventes/reccent500")
        Call<List<Vente>> refreshVentes();

        @PATCH("ventes/{uid}/update/duedate")
        Call<String> updateDueDate(@Path("uid") String var1, @Body Vente v);

        @GET("ventes/all/my/sales")
        @Deprecated
        Call<List<Vente>> getMySales();

        @GET("ventes/all/my/sales/{entr}")
        Call<List<Vente>> getMySales(@Path("entr") String e);

        @PATCH("inventory/save")
        Call<ResponseBody> createInventaire(@Body Inventaire e);

        @POST("inventory/savejson")
        Call<Inventaire> createInventair(@Body Inventaire e);

        @PATCH("inventory/savecount")
        Call<Compter> createCompter(@Body Compter content);

        @POST("ventes/sales")
        Call<List<Vente>> getSales(@Body Entreprise eze);

        @GET("ventes/{uid}/show/its/client")
        Call<Client> getClientForThisSale(@Path("uid") String var1);

        @GET("ventes/search/{ref}/{ent}")
        Call<SoldeDetteClient> searchPayment(@Path("ref") String ref, @Path("ent") String ent);

        @POST("ventes/sales/for/credit")
        Call<List<Vente>> getCreditSales(@Body Entreprise entr);

        @GET("ventes/marges/{date1}/{date2}")
        Call<Resultat> getMargesInUSD(@Path("date1") String var1, @Path("date2") String var2);

        @POST("ventes/save/cash/point")
        Call<VenteResult> saveVenteCash(@Body Vente var1);

        @GET("ventes/sales/show/notified/{ref}")
        Call<Vente> getNotifiedVente(@Path("ref") String var1);

        @DELETE("ventes/{uid}/delete")
        Call<Vente> deleteSale(@Path("uid") String var1);

        @POST("ventes/x-sync")
        Call<Vente> syncSale(@Body VenteHelper helper);
        
        @POST("ventes/modify")
        Call<Vente> modifySale(@Body VenteHelper helper);


        @GET("ventes/{uid}/show")
        Call<Vente> showVente(@Path("uid") String var1);

        @PATCH("taxer/sync")
        Call<String> syncTaxer(@Body List<Taxer> var1);

        // upsync methods
        @PATCH("produit/web/sync")
        Call<WebResult> syncProduct(@Body List<ProductMarshalAdapter> produx);

        @PATCH("clients/sync")
        Call<WebResult> syncClient(@Body List<Client> var1);

        @PATCH("supplier/sync")
        Call<WebResult> syncFournisseur(@Body List<Fournisseur> var1);

        @PATCH("ventes/sync")
        Call<WebResult> syncVentes(@Body List<VenteHelper> vhelpers);

        @PATCH("charges/sync")
        Call<WebResult> syncOperations(@Body List<Operation> var1);

        @PATCH("prices/sync")
        Call<WebResult> syncPrices(@Body List<PrixDeVente> var1);

        @PATCH("tresor/sync")
        public Call<WebResult> syncCompteTresor(@Body List<CompteTresor> comptes);

        @PATCH("tresor/sync")
        public Call<WebResult> syncInventories(@Body List<Inventaire> invents);

        @PATCH("inventory/comptage/sync")
        public Call<WebResult> syncCounts(@Body List<Compter> counts);

        @PATCH("req/sync")
        Call<WebResult> syncRecquisition(@Body List<Recquisition> var1);

        @PATCH("traisorerie/sync")
        Call<WebResult> syncTraisorerie(@Body List<Traisorerie> var1);

        @PATCH("stocks/sync")
        Call<WebResult> syncStocks(@Body List<Stocker> stoks);

        @PATCH("mesures/sync")
        Call<WebResult> syncMesure(@Body List<Mesure> var1);

        @PATCH("lignevente/sync")
        Call<WebResult> syncLigneVente(@Body List<LigneVente> var1);

        @PATCH("livraison/sync")
        Call<WebResult> syncLivraison(@Body List<Livraison> var1);

        @PATCH("expenses/sync")
        Call<WebResult> syncDepense(@Body List<Depense> var1);

        @PATCH("retour-stock/sync")
        Call<WebResult> syncRetourStock(@Body List<RetourDepot> var1);

        @PATCH("categories/sync")
        Call<WebResult> syncCategories(@Body List<Category> elts);

        @PATCH("destockage/sync")
        Call<WebResult> syncDestockage(@Body List<Destocker> var1);

        @PATCH("tresor/sync")
        Call<WebResult> syncTresor(@Body List<CompteTresor> var1);

        @PATCH("matieres-industrial/sync")
        Call<WebResult> syncMatiere(@Body List<Matiere> var1);

        @PATCH("matieres-sku/sync")
        Call<WebResult> syncMatiereSku(@Body List<MatiereSku> var1);

        @PATCH("entreposages/sync")
        Call<WebResult> syncEntreposage(@Body List<Entreposer> var1);

        @PATCH("production/sync")
        Call<WebResult> syncProduction(@Body List<Production> var1);

        @PATCH("repartitions/sync")
        Call<WebResult> syncRepartir(@Body List<Repartir> var1);

        @PATCH("imputers/sync")
        Call<WebResult> syncImputer(@Body List<Imputer> var1);

        @PATCH("depots-industrial/sync")
        Call<WebResult> syncDepot(@Body List<Depot> var1);

        @PATCH("commande/sync")
        Call<WebResult> syncCommande(@Body List<Commande> var1);

        @PATCH("commande-lines/sync")
        Call<WebResult> syncCommandeLister(@Body List<CommandeLister> var1);

        @PATCH("expedition/sync")
        Call<WebResult> syncExpedier(@Body List<Expedition> var1);

        @PATCH("satisfaire/sync")
        Call<WebResult> syncSatisfaire(@Body List<Satisfaire> var1);

        // end upsync

        @FormUrlEncoded
        @PATCH("prices/x-sync")
        Call<PrixDeVente> savePrice(@Field("uid") String uid,
                        @Field("qmin") String qmin,
                        @Field("qmax") String qmax,
                        @Field("uprice") String prixUn,
                        @Field("devise") String devise,
                        @Field("mesureId") String mesureId,
                        @Field("requisId") String requisId,
                        @Field("entr") String entrep);

        @GET("safeload/check-before/{table}/{uidelt}")
        Call<Checked> checkDependancy(@Path("table") String table, @Path("uidelt") String uidelt);

        @GET("safesync/categories/{entr}/{first}")
        Call<List<Category>> syncDownCategories(@Path("entr") String entr, @Path("first") String dcount);

        @GET("safesync/destocks/{entr}/{first}")
        Call<List<Destocker>> syncDownDestockers(@Path("entr") String entr, @Path("first") String dcount);

        @GET("safesync/ligneventes/{entr}/{first}")
        Call<List<LigneVente>> syncDownLigneVentes(@Path("entr") String entr, @Path("first") String dcount);

        @GET("safesync/livraisons/{entr}/{first}")
        Call<List<Livraison>> syncDownLivraisons(@Path("entr") String entr, @Path("first") String dcount);

        @GET("safesync/mesures/{entr}/{first}")
        Call<List<Mesure>> syncDownMesures(@Path("entr") String entr, @Path("first") String dcount);

        @GET("safesync/operations/{entr}/{first}")
        Call<List<Operation>> syncDownOperations(@Path("entr") String entr, @Path("first") String dcount);

        @GET("safesync/prices/{entr}/{first}")
        Call<List<PrixDeVente>> syncDownPrices(@Path("entr") String entr, @Path("first") String dcount);

        @GET("safesync/produits/{entr}/{first}")
        Call<List<Produit>> syncDownProducts(@Path("entr") String entr, @Path("first") String dcount);

        @GET("safesync/recquisitions/{entr}/{first}")
        Call<List<Recquisition>> syncDownRecquisitions(@Path("entr") String entr, @Path("first") String dcount);

        @GET("safesync/stocks/{entr}/{first}")
        Call<List<Stocker>> syncDownStockers(@Path("entr") String entr, @Path("first") String dcount);

        @GET("safesync/taxers/{entr}/{first}")
        Call<List<Taxer>> syncDownTaxers(@Path("entr") String entr, @Path("first") String dcount);

        @GET("safesync/traisories/{entr}/{first}")
        Call<List<Traisorerie>> syncDownTraisories(@Path("entr") String entr, @Path("first") String dcount);

        @GET("safesync/sales/{entr}/{first}")
        Call<List<Vente>> syncDownVentes(@Path("entr") String entr, @Path("first") String dcount);

        @GET("safesync/suppliers/{entr}/{first}")
        Call<List<Fournisseur>> syncDownSuppliers(@Path("entr") String entr, @Path("first") String dcount);

        @GET("safesync/customers/{entr}/{first}")
        Call<List<Client>> syncDownCustomers(@Path("entr") String entr, @Path("first") String dcount);

        @PUT("tresor/{name}/save/point")
        public Call<CompteTresor> saveCompteTesor(@Path("name") String id, @Body CompteTresor ctr);

        @POST("expenses/{name}/save/point")
        public Call<Depense> saveDepense(@Path("name") String id, @Body Depense d);

        @FormUrlEncoded
        @PATCH("tresor/x-sync")
        public Call<CompteTresor> saveCompteTresorByForm(@Field("uid") String uid,
                        @Field("bankname") String bankname,
                        @Field("intitule") String intitule,
                        @Field("solde") double soleMin,
                        @Field("numeroCompte") String numeroCompte,
                        @Field("region") String region,
                        @Field("typeCompte") String typeCompte);

        @GET("perms/load/available")
        public Call<List<Permission>> loadPermissions();

        @GET("perms/load/for/{engId}")
        public Call<List<Permission>> loadAgentPermission(@Path("engId") String engagementId);

        @PATCH("perms")
        @FormUrlEncoded
        public Call<Affecter> createAffecter(@Field("uidPerm") String uidperm,
                        @Field("role_name") String rolename,
                        @Field("region") String region, @Field("emp") String empl);

        @GET("perms/change/{p}/{e}")
        public Call<String> changePermission(@Path("p") String permid, @Path("e") String empid);

        @POST("perms/role/save/point")
        public Call<Role> createRole(@Body Role role);

        @GET("perms/role/list")
        public Call<List<Role>> getRoles();

        @GET("perms/show/role")
        public Call<Role> getRoleWithName(@Query("name") String name);

        // httpsync
        @GET("categories/downsync")
        public Call<List<Category>> syncMissedCategories(@Query("lt") String timestamp);

        @GET("produit/downsync")
        public Call<List<Produit>> syncMissedProducts(@Query("lt") String timestamp);

        @GET("mesures/downsync")
        public Call<List<Mesure>> syncMissedMesures(@Query("lt") String timestamp);

        @GET("supplier/downsync")
        public Call<List<Fournisseur>> syncMissedSuppliers(@Query("lt") String timestamp);

        @GET("livraison/downsync")
        public Call<List<Livraison>> syncMissedDeliveries(@Query("lt") String timestamp);

        @GET("stocks/downsync")
        public Call<List<Stocker>> syncMissedStocks(@Query("lt") String timestamp);

        @GET("destockage/downsync")
        public Call<List<Destocker>> syncMissedDestokers(@Query("lt") String timestamp);

        @GET("req/downsync")
        public Call<List<Recquisition>> syncMissedRecquisitions(@Query("lt") String timestamp);

        @GET("prices/downsync")
        public Call<List<PrixDeVente>> syncMissedPrices(@Query("lt") String timestamp);

        @GET("clients/downsync")
        public Call<List<Client>> syncMissedClients(@Query("lt") String timestamp);

        @GET("ventes/downsync")
        public Call<List<Vente>> syncMissedSales(@Query("lt") String timestamp);

        @GET("lignevente/downsync")
        public Call<List<LigneVente>> syncMissedSaleItems(@Query("lt") String timestamp);

        @GET("traisorerie/downsync")
        public Call<List<Traisorerie>> syncMissedTransactions(@Query("lt") String timestamp);

        @GET("tresor/downsync")
        public Call<List<CompteTresor>> syncMissedAccounts(@Query("lt") String timestamp);

        @GET("inventory/counts/downsync")
        public Call<List<Compter>> syncMissedCounts(@Query("lt") String timestamp);

        @GET("inventory/downsync")
        public Call<List<Inventaire>> syncMissedInventaires(@Query("lt") String timestamp);

        @GET("charges/downsync")
        public Call<List<Operation>> syncMissedOperations(@Query("lt") String timestamp);

        @GET("expenses/downsync")
        public Call<List<Depense>> syncMissedDepenses(@Query("lt") String timestamp);

        @GET("matieres-industrial/downsync")
        public Call<List<Matiere>> syncMissedMatieres(@Query("lt") String timestamp);

        @GET("matieres-sku/downsync")
        public Call<List<MatiereSku>> syncMissedMatiereSkus(@Query("lt") String timestamp);

        @GET("depots-industrial/downsync")
        public Call<List<Depot>> syncMissedDepots(@Query("lt") String timestamp);

        @GET("entreposages/downsync")
        public Call<List<Entreposer>> syncMissedEntreposages(@Query("lt") String timestamp);

        @GET("production/downsync")
        public Call<List<Production>> syncMissedProductions(@Query("lt") String timestamp);

        @GET("repartitions/downsync")
        public Call<List<Repartir>> syncMissedRepartirs(@Query("lt") String timestamp);

        @GET("imputers/downsync")
        public Call<List<Imputer>> syncMissedImputers(@Query("lt") String timestamp);

        @GET("expeditions/downsync")
        public Call<List<Expedition>> syncMissedExpeditions(@Query("lt") String timestamp);

        @GET("commandes/downsync")
        public Call<List<Commande>> syncMissedCommandes(@Query("lt") String timestamp);

        @GET("list-commandes/downsync")
        public Call<List<CommandeLister>> syncMissedCommandListers(@Query("lt") String timestamp);

}
