/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.util.List;
import java.util.Objects;
import data.Destocker;
import data.Mesure;
import data.PrixDeVente;
import data.Produit;
import data.Recquisition;
import data.Stocker;

/**
 *
 * @author eroot
 */
public class DataGrosImporter {
    private Produit product;
    private List<LigneImport> imports;
    private List<Mesure> mesures;
  

    public DataGrosImporter() {
    }

   

    public Produit getProduct() {
        return product;
    }

    public void setProduct(Produit product) {
        this.product = product;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.product);
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
        final DataGrosImporter other = (DataGrosImporter) obj;
        if (!Objects.equals(this.product, other.product)) {
            return false;
        }
        return true;
    }

   
   


    public List<Mesure> getMesures() {
        return mesures;
    }

    public void setMesures(List<Mesure> mesures) {
        this.mesures = mesures;
    }

  

    public List<LigneImport> getImports() {
        return imports;
    }

    public void setImports(List<LigneImport> imports) {
        this.imports = imports;
    }
    
    
    
}
