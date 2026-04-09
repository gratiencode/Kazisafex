package services.dialect;
import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.dialect.pagination.LimitOffsetLimitHandler;

public class TestDialect {
    public static void main(String[] args) {
        try {
            System.out.println("TESTING KSQLiteDialect instantiation...");
            Class<?> clazz = Class.forName("services.dialect.KSQLiteDialect");
            Object obj = clazz.getDeclaredConstructor().newInstance();
            System.out.println("Success! " + obj);
        } catch (Throwable t) {
            System.err.println("FAILED TO INSTANTIATE KSQLiteDialect: " + t.getMessage());
            t.printStackTrace(System.err);
            Throwable cause = t.getCause();
            while (cause != null) {
                System.err.println("CAUSE: " + cause.getMessage());
                cause.printStackTrace(System.err);
                cause = cause.getCause();
            }
        }
    }
}
