package services.utils;

import data.network.Kazisafe;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Charge les regions depuis le cache local Preferences puis les synchronise
 * avec l'API afin que toutes les ComboBox reutilisent la meme source.
 */
public final class RegionRegistry {

    private static final Logger LOG = Logger.getLogger(RegionRegistry.class.getName());
    private static final String REGION_PREF = "region";
    private static final String REGION_PREFIX = "region";

    private RegionRegistry() {
    }

    public static void loadAndSync(Preferences pref, Kazisafe api, ObservableList<String> target) {
        loadAndSync(pref, api, target, List.of());
    }

    public static void loadAndSync(Preferences pref, Kazisafe api, ObservableList<String> target, List<String> extras) {
        if (pref == null || target == null) {
            return;
        }
        replaceItems(target, withExtras(loadLocal(pref), extras));
        if (api == null) {
            return;
        }
        api.getRegions().enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                List<String> remote = response != null && response.isSuccessful() && response.body() != null
                        ? response.body()
                        : List.of();
                if (remote.isEmpty()) {
                    replaceItems(target, withExtras(loadLocal(pref), extras));
                    return;
                }
                List<String> merged = merge(pref, remote);
                persistApiRegions(pref, merged);
                replaceItems(target, withExtras(merged, extras));
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable throwable) {
                replaceItems(target, withExtras(loadLocal(pref), extras));
            }
        });
    }

    public static void selectSavedRegion(Preferences pref, ComboBox<String> comboBox) {
        if (pref == null || comboBox == null) {
            return;
        }
        ObservableList<String> items = comboBox.getItems();
        if (items == null) {
            items = FXCollections.observableArrayList();
            comboBox.setItems(items);
        }
        String currentRegion = normalize(pref.get(REGION_PREF, null));
        if (currentRegion != null && items.contains(currentRegion)) {
            comboBox.getSelectionModel().select(currentRegion);
            return;
        }
        if (!items.isEmpty()) {
            comboBox.getSelectionModel().selectFirst();
        }
    }

    public static List<String> loadLocal(Preferences pref) {
        Set<String> merged = new LinkedHashSet<>();
        String currentRegion = normalize(pref.get(REGION_PREF, null));
        if (currentRegion != null) {
            merged.add(currentRegion);
        }
        try {
            List<String> keys = new ArrayList<>();
            for (String key : pref.keys()) {
                if (isApiRegionKey(key)) {
                    keys.add(key);
                }
            }
            keys.sort(Comparator.comparingInt(RegionRegistry::regionIndex));
            for (String key : keys) {
                String region = normalize(pref.get(key, null));
                if (region != null) {
                    merged.add(region);
                }
            }
        } catch (BackingStoreException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return new ArrayList<>(merged);
    }

    private static List<String> merge(Preferences pref, List<String> remote) {
        Set<String> merged = new LinkedHashSet<>();
        String currentRegion = normalize(pref.get(REGION_PREF, null));
        if (currentRegion != null) {
            merged.add(currentRegion);
        }
        for (String region : remote) {
            String normalized = normalize(region);
            if (normalized != null) {
                merged.add(normalized);
            }
        }
        for (String region : loadLocal(pref)) {
            String normalized = normalize(region);
            if (normalized != null) {
                merged.add(normalized);
            }
        }
        return new ArrayList<>(merged);
    }

    private static List<String> withExtras(List<String> base, List<String> extras) {
        Set<String> merged = new LinkedHashSet<>();
        if (base != null) {
            merged.addAll(base);
        }
        if (extras != null) {
            for (String extra : extras) {
                String normalized = normalize(extra);
                if (normalized != null) {
                    merged.add(normalized);
                }
            }
        }
        return new ArrayList<>(merged);
    }

    private static void persistApiRegions(Preferences pref, List<String> regions) {
        try {
            for (String key : pref.keys()) {
                if (isApiRegionKey(key)) {
                    pref.remove(key);
                }
            }
            int index = 0;
            for (String region : regions) {
                String normalized = normalize(region);
                if (normalized != null) {
                    pref.put(REGION_PREFIX + (++index), normalized);
                }
            }
            pref.flush();
        } catch (BackingStoreException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private static void replaceItems(ObservableList<String> target, List<String> values) {
        Runnable update = () -> target.setAll(values);
        if (Platform.isFxApplicationThread()) {
            update.run();
        } else {
            Platform.runLater(update);
        }
    }

    private static boolean isApiRegionKey(String key) {
        return key != null && key.startsWith(REGION_PREFIX) && !REGION_PREF.equals(key);
    }

    private static int regionIndex(String key) {
        try {
            return Integer.parseInt(key.substring(REGION_PREFIX.length()));
        } catch (NumberFormatException ex) {
            return Integer.MAX_VALUE;
        }
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
