/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import delegates.FournisseurDelegate;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import data.BaseModel;
import data.Fournisseur;
import tools.Tables;

/**
 *
 * @author eroot
 */
public class SyncBucket implements Callable<String> {

    BlockingQueue<BaseModel> data;

    public SyncBucket(BlockingQueue<BaseModel> models) {
        this.data = models;
      }

    @Override
    public String call() throws Exception {
        List<Fournisseur> objs = FournisseurDelegate.findFournisseurs();
        System.err.println("obs fss "+objs.size());                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   
        if (objs != null) {
        for (Fournisseur obj : objs) {
            try {
                        obj.setType(Tables.FOURNISSEUR.name());
                        obj.setPriority(tools.Constants.PRIORITY_SYNC);
                        data.put(obj);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(SyncBucket.class.getName()).log(Level.SEVERE, null, ex);
                    }
        }
         System.err.println("BQ FSS size "+data.size());
        }
     
          System.err.println("BQ size "+data.size());
        
        return "Sync-finish";
    }

}
