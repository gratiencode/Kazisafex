/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.prefs.Preferences;

/**
 *
 * @author eroot
 */
public class Constants {

    public static final String APP_VERSION = "1.2.2-ZR";
    public static final String LOGIN_SCREEN = "keti_UI.fxml";
    public static final String MAIN_SCREEN = "mainuix.fxml";
    public static final String STORAGE_VIEW = "Goodstorage.fxml";
    public static final String CLIENT_VIEW = "clients.fxml";
    public static final String REPPORT_VIEW = "repport.fxml";
    public static final String AGENTS_VIEW = "agent.fxml";
    public static final String PRODUITS_VIEW = "produits.fxml";
    public static final String PARAMETRE_VIEW = "parametre.fxml";
    public static final String POS_VIEW = "Pos.fxml";
    public static final String CAISSE_VIEW = "tresorerie.fxml";
    public static final String ENTREPRISE_VIEW = "entreprise.fxml";

    public static final String LOGIN = "login-ui";
    public static final String MAIN = "main-ui";
    public static final String CLIENTS = "clients-ui";
    public static final String STORAGE = "storage-ui";
    public static final String PRODUIT = "produit-ui";
    public static final String AGENTS = "agents-ui";
    public static final String PARAMETRES = "parametre-ui";
    public static final String ENTREPRISE = "retrait-ui";
    public static final String POS = "pos-ui";
    public static final String CAISSES = "finance-ui";
    public static final String REPPORTS = "repport-ui";

    public static final SimpleDateFormat DATE_HEURE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    public static final SimpleDateFormat DATE_ONLY_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat YEAR_AND_MONTH_FORMAT = new SimpleDateFormat("yyyy-MM");
    public static final SimpleDateFormat YEAR_ONLY_FORMAT = new SimpleDateFormat("yyyy");
    public static final SimpleDateFormat USER_READABLE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
    public static final SimpleDateFormat DATE_HEURE_USER_READABLE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    public static final SimpleDateFormat TIMESTAMPED_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
    public static final Calendar Calendrier = Calendar.getInstance();
    public static final long MILLSECONDS_JOURN = 86400000;

    /**
     * Millisecond in one month
     */
    public static final long UN_MOIS = MILLSECONDS_JOURN * 30;
    public static final long TROIS_MOIS = UN_MOIS * 3;
    public static final long SIX_MOIS = UN_MOIS * 6;
    public static final long ANNEE = SIX_MOIS * 2;
    public static final long MUNITE_MILLIS = 60000;
    public static final int PRIORITY_DEFAULT = 0;
    public static final int PRIORITY_SYNC = 1;

    //dialogs
    public static final String PRODUCT_DLG = "produit_item.fxml";
    public static final String PAYMENT_DLG = "payment.fxml";
    public static final String STOCKAGE_DLG = "storeform.fxml";
    public static final String DESTOCKAGE_DLG = "destock.fxml";
    public static final String PARAMETRE_DLG = "parametres.fxml";
    public static final String FICHESTOCK_DLG = "fichedestock.fxml";
    public static final String MESURE_DLG = "Mezure.fxml";
    public static final String DELIVERY_DLG = "Delivery.fxml";
    public static final String RECQ_DLG = "recq.fxml";
    public static final String CLIENT_DLG = "client.fxml";
    public static final String RELEVEE_DLG = "relevee.fxml";
    public static final String FOURNISSEUR_DLG = "suppliers.fxml";
    public static final String PANIER_DLG = "panierappender.fxml";

    //constants
    public static final String PAYMENT_CASH = "Paiement Cash";
    public static final String PAYMENT_CREDIT_CASH = "Paiement Credit partiel";
    public static final String PAYEMENT_CREDIT = "Paiement a credit";
    public static final String PAYEMENT_BANQUE = "Paiement par Banque";
    public static final String BILL_STATUS_PAID = "Payée";
    public static final String BILL_STATUS_CANCELED = "Annulée";
    public static final String BILL_STATUS_UNPAID = "Non Payée";
    public static final String BILL_STATUS_INPAYMENT = "Payée partiellement";

    //
    public static final String ACTION_CREATE = "create";
    public static final String ACTION_UPDATE = "update";
    public static final String ACTION_DELETE = "delete";
    public static final String ACTION_SYNC = "sync";
    public static final String LAST_SESSION_ENDS = "last_time_session";
    public static final long ABONNEMENT_MESUEL = 86400000 * 30;
    public static final String ABONNEMENT_BONUS = "Ab-Bonus";
    public static final String COMP_ENTITY = "loyal_id";
    public static final String BONUS_RECORD = "BONUS_RECORD";
    public static final String DUPLICATE_ERROR_MESSAGE = "Duplicate entry";
    public static final String SYNC_STATE_KEY = "sync-key";
    public static final String SYNC_STARTED = "sync-started";
    public static final String SYNC_PROGRESS = "sync-in-progress";
    public static final String KAZISAFE_FACTOR = "KSF-MODEL";
    public static final String SYNC_FINISHED = "sync-finish";
    public static final String SYNC_NOT_STARTED = "sync-not-started";
    public static final String SYNC_STOPPED = "sync-stopped";
    public static final String PROMO_VIP_CREDIT = "Promo_pref_credit";
    public static final String SUBSCRIPTION_GOLD = "Gold";
    public static final String SUBSCRIPTION_METAL = "Metal";
    public static final String SUBSCRIPTION_TITAN = "Titan";
    public static final String SUBSCRIPTION_SILVER = "Silver";
    public static final String SUBSCRIPTION_ARSENIC = "Arsenic";
    public static final String SUBSCRIPTION_PENDING = "En attente";
    public static final String SUBSCRIPTION_RECEIVED = "En consommation";
    public static final String SUBSCRIPTION_OPEN = "OPEN";
    public static final String SUBSCRIPTION_CLOSE = "CLOSE";
    public static final String SUSPENDU = "Suspendu";
    public static final boolean OPERATIONNEL = false;
    public static final String DATA_AVAILABLE = "data-exists";
    public static final String TAUX_CHANGE_USD = "TAUX_USD";
    public static final String TAUX_FACTURATION = "TAUX_FACTURE";
    public static final String STATE_YES = "yes";
    public static final String STATE_NO = "no";
    public static final String AIRTEL = "AIRTEL";
    public static final String ORANGE = "ORANGE";
    public static final String VODACOM = "VODACOM";
    public static final String AFRICELL = "AFRICELL";
    public static final String DATA_PRODUIT_POS = "postion-produit";
    public static final String DATA_DESTOCKER_POS = "position-destocker";
    public static final String AIRTIME_BONUS_QUANT = "QUANT_AIRTIME_BONUS";
    public static final String COUNT_CLIENT_PURCHASE = "COUNT_PURCHASE";
    public static final String PARAM_SUBSCRIPTION_PRICE = "COUNT_PURCHASE";
    public static final String IS_PROMOTION_VALID = "VALID_PROMOTION";
    public static final String NEW_SALE = "Nouveau";
    public static final String FIDELISER = "Fidelise";
    public final static String DATA_STOCKER_POS = "position-stocker";
    public final static String ROOT = System.getProperty("user.home") + File.separator + "KSafe" + File.separator + "files" + File.separator + "marshal";
    public final static String ROOT_STORAGE = System.getProperty("user.home") + File.separator + "KSafe" + File.separator + "files";
    public final static int TIMEOUT_USSD = 3000;
    public final static String CASH = "Paiement Cash";
    public final static String CREDIT = "A Credit";
    public final static String CREDIT_ET_CASH = "Cash et credit";
    public final static String BANK = "Paiement par Banque";
    public final static String MOBILE_MONEY = "Mobile money";
    public static final String PIN_AIRTEL = "PIN_AIRTEL";
    public static final String PIN_VODACOM = "PIN_VODACOM";
    public static final String PIN_ORANGE = "PIN_ORANGE";
    public static final String HIGH_PRICED_PROMO = "Promo_pref_usd";
    public static final String PIN_AFRICELL = "PIN_AFRICELL";
    public static final String BONUS_MENS = "Bonus mensuel";
    public static final String BONUS_JOURN = "Bonus journalier";
    public static final String ETAT_SUBSCRIPTION_EXPIRY = "Expiree";
    public static final String ETAT_SUBSCRIPTION_NEW = "Valide";
    public static final String SYNC_STATUS_SYNCED = "SYNCED";
    public static final String SYNC_STATUS_NEW = "NEW";
    public static final String SYNC_STATUS_DELETE = "DELETE";

    /**
     * Millisecond in one month
     */
    public static final long METAL = MILLSECONDS_JOURN * 30;
    public static final long TITANE = METAL * 3;
    public static final long SILVER = METAL * 6;
    public static final long ARSENIC = SILVER * 2;
    public static final String DATE = "today";
    public static final String ACTION_READ = "read";
    public static SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd");

    public static class Datetime {

        public static String format(Date d) {
            SimpleDateFormat dateF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return dateF.format(d);
        }

        public static Date toDate(Date d) {
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US);
            Date datesL;
            datesL = Date.from(LocalDateTime.parse(d.toString(), df).atOffset(ZoneOffset.UTC).toInstant());
            return datesL;
        }

        public static Date parse(String date) {
            return Date.from(LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US)).toInstant(ZoneOffset.UTC));
        }

        public static Date toSimpleDate(Date date) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String fd = sdf.format(date);
            int y = Integer.parseInt(fd.split("-")[0]);
            int m = Integer.parseInt(fd.split("-")[1]);
            int d = Integer.parseInt(fd.split("-")[2]);
            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, y);
            c.set(Calendar.MONTH, m);
            c.set(Calendar.DAY_OF_MONTH, d);
            Date dd = c.getTime();
            return dd;
        }

        public static Date toUtilDate(LocalDate ld) {
            if (ld == null) {
                return null;
            }
            Instant i = ld.atStartOfDay(ZoneId.systemDefault()).toInstant();
            return Date.from(i);
        }

        public static LocalDate toLocalDate(java.util.Date date) {
            if (date != null) {
                Instant i = Instant.ofEpochMilli(date.getTime());
                if (i == null) {
                    return null;
                }
                return i.atZone(ZoneId.systemDefault()).toLocalDate();
            }
            return null;
        }

        public static LocalDateTime toLocalDateTime(Date date) {
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        }

        public static long dateInMillis(LocalDate ldt) {
            return ldt.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }

        public static String getMonthName(String numRepres) {
            if (numRepres.endsWith("01")) {
                return "Janvier";
            } else if (numRepres.endsWith("02")) {
                return "Fevrier";
            } else if (numRepres.endsWith("03")) {
                return "Mars";
            } else if (numRepres.endsWith("04")) {
                return "Avril";
            } else if (numRepres.endsWith("05")) {
                return "Mai";
            } else if (numRepres.endsWith("06")) {
                return "Juin";
            } else if (numRepres.endsWith("07")) {
                return "Juillet";
            } else if (numRepres.endsWith("08")) {
                return "Aout";
            } else if (numRepres.endsWith("09")) {
                return "Septembre";
            } else if (numRepres.endsWith("10")) {
                return "Octobre";
            } else if (numRepres.endsWith("11")) {
                return "Novemebre";
            } else if (numRepres.endsWith("12")) {
                return "Decembre";
            }
            return "";
        }

    }
    
    public static String getStringPref(String key,String def){
        Preferences pref=Preferences.userNodeForPackage(SyncEngine.class);
        return pref.get(key, def);
    }
}
