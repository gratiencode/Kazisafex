package data.network;

import data.ProduitHelper;
import java.util.HashMap;
import java.util.List;
import data.Abonnement;
import data.Category;
import data.Client;
import data.CompteTresor;
import data.Depense;
import data.Destocker;
import data.Employee; 
import data.Entreprise;
import data.Fournisseur;
import data.InputSet;
import data.LigneVente;
import data.Livraison;
import data.LoginResult;
import data.Mesure;
import data.MetaData;
import data.Module;
import data.Operation;
import data.PVUnit;
import data.Parametre;
import data.Pool;
import data.PrixDeVente;
import data.Produit;
import data.Recquisition;
import data.Stocker;
import data.Taxe;
import data.Taxer;
import data.Traisorerie;
import data.User;
import data.Vente;
import data.VenteResult;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import data.helpers.Checked;
import data.helpers.Credentials;
import data.helpers.Luggage;
import data.helpers.NetCash;
import data.helpers.Resultat;
import data.helpers.SoldeDetteClient;
import data.helpers.SubscriptionPayment;

import data.helpers.Token;
import data.helpers.VuStock;
import data.helpers.VueAbonnement;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;

public interface Kazisafe {

    @Multipart
    @POST("user/signup")
    Call<User> singUp(@Part okhttp3.MultipartBody.Part var1, @Part("file_desc") RequestBody var2, @Part("email") RequestBody var3, @Part("nom") RequestBody var4, @Part("prenom") RequestBody var5, @Part("phone") RequestBody var6, @Part("password") RequestBody var7);

    @Multipart
    @PATCH("user/{uid}/update")
    Call<User> updateProfile(@Part okhttp3.MultipartBody.Part var1, @Part("file_desc") RequestBody var2, @Part("email") RequestBody var3, @Part("nom") RequestBody var4, @Part("prenom") RequestBody var5, @Part("phone") RequestBody var6, @Part("password") RequestBody var7, @Path("uid") String var8);

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
    
    @POST("produit/upload/{entr}")
    Call<String> uploadProduit(@Path("entr") String entr, @Body Luggage lug);

    @GET("produit/show/all/{entr}")
    Call<List<Produit>> getProducts(@Path("entr") String entr);
    
    @POST("produit/save/web/point")
    Call<Produit> saveLite(@Body ProduitHelper produx);

    @GET("subscription/show/all")
    Call<List<Abonnement>> getAllAbonnement();

    @PATCH("subscription/{uid}/x0e243c99e250e30ab/u00901/x501/{state}/{entr}")
    Call<ResponseBody> notifySubscribed(@Path("uid") String path, @Path("state") String state, @Path("entr") String entr);

    @GET("params/{key}/param/view")
    Call<Parametre> getParameter(@Path("key") String var1);

    @GET("params/sync/icount/{table}/{entrep}")
    Call<ResponseBody> verifySync(@Path("table") String var1, @Path("entrep") String ese);

    @POST("subscription/save/point")
    Call<SubscriptionPayment> getAbonnementPayment(@Body SubscriptionPayment var1);

    @GET("repport/{rccm}/{date1}/{date2}")
    Call<VueAbonnement> showAbonnement(@Path("rccm") String var1, @Path("date1") String var2, @Path("date2") String var3);

    @GET("subscription/valide/check/login/{entr}")
    Call<Abonnement> checkSigninValidation(@Path("entr") String euid);

    @GET("subscription/{uid}/show")
    Call<Abonnement> getAbonnement(@Path("uid") String var1);

    @POST("auth/tr2")
    Call<String> signin(@Body Credentials var1);

    @POST("auth/auth0/desk/signin")
    Call<LoginResult> desktopSignin(@Body Credentials var1);

    @POST("auth/auth0/signin")
    Call<ResponseBody> login(@Body Credentials var1);

    @POST("auth/auth2/refresh/token")
    Call<ResponseBody> refreshToken(@Body Token var1);

    @GET("organization/check/{ident}")
    Call<Entreprise> getEntreprise(@Path("ident") String var1);

    @GET("organization/infolite/search/{uid}")
    Call<Entreprise> getEntrepriseInfo(@Path("uid") String var1);

    @GET("organization/newcomp/check/{ident}")
    Call<Entreprise> lookEntreprise(@Path("ident") String var1);

    @GET("categories/show/all")
    @Deprecated
    Call<List<Category>> getCategories();

    @POST("categories/allcats")
    Call<List<Category>> getCategories(@Body Entreprise entr);

    @GET("categories/show/all/{entr}")
    Call<List<Category>> getCategoriesByEuid(@Path("entr") String entr);

    @GET("categories/show/by/name/{nom_cat}")
    Call<List<Category>> getByName(@Path("nom_cat") String var1);

    @GET("categories/{name}/show")
    Call<Category> getCategoryProduct(@Path("name") String var1);

    @POST("categories/save/point")
    Call<ResponseBody> saveCategory(@Body Category var1);

    @GET("categories/{name}/update")
    Call<Category> updateCategory(@Path("name") String var1);

    @PATCH("categories/{entr}/sync")
    Call<String> syncCategories(@Path("entr") String euid, @Body List<Category> elts);

    @DELETE("categories/{name}/update")
    Call<Void> deleteCat(@Path("name") String var1);

    @GET("destockage/out/interval/{date1}/{date2}")
    Call<List<Destocker>> getDestockers(@Path("date1") String var1, @Path("date2") String var2);

    @GET("destockage/{uid}/show")
    Call<Destocker> getDestocker(@Path("uid") String var1);

    @POST("destockage/save/point")
    Call<Destocker> saveDestocker(@Body Destocker var1);

    @GET("destockage/dest/recq/{ref}/{prod}/{entr}")
    Call<Destocker> getDestockageByRef(@Path("ref") String var1, @Path("prod") String prod8, @Path("entr") String entr);

    @PATCH("destockage/{entr}/sync")
    Call<String> syncDestockage(@Path("entr") String entr, @Body List<Destocker> var1);
    
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

    @GET("engagee/show/all/regions/{entr}")
    Call<List<String>> getRegions(@Path("entr") String entr);

   
    @GET("engagee/show/employees/{entr}")
    Call<List<Employee>> findEmployees(@Path("entr") String entr);

    @DELETE("engagee/{uid}/update")
    Call<Void> deleteEngager(@Path("uid") String var1);

    @POST("clients/save")
    Call<Client> saveClient(@Body Client var1);

    @PATCH("clients/{name}/update")
    Call<Client> updateclient(@Body Client var1, @Path("name") String var2);

    @PATCH("clients/sync")
    Call<String> syncClient(@Body List<Client> var1);

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

    @PATCH("supplier/sync")
    Call<String> syncForunisseur(@Body List<Fournisseur> var1);

  
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

    
    @POST("lignevente/save/point")
    Call<LigneVente> saveLigneVente(@Body List<LigneVente> var1);

    @POST("lignevente/one/save/point")
    Call<LigneVente> saveOneLigneVente(@Body LigneVente var1);

    @PATCH("lignevente/sync")
    Call<String> syncLigneVente(@Body List<LigneVente> var1);

    @GET("livraison/{uid}/show")
    Call<Livraison> showLivraison(@Path("uid") String var1);

    @POST("livraison/save/point")
    Call<Livraison> saveLivraison(@Body Livraison var1);

    @PATCH("livraison/{uid}/update")
    Call<Livraison> updateLivraison(@Path("uid") String var1, @Body Livraison var2);

    @DELETE("livraison/{uid}/delete")
    Call<Void> deleteLivraison(@Path("uid") String var1);

    @PATCH("livraison/{entr}/sync")
    Call<String> syncLivraison(@Path("entr") String e, @Body List<Livraison> var1);
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
    Call<List<Mesure>> findMesuresForProduit(@Path("uidpro") String uidPro);

    @POST("mesures/syncdown/{entr}")
    Call<List<Mesure>> syncDownMesures(@Body List<String> prodIds, @Path("entr") String entrep);

    @GET("mesures/show/all/for/produit/loc/{rccm}/{codebar}/{region}")
    Call<List<Mesure>> getMesureForProductInRegion(@Path("rccm") String var1, @Path("region") String var2, @Path("codebar") String var3);

    @POST("mesures/save/point")
    Call<Mesure> saveMesure(@Body Mesure var1);

    @GET("mesures/{uid}/show")
    Call<Mesure> showMesure(@Path("uid") String var1);

    @PATCH("mesures/{uid}/update")
    Call<Mesure> updateMesure(@Path("uid") String var1, @Body Mesure var2);

    @GET("mesures/show/all/my/mesures/{entr}")
    Call<List<Mesure>> getAllMesures(@Path("entr") String entrep);

    @PATCH("mesures/sync")
    Call<String> syncMesure(@Body Luggage var1);


    @GET("modules/update-check")
    Call<Module> checkUpdates();

    @POST("notification/news/event")
    Call<Void> notifyEvent(@Query("id") String var1, @Query("sender") String var2, @Query("receiver") String var3, @Query("region") String var4, @Query("contenu") String var5, @Query("facture") String var6);

    @GET("charges/show/all/for/region/{region}/period/{date1}/{date2}")
    Call<List<Operation>> getExpenseForRegionInPeriod(@Path("region") String var1, @Path("date1") String var2, @Path("date2") String var3);

    @GET("charges/show/all/regs/for/period/{date1}/{date2}")
    Call<List<Operation>> getExpensesForAllInPeriod(@Path("date1") String var1, @Path("date2") String var2);

    @GET("charges/show/all/ops/for/region/{region}")
    Call<List<Operation>> getExpenseForRegion(@Path("region") String var1);

    @GET("charges/show/all/sums/for/region/{region}/{date1}/{date2}")
    Call<List<Operation>> getSumExpenseForRegionInPeriod(@Path("region") String var1, @Path("date1") String var2, @Path("date2") String var3);

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
     * @param qmin quantite minimum
     * @param qmax quantite maximum
     * @param devise devise
     * @param pvu prix de vente unitaire
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
    Call<PrixDeVente> getPrixDeventeForProduct(@Path("codebar") String var1, @Path("mesure_name") String var2, @Path("quant") String var3);
    
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

    @GET("produit/show/all")
    @Deprecated
    Call<List<Produit>> allProduit();

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

    @PATCH("req/{entr}/sync")
    Call<String> syncRecquisition(@Path("entr") String e, @Body List<Recquisition> var1);
    
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

    @POST("stocks/storein/save/point")
    Call<Stocker> saveStockage(@Body Stocker var1);

    @POST("stocks/cloture/save/point")
    Call<List<Stocker>> cloturerStock(@Body MetaData var1);

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
    Call<List<VuStock>> showMouvementStockProduit(@Path("produit") String var1, @Path("date1") String var2, @Path("date2") String var3);

    @PATCH("stocks/{entr}/sync")
    Call<String> syncStocks(@Path("entr") String e, @Body List<Stocker> var1);
    
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
    Call<List<Traisorerie>> showTresorParTypeParRegion(@Path("region") String var1, @Path("date1") String var2, @Path("date2") String var3);

    @GET("traisorerie/show/entree/region/{region}/{date1}/{date2}")
    Call<List<Traisorerie>> showEntreeParTypeParRegion(@Path("region") String var1, @Path("date1") String var2, @Path("date2") String var3);

    @GET("traisorerie/show/sortie/region/{region}/{date1}/{date2}")
    Call<List<Traisorerie>> showSortieParTypeParRegion(@Path("region") String var1, @Path("date1") String var2, @Path("date2") String var3);

    @GET("traisorerie/etat/{region}/{type}")
    Call<NetCash> showTreasuryStatus(@Path("region") String var1, @Path("type") String var2);

    @GET("traisorerie/show/enks/all/{date1}/{date2}")
    Call<List<Traisorerie>> allEntreeCaisse(@Path("date1") String var1, @Path("date2") String var2);

    @GET("traisorerie/showVente/dks/all/{date1}/{date2}")
    Call<List<Traisorerie>> allSortieCaisse(@Path("date1") String var1, @Path("date2") String var2);

    @PATCH("traisorerie/{entr}/sync")
    Call<String> syncTraisorerie(@Path("entr") String e, @Body List<Traisorerie> var1);

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

    @PATCH("ventes/{uid}/update/duedate")
    Call<String> updateDueDate(@Path("uid") String var1, @Body Vente v);

    @GET("ventes/all/my/sales")
    @Deprecated
    Call<List<Vente>> getMySales();

    @GET("ventes/all/my/sales/{entr}")
    Call<List<Vente>> getMySales(@Path("entr") String e);

    @POST("ventes/sales")
    Call<List<Vente>> getSales(@Body Entreprise eze);

    @GET("ventes/{uid}/show/its/client")
    Call<Client> getClientForThisSale(@Path("uid") String var1);

    @GET("ventes/search/{ref}/{ent}")
    Call<SoldeDetteClient> searchPayment(@Path("ref") String ref, @Path("ent") String ent);

    @GET("ventes/sales/show/credit")
    @Deprecated(since = "1.0")
    Call<List<Vente>> getCreditSales();

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

    @PATCH("ventes/{entr}/sync")
    Call<String> syncVentes(@Path("entr") String qdos, @Body List<Vente> var1);
    
    @FormUrlEncoded
    @POST("ventes/x-sync")
    Call<Vente> syncSale(@Field("uid") int uid,
            @Field("clientId") String clientId,
            @Field("reference") String reference,
            @Field("libelle") String libelle,
            @Field("observation") String observation,
            @Field("date") String dateVente,
            @Field("paymentMode") String paymentMode,
            @Field("montantCdf") double montantcdf,
            @Field("montantUsd") double montantusd,
            @Field("montantDette") double montantDette,
            @Field("latitude") double latitude,
            @Field("longitude") double longitude,
            @Field("region") String region,
            @Field("compteCaisse") String tresorId,
            @Field("transFinId") String transactionId,
            @Field("smsBill") String smsBill);

    @GET("ventes/{uid}/show")
    Call<Vente> showVente(@Path("uid") String var1);

    @PATCH("charges/{entr}/sync")
    Call<String> syncOperations(@Path("entr") String e, @Body List<Operation> var1);

    @PATCH("taxer/sync")
    Call<String> syncTaxer(@Body List<Taxer> var1);

    @PATCH("prices/sync")
    Call<String> syncPrices(@Body List<PrixDeVente> var1);

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

    @POST("tresor/{name}/save/point")
    public Call<CompteTresor> saveCompteTesor(@Path("name") String id,@Body CompteTresor ctr);

    @POST("expenses/{name}/save/point")
    public Call<Depense> saveDepense(@Path("name") String id,@Body Depense d);

    
}
