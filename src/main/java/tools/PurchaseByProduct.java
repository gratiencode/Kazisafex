/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package tools;

/**
 * Agregat des achats par produit.
 *
 * @author endeleya
 */
public record PurchaseByProduct(String codebar, String produit, String unite, double quantite, double montant) {

}
