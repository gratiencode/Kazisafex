package com.endeleya.kazisafex.tools;

import com.fazecast.jSerialComm.SerialPort;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.ColorSupported;
import javax.print.attribute.standard.PrinterName;
import javax.print.event.PrintJobAttributeListener;
import javax.print.event.PrintJobListener;
import javax.print.event.PrintServiceAttributeListener;
import java.io.IOException;
import java.io.OutputStream;

public class SerialPrintService implements PrintService {
    private final String name;
    private final String portSystemName;
    private final int baudRate;

    public SerialPrintService(String name, String portSystemName) {
        this(name, portSystemName, 9600);
    }

    public SerialPrintService(String name, String portSystemName, int baudRate) {
        this.name = "COM:" + name;
        this.portSystemName = portSystemName;
        this.baudRate = baudRate;
    }

    public String getPortSystemName() {
        return portSystemName;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public DocPrintJob createPrintJob() {
        return new SerialPrintJob(this);
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

    private static class SerialPrintJob implements DocPrintJob {
        private final SerialPrintService service;

        public SerialPrintJob(SerialPrintService service) {
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
            SerialPort port = SerialPort.getCommPort(service.getPortSystemName());
            port.setBaudRate(service.baudRate);
            port.setNumDataBits(8);
            port.setNumStopBits(SerialPort.ONE_STOP_BIT);
            port.setParity(SerialPort.NO_PARITY);
            
            if (port.openPort()) {
                try (OutputStream out = port.getOutputStream()) {
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
                    // Wait a bit for the printer to process
                    Thread.sleep(500);
                } catch (IOException | InterruptedException e) {
                    throw new PrintException("Serial printing failed: " + e.getMessage(), e);
                } finally {
                    port.closePort();
                }
            } else {
                throw new PrintException("Could not open serial port: " + service.getPortSystemName());
            }
        }
    }
}
