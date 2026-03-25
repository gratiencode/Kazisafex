package tools;

import java.util.Locale;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Labeled;
import javafx.scene.control.TextInputControl;

public final class ThemeStyler {

    private static final String ORIGINAL_STYLE_KEY = "kazisafe.theme.original.style";

    private ThemeStyler() {
    }

    public static void apply(Node node, boolean dark) {
        if (node == null) {
            return;
        }
        if (dark) {
            applyDark(node);
        } else {
            restore(node);
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                apply(child, dark);
            }
        }
    }

    private static void applyDark(Node node) {
        String style = node.getStyle();
        if (node.getProperties().get(ORIGINAL_STYLE_KEY) == null) {
            node.getProperties().put(ORIGINAL_STYLE_KEY, style == null ? "" : style);
        }
        String themed = style == null ? "" : style;
        themed = replaceColor(themed, "white", "#111827");
        themed = themed.replace("#ffffff", "#111827")
                .replace("#FFFFFF", "#111827")
                .replace("#eeeeee", "#374151")
                .replace("#EEEEEE", "#374151")
                .replace("#dddddd", "#4b5563")
                .replace("#DDDDDD", "#4b5563")
                .replace("#cccccc", "#4b5563")
                .replace("#CCCCCC", "#4b5563")
                .replace("#000000", "#e5e7eb")
                .replace("#111111", "#e5e7eb")
                .replace("-fx-border-color: #44cef5", "-fx-border-color: #60a5fa")
                .replace("-fx-background-color: #44cef5", "-fx-background-color: #2563eb");

        if (node instanceof Labeled && !containsIgnoreCase(themed, "-fx-text-fill")) {
            themed = themed + "; -fx-text-fill: #e5e7eb;";
        }
        if (node instanceof TextInputControl) {
            if (!containsIgnoreCase(themed, "-fx-text-fill")) {
                themed = themed + "; -fx-text-fill: #f9fafb;";
            }
            if (!containsIgnoreCase(themed, "-fx-prompt-text-fill")) {
                themed = themed + "; -fx-prompt-text-fill: #9ca3af;";
            }
        }
        node.setStyle(themed);
    }

    private static void restore(Node node) {
        Object original = node.getProperties().get(ORIGINAL_STYLE_KEY);
        if (original instanceof String originalStyle) {
            node.setStyle(originalStyle);
            node.getProperties().remove(ORIGINAL_STYLE_KEY);
        }
    }

    private static String replaceColor(String style, String colorName, String replacement) {
        String lower = style.toLowerCase(Locale.ROOT);
        String key = colorName.toLowerCase(Locale.ROOT);
        if (!lower.contains(key)) {
            return style;
        }
        return style.replace(colorName, replacement).replace(colorName.toUpperCase(Locale.ROOT), replacement);
    }

    private static boolean containsIgnoreCase(String value, String pattern) {
        return value.toLowerCase(Locale.ROOT).contains(pattern.toLowerCase(Locale.ROOT));
    }
}
