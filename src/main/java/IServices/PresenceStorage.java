package IServices;

import data.Presence;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface PresenceStorage {
    Presence createPresence(Presence presence);

    Presence updatePresence(Presence presence);

    void deletePresence(Presence presence);

    Presence findPresence(String uid);

    List<Presence> findPresences();

    List<Presence> findPresencesByAgent(String agentId);

    List<Presence> findPresencesByPeriod(LocalDateTime start, LocalDateTime end);

    List<Presence> findPresencesByRegion(String region);

    Long getCount();

    List<Presence> mergeSet(Set<Presence> bulk);

    List<Presence> findUnSyncedPresences(long disconnected_at);

    boolean isExists(String uid);

    boolean isExists(String uid, LocalDateTime atime);
}
