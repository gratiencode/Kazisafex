package IServices;

import data.ImmobilisationAgregate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface ImmobilisationAgregateStorage {
    public ImmobilisationAgregate createImmobilisationAgregate(ImmobilisationAgregate obj);
    public ImmobilisationAgregate updateImmobilisationAgregate(ImmobilisationAgregate obj);
    public void deleteImmobilisationAgregate(ImmobilisationAgregate obj);
    public ImmobilisationAgregate findImmobilisationAgregate(String objId);
    public List<ImmobilisationAgregate> findImmobilisationAgregates();
    public List<ImmobilisationAgregate> findImmobilisationAgregates(int start, int max);
    public List<ImmobilisationAgregate> findByRegion(String region);
    public List<ImmobilisationAgregate> findByImmobilisation(String immobilisationId);
    public Long getCount();
    public List<ImmobilisationAgregate> mergeSet(Set<ImmobilisationAgregate> bulk);
    public List<ImmobilisationAgregate> findUnSynced(long since);
    public boolean isExists(String uid);
}
