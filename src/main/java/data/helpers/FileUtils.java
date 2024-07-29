/*
 * Decompiled with CFR 0_115.
 * 
 * Could not load the following classes:
 *  util.FileUtils
 */
package data.helpers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {

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

    

}
