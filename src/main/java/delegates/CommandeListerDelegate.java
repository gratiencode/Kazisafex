package delegates;

import IServices.CommandeListStorage;
import data.CommandeLister;
import java.util.List;
import tools.ServiceLocator;
import tools.Tables;

public class CommandeListerDelegate {

    public static CommandeLister saveCommandeLister(CommandeLister cl) {
        return getStorage().saveCommandeLister(cl);
    }

    public static CommandeLister updateCommandeLister(CommandeLister cl) {
        return getStorage().updateCommandeLister(cl);
    }

    public static void deleteCommandeLister(CommandeLister cl) {
        getStorage().deleteCommandeLister(cl);
    }

    public static CommandeLister findCommandeLister(String uid) {
        return getStorage().findCommandeLister(uid);
    }

    public static List<CommandeLister> findCommandeListers() {
        return getStorage().findCommandeListers();
    }

    public static CommandeListStorage getStorage() {
        return (CommandeListStorage) ServiceLocator.getInstance().getService(
            Tables.COMMANDELIST
        );
    }
}
