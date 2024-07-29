/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.util.Date;
import java.util.List;
import data.Client;

/**
 *
 * @author eroot
 */
public class Cart {
    private Client client;
    private Date date;
    private double totalUsd;
    private double totalCdf;
    private String methodePaiement;
    private double credit;
    private String deviseCredit;
    private List<CartItem> cartItems;

    public Cart() {
    }

    public Cart(Client client, Date date, double totalUsd, double totalCdf, String methodePaiement, double credit, String deviseCredit, List<CartItem> cartItems) {
        this.client = client;
        this.date = date;
        this.totalUsd = totalUsd;
        this.totalCdf = totalCdf;
        this.methodePaiement = methodePaiement;
        this.credit = credit;
        this.deviseCredit = deviseCredit;
        this.cartItems = cartItems;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getTotalUsd() {
        return totalUsd;
    }

    public void setTotalUsd(double totalUsd) {
        this.totalUsd = totalUsd;
    }

    public double getTotalCdf() {
        return totalCdf;
    }

    public void setTotalCdf(double totalCdf) {
        this.totalCdf = totalCdf;
    }

    public String getMethodePaiement() {
        return methodePaiement;
    }

    public void setMethodePaiement(String methodePaiement) {
        this.methodePaiement = methodePaiement;
    }

    public double getCredit() {
        return credit;
    }

    public void setCredit(double credit) {
        this.credit = credit;
    }

    public String getDeviseCredit() {
        return deviseCredit;
    }

    public void setDeviseCredit(String deviseCredit) {
        this.deviseCredit = deviseCredit;
    }
    
    
}
