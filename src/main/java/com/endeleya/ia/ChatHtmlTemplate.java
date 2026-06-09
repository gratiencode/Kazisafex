package com.endeleya.ia;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import tools.SyncEngine;

public final class ChatHtmlTemplate {

    private ChatHtmlTemplate() {
    }

    public static String jsString(String text) {
        String value = text == null ? "" : text.replaceAll("(?s)<think>.*?</think>", "");
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "")
                + "\"";
    }

    public static String content() {
        String lang = Preferences.userNodeForPackage(SyncEngine.class).get("lang", "fr");
        boolean dark = Preferences.userNodeForPackage(SyncEngine.class).getBoolean("dark_theme_enabled", false);
        String html = """
   <html>
   <head>
       <style>
           :root {
               --brand: #44cef5;
               --brand-dark: #189bc2;
               --ink: #12203a;
               --muted: #72819c;
               --surface: rgba(255, 255, 255, .84);
           }
           * { box-sizing: border-box; }
           body {
               font-family: Inter, "Segoe UI", Arial, sans-serif;
               background:
                   radial-gradient(circle at 12% 8%, rgba(68, 206, 245, .18), transparent 32%),
                   linear-gradient(180deg, #fbfdff 0%, #eef8fb 100%);
               color: var(--ink);
               padding: 14px;
               margin: 0;
               min-height: 100vh;
           }
           body.dark {
               background:
                   radial-gradient(circle at 12% 8%, rgba(68, 206, 245, .16), transparent 32%),
                   linear-gradient(180deg, #0f172a 0%, #111827 100%);
               color: #e5e7eb;
           }
           #chat { display: flex; flex-direction: column; gap: 13px; }
           #chat:empty::before {
               content: "__READY_TEXT__";
               display: block;
               width: fit-content;
               margin: 26px auto 8px;
               padding: 9px 14px;
               border-radius: 999px;
               color: #496277;
               background: rgba(255, 255, 255, .78);
               border: 1px solid rgba(68, 206, 245, .28);
               box-shadow: 0 14px 36px rgba(18, 32, 58, .08);
               font-size: 12px;
               font-weight: 700;
               letter-spacing: .3px;
           }
           body.dark #chat:empty::before {
               color: #d1d5db;
               background: rgba(15, 23, 42, .78);
               border-color: rgba(68, 206, 245, .22);
           }
           .message { display: flex; width: 100%; }
           .message.user { justify-content: flex-end; }
           .message.assistant { justify-content: flex-start; }
           .text {
               max-width: 88%;
               padding: 11px 13px;
               border-radius: 17px;
               font-size: 13px;
               line-height: 1.5;
               overflow-wrap: anywhere;
               box-shadow: 0 16px 34px rgba(18, 32, 58, 0.10);
               position: relative;
           }
           .user .text {
               background: linear-gradient(135deg, var(--brand), var(--brand-dark));
               color: #ffffff;
               border-bottom-right-radius: 6px;
               border: 1px solid rgba(255, 255, 255, .38);
           }
           .assistant .text {
               background: var(--surface);
               color: var(--ink);
               border: 1px solid rgba(68, 206, 245, 0.38);
               border-bottom-left-radius: 6px;
               backdrop-filter: blur(10px);
           }
           body.dark .assistant .text {
               background: rgba(15, 23, 42, .86);
               color: #e5e7eb;
               border-color: rgba(68, 206, 245, .28);
           }
           .assistant .text.process {
               color: #7a8494;
               font-style: italic;
               background: rgba(255, 255, 255, .70);
               border-color: rgba(122, 132, 148, .28);
           }
           body.dark .assistant .text.process {
               color: #9ca3af;
               background: rgba(17, 24, 39, .82);
               border-color: rgba(156, 163, 175, .24);
           }
           .assistant .text::before {
               content: "J";
               position: absolute;
               left: -7px;
               top: -7px;
               width: 19px;
               height: 19px;
               border-radius: 50%;
               background: linear-gradient(135deg, var(--brand), var(--brand-dark));
               color: white;
               font-size: 11px;
               font-weight: 900;
               display: flex;
               align-items: center;
               justify-content: center;
               box-shadow: 0 7px 16px rgba(24, 155, 194, .28);
           }
           .bubble-actions {
               position: absolute;
               right: 8px;
               bottom: -18px;
               display: flex;
               gap: 8px;
               opacity: 0;
               transition: opacity .15s ease;
           }
           .bubble-action {
               border: 1px solid rgba(24, 155, 194, .32);
               background: rgba(255, 255, 255, .92);
               color: #466178;
               border-radius: 999px;
               padding: 2px 8px;
               font-size: 11px;
               font-weight: 700;
               cursor: pointer;
           }
           body.dark .bubble-action {
               background: rgba(17, 24, 39, .94);
               color: #d1d5db;
               border-color: rgba(68, 206, 245, .28);
           }
           .assistant .text:hover .bubble-actions {
               opacity: 1;
           }
           #toast {
               position: fixed;
               left: 50%;
               bottom: 18px;
               transform: translateX(-50%) translateY(18px);
               background: rgba(18, 32, 58, .92);
               color: #ffffff;
               padding: 8px 12px;
               border-radius: 999px;
               font-size: 12px;
               font-weight: 800;
               box-shadow: 0 12px 28px rgba(18, 32, 58, .18);
               opacity: 0;
               pointer-events: none;
               transition: opacity .18s ease, transform .18s ease;
               z-index: 999;
           }
           #toast.show {
               opacity: 1;
               transform: translateX(-50%) translateY(0);
           }
           strong { font-weight: 800; }
           code {
               background: rgba(18, 32, 58, 0.07);
               border-radius: 6px;
               padding: 2px 6px;
               font-family: Consolas, monospace;
           }
           table {
               width: 100%;
               border-collapse: collapse;
               margin: 8px 0;
               background: #ffffff;
               border-radius: 10px;
               overflow: hidden;
               font-size: 12px;
               box-shadow: 0 8px 20px rgba(18, 32, 58, .06);
           }
           body.dark table {
               background: #111827;
               color: #e5e7eb;
           }
           th {
               background: rgba(68, 206, 245, 0.18);
               color: #123247;
               font-weight: 800;
           }
           body.dark th {
               background: rgba(68, 206, 245, 0.14);
               color: #f9fafb;
           }
           th, td {
               border: 1px solid rgba(18, 32, 58, 0.10);
               padding: 7px 8px;
               text-align: left;
               vertical-align: top;
           }
           .barcode-img {
               display: block;
               width: 180px;
               max-width: 100%;
               height: auto;
               padding: 4px;
               background: #ffffff;
               border: 1px solid rgba(18, 32, 58, 0.12);
               border-radius: 4px;
           }
           .attachment-line {
               display: block;
               margin-top: 8px;
               color: inherit;
               opacity: .9;
               font-size: 12px;
           }
           #cursor {
               display: inline-block;
               width: 10px;
               color: var(--brand);
               animation: blink 1s infinite;
               margin-top: 10px;
               font-weight: 900;
           }
           @keyframes blink {
               0% { opacity: 1; }
               50% { opacity: 0; }
               100% { opacity: 1; }
           }
       </style>
   </head>
   <body class="__BODY_CLASS__">
       <div id="chat"></div>
       <div id="toast"></div>
       <span id="cursor">|</span>
       <script>
           let currentBotText = "";
           let currentBotTextDiv = null;
           let currentBotIsProcess = false;
           let chatActionNonce = 0;
           const reflectionPrefix = 'Reflexion : ';

           function escapeHtml(text) {
               return (text || '').replace(/[&<>"']/g, function(ch) {
                   return {'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'}[ch];
               });
           }

           function renderInline(text) {
               return escapeHtml(text)
                   .replace(/!\\[([^\\]]*)\\]\\((data:image\\/(?:png|jpeg|jpg);base64,[^)]+)\\)/g, '<img class="barcode-img" src="$2" alt="$1">')
                   .replace(/`([^`]+)`/g, '<code>$1</code>')
                   .replace(/\\*\\*([^*]+)\\*\\*/g, '<strong>$1</strong>')
                   .replace(/\\*([^*]+)\\*/g, '<strong>$1</strong>');
           }

           function isSeparator(line) {
               return /^\\s*\\|?\\s*:?-{3,}:?\\s*(\\|\\s*:?-{3,}:?\\s*)+\\|?\\s*$/.test(line);
           }

           function renderTable(lines, start) {
               const rows = [];
               let index = start;
               while (index < lines.length && lines[index].includes('|') && lines[index].trim() !== '') {
                   if (!isSeparator(lines[index])) {
                       let cells = lines[index].trim();
                       if (cells.startsWith('|')) cells = cells.substring(1);
                       if (cells.endsWith('|')) cells = cells.substring(0, cells.length - 1);
                       rows.push(cells.split('|').map(cell => renderInline(cell.trim())));
                   }
                   index++;
               }
               if (rows.length === 0) return { html: '', next: start + 1 };
               const head = rows[0];
               const body = rows.slice(1);
               let html = '<table><thead><tr>' + head.map(cell => '<th>' + cell + '</th>').join('') + '</tr></thead><tbody>';
               html += body.map(row => '<tr>' + row.map(cell => '<td>' + cell + '</td>').join('') + '</tr>').join('');
               html += '</tbody></table>';
               return { html, next: index };
           }

           function renderMarkdown(text) {
               const clean = (text || '').replace(/<think>[\\s\\S]*?<\\/think>/g, '').trim();
               const lines = clean.split(/\\n/);
               let html = '';
               for (let i = 0; i < lines.length; i++) {
                   if (lines[i].includes('|') && i + 1 < lines.length && isSeparator(lines[i + 1])) {
                       const result = renderTable(lines, i);
                       html += result.html;
                       i = result.next - 1;
                   } else if (lines[i].trim() === '') {
                       html += '<br>';
                   } else {
                       html += renderInline(lines[i]) + '<br>';
                   }
               }
               return html;
           }

           function assistantActions(textDiv) {
               let actions = textDiv.querySelector('.bubble-actions');
               if (!actions) {
                   actions = document.createElement('div');
                   actions.className = 'bubble-actions';
                   textDiv.appendChild(actions);
               }
               return actions;
           }

           function attachReplyButton(textDiv, textProvider) {
               const button = document.createElement('button');
               button.className = 'bubble-action reply-button';
               button.type = 'button';
               button.textContent = '__REPLY_TEXT__';
               button.onclick = function(event) {
                   event.preventDefault();
                   event.stopPropagation();
                   const text = (textProvider && textProvider()) || textDiv.dataset.rawText || '';
                   replyToJemimaMessage(text);
               };
               assistantActions(textDiv).appendChild(button);
           }

           function attachCopyButton(textDiv, textProvider) {
               const button = document.createElement('button');
               button.className = 'bubble-action copy-button';
               button.type = 'button';
               button.textContent = '__COPY_TEXT__';
               button.onclick = function(event) {
                   event.preventDefault();
                   event.stopPropagation();
                   const text = (textProvider && textProvider()) || textDiv.dataset.rawText || '';
                   copyJemimaMessage(text, button);
               };
               assistantActions(textDiv).appendChild(button);
           }

           function attachAssistantActions(textDiv, textProvider) {
               attachCopyButton(textDiv, textProvider);
               attachReplyButton(textDiv, textProvider);
           }

           function replyToJemimaMessage(text) {
               const value = String(text || '');
               try {
                   if (window.kazisafeChat) {
                       window.kazisafeChat.replyToJemima(value);
                   }
               } catch (error) {
                   console.log('reply bridge unavailable', error);
               }
               const payload = '__KAZISAFE_REPLY__' + encodeURIComponent(value);
               try {
                   document.title = payload + '|' + Date.now() + '-' + (++chatActionNonce);
               } catch (error) {
                   console.log('reply title fallback unavailable', error);
               }
               try {
                   alert(payload);
               } catch (error) {
                   console.log('reply fallback unavailable', error);
               }
           }

           function copyJemimaMessage(text, button) {
               const value = String(text || '');
               const done = function() {
                   const previous = button.textContent;
                   button.textContent = 'Copié';
                   showToast('__COPIED_TEXT__');
                   setTimeout(function() { button.textContent = previous; }, 1200);
               };
               if (navigator.clipboard && navigator.clipboard.writeText) {
                   navigator.clipboard.writeText(value).catch(function() {
                       copyJemimaMessageViaBridge(value);
                   });
               }
               copyJemimaMessageViaBridge(value);
               done();
           }

           function showToast(message) {
               const toast = document.getElementById('toast');
               if (!toast) return;
               toast.textContent = message || '__COPIED_TEXT__';
               toast.classList.add('show');
               clearTimeout(window.__kazisafeToastTimer);
               window.__kazisafeToastTimer = setTimeout(function() {
                   toast.classList.remove('show');
               }, 1500);
           }

           function copyJemimaMessageViaBridge(text) {
               const value = String(text || '');
               try {
                   if (window.kazisafeChat) {
                       window.kazisafeChat.copyJemimaMessage(value);
                   }
               } catch (error) {
                   console.log('copy bridge unavailable', error);
               }
               const payload = '__KAZISAFE_COPY__' + encodeURIComponent(value);
               try {
                   document.title = payload + '|' + Date.now() + '-' + (++chatActionNonce);
               } catch (error) {
                   console.log('copy title fallback unavailable', error);
               }
               try {
                   alert(payload);
               } catch (error) {
                   console.log('copy fallback unavailable', error);
               }
           }

           function reflectionText(text) {
               const value = String(text || '').trimStart();
               if (value.toLowerCase().startsWith(reflectionPrefix.toLowerCase())) {
                   return value;
               }
               return reflectionPrefix + value;
           }

           function finalAnswerText(text) {
               const value = String(text || '');
               if (value.trimStart().toLowerCase().startsWith(reflectionPrefix.toLowerCase())) {
                   return value.trimStart().substring(reflectionPrefix.length);
               }
               return value;
           }

           function appendMessage(type, text) {
               const chat = document.getElementById('chat');
               const msgDiv = document.createElement('div');
               msgDiv.className = 'message ' + type;
               const textDiv = document.createElement('div');
               textDiv.className = 'text';
               textDiv.dataset.rawText = text || '';
               textDiv.innerHTML = renderMarkdown(text);
               if (type === 'assistant') {
                   attachAssistantActions(textDiv, function() { return textDiv.dataset.rawText || ''; });
               }
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
                   attachAssistantActions(textDiv, function() { return currentBotText; });
                   msgDiv.appendChild(textDiv);
                   chat.appendChild(msgDiv);
                   currentBotTextDiv = textDiv;
               }
               currentBotText += text;
               currentBotTextDiv.classList.remove('process');
               currentBotTextDiv.dataset.rawText = currentBotText;
               currentBotTextDiv.innerHTML = renderMarkdown(currentBotText);
               attachAssistantActions(currentBotTextDiv, function() { return currentBotTextDiv.dataset.rawText || currentBotText; });
               window.scrollTo(0, document.body.scrollHeight);
           }

           function showBotProcess(text) {
               if (!currentBotTextDiv) {
                   const chat = document.getElementById('chat');
                   const msgDiv = document.createElement('div');
                   msgDiv.className = 'message assistant';
                   const textDiv = document.createElement('div');
                   textDiv.className = 'text';
                   attachAssistantActions(textDiv, function() { return textDiv.dataset.rawText || ''; });
                   msgDiv.appendChild(textDiv);
                   chat.appendChild(msgDiv);
                   currentBotTextDiv = textDiv;
               }
               currentBotIsProcess = true;
               currentBotTextDiv.classList.add('process');
               const visibleText = reflectionText(text);
               currentBotTextDiv.dataset.rawText = visibleText;
               currentBotTextDiv.innerHTML = renderMarkdown(visibleText);
               attachAssistantActions(currentBotTextDiv, function() { return currentBotTextDiv.dataset.rawText || ''; });
               window.scrollTo(0, document.body.scrollHeight);
           }

           function appendBotAnswer(text) {
               if (currentBotIsProcess) {
                   currentBotText = "";
                   currentBotIsProcess = false;
                   if (currentBotTextDiv) {
                       currentBotTextDiv.classList.remove('process');
                       currentBotTextDiv.dataset.rawText = '';
                       currentBotTextDiv.innerHTML = '';
                   }
               }
               appendBotPartial(finalAnswerText(text));
           }

           function endBotMessage() {
               currentBotText = "";
               currentBotTextDiv = null;
               currentBotIsProcess = false;
           }
       </script>
   </body>
   </html>
        """;
        return html
                .replace("__BODY_CLASS__", dark ? "dark" : "light")
                .replace("__READY_TEXT__", cssText(label(lang, "xjemima.ready", "Jemima est prête")))
                .replace("__REPLY_TEXT__", jsLiteralContent(label(lang, "xjemima.reply", "Répondre")))
                .replace("__COPY_TEXT__", jsLiteralContent(label(lang, "xjemima.copy", "Copier")))
                .replace("__COPIED_TEXT__", jsLiteralContent(label(lang, "xjemima.copied", "Texte copié")));
    }

    private static String label(String lang, String key, String fallback) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("bundles." + lang, new Locale.Builder().setLanguage(lang).build());
            return bundle.containsKey(key) ? bundle.getString(key) : fallback;
        } catch (MissingResourceException | IllegalArgumentException ex) {
            return fallback;
        }
    }

    private static String cssText(String text) {
        return (text == null ? "" : text)
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ");
    }

    private static String jsLiteralContent(String text) {
        return (text == null ? "" : text)
                .replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\n", " ");
    }
}
