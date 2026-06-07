/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package services;
import data.Produit;
/**
 *
 * @author endeleya
 */
public interface ClotureCallback {

    public void onClosure(int index,int size, Produit produit);
    public void onFinish(int count);
}
