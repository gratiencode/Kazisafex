

package data.helpers;

import java.util.Objects;

public class Token {
    private String token = "";

    public Token() {
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.token);
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
            Token other = (Token)obj;
            return Objects.equals(this.token, other.token);
        }
    }
}

