package com.endeleya.kazisafex;

import javax.bluetooth.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class BluetoothPrinterManager {

    public static List<RemoteDevice> findPrinters() throws IOException, InterruptedException {
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
            latch.await();
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
        latch.await();
        return serviceUrl[0];
    }
}
