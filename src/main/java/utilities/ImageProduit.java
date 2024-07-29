/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import java.io.Serializable;
import java.util.Objects;
import data.BaseModel; 

/**
 *
 * @author eroot
 */
public class ImageProduit extends BaseModel implements Serializable{
    private String idProduit;
    private String imageBase64;

    public ImageProduit() {
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.idProduit);
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
        final ImageProduit other = (ImageProduit) obj;
        if (!Objects.equals(this.idProduit, other.idProduit)) {
            return false;
        }
        return true;
    }
    

    public String getIdProduit() {
        return idProduit;
    }

    public void setIdProduit(String idProduit) {
        this.idProduit = idProduit;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }
    
}
