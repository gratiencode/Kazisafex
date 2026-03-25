/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.endeleya.ia;

import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.MessageEvent;

/**
 *
 * @author endeleya
 */
public class IaResponseHandler implements EventHandler {

    private OnAiMessageListener OnAiMessageListener;

    @Override
    public void onOpen() throws Exception {
        System.out.println("AI streming open ");
    }

    @Override
    public void onClosed() throws Exception {
        System.out.println("IA streming closed");
    }

    @Override
    public void onMessage(String string, MessageEvent me) throws Exception {
        System.out.println("Reponse brut de l'IA "+me.getData());
        if (OnAiMessageListener != null) {
            String json = me.getData();
//            String id = me.getLastEventId();
            String name = me.getEventName();
            System.out.println("IA : "+json);
            OnAiMessageListener.onIncomingAiMessage(json,name);
        }
    }

    @Override
    public void onComment(String string) throws Exception {

    }

    @Override
    public void onError(Throwable thrwbl) {
        System.out.println("Erreur IA "+thrwbl.getMessage());
    }

    public void setOnAiMessageListener(OnAiMessageListener OnAiMessageListener) {
        this.OnAiMessageListener = OnAiMessageListener;
    }

}
