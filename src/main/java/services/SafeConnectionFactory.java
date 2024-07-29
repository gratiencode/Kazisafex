/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package services;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.eclipse.persistence.config.EntityManagerProperties;
import tools.SyncEngine;

/**
 *
 * @author endeleya
 */
public class SafeConnectionFactory {

    private static Preferences pref;
    private static final EntityManagerFactory emf;
    private static final ThreadLocal<EntityManager> threadLocal;

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
        threadLocal = new ThreadLocal<>();
    }

    public static EntityManager getEntityManager() {
        EntityManager em = threadLocal.get();
        if (em == null) {
           em = emf.createEntityManager();
           threadLocal.set(em);
        }
        return em;
    }
    
    public static void closeEntityManager() {
        EntityManager em = threadLocal.get();
        if (em != null) {
            em.close();
            threadLocal.set(null);
        }
    }

    public static void closeEntityManagerFactory() {
        emf.close();
    }

    public static void beginTransaction() {
        EntityManager em=getEntityManager();
        EntityTransaction tr=em.getTransaction();
        if(!tr.isActive()){
          tr.begin();
        }
    }
   

    public static void rollback() {
        getEntityManager().getTransaction().rollback();
    }

    public static void commit() {
        try{
        getEntityManager().getTransaction().commit();
        }catch(Exception e){}
    }
}
