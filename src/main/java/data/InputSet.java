/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data; import com.fasterxml.jackson.annotation.JsonFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
 import org.hibernate.annotations.UuidGenerator; import jakarta.xml.bind.annotation.XmlRootElement;
import java.time.LocalDate;

/**
 *
 * @author eroot
 */
 @XmlRootElement 
public class InputSet extends BaseModel implements Serializable {
    private Fournisseur fournisseur;
    private String numeroPiece;
    private Entreprise entreprise;
    private Livraison livraison;
    
    private LocalDate dateLivraison;
    private List<Input> entrees;

    public InputSet() {
    }

    public Fournisseur getFournisseur() {
        return fournisseur;
    }

    public void setFournisseur(Fournisseur fournisseur) {
        this.fournisseur = fournisseur;
    }

    public String getNumeroPiece() {
        return numeroPiece;
    }

    public void setNumeroPiece(String numeroPiece) {
        this.numeroPiece = numeroPiece;
    }

    public LocalDate getDateLivraison() {
        return dateLivraison;
    }

    public void setDateLivraison(LocalDate dateLivraison) {
        this.dateLivraison = dateLivraison;
    }

 
     public List<Input> getEntrees() {
        return entrees;
    }

    public void setEntrees(List<Input> entrees) {
        this.entrees = entrees;
    }

    public Entreprise getEntreprise() {
        return entreprise;
    }

    public void setEntreprise(Entreprise entreprise) {
        this.entreprise = entreprise;
    }

    public Livraison getLivraison() {
        return livraison;
    }

    public void setLivraison(Livraison livraison) {
        this.livraison = livraison;
    }
    
    
    
}
