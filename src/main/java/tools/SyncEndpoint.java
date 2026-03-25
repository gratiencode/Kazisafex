/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import com.endeleya.kazisafex.EntrepriseController;
import com.endeleya.kazisafex.GoodstorageController;
import com.endeleya.kazisafex.PosController;
import delegates.AretirerDelegate;
import delegates.CategoryDelegate;
import delegates.ClientAppartenirDelegate;
import delegates.ClientDelegate;
import delegates.ClientOrganisationDelegate;
import delegates.CompteTresorDelegate;
import delegates.DepenseDelegate;
import delegates.DestockerDelegate;
import delegates.FactureDelegate;
import delegates.FournisseurDelegate;
import delegates.LigneVenteDelegate;
import delegates.LivraisonDelegate;
import delegates.MesureDelegate;
import delegates.OperationDelegate;
import delegates.PrixDeVenteDelegate;
import delegates.ProduitDelegate;
import delegates.RecquisitionDelegate;
import delegates.RetourDepotDelegate;
import delegates.RetourMagasinDelegate;
import delegates.StockerDelegate;
import delegates.TraisorerieDelegate;
import delegates.VenteDelegate;
import util.decoders.ImageProductDecoder;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.CloseReason;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.EncodeException;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import jakarta.xml.bind.DatatypeConverter;
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
import data.RetourDepot;
import data.RetourMagasin;
import data.Stocker;
import data.Traisorerie;
import data.Vente;
import jakarta.websocket.ClientEndpointConfig;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import util.decoders.BaseModelDecoder;
import util.encoders.AbonnementEncoder;
import util.encoders.AretirerEncoder;
import util.encoders.CategoryEncoder;
import util.encoders.ClientAppartenirEncoder;
import util.encoders.ClientEncoder;
import util.encoders.ClientOrganisationEncoder;
import util.encoders.CompteTresorEncoder;
import util.encoders.DepenseEncoder;
import util.encoders.DestockerEncoder;
import util.encoders.FactureEncoder;
import util.encoders.FournisseurEncoder;
import util.encoders.ImageProductEncoder;
import util.encoders.LigneVenteEncoder;
import util.encoders.LivraisonEncoder;
import util.encoders.MesureEncoder;
import util.encoders.OperationEncoder;
import util.encoders.PrixDeVenteEncoder;
import util.encoders.ProduitEncoder;
import util.encoders.RecquisitionEncoder;
import util.encoders.RefresherEncoder;
import util.encoders.RetourDepotEncoder;
import util.encoders.RetourMagasinEncoder;
import util.encoders.StockerEncoder;
import util.encoders.TraisorerieEncoder;
import util.encoders.VenteEncoder;
import util.listencoders.ListEncoder;
import utilities.ImageProduit;

/**
 *
 * @author eroot
 */
public class SyncEndpoint {

    private static SyncEndpoint instance;
    private OnWebsocketCloseListener onWebsocketCloseListener;

    public static SyncEndpoint getInstance() {
        return instance;
    }
    Preferences pref = Preferences.userNodeForPackage(SyncEngine.class);
    int close = 0;

    public SyncEndpoint() {
    }
    Session session;

    public SyncEndpoint(String uri, String token) {
        super();
        try {
            connect(uri, token);
            instance = this;
            pref.putInt("exit", 0);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(SyncEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void connect(String uri, String token) throws NoSuchAlgorithmException {
        WebSocketContainer wscontainer = ContainerProvider.getWebSocketContainer();
        ClientEndpointConfig.Configurator configurator = new ClientEndpointConfig.Configurator() {
            @Override
            public void beforeRequest(Map<String, List<String>> headers) {
                headers.put("Authorization", Collections.singletonList("Bearer " + token));
            }
        };
        try {
            ClientEndpointConfig config = ClientEndpointConfig.Builder.create()
                    .configurator(configurator)
                    .decoders(List.of(BaseModelDecoder.class, ImageProductDecoder.class))
                    .encoders(List.of(AbonnementEncoder.class, AretirerEncoder.class, CategoryEncoder.class, ProduitEncoder.class, MesureEncoder.class, FournisseurEncoder.class, ListEncoder.class, RefresherEncoder.class,
                            LivraisonEncoder.class, StockerEncoder.class, DestockerEncoder.class, RecquisitionEncoder.class, PrixDeVenteEncoder.class,
                            ClientEncoder.class, ClientAppartenirEncoder.class, ClientOrganisationEncoder.class, VenteEncoder.class, LigneVenteEncoder.class, ImageProductEncoder.class,
                            FactureEncoder.class, CompteTresorEncoder.class, TraisorerieEncoder.class, DepenseEncoder.class, OperationEncoder.class, RetourMagasinEncoder.class,
                            RetourDepotEncoder.class, ListEncoder.class))
                    .build();

            URI urlx = URI.create(uri);
            WsClientEndpoint endpoint = new WsClientEndpoint();
            session = wscontainer.connectToServer(endpoint, config, urlx);
            endpoint.setOnWebsocketCloseListener((Session ss, boolean isClosing) -> {
                if (isClosing) {
                    if (onWebsocketCloseListener != null) {
                        onWebsocketCloseListener.onWebSocketClose(ss, isClosing);
                    }
                }
            });
        } catch (DeploymentException ex) {
            Logger.getLogger(SyncEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            MainUI.notify(null, "Erreur", "Veuillez vérifier la qualité de votre connection internet", 6, "error");
            Logger.getLogger(SyncEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void sendObject(BaseModel model) {
        try {
            if (this.session != null) {
                this.session.getBasicRemote().sendObject(model);
            }
        } catch (IOException e) {
            Logger.getLogger(SyncEndpoint.class.getName()).log(Level.SEVERE, e.getMessage());
        } catch (EncodeException ex) {
            Logger.getLogger(SyncEndpoint.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void closeSession() {
        if (this.session != null) {
            close = pref.getInt("exit", 0);
            if (close == 1) {
                try {
                    session.close();
                } catch (IOException ex) {
                    Logger.getLogger(SyncEndpoint.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

}
