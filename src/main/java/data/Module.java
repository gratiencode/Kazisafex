package data; import com.fasterxml.jackson.annotation.JsonFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import java.io.Serializable;

import java.util.Objects;
import jakarta.persistence.Entity;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.time.LocalDate;

@XmlRootElement
@Entity
public class Module extends BaseModel implements Serializable {

    private String nomModule;
    @JsonFormat(
            shape = Shape.STRING,
            pattern = "yyyy-MM-dd"
    )
    private LocalDate dateLancer;
    private String version;
    private static final long serialVersionUID = 1L;
    @jakarta.persistence.Id
    private String uid;

    public Module() {
    }

    public Module(String uid) {
        this.uid = uid;
    }

    public Module(String uid, String nomModule, LocalDate dateLancer, String version) {
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

    public LocalDate getDateLancer() {
        return this.dateLancer;
    }

    public void setDateLancer(LocalDate dateLancer) {
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
            Module other = (Module) object;
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
