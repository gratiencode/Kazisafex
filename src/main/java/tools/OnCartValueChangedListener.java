/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package tools;

import data.LigneVente;

/**
 *
 * @author endeleya
 */
public interface OnCartValueChangedListener {
    public void onCartValueChanged(LigneVente ligne,double newSum);
}
