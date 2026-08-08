/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package tools;

/**
 * Agregat des achats par fournisseur.
 *
 * @author endeleya
 */
public record PurchaseBySupplier(String nom, String adresse, String phone, long nbLivraisons, double montant) {

}
