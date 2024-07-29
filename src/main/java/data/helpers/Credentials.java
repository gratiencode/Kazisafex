
package data.helpers;

import java.util.Objects;

public class Credentials {
    private String username;
    private String password;
    private String entreprise;

    public Credentials(String username, String password, String entrep) {
        this.username = username;
        this.password = password;
        this.entreprise = entrep;
    }

    public Credentials() {
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.username);
        return hash;
    }

   

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            Credentials other = (Credentials)obj;
            return Objects.equals(this.username, other.username);
        }
    }

    public String getEntreprise() {
        return this.entreprise;
    }

    public void setEntreprise(String entreprise) {
        this.entreprise = entreprise;
    }
}

