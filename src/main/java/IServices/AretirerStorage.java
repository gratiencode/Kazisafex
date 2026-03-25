/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IServices;

import java.util.List; 
import java.util.Set;
import data.Aretirer;
import java.time.LocalDateTime;

/**
 *
 * @author eroot
 */
public interface AretirerStorage {
    public Aretirer createAretirer(Aretirer cat);
    public Aretirer updateAretirer(Aretirer cat);
    public void deleteAretirer(Aretirer cat);
    public Aretirer findAretirer(String catId);
    public List<Aretirer> findAretirer();
    public Long getCount();
    public List<Aretirer> findAretirer(int start,int max);
    public Aretirer findAretirerByReference(String catId);
    public List<Aretirer> mergeSet(Set<Aretirer> bulk);
    public List<Aretirer> findUnSyncedAretirers(long disconnected_at);
    
    public boolean isExists(String uid);
    public boolean isExists(String uid, LocalDateTime atime);
}
