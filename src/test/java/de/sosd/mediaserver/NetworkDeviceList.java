package de.sosd.mediaserver;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class NetworkDeviceList {

    /**
     * @param args
     * @throws SocketException
     */
    public static void main(final String[] args) throws SocketException {
        Enumeration<NetworkInterface> ifaces = NetworkInterface
                .getNetworkInterfaces();

        final String systemId = getLoopbacksMac(ifaces);
        ifaces = NetworkInterface.getNetworkInterfaces();
        System.out.println(">" + systemId + "<");

        // configured one does not work! try any working ipv4 iface
        while (ifaces.hasMoreElements()) {
            final NetworkInterface iface = ifaces.nextElement();
            if (iface.getName().toLowerCase().startsWith("eth")) {
                final Enumeration<InetAddress> addressList = iface
                        .getInetAddresses();
                while (addressList.hasMoreElements()) {
                    final InetAddress address = addressList.nextElement();
                    if (!(address instanceof Inet6Address)) {

                        final String hostAddress = address.getHostAddress();
                        final String hostName = address.getHostName();
                        final String interfaceName = iface.getName();

                        System.out.println("ipv4 " + iface.getName() + " ("
                                + iface.getDisplayName() + ") " + address + " "
                                + iface.getHardwareAddress());
                    } else {
                        System.out.println("ipv6 " + iface.getName() + " ("
                                + iface.getDisplayName() + ") " + address + " "
                                + iface.getHardwareAddress());
                    }
                }

            } else {
                final Enumeration<InetAddress> addressList = iface
                        .getInetAddresses();
                while (addressList.hasMoreElements()) {
                    final InetAddress address = addressList.nextElement();
                    if (!(address instanceof Inet6Address)) {

                        final String hostAddress = address.getHostAddress();
                        final String hostName = address.getHostName();
                        final String interfaceName = iface.getName();

                        System.out.println("no eth ipv4 " + iface.getName()
                                + " (" + iface.getDisplayName() + ") "
                                + address + " " + iface.getHardwareAddress());
                    } else {
                        System.out.println("no eth ipv6 " + iface.getName()
                                + " (" + iface.getDisplayName() + ") "
                                + address + " " + iface.getHardwareAddress());
                    }
                }
            }

        }
    }

    private static String getLoopbacksMac(
            final Enumeration<NetworkInterface> ifaces) throws SocketException {
        while (ifaces.hasMoreElements()) {
            final NetworkInterface iface = ifaces.nextElement();
            if (iface.getName().toLowerCase().startsWith("eth")
                    && iface.getHardwareAddress() != null) {
                return getMac(iface.getHardwareAddress());
            }
        }
        return "none";
    }

    private static String getMac(final byte[] mac) {

        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
            sb.append(String.format("%02X%s", mac[i], i < mac.length - 1 ? "-"
                    : ""));
        }
        return sb.toString();
    }

}
