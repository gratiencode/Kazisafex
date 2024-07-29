/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utilities;

/**
 *
 * @author endeleya
 */  
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
public class Configuration {


    public static ObjectMapper configureObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
//        Hibernate5Module hibernate5Module = new Hibernate5Module();
//        
//        // Configurez le module si nécessaire
//        hibernate5Module.configure(Hibernate5Module.Feature.FORCE_LAZY_LOADING, false);
//        
//        objectMapper.registerModule(hibernate5Module);
        return objectMapper;
    }


}
