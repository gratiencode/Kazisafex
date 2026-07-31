package tools;

import data.Module;
import data.network.Kazisafe;
import services.PlatformUtil;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.prefs.Preferences;
import retrofit2.Response;

public class UpdateManager {

    private static final String UPDATE_URL = "https://cloud.kazisafe.com/download";
    private static final String JAR_NAME = "Kazisafex.jar";
    private static final String PREF_VERSION = "ksf_version";

    private final Kazisafe kazisafe;
    private final Preferences prefs;

    public UpdateManager(Kazisafe kazisafe) {
        this.kazisafe = kazisafe;
        this.prefs = Preferences.userNodeForPackage(SyncEngine.class);
    }

    public interface UpdateListener {
        void onUpdateAvailable(Module module);
        void onUpToDate();
        void onError(String message);
    }

    public void checkForUpdate(UpdateListener listener) {
        new Thread(() -> {
            try {
                String currentVersion = prefs.get(PREF_VERSION, Constants.APP_VERSION);
                Response<Module> response = kazisafe.checkUpdates().execute();
                if (response.isSuccessful()) {
                    Module module = response.body();
                    if (module != null && module.getVersion() != null
                            && !module.getVersion().equalsIgnoreCase(currentVersion)) {
                        listener.onUpdateAvailable(module);
                    } else {
                        listener.onUpToDate();
                    }
                } else {
                    listener.onError("Erreur serveur: " + response.code());
                }
            } catch (Exception e) {
                listener.onError(e.getMessage());
            }
        }).start();
    }

    public void downloadJar(Module module, DownloadProgressListener listener) {
        new Thread(() -> {
            try {
                String filename = module.getNomModule();
                if (filename == null || filename.isEmpty()) {
                    filename = JAR_NAME;
                }
                String localPath = MainUI.cPath("/Media/Update");
                String filePath = localPath + File.separator + filename;

                URLConnection connexion = new URL(UPDATE_URL + "/" + filename).openConnection();
                long totalSize = connexion.getContentLengthLong();

                try (InputStream is = connexion.getInputStream();
                     OutputStream os = Files.newOutputStream(Paths.get(filePath))) {
                    long nread = 0L;
                    byte[] buffer = new byte[8192];
                    int n;
                    while ((n = is.read(buffer)) > 0) {
                        os.write(buffer, 0, n);
                        nread += n;
                        if (listener != null) {
                            listener.onProgress(nread, totalSize);
                        }
                    }
                }
                if (listener != null) {
                    listener.onComplete(filePath);
                }
            } catch (Exception e) {
                if (listener != null) {
                    listener.onError(e.getMessage());
                }
            }
        }).start();
    }

    public interface DownloadProgressListener {
        void onProgress(long bytesRead, long totalBytes);
        void onComplete(String downloadedFilePath);
        void onError(String message);
    }

    public void scheduleRestart(String downloadedFilePath) {
        try {
            String updateScriptDir = MainUI.cPath("/Media/Update");
            String scriptPath;
            String[] command;

            if (PlatformUtil.isWindows()) {
                scriptPath = updateScriptDir + File.separator + "update.bat";
                String installDir = System.getenv("ProgramFiles") + File.separator
                        + "kazisafe-win" + File.separator + "app";
                String jarPath = installDir + File.separator + JAR_NAME;

                StringBuilder sb = new StringBuilder();
                sb.append("@echo off\n");
                sb.append("echo Mise a jour de Kazisafe...\n");
                sb.append("timeout /t 3 /nobreak >nul\n");
                sb.append("copy /Y \"").append(downloadedFilePath).append("\" \"").append(jarPath).append("\"\n");
                sb.append("if errorlevel 1 (\n");
                sb.append("    echo Echec de la mise a jour\n");
                sb.append("    pause\n");
                sb.append("    exit /b 1\n");
                sb.append(")\n");
                sb.append("echo Mise a jour reussie!\n");
                sb.append("start \"\" \"").append(installDir).append(File.separator).append("Kazisafex.exe\"\n");
                sb.append("del \"%~f0\"\n");
                Files.writeString(Paths.get(scriptPath), sb.toString());
                command = new String[]{"cmd", "/c", "start", "/min", scriptPath};
            } else if (PlatformUtil.isMac()) {
                scriptPath = updateScriptDir + File.separator + "update.sh";
                String installDir = "/Applications/Kazisafe.app/Contents/Resources";
                String jarPath = installDir + File.separator + JAR_NAME;

                StringBuilder sb = new StringBuilder();
                sb.append("#!/bin/bash\n");
                sb.append("sleep 3\n");
                sb.append("cp -f \"").append(downloadedFilePath).append("\" \"").append(jarPath).append("\"\n");
                sb.append("if [ $? -eq 0 ]; then\n");
                sb.append("    open -a Kazisafe\n");
                sb.append("else\n");
                sb.append("    echo 'Echec de la mise a jour'\n");
                sb.append("fi\n");
                sb.append("rm -- \"$0\"\n");
                Files.writeString(Paths.get(scriptPath), sb.toString());
                new File(scriptPath).setExecutable(true);
                command = new String[]{"/bin/bash", scriptPath};
            } else {
                scriptPath = updateScriptDir + File.separator + "update.sh";
                String installDir = "/opt/kazisafe/app";
                String jarPath = installDir + File.separator + JAR_NAME;

                StringBuilder sb = new StringBuilder();
                sb.append("#!/bin/bash\n");
                sb.append("sleep 3\n");
                sb.append("cp -f \"").append(downloadedFilePath).append("\" \"").append(jarPath).append("\"\n");
                sb.append("if [ $? -eq 0 ]; then\n");
                sb.append("    java -jar \"").append(jarPath).append("\" &\n");
                sb.append("else\n");
                sb.append("    echo 'Echec de la mise a jour'\n");
                sb.append("fi\n");
                sb.append("rm -- \"$0\"\n");
                Files.writeString(Paths.get(scriptPath), sb.toString());
                new File(scriptPath).setExecutable(true);
                command = new String[]{"/bin/bash", scriptPath};
            }

            Runtime.getRuntime().exec(command);
            prefs.put(PREF_VERSION, "");

        } catch (Exception e) {
            System.err.println("Erreur lors de la preparation de la mise a jour: " + e.getMessage());
        }
    }
}
