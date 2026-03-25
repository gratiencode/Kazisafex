/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.endeleya.kazisafex;

import data.core.KazisafeServiceFactory;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import com.endeleya.ia.IaResponseHandler;
import com.launchdarkly.eventsource.EventSource;
import java.util.StringTokenizer;
import javafx.event.ActionEvent;
import okhttp3.Headers;
import tools.MainUI;

/**
 * FXML Controller class
 *
 * @author endeleya
 */
public class IassistantController implements Initializable {

    @FXML
    private TextField tf_input_msg_ia;
    @FXML
    private WebView ia_webvu_chat;

    WebEngine webE;
    IaResponseHandler responseHandler;
    Preferences pref;
    boolean isNofication = false;

    String entreprise;
    String user;
    String token;
    EventSource evs;

    String initialHtml = """
   <html>
   <head>
       <style>
           body {
               font-family: sans-serif;
               background-color: #f7f7f7;
               padding: 10px;
           }
           .message {
               margin-bottom: 10px;
           }
           .user .text {
               background-color: #d1e7dd;
               padding: 8px 12px;
               border-radius: 8px;
               color: #0f5132;
           }
           .assistant .text {
               background-color: #e2e3e5;
               padding: 8px 12px;
               border-radius: 8px;
               color: #41464b;
           }
           #chat {
               white-space: pre-wrap;
           }
           #cursor {
               display: inline-block;
               width: 10px;
               animation: blink 1s infinite;
           }
           @keyframes blink {
               0% { opacity: 1; }
               50% { opacity: 0; }
               100% { opacity: 1; }
           }
       </style>
   </head>
   <body>
       <div id="chat"></div>
       <span id="cursor">|</span>
       <script>
           let currentBotText = "";
           let currentBotTextDiv = null;
   
           function appendMessage(type, text) {
               const chat = document.getElementById('chat');
               const msgDiv = document.createElement('div');
               msgDiv.className = 'message ' + type;
   
               const textDiv = document.createElement('div');
               textDiv.className = 'text';
               textDiv.innerText = text;
   
               msgDiv.appendChild(textDiv);
               chat.appendChild(msgDiv);
               window.scrollTo(0, document.body.scrollHeight);
           }
   
           function appendUser(text) {
               appendMessage('user', text);
           }
   
           function appendBotPartial(text) {
               if (!currentBotTextDiv) {
                   const chat = document.getElementById('chat');
                   const msgDiv = document.createElement('div');
                   msgDiv.className = 'message assistant';
   
                   const textDiv = document.createElement('div');
                   textDiv.className = 'text';
                   textDiv.id = 'bot-msg-last';
   
                   msgDiv.appendChild(textDiv);
                   chat.appendChild(msgDiv);
                   currentBotTextDiv = textDiv;
               }
   
               currentBotText += text;
               currentBotTextDiv.innerText = currentBotText;
               window.scrollTo(0, document.body.scrollHeight);
           }
   
           function endBotMessage() {
               currentBotText = "";
               currentBotTextDiv = null;
           }
       </script>
   </body>
   </html>
""";

    /**
     * Initializes the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        pref = Preferences.userNodeForPackage(tools.SyncEngine.class);
        webE = ia_webvu_chat.getEngine();
        webE.loadContent(initialHtml);
       
        responseHandler = new IaResponseHandler();
        responseHandler.setOnAiMessageListener((String message, String name) -> {
            System.out.println("msg : " + message);
            Platform.runLater(() -> {
                if (name.equals("[on-going]")) {
                    webE.executeScript("appendBotPartial(" + escapeForJS(message) + ")");
                } else if (name.equals("[end]")) {
                    webE.executeScript("appendBotPartial(" + escapeForJS(message) + ")");
                    webE.executeScript("endBotMessage()");
                    evs.close();
                } else if (name.equals("[notification]")) {
                    if (!isNofication) {
                        webE.executeScript("appendBotPartial(" + escapeForJS(message) + ")");
                        webE.executeScript("endBotMessage()");
                        isNofication = true;
                        evs.close();
                    }
                }
            });
        });
                
    }

    public void setup(String entreprise, String user) {
        this.token = pref.get("token", null);
 
    }
    
    private void inform(String msg){
        StringTokenizer st=new StringTokenizer(msg," ");
        while(st.hasMoreTokens()){
            String tkn=st.nextToken();
            System.out.println("iA--->");
            webE.executeScript("appendBotPartial(" + escapeForJS(tkn) + ")");
        } 
    }

    @FXML
    private void onHoverHome(MouseEvent event) {
        ImageView img = (ImageView) event.getSource();
        MainUI.setShadowEffect(img);
    }

    @FXML
    private void onOutHome(MouseEvent event) {
        ImageView img = (ImageView) event.getSource();
        MainUI.removeShaddowEffect(img);
    }

    @FXML
    public void close(Event evt) {
        Node n = (Node) evt.getSource();
        Stage st = (Stage) n.getScene().getWindow();
        st.close();
    }

    private String escapeForJS(String text) {
        return "\"" + text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "").replace("<think>.*?</think>", "")
                + "\"";
    }
//46.202.195.104

    @FXML
    private void sendMessage(Event event) {
        this.token = pref.get("token", null);
        sendText();
    }

    private String urlWithMessage(String message) {
        String q = URLEncoder.encode(message, StandardCharsets.UTF_8);
        String url = KazisafeServiceFactory.BASE_URL + "ia/ask?q=" + q;
        return url;
    }

    private void sendText() {
        if (tf_input_msg_ia.getText().isBlank()) {
            MainUI.notify(null, "", "Veuillez envoyer un message valide", 4, "error");
            return;
        }
        new Thread(() -> {
            Headers hd = new Headers.Builder()
                    .add("Authorization", "Bearer " + this.token).build();
            Platform.runLater(() -> {
                webE.executeScript("appendUser(" + escapeForJS(tf_input_msg_ia.getText()) + ")");
            });
            String url = urlWithMessage(tf_input_msg_ia.getText());
            System.out.println("Url = " + url);
            EventSource.Builder evb = new EventSource.Builder(responseHandler, URI.create(url))
                    .headers(hd).readTimeout(3, TimeUnit.MINUTES)
                    .reconnectTime(1, TimeUnit.SECONDS);
            evs = evb.build();
            evs.start();
            Platform.runLater(() -> {
                tf_input_msg_ia.clear();
            });
        }).start();

    }

    @FXML
    private void addAttachemnts(ActionEvent event) {
        MainUI.notify(null, "Info", "Info : coming soon!", 5, "info");
    }

}
