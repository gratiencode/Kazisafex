/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.util.HashMap;

/**
 *
 * @author eroot
 */
public class ServiceLocator {

    InitialContext context;
    private HashMap<String, Object> cache;
    private static ServiceLocator instance = new ServiceLocator();

    public static ServiceLocator getInstance() {
        return instance;
    }

    public ServiceLocator() {
        context = new InitialContext();
        cache = new HashMap<>();
    }

    public Object getService(Tables table) {
        Object result = cache.get(table.name());
        if (result == null) {
            result = context.lookup(table);
            cache.put(table.name(), result);
        }
        return result;
    }

}
