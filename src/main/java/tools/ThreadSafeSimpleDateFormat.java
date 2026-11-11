package tools;

import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * {@link SimpleDateFormat} synchronisé, compatible avec les champs statiques
 * historiques de {@link Constants}.
 *
 * <p>Les instances de {@code SimpleDateFormat} ne sont pas thread-safe. Les
 * synchroniser explicitement évite les dates corrompues lorsqu'un rapport,
 * une impression ou la synchronisation formate en parallèle. L'API publique
 * reste identique pour les appelants existants.</p>
 */
public final class ThreadSafeSimpleDateFormat extends SimpleDateFormat {

    private static final long serialVersionUID = 1L;

    public ThreadSafeSimpleDateFormat(String pattern) {
        super(pattern);
    }

    @Override
    public synchronized StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition pos) {
        return super.format(date, toAppendTo, pos);
    }

    @Override
    public synchronized Date parse(String text, ParsePosition pos) {
        return super.parse(text, pos);
    }

    @Override
    public synchronized Date parse(String text) throws java.text.ParseException {
        return super.parse(text);
    }

    @Override
    public synchronized Object parseObject(String source, ParsePosition pos) {
        return super.parseObject(source, pos);
    }
}
