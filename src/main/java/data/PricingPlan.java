/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.GeneratedValue;  import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
 import org.hibernate.annotations.UuidGenerator; import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author eroot
 */

 @XmlRootElement
@Entity 
public class PricingPlan extends BaseModel implements Serializable {

    private String name;
  
    private String timeInfo;
   
    private String dureeMillis;
    
    private String resume;

   
    @Id
    private String uid;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    
    private Double price;
    @JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd"
    )
    @Temporal(TemporalType.DATE)
    private Date dateValide;
    private boolean valide;
    
    private List<Entreprise> entrepriseList;

    public PricingPlan() {
    }

    public PricingPlan(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }


    public String getTimeInfo() {
        return timeInfo;
    }

    public void setTimeInfo(String timeInfo) {
        this.timeInfo = timeInfo;
    }

    public String getDureeMillis() {
        return dureeMillis;
    }

    public void setDureeMillis(String dureeMillis) {
        this.dureeMillis = dureeMillis;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }


     @JsonbTransient public List<Entreprise> getEntrepriseList() {
        return entrepriseList;
    }

    public void setEntrepriseList(List<Entreprise> entrepriseList) {
        this.entrepriseList = entrepriseList;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (uid != null ? uid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PricingPlan)) {
            return false;
        }
        PricingPlan other = (PricingPlan) object;
        if ((this.uid == null && other.uid != null) || (this.uid != null && !this.uid.equals(other.uid))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entities.PricingPlan[ uid=" + uid + " ]";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getResume() {
        return resume;
    }

    public void setResume(String resume) {
        this.resume = resume;
    }

    public Date getDateValide() {
        return dateValide;
    }

    public void setDateValide(Date dateValide) {
        this.dateValide = dateValide;
    }

    public boolean isValide() {
        return valide;
    }

    public void setValide(boolean valide) {
        this.valide = valide;
    }
    
    
}
