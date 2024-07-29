/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;


import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
/**
 *
 * @author eroot
 */
public class DataId {

    public static String generate() {
        UUID uuid = UUID.randomUUID();//UuidCreator.getTimeBased();
        return uuid.toString().replace("-", "").toLowerCase();
    }
    
    public static int generateInt() {
        return ((int)(Math.random()*1000000000));
    }
    public static long generateLong() {
        return ((long)(Math.random()*10000000000000l));
    }

    public static boolean isConcerned(String uid, int period) {
        //System.out.println("Great - rand UUID : " + UuidCreator.getTimeOrderedWithHash().toString() + " " + uid);
        String seq1 = uid.substring(0, 8);
        String seq2 = uid.substring(8, 12);
        String seq3 = uid.substring(12, 16);
        String seq4 = uid.substring(16, 20);
        String seq5 = uid.substring(20);
        System.out.println("UUID FABR " + seq1 + "-" + seq2 + "-" + seq3 + "-" + seq4 + "-" + seq5);
        String id = seq1 + "-" + seq2 + "-" + seq3 + "-" + seq4 + "-" + seq5;
        UUID uuid = UUID.fromString(id);
        System.out.println(" Version : " + uuid.version());
        if (uuid.version() == 1) {
            long uuidTimestamp = uuid.getMostSignificantBits();
            long millisecond=uuidTimestamp / 1000000;
            Calendar di = Calendar.getInstance();
            di.setTimeInMillis(millisecond);
         
            System.out.println("Timestamp From data: " + uuidTimestamp+" "+new Date(millisecond));
            Instant instant = Instant.ofEpochMilli(millisecond);
            LocalDateTime datetime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
            LocalDate uuidate = datetime.toLocalDate();
            LocalDate leo = LocalDate.now();
            long interv = ChronoUnit.HOURS.between(uuidate, leo);
            System.out.println("DIFF UUIDATE " + interv);
            return interv < period;
        }
        return false;
    }

    public static boolean isConcerned(Date date, long period) {
        long uuidTimestamp = date.getTime() / 1000;
        long currentTimeMillis = System.currentTimeMillis() / 1000;
        long diff = currentTimeMillis - uuidTimestamp;
        return diff <= period;
    }

    
}
