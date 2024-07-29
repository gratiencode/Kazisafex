/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import data.BaseModel;
import data.Produit;
import data.Refresher;

/**
 *
 * @author eroot
 */
public class Producer implements Runnable {

    BlockingQueue<BaseModel> products;
    Collection<BaseModel> prox;

    public Producer(BlockingQueue<BaseModel> products, Collection<BaseModel> fromProdx) {
        this.products = products;
        prox = fromProdx;
    }

    @Override
    public void run() {
        try {
            for (BaseModel produit : prox) {
                products.put(produit);//.offer(produit);
                System.out.println("Producer Thread - " + Thread.currentThread().getName() + " " + produit);
                TimeUnit.SECONDS.sleep(2);
            }
            products.put(new Refresher("END"));
        } catch (InterruptedException ex) {
            Logger.getLogger(Producer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
