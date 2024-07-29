/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data.helpers;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 *
 * @author PC
 */
@Deprecated(forRemoval = true)
public interface Constants {

    public static final long ABONNEMENT_MESUEL = 86400000 * 30;
    public static final String ABONNEMENT_BONUS = "Ab-Bonus";
    public static final String COMP_ENTITY="loyal_id";
    public static final String BONUS_RECORD="BONUS_RECORD";
    public static final String DUPLICATE_ERROR_MESSAGE="Duplicate entry";
    public static final String SYNC_STATE_KEY = "sync-key";
    public static final String SYNC_STARTED = "sync-started";
    public static final String SYNC_PROGRESS = "sync-in-progress";
    public static final String KAZISAFE_FACTOR="KSF-MODEL";
    public static final String SYNC_FINISHED = "sync-finish";
    public static final String SYNC_NOT_STARTED = "sync-not-started";
    public static final String SYNC_STOPPED = "sync-stopped";
    public static final String PROMO_VIP_CREDIT= "Promo_pref_credit";
    public static final String SUBSCRIPTION_GOLD="Gold";
    public static final String SUBSCRIPTION_METAL="Metal";
    public static final String SUBSCRIPTION_TITAN="Titan";
    public static final String SUBSCRIPTION_SILVER="Silver";
    public static final String SUBSCRIPTION_ARSENIC="Arsenic";
     public static final String SUBSCRIPTION_PENDING="En attente";
    public static final String SUBSCRIPTION_RECEIVED="En consommation";
    public static final String SUBSCRIPTION_OPEN="OPEN";
    public static final String SUBSCRIPTION_CLOSE="CLOSE";
    public static final String SUSPENDU = "Suspendu";
    public static final boolean OPERATIONNEL = false;
    public static final String DATA_AVAILABLE = "data-exists";
    public static final String TAUX_CHANGE_USD = "TAUX_USD";
    public static final String TAUX_FACTURATION="TAUX_FACTURE";
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
    public final static String BANK="Paiement par Banque";
    public final static String MOBILE_MONEY="Mobile money";
    public static final String PIN_AIRTEL = "PIN_AIRTEL";
    public static final String PIN_VODACOM = "PIN_VODACOM";
    public static final String PIN_ORANGE = "PIN_ORANGE";
    public static final String HIGH_PRICED_PROMO="Promo_pref_usd";
    public static final String PIN_AFRICELL = "PIN_AFRICELL";
    public static final String BONUS_MENS = "Bonus mensuel";
    public static final String BONUS_JOURN = "Bonus journalier";
    public static final String ETAT_SUBSCRIPTION_EXPIRY = "Expiree";
    public static final String ETAT_SUBSCRIPTION_NEW = "Valide";
    public static final String SYNC_STATUS_SYNCED="SYNCED";
    public static final String SYNC_STATUS_NEW="NEW";
    public static final String SYNC_STATUS_DELETE="DELETE";
    public static final long MILLSECONDS_JOURN = 86400000;

    /**
     *Millisecond in one month
     */
    public static final long METAL = MILLSECONDS_JOURN*30;
    public static final long TITANE = METAL*3;
    public static final long SILVER = METAL*6;
    public static final long ARSENIC = SILVER*2;
    public static final long MUNITE_MILLIS = 60000;
    public static final String DATE = "today";
    public static final String ACTION_READ="read";
    SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd");

    public static class Datetime {

        
        
        public static String format(Date d){
           SimpleDateFormat dateF=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
           return dateF.format(d);
        }

        public static Date toDate(Date d) {
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US);
            Date datesL;
            datesL = Date.from(LocalDateTime.parse(d.toString(), df).atOffset(ZoneOffset.UTC).toInstant());
            return datesL;
        }
        public static Date parse(String date){
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
            Date dd=c.getTime();
            return dd;
        }
     
//        public static Date todayFrom(Date date) {
//            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//            Date d1 = Date.from(date.toInstant());
//            d1.format(df);
//            Date d = java.util.Date.from(d1.atStartOfDay(ZoneId.systemDefault()).toInstant());
//            return d;
//        }

//        public static Date datetimeFrom(Date date) {
//            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd à HH:mm:ss");
//            Date d1 = Date.from(ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault()));
//            d1.format(df);
//            Date d = java.util.Date.from(d1.atZone(ZoneId.systemDefault()).toInstant());
//            return d;
//        }
    }
}
