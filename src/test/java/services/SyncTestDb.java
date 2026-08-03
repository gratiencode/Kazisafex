package services;

import data.SyncOutbox;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.mockito.MockedStatic;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Harness de test partagé : remplace {@link ManagedSessionFactory} par un
 * mock statique et exécute les lambdas JPA contre un {@link EntityManager}
 * simulé adossé à des maps mémoire. Permet de tester le pipeline downsync
 * (createDownsyncRecord, materializeDownsyncRecords, catch-up) sans base de
 * données réelle, tout en observant les appels persist/merge/remove/find.
 */
public final class SyncTestDb implements AutoCloseable {

    public final MockedStatic<ManagedSessionFactory> msf;
    public final EntityManager em = mock(EntityManager.class);

    /** Toutes les lignes SyncOutbox "en base", par uid. */
    public final Map<String, SyncOutbox> records = new LinkedHashMap<>();
    /** Entités métier simulées (ligne trouvable par em.find). */
    public final Map<Class<?>, Map<String, Object>> entityDb = new HashMap<>();

    /** Trace des appels JPA. */
    public final List<Object> mergedEntities = new java.util.ArrayList<>();
    public final List<Object> removedEntities = new java.util.ArrayList<>();
    public final List<Object> persistedEntities = new java.util.ArrayList<>();

    private final Map<String, Object> params = new HashMap<>();

    @SuppressWarnings("unchecked")
    public SyncTestDb() {
        msf = mockStatic(ManagedSessionFactory.class);
        msf.when(ManagedSessionFactory::isEmbedded).thenReturn(false);
        msf.when(ManagedSessionFactory::getEntityManager).thenReturn(em);
        msf.when(() -> ManagedSessionFactory.executeRead(any()))
                .thenAnswer(inv -> {
                    Function<EntityManager, ?> fn = inv.getArgument(0);
                    return fn.apply(em);
                });
        msf.when(() -> ManagedSessionFactory.executeWrite(any()))
                .thenAnswer(inv -> {
                    Function<EntityManager, ?> fn = inv.getArgument(0);
                    return fn.apply(em);
                });

        TypedQuery<SyncOutbox> typedQ = mock(TypedQuery.class);
        when(em.createQuery(anyString(), eq(SyncOutbox.class))).thenReturn(typedQ);
        when(typedQ.setParameter(anyString(), any())).thenAnswer(inv -> {
            params.put(inv.getArgument(0), inv.getArgument(1));
            return typedQ;
        });
        when(typedQ.setMaxResults(anyInt())).thenReturn(typedQ);
        when(typedQ.getResultList()).thenAnswer(inv -> {
            Object tables = params.get("tables");
            if (tables instanceof List<?> tl) {
                return records.values().stream()
                        .filter(r -> "DOWNSYNCED".equals(r.getStatus()))
                        .filter(r -> tl.contains(r.getTableName()))
                        .collect(Collectors.toList());
            }
            String table = (String) params.get("tableName");
            String entityId = (String) params.get("entityId");
            if (table != null && entityId != null) {
                return records.values().stream()
                        .filter(r -> "DOWNSYNCED".equals(r.getStatus()))
                        .filter(r -> table.equals(r.getTableName()) && entityId.equals(r.getEntityId()))
                        .collect(Collectors.toList());
            }
            return List.of();
        });

        Query deleteQ = mock(Query.class);
        when(em.createQuery(anyString())).thenReturn(deleteQ);
        when(deleteQ.executeUpdate()).thenReturn(0);

        when(em.find(any(Class.class), any())).thenAnswer(inv -> {
            Class<?> cls = inv.getArgument(0);
            Object id = inv.getArgument(1);
            if (cls == SyncOutbox.class) {
                return records.get(String.valueOf(id));
            }
            Map<String, Object> byId = entityDb.get(cls);
            return byId == null ? null : byId.get(String.valueOf(id));
        });

        doAnswer(inv -> {
            SyncOutbox rec = inv.getArgument(0);
            records.put(rec.getUid(), rec);
            persistedEntities.add(rec);
            return null;
        }).when(em).persist(any());

        when(em.merge(any())).thenAnswer(inv -> {
            Object o = inv.getArgument(0);
            mergedEntities.add(o);
            return o;
        });

        doAnswer(inv -> {
            removedEntities.add(inv.getArgument(0));
            return null;
        }).when(em).remove(any());
    }

    /** Ajoute un enregistrement downsync DOWNSYNCED à la "base" simulée. */
    public SyncOutbox record(String table, String entityId, String action, String payload) {
        SyncOutbox r = new SyncOutbox();
        r.setUid(UUID.randomUUID().toString().replaceAll("-", ""));
        r.setTableName(table);
        r.setEntityId(entityId);
        r.setAction(action);
        r.setPayload(payload);
        r.setStatus("DOWNSYNCED");
        r.setCreatedAt(LocalDateTime.now());
        records.put(r.getUid(), r);
        return r;
    }

    /** Vrai si toutes les lignes SyncOutbox simulées ont le statut donné. */
    public boolean outboxStatusesAll(String status) {
        return !records.isEmpty() && records.values().stream().allMatch(r -> status.equals(r.getStatus()));
    }

    /** Rend une entité métier trouvable par em.find (pour les tests REMOVE). */
    public void seedEntity(Object entity) {
        String uid = uidOf(entity);
        if (uid == null) {
            return;
        }
        entityDb.computeIfAbsent(entity.getClass(), k -> new HashMap<>()).put(uid, entity);
    }

    public static String uidOf(Object entity) {
        if (entity == null) {
            return null;
        }
        try {
            Object v = entity.getClass().getMethod("getUid").invoke(entity);
            return v == null ? null : v.toString();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void close() {
        msf.close();
    }
}
