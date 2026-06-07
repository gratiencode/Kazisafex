package data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "creance_agregate")
public class CreanceAgregate implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "uid")
    private String uid;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "region")
    private String region;

    @Column(name = "montant_usd")
    private Double montantUsd;

    public CreanceAgregate() {
    }

    @PrePersist
    protected void prepersist() {
        if (this.uid == null) {
            this.uid = UUID.randomUUID().toString().toLowerCase().replace("-", "");
        }
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Double getMontantUsd() {
        return montantUsd;
    }

    public void setMontantUsd(Double montantUsd) {
        this.montantUsd = montantUsd;
    }
}
