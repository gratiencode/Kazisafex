package tools;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import java.util.function.Consumer;
import java.util.prefs.Preferences;

public class FingerprintScanner {

    private SerialPort serialPort;
    private Consumer<String> onScanCallback;
    private static FingerprintScanner instance;

    public static FingerprintScanner getInstance() {
        if (instance == null) {
            instance = new FingerprintScanner();
        }
        return instance;
    }

    private FingerprintScanner() {
    }

    public void startListening(Consumer<String> callback) {
        this.onScanCallback = callback;
        Preferences pref = Preferences.userNodeForPackage(SyncEngine.class);
        String portName = pref.get("fingerprint_port", null);

        if (portName == null) {
            System.err.println("Aucun port série configuré pour le scanner d'empreintes.");
            return;
        }

        serialPort = SerialPort.getCommPort(portName);
        serialPort.setBaudRate(9600);

        if (serialPort.openPort()) {
            System.out.println("Scanner d'empreintes connecté sur " + portName);
            serialPort.addDataListener(new SerialPortDataListener() {
                @Override
                public int getListeningEvents() {
                    return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
                }

                @Override
                public void serialEvent(SerialPortEvent event) {
                    if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
                        return;
                    }
                    byte[] newData = new byte[serialPort.bytesAvailable()];
                    int numRead = serialPort.readBytes(newData, newData.length);
                    if (numRead > 0) {
                        String hash = new String(newData).trim();
                        if (!hash.isEmpty() && onScanCallback != null) {
                            onScanCallback.accept(hash);
                        }
                    }
                }
            });
        } else {
            System.err.println("Impossible d'ouvrir le port série " + portName);
        }
    }

    public void stopListening() {
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.removeDataListener();
            serialPort.closePort();
            System.out.println("Scanner d'empreintes déconnecté.");
        }
    }
}
