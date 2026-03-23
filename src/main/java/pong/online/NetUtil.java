package pong.online;

import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/** Utility for discovering the host's viable LAN IPv4 addresses. */
public final class NetUtil {
    private NetUtil() {}

    /**
     * Returns a list of IPv4 address strings that remote clients can use to
     * connect to this machine.
     *
     * <ul>
     *   <li>Loopback interfaces are skipped (they cannot be used over LAN).</li>
     *   <li>Down interfaces are skipped.</li>
     *   <li>{@code "localhost"} is always prepended for same-machine testing.</li>
     * </ul>
     */
    public static List<String> getLocalIpv4Addresses() {
        List<String> result = new ArrayList<>();
        result.add("localhost");
        try {
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            if (ifaces == null) return result;
            while (ifaces.hasMoreElements()) {
                NetworkInterface iface = ifaces.nextElement();
                if (!iface.isUp() || iface.isLoopback()) continue;
                Enumeration<InetAddress> addrs = iface.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                        result.add(addr.getHostAddress());
                    }
                }
            }
        } catch (SocketException ignored) {}
        return result;
    }
}
