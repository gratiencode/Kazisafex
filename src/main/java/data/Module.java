

package data;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.GeneratedValue;  import jakarta.persistence.Entity;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
 import org.hibernate.annotations.UuidGenerator; import jakarta.xml.bind.annotation.XmlRootElement;

 @XmlRootElement
@Entity 
public class Module extends BaseModel implements Serializable {
    private String nomModule;
    @JsonFormat(
        shape = Shape.STRING,
        pattern = "yyyy-MM-dd"
    )
    @Temporal(TemporalType.DATE)
    private Date dateLancer;
    private String version;
    private static final long serialVersionUID = 1L;
    @jakarta.persistence.Id
    private String uid;
    
    

    public Module() {
    }

    public Module(String uid) {
        this.uid = uid;
    }

    public Module(String uid, String nomModule, Date dateLancer, String version) {
        this.uid = uid;
        this.nomModule = nomModule;
        this.dateLancer = dateLancer;
        this.version = version;
    }

    public String getUid() {
        return this.uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNomModule() {
        return this.nomModule;
    }

    public void setNomModule(String nomModule) {
        this.nomModule = nomModule;
    }

    public Date getDateLancer() {
        return this.dateLancer;
    }

    public void setDateLancer(Date dateLancer) {
        this.dateLancer = dateLancer;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.uid);
        return hash;
    }

   

    public boolean equals(Object object) {
        if (!(object instanceof Module)) {
            return false;
        } else {
            Module other = (Module)object;
            return (this.uid != null || other.uid == null) && (this.uid == null || this.uid.equals(other.uid));
        }
    }

    public String toString() {
        return "entities.Module[ uid=" + this.uid + " ]";
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}

