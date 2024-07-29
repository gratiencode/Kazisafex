package data;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class VenteResult extends BaseModel implements Serializable {
    private Vente vente;
    private String region;
    private String contenu;
    private String facture;
    private String trader;
    private String saler;
    private double records;
    private List<LigneVente> ligneVente;

    public VenteResult() {
    }

    public VenteResult(String region, String contenu, String facture, String trader, String saler) {
        this.region = region;
        this.contenu = contenu;
        this.facture = facture;
        this.trader = trader;
        this.saler = saler;
    }

    public Vente getVente() {
        return this.vente;
    }

    public void setVente(Vente vente) {
        this.vente = vente;
    }

    public String getTrader() {
        return this.trader;
    }

    public void setTrader(String trader) {
        this.trader = trader;
    }

    public String getSaler() {
        return this.saler;
    }

    public void setSaler(String saler) {
        this.saler = saler;
    }

    public String getRegion() {
        return this.region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getContenu() {
        return this.contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public String getFacture() {
        return this.facture;
    }

    public void setFacture(String facture) {
        this.facture = facture;
    }

    public double getRecords() {
        return this.records;
    }

    public void setRecords(double records) {
        this.records = records;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.vente);
        hash = 97 * hash + Objects.hashCode(this.region);
        hash = 97 * hash + Objects.hashCode(this.saler);
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
            VenteResult other = (VenteResult)obj;
            if (!Objects.equals(this.region, other.region)) {
                return false;
            } else if (!Objects.equals(this.saler, other.saler)) {
                return false;
            } else {
                return Objects.equals(this.vente, other.vente);
            }
        }
    }

   public List<LigneVente> getLigneVente() {
        return ligneVente;
    }

    public void setLigneVente(List<LigneVente> ligneVente) {
        this.ligneVente = ligneVente;
    }
}

