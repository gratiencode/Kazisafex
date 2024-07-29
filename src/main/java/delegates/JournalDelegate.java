/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package delegates;

import IServices.JournalStorage;
import java.util.List;
import java.util.Set;
import data.Journal;
import tools.ServiceLocator;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class JournalDelegate {
     public static Journal saveJournal(Journal cat) {
        return getStorage().createJournal(cat);
    }

    public static Journal updateJournal(Journal cat) {
        return getStorage().updateJournal(cat);
    }

    public static void deleteJournal(Journal cat) {
        getStorage().deleteJournal(cat);
    }

    public static Journal findJournal(long objId) {
        return getStorage().findJournal(objId);
    }
    
    
    public static List<Journal> findJournals(){
       return getStorage().findJournals();
    }
    
    public static List<Journal> createJournals(Set<Journal> jrls){
        return getStorage().setJournals(jrls);
    }
   
    
    public static JournalStorage getStorage(){
        JournalStorage cats=(JournalStorage)ServiceLocator.getInstance().getService(Tables.JOURNAL);
        return cats;
    }
    
     public static boolean isDataSynced(String uid){
       return getStorage().isStringRecordSynced(uid);
    }
     
     public static boolean isDataSynced(int uid){
       return getStorage().isIntRecordSynced(uid);
    }
     public static boolean isDataSynced(long uid){
       return getStorage().isLongRecordSynced(uid);
    }
     
   public static List<Journal> findSynced(){
       return getStorage().findSyncedJournal();
   }
   
    public static List<Journal> findNonSynced(){
       return getStorage().findNonSyncedJournal();
   }
   

     public static List<Journal> findJournalsFor(String uid){
       return getStorage().findJournalFor(uid);
    }
     
       public static List<Journal> findJournalsFor(int uid){
     return getStorage().findJournalFor(uid);
    }
  public static List<Journal> findJournalsFor(long uid){
       return getStorage().findJournalFor(uid);
    }
    public static Long getCount() {
        return getStorage().getCount();
    }

    public static void swipe() {
        getStorage().swipe(); 
    }
}
