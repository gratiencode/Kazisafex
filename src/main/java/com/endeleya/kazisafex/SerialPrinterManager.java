package com.endeleya.kazisafex;

import com.fazecast.jSerialComm.SerialPort;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import tools.SyncLogger;

public class SerialPrinterManager {

    public static List<SerialPort> findSerialPrinters() {
        List<SerialPort> printers = new ArrayList<>();
        SerialPort[] ports = SerialPort.getCommPorts();
        SyncLogger.getInstance().log(new Exception("Printers forund size "+ports.length), "SerialPrinterManager.findSerialPrinters");
        printers.addAll(Arrays.asList(ports)); // Heuristic or just list all ports?
        // Usually printers have descriptive names but it varies by OS
        SyncLogger.getInstance().log(new Exception("Printers ready to load "+ports.length), "SerialPrinterManager.findSerialPrinters");
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
