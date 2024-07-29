/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;


import java.util.List;
import java.util.Objects;
import data.PrixDeVente;
import data.Stocker;

/**
 *
 * @author eroot
 */
public class StockerHelper {
    private Stocker stocker;
    private List<PrixDeVente> prices;

    public StockerHelper() {
    }

    public Stocker getStocker() {
        return stocker;
    }

    public void setStocker(Stocker stocker) {
        this.stocker = stocker;
    }

    public List<PrixDeVente> getPrices() {
        return prices;
    }

    public void setPrices(List<PrixDeVente> prices) {
        this.prices = prices;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.stocker);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StockerHelper other = (StockerHelper) obj;
        if (!Objects.equals(this.stocker, other.stocker)) {
            return false;
        }
        return true;
    }
    
}
