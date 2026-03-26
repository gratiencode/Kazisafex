package com.endeleya.kazisafex;

import com.endeleya.kazisafex.tools.SerialPrintService;
import com.fazecast.jSerialComm.SerialPort;
import java.util.ArrayList;
import java.util.List;

public class SerialPrinterManager {

    public static List<SerialPort> findSerialPrinters() {
        List<SerialPort> printers = new ArrayList<>();
        SerialPort[] ports = SerialPort.getCommPorts();
        for (SerialPort port : ports) {
            // Heuristic or just list all ports? 
            // Usually printers have descriptive names but it varies by OS
            printers.add(port);
        }
        return printers;
    }

    public static String getPortName(SerialPort port) {
        // Descriptive name (Windows: Friendly Name, Linux: Device Path)
        return port.getDescriptivePortName();
    }

    public static String getPortSystemName(SerialPort port) {
        // System name (e.g. COM3, /dev/ttyUSB0)
        return port.getSystemPortName();
    }
}
