/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Record.java to edit this template
 */
package tools;

/**
 *
 * @author endeleya
 */
public record SaleReport(String category,String codebar,
        String produit,
        double quantite,String unite,
        String devise,double coutAchat,
        double vente,double marge,double percentMarge) {

}
