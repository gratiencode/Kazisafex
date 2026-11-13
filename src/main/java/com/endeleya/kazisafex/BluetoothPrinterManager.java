package com.endeleya.kazisafex;

import javax.bluetooth.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import tools.SyncLogger;

public class BluetoothPrinterManager {

    /**
     * Duree maximale d'une recherche Bluetooth. Sans ce delai, un adaptateur
     * present mais muet (pas de pilote, adaptateur desactive, pare-feu) ne
     * rappelle jamais inquiryCompleted et la recherche bloque indefiniment.
     */
    private static final long INQUIRY_TIMEOUT_SECONDS = 25L;

    private static final long SERVICE_SEARCH_TIMEOUT_SECONDS = 15L;

    /**
     * Indique si la machine peut utiliser le Bluetooth. Retourne false plutot
     * que de lever une exception quand la pile JSR-82 ou l'adaptateur est
     * absent, ce qui est le cas d'une grande partie des PC de caisse.
     *
     * @return true si une pile Bluetooth utilisable est presente
     */
    public static boolean isSupported() {
        try {
            return LocalDevice.getLocalDevice() != null;
        } catch (Throwable t) {
            SyncLogger.getInstance().logMessage("BluetoothPrinterManager.isSupported",
                    "Bluetooth indisponible sur cette machine : " + t);
            return false;
        }
    }

    public static List<RemoteDevice> findPrinters() throws IOException, InterruptedException {
        if (!isSupported()) {
            throw new IOException("Bluetooth non supporte par cette machine");
        }
        final List<RemoteDevice> devicesDiscovered = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(1);

        DiscoveryListener listener = new DiscoveryListener() {
            @Override
            public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
                // COD 1664 matches many thermal printers (Imaging/Printing)
                // However, we'll list all and maybe filter later by name or service.
                System.out.println("Searching - BTD "+btDevice.getBluetoothAddress());
                devicesDiscovered.add(btDevice);
                try {
                    System.out.println("Found: " + btDevice.getFriendlyName(false));
                } catch (IOException e) {
                    SyncLogger.getInstance().log(e, "BluetoothPrinterManager.findPrinters");
                    System.out.println("Found device but name unknown");
                }
            }

            @Override
            public void inquiryCompleted(int discType) {
                latch.countDown();
            }

            @Override
            public void serviceSearchCompleted(int transID, int respCode) {}

            @Override
            public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {}
        };

        synchronized (BluetoothPrinterManager.class) {
            LocalDevice.getLocalDevice()
                    .getDiscoveryAgent()
                    .startInquiry(DiscoveryAgent.GIAC, listener);
            if (!latch.await(INQUIRY_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                SyncLogger.getInstance().logMessage("BluetoothPrinterManager.findPrinters",
                        "Recherche Bluetooth interrompue apres " + INQUIRY_TIMEOUT_SECONDS + "s sans reponse.");
            }
        }

        return devicesDiscovered;
    }

    public static String getServiceUrl(RemoteDevice btDevice) throws IOException, InterruptedException {
        final String[] serviceUrl = {null};
        final CountDownLatch latch = new CountDownLatch(1);
        UUID sppUuid = new UUID("1101", true); // Serial Port Profile (SPP)

        DiscoveryListener listener = new DiscoveryListener() {
            @Override
            public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {}

            @Override
            public void inquiryCompleted(int discType) {}

            @Override
            public void serviceSearchCompleted(int transID, int respCode) {
                latch.countDown();
            }

            @Override
            public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
                if (servRecord != null && servRecord.length > 0) {
                    serviceUrl[0] = servRecord[0].getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
                }
            }
        };

        LocalDevice.getLocalDevice().getDiscoveryAgent().searchServices(null, new UUID[]{sppUuid}, btDevice, listener);
        if (!latch.await(SERVICE_SEARCH_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
            SyncLogger.getInstance().logMessage("BluetoothPrinterManager.getServiceUrl",
                    "Recherche du service SPP interrompue apres " + SERVICE_SEARCH_TIMEOUT_SECONDS + "s : "
                            + btDevice.getBluetoothAddress() + " ignoree.");
        }
        return serviceUrl[0];
    }
}
