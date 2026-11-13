package com.endeleya.kazisafex.tools;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.ColorSupported;
import javax.print.attribute.standard.PrinterName;
import javax.print.event.PrintJobAttributeListener;
import javax.print.event.PrintJobListener;
import javax.print.event.PrintServiceAttributeListener;
import java.io.IOException;
import java.io.OutputStream;

public class BluetoothPrintService implements PrintService {
    private final String name;
    private final String url;

    public BluetoothPrintService(String name, String url) {
        this.name = "BT:" + name;
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public DocPrintJob createPrintJob() {
        return new BluetoothPrintJob(this);
    }

    @Override
    public void addPrintServiceAttributeListener(PrintServiceAttributeListener listener) {}

    @Override
    public void removePrintServiceAttributeListener(PrintServiceAttributeListener listener) {}

    @Override
    public PrintServiceAttributeSet getAttributes() {
        HashPrintServiceAttributeSet attrs = new HashPrintServiceAttributeSet();
        attrs.add(new PrinterName(name, null));
        attrs.add(ColorSupported.NOT_SUPPORTED);
        return attrs;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends PrintServiceAttribute> T getAttribute(Class<T> category) {
        return (T) getAttributes().get(category);
    }

    @Override
    public DocFlavor[] getSupportedDocFlavors() {
        return new DocFlavor[]{
            DocFlavor.INPUT_STREAM.AUTOSENSE,
            DocFlavor.BYTE_ARRAY.AUTOSENSE,
            DocFlavor.SERVICE_FORMATTED.PRINTABLE
        };
    }

    @Override
    public boolean isDocFlavorSupported(DocFlavor flavor) {
        return true;
    }

    @Override
    public ServiceUIFactory getServiceUIFactory() {
        return null;
    }

    @Override
    public Object getDefaultAttributeValue(Class<? extends Attribute> category) {
        return null;
    }

    @Override
    public Object getSupportedAttributeValues(Class<? extends Attribute> category, DocFlavor flavor, AttributeSet attributes) {
        return null;
    }

    @Override
    public boolean isAttributeValueSupported(Attribute attr, DocFlavor flavor, AttributeSet attributes) {
        return true;
    }

    @Override
    public AttributeSet getUnsupportedAttributes(DocFlavor flavor, AttributeSet attributes) {
        return null;
    }

    @Override
    public Class<?>[] getSupportedAttributeCategories() {
        return new Class[0];
    }

    @Override
    public boolean isAttributeCategorySupported(Class<? extends Attribute> category) {
        return false;
    }

    @Override
    public String toString() {
        return name;
    }

    private static class BluetoothPrintJob implements DocPrintJob {
        private final BluetoothPrintService service;

        public BluetoothPrintJob(BluetoothPrintService service) {
            this.service = service;
        }

        @Override
        public PrintService getPrintService() {
            return service;
        }

        @Override
        public PrintJobAttributeSet getAttributes() {
            return new HashPrintJobAttributeSet();
        }

        @Override
        public void addPrintJobListener(PrintJobListener listener) {}

        @Override
        public void removePrintJobListener(PrintJobListener listener) {}

        @Override
        public void addPrintJobAttributeListener(PrintJobAttributeListener listener, PrintJobAttributeSet attributes) {}

        @Override
        public void removePrintJobAttributeListener(PrintJobAttributeListener listener) {}

        @Override
        public void print(Doc doc, PrintRequestAttributeSet attributes) throws PrintException {
            StreamConnection connection = null;
            OutputStream out = null;
            long started = System.currentTimeMillis();
            byte[] payload = new byte[0];
            long sent = 0L;
            boolean failed = false;
            try {
                // escpos-coffee fournit le flux ESC/POS via DocFlavor.INPUT_STREAM :
                // lire uniquement getPrintData() renvoyait null et l'imprimante
                // ne recevait rien (papier sorti vide).
                payload = EscPosPrintSupport.docToBytes(doc);
                connection = (StreamConnection) Connector.open(service.getUrl());
                out = connection.openOutputStream();
                // Envoi par blocs : une imprimante thermique d'entree de gamme
                // deborde de son tampon si on lui vide 30 Ko d'un seul coup, ce qui
                // la desynchronise et fait sortir du papier vierge.
                sent = EscPosPrintSupport.writeChunked(out, payload);
                out.flush();
                EscPosPrintSupport.pause(EscPosPrintSupport.CUT_DELAY_MS);
            } catch (IOException e) {
                failed = true;
                tools.SyncLogger.getInstance().log(e, "BluetoothPrintService.print");
                EscPosPrintSupport.logPayload("BluetoothPrintService.print (ECHEC)",
                        service.getName(), payload, sent, System.currentTimeMillis() - started);
                throw new PrintException("Bluetooth printing failed: " + e.getMessage(), e);
            } finally {
                if (out != null) try { out.close(); } catch (IOException e) {}
                if (connection != null) try { connection.close(); } catch (IOException e) {}
                if (!failed) {
                    EscPosPrintSupport.logPayload("BluetoothPrintService.print",
                            service.getName(), payload, sent, System.currentTimeMillis() - started);
                }
            }
        }
    }
}
