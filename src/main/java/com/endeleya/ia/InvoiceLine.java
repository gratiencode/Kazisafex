package com.endeleya.ia;

public class InvoiceLine {

    private String productName;
    private String category;
    private double quantity;
    private double purchaseUnitPrice;
    private double total;
    private Double salePrice;
    private Double salePriceQmin;
    private Double salePriceQmax;
    private String saleCurrency;
    private String measureName;
    // Donnees de lot lues sur la facture ou demandees a l'utilisateur avant l'insertion.
    private String lotNumber;
    private String expiryDate;

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getPurchaseUnitPrice() {
        return purchaseUnitPrice;
    }

    public void setPurchaseUnitPrice(double purchaseUnitPrice) {
        this.purchaseUnitPrice = purchaseUnitPrice;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public Double getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(Double salePrice) {
        this.salePrice = salePrice;
    }

    public Double getSalePriceQmin() {
        return salePriceQmin;
    }

    public void setSalePriceQmin(Double salePriceQmin) {
        this.salePriceQmin = salePriceQmin;
    }

    public Double getSalePriceQmax() {
        return salePriceQmax;
    }

    public void setSalePriceQmax(Double salePriceQmax) {
        this.salePriceQmax = salePriceQmax;
    }

    public String getSaleCurrency() {
        return saleCurrency;
    }

    public void setSaleCurrency(String saleCurrency) {
        this.saleCurrency = saleCurrency;
    }

    public String getMeasureName() {
        return measureName;
    }

    public void setMeasureName(String measureName) {
        this.measureName = measureName;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

}
