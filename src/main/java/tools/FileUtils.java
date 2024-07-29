/*
 * Decompiled with CFR 0_115.
 * 
 * Could not load the following classes:
 *  util.FileUtils
 */
package tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileUtils {

    public static File pointFile(String fileName) {
        String path = MainUI.mediaRootPath() + File.separator + "Produits" + File.separator + "images" + File.separator + fileName;
        File target = new File(path);
        return target;
    }

    public static InputStream fileToStream(String fileName) throws FileNotFoundException {
        File f = pointFile(fileName);
        return new FileInputStream(f);

    }

    public static byte[] readAllBytes(InputStream is) throws IOException {
        int nRead;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        byte[] byteArray = buffer.toByteArray();
        buffer.close();
        return byteArray;
    }

    public static byte[] readFromFile(File file) throws IOException, FileNotFoundException {
        int nRead;
        FileInputStream is = new FileInputStream(file);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        byte[] byteArray = buffer.toByteArray();
        buffer.close();
        is.close();
        return byteArray;
    }

    public static InputStream byteToInputStream(byte[] buf) {
        return new ByteArrayInputStream(buf);
    }

    public static File streamTofile(InputStream is) {
        OutputStream outStream = null;
        String path = MainUI.createFileWithPath("Produits/images/", "picture.jpeg");
        File targetFile = new File(path);
        File doss = new File(MainUI.mediaRootPath() + "/Produits/images/");
        try {
            if (!doss.exists()) {
                doss.mkdirs();
            }
            if (!targetFile.exists()) {
                targetFile.createNewFile();
            }
//            File targetFile = new File(MainUI.createFileWithPath("/Products/images/","picture.jpeg"));
            outStream = new FileOutputStream(targetFile);
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
            outStream.flush();
            outStream.close();
            return targetFile;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SyncEngine.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SyncEngine.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                outStream.close();
            } catch (IOException ex) {
                Logger.getLogger(SyncEngine.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;

    }

    public static File byteToFile(String name, byte b[]) {

        String path = MainUI.createFileWithPath("Produits/images/", name + ".jpeg");
        File file = new File(path);
        File doss = new File(MainUI.mediaRootPath() + "/Produits/images/");
        try {
            if (!doss.exists()) {
                doss.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fout
                    = new FileOutputStream(file);
            fout.write(b);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    public static File byteToFile(String name, byte b[], String ext) {

        String path = MainUI.createFileWithPath("Produits/images/", name + "." + ext);
        File file = new File(path);
        File doss = new File(MainUI.mediaRootPath() + "/Produits/images/");
        try {
            if (!doss.exists()) {
                doss.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fout
                    = new FileOutputStream(file);
            fout.write(b);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

}
