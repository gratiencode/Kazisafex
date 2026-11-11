package services;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class ExecuteReadUsageTest {

    private static final Pattern BAD =
            Pattern.compile("executeRead\\s*\\(\\s*em\\s*->\\s*\\{([^}]*)\\}", Pattern.DOTALL);

    @Test
    void executeReadLambdasMustUseTheirOwnEntityManager() throws Exception {
        List<String> offenders = new ArrayList<>();
        try (Stream<Path> files = Files.walk(Path.of("src/main/java"))) {
            files.filter(p -> p.toString().endsWith(".java")).forEach(p -> {
                try {
                    String source = Files.readString(p);
                    Matcher m = BAD.matcher(source);
                    while (m.find()) {
                        if (m.group(1).contains("getEntityManager()")) {
                            offenders.add(p + ":" + (source.substring(0, m.start()).split("\n").length));
                        }
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });
        }
        assertTrue(offenders.isEmpty(),
                "executeRead doit utiliser son EntityManager, pas le global : " + offenders);
    }
}
