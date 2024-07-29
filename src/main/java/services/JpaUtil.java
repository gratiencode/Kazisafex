/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package services;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.eclipse.persistence.config.EntityManagerProperties;
import tools.SyncEngine;

/**
 *
 * @author endeleya
 */
public class JpaUtil {

    private static final Preferences pref;
    private static final EntityManagerFactory emf;
    

    static {
        pref = Preferences.userNodeForPackage(SyncEngine.class);
        String databaseName = pref.get("eUid", null);
        Map<String, String> properties = new HashMap<>();
        String mysqlx = "jdbc:mysql://localhost:" + pref.getInt("default_mysql_port", 3306) + "/ksf_" + databaseName + "?createDatabaseIfNotExist=true&useSsl=false&zeroDateTimeBehavior=convertToNull&"
                + "sessionVariables=sql_mode=''";
        properties.put(EntityManagerProperties.JDBC_USER, "root");
        properties.put(EntityManagerProperties.JDBC_URL, mysqlx);
        properties.put(EntityManagerProperties.JDBC_PASSWORD, "Admin*21");
        emf = Persistence.createEntityManagerFactory("kazisafe-jmx", properties);
        
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    public static void closeEntityManagerFactory() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
