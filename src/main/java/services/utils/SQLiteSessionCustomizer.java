/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package services.utils;

import org.eclipse.persistence.sessions.SessionEvent;
import org.eclipse.persistence.sessions.SessionEventAdapter;

import java.sql.Connection;
import java.sql.Statement;

/**
 *
 * @author endeleya
 */

public class SQLiteSessionCustomizer extends SessionEventAdapter {

    @Override
    public void postAcquireConnection(SessionEvent event) {
        try {
            Connection conn = (Connection) event.getResult();
            String key = SecurePreferences.loadDecryptedValue(SecurePreferences.getEntr());
            if (key != null && !key.isEmpty()) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("PRAGMA key = '" + key.replace("'", "''") + "';");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Impossible d’appliquer la clé SQLCipher", e);
        }
    }
}
