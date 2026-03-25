package delegates;

import IServices.PresenceStorage;
import data.Presence;
import java.time.LocalDateTime;
import java.util.List;
import tools.ServiceLocator;
import tools.Tables;

public class PresenceDelegate {

    public static Presence savePresence(Presence presence) {
        return getPresenceStorage().createPresence(presence);
    }

    public static Presence updatePresence(Presence presence) {
        return getPresenceStorage().updatePresence(presence);
    }

    public static void deletePresence(Presence presence) {
        getPresenceStorage().deletePresence(presence);
    }

    public static Presence findPresence(String uid) {
        return getPresenceStorage().findPresence(uid);
    }

    public static List<Presence> findPresences() {
        return getPresenceStorage().findPresences();
    }

    public static List<Presence> findPresencesByAgent(String agentId) {
        return getPresenceStorage().findPresencesByAgent(agentId);
    }

    public static List<Presence> findPresencesByPeriod(LocalDateTime start, LocalDateTime end) {
        return getPresenceStorage().findPresencesByPeriod(start, end);
    }

    public static List<Presence> findPresencesByRegion(String region) {
        return getPresenceStorage().findPresencesByRegion(region);
    }

    public static PresenceStorage getPresenceStorage() {
        return (PresenceStorage) ServiceLocator.getInstance().getService(Tables.PRESENCE);
    }

    public static Long getCount() {
        return getPresenceStorage().getCount();
    }

    public static List<Presence> findUnSyncedPresences(long disconnected_at) {
        return getPresenceStorage().findUnSyncedPresences(disconnected_at);
    }

    public static boolean isExists(String uid, LocalDateTime attime) {
        return getPresenceStorage().isExists(uid, attime);
    }

    public static boolean isExists(String uid) {
        return getPresenceStorage().isExists(uid);
    }
}
