/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package IServices;

import data.CommandeLister;
import java.util.List; import java.time.LocalDateTime;

/**
 *
 * @author endeleya
 */
public interface CommandeListStorage {

    public CommandeLister saveCommandeLister(CommandeLister d);

    public CommandeLister updateCommandeLister(CommandeLister d);

    public CommandeLister findCommandeLister(String d);

    public List<CommandeLister> findCommandeListers();

    public void deleteCommandeLister(CommandeLister choosenCommandeLister);
    
    public boolean isExists(String uid);public boolean isExists(String uid, LocalDateTime atime);
    
}
