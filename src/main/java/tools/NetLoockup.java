package tools;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import java.net.URLConnection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

public class NetLoockup {

    OnNetworkStateChangeListener onNetworkStateChangeListener;

    public static String NETWORK_ERROR_MESSAGE = "Unable to resolve host";
    public static String NETWORK_ERROR_MESSAGE2 = "Failed to connect to";
    public static String NETWORK_STATUS = "Network-status";
    public static boolean NETWORK_STATUS_DEFAULT = false;
    public static boolean NETWORK_STATUS_ON = true;
    public static String NETWORK_RESET_ERROR_MESSAGE = "recvfrom failed: ECONNRESET (Connection reset by peer)";
    Preferences pref;

    public NetLoockup() {
        pref = Preferences.userNodeForPackage(SyncEngine.class);
        ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
        ses.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = URI.create("https://www.google.com").toURL();
                    URLConnection connection = url.openConnection();
                    connection.connect();
                    pref.putBoolean(NETWORK_STATUS, true);
                     NETWORK_STATUS_ON = true;
                    notifyNetwork(true);
                } catch (MalformedURLException e) {
                    pref.putBoolean(NETWORK_STATUS, NETWORK_STATUS_DEFAULT);
                    notifyNetwork(false);
                    NETWORK_STATUS_ON = false;
                } catch (IOException e) {
                    pref.putBoolean(NETWORK_STATUS, NETWORK_STATUS_DEFAULT);
                    notifyNetwork(false);
                    NETWORK_STATUS_ON = false;
                }
            }
        }, 3, 8, TimeUnit.SECONDS);

    }

    public void setOnNetworkStateChangeListener(OnNetworkStateChangeListener onNetworkOpenListener) {
        this.onNetworkStateChangeListener = onNetworkOpenListener;
    }

    private void notifyNetwork(boolean isOk) {
        if (this.onNetworkStateChangeListener != null) {
            this.onNetworkStateChangeListener.onNetworkStateChange(isOk);
        }
    }
}
