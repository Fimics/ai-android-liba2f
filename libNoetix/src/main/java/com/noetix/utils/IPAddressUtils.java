package com.noetix.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.InetSocketAddress;
import java.util.Enumeration;

public class IPAddressUtils {

    private static final String TAG = "nx_app";

    public static InetAddress getLocalIPv4Address() throws SocketException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress inetAddress = inetAddresses.nextElement();
                // Check if not loopback address and is IPv4
                if (!inetAddress.isLoopbackAddress() && inetAddress instanceof java.net.Inet4Address) {
                    return inetAddress;
                }
            }
        }
        return null; // No IPv4 address found, or I/O error occurred
    }

    public static void test() {
        try {
            InetAddress localAddress = getLocalIPv4Address();
            if (localAddress != null) {
                int port = 8080; // Replace with the port you want to use
                InetSocketAddress socketAddress = new InetSocketAddress(localAddress, port);
                KLog.d(TAG,"Local IP Address: " + localAddress.getHostAddress());
                KLog.d(TAG,"Socket Address: " + socketAddress.getAddress().getHostAddress() + ":" + socketAddress.getPort());
                // Now you can use 'socketAddress' to create a server or client socket
            } else {
                KLog.d(TAG,"No local IPv4 address found.");
            }
        } catch (SocketException e) {
            KLog.d(TAG,"ex ->"+e.getMessage());
        }
    }
}
