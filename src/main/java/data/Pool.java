package data; import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.List;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Pool {
    private String codebar;
    private Livraison livraison;
    private Stocker stocker;
    private Destocker destocker;
    private Recquisition recquisition;
    private List<PrixDeVente> pricesList;
    private List<PVUnit> pricesOutList;

    public Pool() {
    }

    public Pool(String codebar, Livraison livraison, Stocker stocker, Destocker destocker, Recquisition recquisition) {
        this.codebar = codebar;
        this.livraison = livraison;
        this.stocker = stocker;
        this.destocker = destocker;
        this.recquisition = recquisition;
    }
    
    

    public Pool(String codebar) {
        this.codebar = codebar;
    }

    public String getCodebar() {
        return this.codebar;
    }

    public void setCodebar(String codebar) {
        this.codebar = codebar;
    }

    public Livraison getLivraison() {
        return this.livraison;
    }

    public void setLivraison(Livraison livraison) {
        this.livraison = livraison;
    }

    public Stocker getStocker() {
        return this.stocker;
    }

    public void setStocker(Stocker stocker) {
        this.stocker = stocker;
    }

    public Destocker getDestocker() {
        return this.destocker;
    }

    public void setDestocker(Destocker destocker) {
        this.destocker = destocker;
    }

    public Recquisition getRecquisition() {
        return this.recquisition;
    }

    public void setRecquisition(Recquisition recquisition) {
        this.recquisition = recquisition;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + Objects.hashCode(this.codebar);
        return hash;
    }

    

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (this.getClass() != obj.getClass()) {
            return false;
        } else {
            Pool other = (Pool)obj;
            return Objects.equals(this.codebar, other.codebar);
        }
    }

   
 
     public List<PrixDeVente> getPricesList() {
        return this.pricesList;
    }

    public void setPricesList(List<PrixDeVente> pricesList) {
        this.pricesList = pricesList;
    }

 public List<PVUnit> getPricesOutList() {
        return this.pricesOutList;
    }

    public void setPricesOutList(List<PVUnit> pricesOutList) {
        this.pricesOutList = pricesOutList;
    }
}

