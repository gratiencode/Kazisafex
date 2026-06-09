package tools;

import java.util.LinkedHashMap;
import java.util.Map;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

public class FinancialRowModel {

    private final IntegerProperty sortOrder = new SimpleIntegerProperty();
    private final StringProperty lineCode = new SimpleStringProperty();
    private final StringProperty statementType = new SimpleStringProperty();
    private final StringProperty rubrique = new SimpleStringProperty();
    private final StringProperty nature = new SimpleStringProperty();
    private final BooleanProperty total = new SimpleBooleanProperty();
    private final BooleanProperty sectionHeader = new SimpleBooleanProperty();
    private final ObservableMap<Integer, DoubleProperty> valuesByYear = FXCollections.observableHashMap();

    public IntegerProperty sortOrderProperty() {
        return sortOrder;
    }

    public int getSortOrder() {
        return sortOrder.get();
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder.set(sortOrder);
    }

    public StringProperty lineCodeProperty() {
        return lineCode;
    }

    public String getLineCode() {
        return lineCode.get();
    }

    public void setLineCode(String lineCode) {
        this.lineCode.set(lineCode);
    }

    public StringProperty statementTypeProperty() {
        return statementType;
    }

    public String getStatementType() {
        return statementType.get();
    }

    public void setStatementType(String statementType) {
        this.statementType.set(statementType);
    }

    public StringProperty rubriqueProperty() {
        return rubrique;
    }

    public String getRubrique() {
        return rubrique.get();
    }

    public void setRubrique(String rubrique) {
        this.rubrique.set(rubrique);
    }

    public StringProperty natureProperty() {
        return nature;
    }

    public String getNature() {
        return nature.get();
    }

    public void setNature(String nature) {
        this.nature.set(nature);
    }

    public BooleanProperty totalProperty() {
        return total;
    }

    public boolean isTotal() {
        return total.get();
    }

    public void setTotal(boolean total) {
        this.total.set(total);
    }

    public BooleanProperty sectionHeaderProperty() {
        return sectionHeader;
    }

    public boolean isSectionHeader() {
        return sectionHeader.get();
    }

    public void setSectionHeader(boolean sectionHeader) {
        this.sectionHeader.set(sectionHeader);
    }

    public ObservableMap<Integer, DoubleProperty> valuesByYearProperty() {
        return valuesByYear;
    }

    public Map<Integer, Double> getValuesByYear() {
        Map<Integer, Double> values = new LinkedHashMap<>();
        valuesByYear.forEach((year, amount) -> values.put(year, amount == null ? 0d : amount.get()));
        return values;
    }

    public double getValueForYear(int year) {
        DoubleProperty amount = valuesByYear.get(year);
        return amount == null ? 0d : amount.get();
    }

    public DoubleProperty valueForYearProperty(int year) {
        return valuesByYear.computeIfAbsent(year, ignored -> new SimpleDoubleProperty(0d));
    }

    public void setValueForYear(int year, double value) {
        valueForYearProperty(year).set(value);
    }
}
