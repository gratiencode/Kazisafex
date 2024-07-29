/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import javafx.concurrent.Task;

/**
 *
 * @author eroot
 */
public class DownloadTask extends Task<Void> {

    String url;
    String localPath;

    public DownloadTask(String url, String localPath) {
        this.url = url;
        this.localPath = localPath;
    }

    @Override
    protected Void call() throws Exception {
        URLConnection connexion = new URL("https://www.kazisafe.com/download/" + this.url).openConnection();
        long taille = connexion.getContentLengthLong();
        try (InputStream is = connexion.getInputStream(); OutputStream os = Files.newOutputStream(Paths.get(localPath + "/" + url))) {
            long nread = 0L;
            byte[] buffer = new byte[8192];
            int n;
            while ((n = is.read(buffer)) > 0) {
                os.write(buffer, 0, n);
                nread += n;
                updateProgress(nread, taille);
            }
        }
        return null;
    }

    @Override
    protected void failed() {
        super.failed();
    }

    @Override
    protected void succeeded() {
        super.succeeded();
    }
}
