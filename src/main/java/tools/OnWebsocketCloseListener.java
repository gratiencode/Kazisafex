/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import jakarta.websocket.Session;

/**
 *
 * @author eroot
 */
public interface OnWebsocketCloseListener {
    public void onWebSocketClose(Session ss, boolean isClosing);
}
