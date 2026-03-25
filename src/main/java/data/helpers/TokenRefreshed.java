/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package data.helpers;

import java.util.Objects;

/**
 *
 * @author endeleya
 */
public class TokenRefreshed {
    private String accessToken;
    private String tokenType;
    private long expiresIn;
    private long expiresAt;
    private long refreshedAt;

    public TokenRefreshed() {
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public long getRefreshedAt() {
        return refreshedAt;
    }

    public void setRefreshedAt(long refreshedAt) {
        this.refreshedAt = refreshedAt;
    }

    @Override
    public int hashCode() {
        int hash = 7;
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
        final TokenRefreshed other = (TokenRefreshed) obj;
        if (this.refreshedAt != other.refreshedAt) {
            return false;
        }
        return Objects.equals(this.accessToken, other.accessToken);
    }
    
}
