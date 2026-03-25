/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data; import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Objects;

/**
 *
 * @author eroot
 */
public class LoginResult {

    private String token;
    private String entreprise;
    private String role;
    private String region;

    public LoginResult() {
    }

    public LoginResult(String token, String entreprise, String role) {
        this.token = token;
        this.entreprise = entreprise;
        this.role = role;
    }

    public String getEntreprise() {
        return entreprise;
    }

    public void setEntreprise(String entreprise) {
        this.entreprise = entreprise;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.token);
        hash = 71 * hash + Objects.hashCode(this.entreprise);
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
        final LoginResult other = (LoginResult) obj;
        if (!Objects.equals(this.token, other.token)) {
            return false;
        }
        if (!Objects.equals(this.entreprise, other.entreprise)) {
            return false;
        }
        return true;
    }
    
    

}
