/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package IServices;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Set;
import data.Immobilisation;

/**
 *
 * @author eroot
 */
public interface ImmobilisationStorage {
    public Immobilisation createImmobilisation(Immobilisation obj);

    public Immobilisation updateImmobilisation(Immobilisation obj);

    public void deleteImmobilisation(Immobilisation obj);

    public Long getCount();

    public Immobilisation findImmobilisation(String objId);

    public List<Immobilisation> findImmobilisations();

    public List<Immobilisation> findImmobilisations(int start, int max);

    public List<Immobilisation> findImmobilisationByRegion(String region);

    public List<Immobilisation> mergeSet(Set<Immobilisation> bulk);

    public List<Immobilisation> findUnSynced(long since);

    public List<Immobilisation> findUnSyncedImmobilisations(long disconnected_at);

    public boolean isExists(String uid);

    public boolean isExists(String uid, LocalDateTime atime);
}
