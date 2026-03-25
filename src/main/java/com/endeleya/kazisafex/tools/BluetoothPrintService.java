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
        return new DocFlavor[]{DocFlavor.SERVICE_FORMATTED.PRINTABLE, DocFlavor.BYTE_ARRAY.AUTOSENSE};
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
            try {
                connection = (StreamConnection) Connector.open(service.getUrl());
                out = connection.openOutputStream();
                Object data = doc.getPrintData();
                if (data instanceof byte[]) {
                    out.write((byte[]) data);
                } else if (data instanceof java.io.InputStream) {
                    byte[] buffer = new byte[1024];
                    int len;
                    java.io.InputStream in = (java.io.InputStream) data;
                    while ((len = in.read(buffer)) != -1) {
                        out.write(buffer, 0, len);
                    }
                }
                out.flush();
            } catch (IOException e) {
                throw new PrintException("Bluetooth printing failed: " + e.getMessage(), e);
            } finally {
                if (out != null) try { out.close(); } catch (IOException e) {}
                if (connection != null) try { connection.close(); } catch (IOException e) {}
            }
        }
    }
}
