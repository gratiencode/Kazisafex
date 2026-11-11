package tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;

class ThreadSafeSimpleDateFormatTest {

    @Test
    void sharedFormatterIsSafeUnderConcurrentFormatAndParse() throws Exception {
        ThreadSafeSimpleDateFormat formatter =
                new ThreadSafeSimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = new Date(1_760_000_000_000L);
        String expected = formatter.format(date);

        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            List<Callable<String>> tasks = new ArrayList<>();
            for (int i = 0; i < 200; i++) {
                tasks.add(() -> {
                    String text = formatter.format(date);
                    try {
                        return formatter.parse(text).equals(date) ? text : "PARSE_MISMATCH";
                    } catch (ParseException ex) {
                        return "PARSE_ERROR";
                    }
                });
            }
            for (Future<String> result : pool.invokeAll(tasks)) {
                assertEquals(expected, result.get());
            }
        } finally {
            pool.shutdownNow();
        }
        assertTrue(Constants.DATE_ONLY_FORMAT instanceof ThreadSafeSimpleDateFormat);
    }
}
