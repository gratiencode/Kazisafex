package com.endeleya.ia;

import java.util.ArrayList;
import java.util.List;

public class InvoiceDraft {

    private String supplier;
    private String supplierIdNat;
    private String supplierRccm;
    private String supplierTaxNumber;
    private String supplierAddress;
    private String supplierPhone;
    private String reference;
    private String invoiceDate;
    private String currency;
    private Double payed;
    private Double reduction;
    private List<InvoiceLine> lines = new ArrayList<>();
    private List<String> missingSalePrices = new ArrayList<>();

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getSupplierIdNat() {
        return supplierIdNat;
    }

    public void setSupplierIdNat(String supplierIdNat) {
        this.supplierIdNat = supplierIdNat;
    }

    public String getSupplierRccm() {
        return supplierRccm;
    }

    public void setSupplierRccm(String supplierRccm) {
        this.supplierRccm = supplierRccm;
    }

    public String getSupplierTaxNumber() {
        return supplierTaxNumber;
    }

    public void setSupplierTaxNumber(String supplierTaxNumber) {
        this.supplierTaxNumber = supplierTaxNumber;
    }

    public String getSupplierAddress() {
        return supplierAddress;
    }

    public void setSupplierAddress(String supplierAddress) {
        this.supplierAddress = supplierAddress;
    }

    public String getSupplierPhone() {
        return supplierPhone;
    }

    public void setSupplierPhone(String supplierPhone) {
        this.supplierPhone = supplierPhone;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Double getPayed() {
        return payed;
    }

    public void setPayed(Double payed) {
        this.payed = payed;
    }

    public Double getReduction() {
        return reduction;
    }

    public void setReduction(Double reduction) {
        this.reduction = reduction;
    }

    public List<InvoiceLine> getLines() {
        return lines;
    }

    public void setLines(List<InvoiceLine> lines) {
        this.lines = lines == null ? new ArrayList<>() : lines;
    }

    public List<String> getMissingSalePrices() {
        return missingSalePrices;
    }

    public void setMissingSalePrices(List<String> missingSalePrices) {
        this.missingSalePrices = missingSalePrices == null ? new ArrayList<>() : missingSalePrices;
    }

    public boolean hasLines() {
        return lines != null && !lines.isEmpty();
    }
}
