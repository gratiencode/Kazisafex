/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IServices;

import java.util.Date;
import java.util.List;
import java.util.Set;
import data.Journal;

/**
 *
 * @author eroot
 */
public interface JournalStorage {

    public Journal createJournal(Journal obj);

    public Journal updateJournal(Journal obj);
    
    public List<Journal> setJournals(Set<Journal> ljs);

    public void deleteJournal(Journal obj);

    public Long getCount();

    public Journal findJournal(long objId);

    public List<Journal> findJournalFor(String objId);

    public List<Journal> findJournalFor(long objId);

    public List<Journal> findJournalFor(int objId);

    public List<Journal> findSyncedJournal();

    public List<Journal> findNonSyncedJournal();

    public List<Journal> findJournals();

    public List<Journal> findJournalInPeriod(Date date1, Date date2);

    public boolean isStringRecordSynced(String suid);

    public boolean isLongRecordSynced(long luid);

    public boolean isIntRecordSynced(int iuid);

    public void swipe();

}
