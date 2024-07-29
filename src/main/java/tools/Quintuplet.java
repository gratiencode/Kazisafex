/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

/**
 *
 * @author eroot
 */
public class Quintuplet <V,W,X,Y,Z>{
    private V v;
    private W w;
    private X x;
    private Y y;
    private Z z;

    public Quintuplet(V v, W w, X x, Y y, Z z) {
        this.v = v;
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Quintuplet() {
    }

    public V getV() {
        return v;
    }

    public void setV(V v) {
        this.v = v;
    }

    public W getW() {
        return w;
    }

    public void setW(W w) {
        this.w = w;
    }

    public X getX() {
        return x;
    }

    public void setX(X x) {
        this.x = x;
    }

    public Y getY() {
        return y;
    }

    public void setY(Y y) {
        this.y = y;
    }

    public Z getZ() {
        return z;
    }

    public void setZ(Z z) {
        this.z = z;
    }
    
    
    
}
