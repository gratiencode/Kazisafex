/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import data.Module;

/**
 *
 * @author eroot
 */
public interface OnUpdateVersionListener {
    public void onNewUpdate(Module module);
    public void onSameUpdate(Module module);
}
