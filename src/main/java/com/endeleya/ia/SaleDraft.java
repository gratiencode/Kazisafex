package com.endeleya.ia;

import java.util.ArrayList;
import java.util.List;

public class SaleDraft {

    private String reference;
    private String saleDate;
    private String clientName;
    private String clientPhone;
    private String currency = "USD";
    private List<SaleLine> lines = new ArrayList<>();

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(String saleDate) {
        this.saleDate = saleDate;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientPhone() {
        return clientPhone;
    }

    public void setClientPhone(String clientPhone) {
        this.clientPhone = clientPhone;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<SaleLine> getLines() {
        return lines;
    }

    public void setLines(List<SaleLine> lines) {
        this.lines = lines == null ? new ArrayList<>() : lines;
    }

    public boolean hasLines() {
        return lines != null && !lines.isEmpty();
    }
}
