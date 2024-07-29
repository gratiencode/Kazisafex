/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;


import java.util.List;
import java.util.Objects;
import data.Livraison;

/**
 *
 * @author eroot
 */
public class LivraisonHelper {
    private Livraison livraison;
    private List<StockerHelper> stockerHelpers;

    public LivraisonHelper() {
    }

    public Livraison getLivraison() {
        return livraison;
    }

    public void setLivraison(Livraison livraison) {
        this.livraison = livraison;
    }

   

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 19 * hash + Objects.hashCode(this.livraison);
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
        final LivraisonHelper other = (LivraisonHelper) obj;
        if (!Objects.equals(this.livraison, other.livraison)) {
            return false;
        }
        return true;
    }

    public List<StockerHelper> getStockerHelpers() {
        return stockerHelpers;
    }

    public void setStockerHelpers(List<StockerHelper> stockerHelpers) {
        this.stockerHelpers = stockerHelpers;
    }
    
}
