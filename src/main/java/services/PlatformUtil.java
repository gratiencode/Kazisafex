

package services;

/**
 *
 * @author endeleya
 */
public class PlatformUtil {

    private static final String os = System.getProperty("os.name");
    private static final String version = System.getProperty("os.version");
    private static final boolean WINDOWS = os.startsWith("Windows");
    private static final boolean MAC = os.startsWith("Mac");
    private static final boolean LINUX = os.startsWith("Linux");

    public static boolean isWindows() {
        return WINDOWS;
    }

    public static boolean isLinux() {
        return LINUX;
    }

    public static boolean isMac() {
        return MAC;
    }
    
    public static String getVersion(){
        return version;
    }

    public PlatformUtil() {
    }
}
