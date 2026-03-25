/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package data.helpers;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author endeleya
 */
public class LoginWebResult implements Serializable{

    private String token;
    private String categoryEntreprise;
    private String role;
    private String region;
    private String phone;
    private String entrepriseId;
    private String rccm;
    private String idNat;
    private String nomentreprise;
    private String prenomUtilisateur;
    private String nomUtilisateur;
    private String userId;
    private String numeroImpot;
    private String emailEntreprise;
    private String phoneEntrerprise;
    private String adresseEntreprise;
    private String subscriptionId;
    private String userContract;
    private String jsonPermissions;
    private long creationTimestamp;

    public LoginWebResult() {
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getIdNat() {
        return idNat;
    }

    public void setIdNat(String idNat) {
        this.idNat = idNat;
    }

    public String getNomentreprise() {
        return nomentreprise;
    }

    public void setNomentreprise(String nomentreprise) {
        this.nomentreprise = nomentreprise;
    }

    public String getPrenomUtilisateur() {
        return prenomUtilisateur;
    }

    public void setPrenomUtilisateur(String prenomUtilisateur) {
        this.prenomUtilisateur = prenomUtilisateur;
    }

    public String getNomUtilisateur() {
        return nomUtilisateur;
    }

    public void setNomUtilisateur(String nomUtilisateur) {
        this.nomUtilisateur = nomUtilisateur;
    }

    public String getNumeroImpot() {
        return numeroImpot;
    }

    public void setNumeroImpot(String numeroImpot) {
        this.numeroImpot = numeroImpot;
    }

    public String getEmailEntreprise() {
        return emailEntreprise;
    }

    public void setEmailEntreprise(String emailEntreprise) {
        this.emailEntreprise = emailEntreprise;
    }

    public String getPhoneEntrerprise() {
        return phoneEntrerprise;
    }

    public void setPhoneEntrerprise(String phoneEntrerprise) {
        this.phoneEntrerprise = phoneEntrerprise;
    }

    public String getAdresseEntreprise() {
        return adresseEntreprise;
    }

    public void setAdresseEntreprise(String adresseEntreprise) {
        this.adresseEntreprise = adresseEntreprise;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getCategoryEntreprise() {
        return categoryEntreprise;
    }

    public void setCategoryEntreprise(String categoryEntreprise) {
        this.categoryEntreprise = categoryEntreprise;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getEntrepriseId() {
        return entrepriseId;
    }

    public void setEntrepriseId(String entrepriseId) {
        this.entrepriseId = entrepriseId;
    }

    public String getRccm() {
        return rccm;
    }

    public void setRccm(String rccm) {
        this.rccm = rccm;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.phone);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LoginWebResult other = (LoginWebResult) obj;
        return Objects.equals(this.phone, other.phone);
    }

    @Override
    public String toString() {
        return "LoginWebResult{" + "token=" + token + ", categoryEntreprise=" + categoryEntreprise + ", role=" + role + ", region=" + region + ", phone=" + phone + ", entrepriseId=" + entrepriseId + ", rccm=" + rccm + ", idNat=" + idNat + ", nomentreprise=" + nomentreprise + ", prenomUtilisateur=" + prenomUtilisateur + ", nomUtilisateur=" + nomUtilisateur + ", numeroImpot=" + numeroImpot + ", emailEntreprise=" + emailEntreprise + ", phoneEntrerprise=" + phoneEntrerprise + ", adresseEntreprise=" + adresseEntreprise + '}';
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getJsonPermissions() {
        return jsonPermissions;
    }

    public void setJsonPermissions(String jsonPermissions) {
        this.jsonPermissions = jsonPermissions;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserContract() {
        return userContract;
    }

    public void setUserContract(String userContract) {
        this.userContract = userContract;
    }

    public long getCreationTimestamp() {
        return creationTimestamp;
    }

    public void setCreationTimestamp(long creationTimestamp) {
        this.creationTimestamp = creationTimestamp;
    }

    
    
}
