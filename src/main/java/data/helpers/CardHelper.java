/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package data.helpers;

import data.Mesure;
import data.Recquisition;

/**
 *
 * @author endeleya
 */
public class CardHelper {
    private Recquisition recquisition;
    private double remainedQuantity;
    private Mesure remainedMesure;

    public Recquisition getRecquisition() {
        return recquisition;
    }

    public void setRecquisition(Recquisition recquisition) {
        this.recquisition = recquisition;
    }

    public double getRemainedQuantity() {
        return remainedQuantity;
    }

    public void setRemainedQuantity(double remainedQ) {
        this.remainedQuantity = remainedQ;
    }

    public Mesure getRemainedMesure() {
        return remainedMesure;
    }

    public void setRemainedMesure(Mesure remainedMesure) {
        this.remainedMesure = remainedMesure;
    }
    
    
    
}
