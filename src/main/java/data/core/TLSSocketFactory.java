/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.core;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import okhttp3.OkHttpClient;

/**
 *
 * @author eroot
 */
public class TLSSocketFactory extends SSLSocketFactory {
    private final SSLSocketFactory delegate;
    private TrustManager[] trustManagers;

public TLSSocketFactory() throws KeyStoreException, KeyManagementException, NoSuchAlgorithmException {
    generateTrustManagers();
    SSLContext context = SSLContext.getInstance("TLS");
    context.init(null, trustManagers, null);
    delegate = context.getSocketFactory();
}

private void generateTrustManagers() throws KeyStoreException, NoSuchAlgorithmException {
    TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    trustManagerFactory.init((KeyStore) null);
    TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

    if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
        throw new IllegalStateException("Unexpected default trust managers:"
                + Arrays.toString(trustManagers));
    }

    this.trustManagers = trustManagers;
}

@Override
public String[] getDefaultCipherSuites() {
    return delegate.getDefaultCipherSuites();
}

@Override
public String[] getSupportedCipherSuites() {
    return delegate.getSupportedCipherSuites();
}

@Override
public Socket createSocket() throws IOException {
    return enableTLSOnSocket(delegate.createSocket());
}

@Override
public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
    return enableTLSOnSocket(delegate.createSocket(s, host, port, autoClose));
}

@Override
public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
    return enableTLSOnSocket(delegate.createSocket(host, port));
}

@Override
public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
    return enableTLSOnSocket(delegate.createSocket(host, port, localHost, localPort));
}

@Override
public Socket createSocket(InetAddress host, int port) throws IOException {
    return enableTLSOnSocket(delegate.createSocket(host, port));
}

@Override
public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
    return enableTLSOnSocket(delegate.createSocket(address, port, localAddress, localPort));
}

private Socket enableTLSOnSocket(Socket socket) {
    if (socket instanceof SSLSocket) {
        ((SSLSocket) socket).setEnabledProtocols(new String[]{"TLSv1.1", "TLSv1.2"});
    }
    return socket;
}

public X509TrustManager getTrustManager() {
    return  (X509TrustManager) trustManagers[0];
}

public static SSLSocketFactory trustAllCerts() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            return sslSocketFactory;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

public static void disableSSLVerification() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }
                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            HostnameVerifier allHostsValid = (hostname, session) -> true;
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
  
public static OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            final javax.net.ssl.SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            // Create an OkHttpClient that uses the all-trusting trust manager
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier((String hostname, SSLSession session) -> true);

            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
