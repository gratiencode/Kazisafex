

package data;

import java.io.Serializable;
import java.util.Objects;
import jakarta.persistence.GeneratedValue;  import jakarta.persistence.Entity;
 import org.hibernate.annotations.UuidGenerator; import jakarta.xml.bind.annotation.XmlRootElement;


 @XmlRootElement
@Entity 
public class Parametre extends BaseModel implements Serializable {
    @jakarta.persistence.Id
    private String key;
    private String value;

    public Parametre() {
    }

    public Parametre(String key) {
        this.key = key;
    }

    public Parametre(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 73 * hash + Objects.hashCode(this.key);
        return hash;
    }

   

    public boolean equals(Object object) {
        if (!(object instanceof Parametre)) {
            return false;
        } else {
            Parametre other = (Parametre)object;
            return (this.key != null || other.key == null) && (this.key == null || this.key.equals(other.key));
        }
    }

    public String toString() {
        return "entities.Parametre[ key=" + this.key + " ]";
    }
}

