package tools;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

public final class FinancialTableBinder {

    private FinancialTableBinder() {
    }

    public static void bind(TableView<FinancialRowModel> table, List<Integer> years, List<FinancialRowModel> rows) {
        table.getColumns().setAll(baseColumns());
        for (Integer year : years) {
            TableColumn<FinancialRowModel, String> yearColumn = new TableColumn<>(String.valueOf(year));
            yearColumn.setPrefWidth(120);
            yearColumn.setCellValueFactory(param -> new SimpleStringProperty(formatAmount(param.getValue().getValueForYear(year))));
            table.getColumns().add(yearColumn);
        }
        applyRows(table, rows);
    }

    public static void bindWithHeaders(TableView<FinancialRowModel> table, List<String> headers,
            List<FinancialRowModel> rows) {
        table.getColumns().setAll(baseColumns());
        for (int i = 0; i < headers.size(); i++) {
            final int valueIndex = i;
            TableColumn<FinancialRowModel, String> yearColumn = new TableColumn<>(headers.get(i));
            yearColumn.setPrefWidth(120);
            yearColumn.setCellValueFactory(param -> new SimpleStringProperty(formatAmount(param.getValue().getValueForYear(valueIndex))));
            table.getColumns().add(yearColumn);
        }
        applyRows(table, rows);
    }

    private static void applyRows(TableView<FinancialRowModel> table, List<FinancialRowModel> rows) {
        table.setItems(FXCollections.observableArrayList(rows));
        table.setRowFactory(ignored -> new TableRow<>() {
            @Override
            protected void updateItem(FinancialRowModel item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (item.isTotal()) {
                    setStyle("-fx-font-weight: bold; -fx-background-color: #d2e8f0;");
                } else if (item.isSectionHeader()) {
                    setStyle("-fx-font-weight: bold; -fx-background-color: #dcf4fc;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private static List<TableColumn<FinancialRowModel, ?>> baseColumns() {
        TableColumn<FinancialRowModel, Number> sort = new TableColumn<>("#");
        sort.setPrefWidth(55);
        sort.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getSortOrder()));

        TableColumn<FinancialRowModel, String> rubrique = new TableColumn<>(label("xfinancial.rubrique", "Rubrique"));
        rubrique.setPrefWidth(310);
        rubrique.setCellValueFactory(param -> param.getValue().rubriqueProperty());

        TableColumn<FinancialRowModel, String> nature = new TableColumn<>(label("xfinancial.nature", "Nature"));
        nature.setPrefWidth(300);
        nature.setCellValueFactory(param -> param.getValue().natureProperty());

        return List.of(sort, rubrique, nature);
    }

    private static String label(String key, String fallback) {
        try {
            String lang = Preferences.userNodeForPackage(SyncEngine.class).get("lang", "fr");
            ResourceBundle bundle = ResourceBundle.getBundle("bundles." + lang, new Locale.Builder().setLanguage(lang).build());
            return bundle.containsKey(key) ? bundle.getString(key) : fallback;
        } catch (MissingResourceException | IllegalArgumentException ex) {
            return fallback;
        }
    }

    private static String formatAmount(double value) {
        double rounded = BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_EVEN).doubleValue();
        String text = BigDecimal.valueOf(Math.abs(rounded)).stripTrailingZeros().toPlainString();
        return rounded < 0 ? "(" + text + ")" : text;
    }
}
