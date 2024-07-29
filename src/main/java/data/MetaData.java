
package data;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import jakarta.json.bind.annotation.JsonbTransient;

public class MetaData {
    @JsonFormat(
        shape = Shape.STRING,
        pattern = "yyyy-MM-dd'T'HH:mm:ss"
    )
    private Date dateInitiale;
    @JsonFormat(
        shape = Shape.STRING,
        pattern = "yyyy-MM-dd'T'HH:mm:ss"
    )
    private Date dateFinale;
    private double coutMoyenPondere;
    private List<Stocker> stocker;

    public MetaData() {
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + Objects.hashCode(this.dateInitiale);
        hash = 53 * hash + Objects.hashCode(this.dateFinale);
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
            MetaData other = (MetaData)obj;
            if (!Objects.equals(this.dateInitiale, other.dateInitiale)) {
                return false;
            } else {
                return Objects.equals(this.dateFinale, other.dateFinale);
            }
        }
    }

    public Date getDateInitiale() {
        return this.dateInitiale;
    }

    public void setDateInitiale(Date dateInitiale) {
        this.dateInitiale = dateInitiale;
    }

    public Date getDateFinale() {
        return this.dateFinale;
    }

    public void setDateFinale(Date dateFinale) {
        this.dateFinale = dateFinale;
    }

    public double getCoutMoyenPondere() {
        return this.coutMoyenPondere;
    }

    public void setCoutMoyenPondere(double coutMoyenPondere) {
        this.coutMoyenPondere = coutMoyenPondere;
    }

     @JsonbTransient public List<Stocker> getStocker() {
        return this.stocker;
    }

    public void setStocker(List<Stocker> stocker) {
        this.stocker = stocker;
    }
}

