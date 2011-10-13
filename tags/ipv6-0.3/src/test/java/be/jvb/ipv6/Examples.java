package be.jvb.ipv6;

import org.junit.Test;

/**
 * @author Jan Van Besien
 */
public class Examples
{
    @Test
    public void ipAddressConstruction()
    {
        final IPv6Address iPv6Address = IPv6Address.fromString("fe80::226:2dff:fefa:cd1f");
    }

    @Test
    public void ipAddressAdditionAndSubtraction()
    {
        final IPv6Address iPv6Address = IPv6Address.fromString("fe80::226:2dff:fefa:cd1f");
        final IPv6Address next = iPv6Address.add(1);
        final IPv6Address previous = iPv6Address.subtract(1);
        System.out.println(next.toString()); // prints fe80::226:2dff:fefa:cd20
        System.out.println(previous.toString()); // prints fe80::226:2dff:fefa:cd1e
    }

    @Test
    public void ipAddressRangeConstruction()
    {
        final IPv6AddressRange range = new IPv6AddressRange(IPv6Address.fromString("fe80::226:2dff:fefa:cd1f"),
                                                            IPv6Address.fromString("fe80::226:2dff:fefa:ffff"));
        System.out.println(range.contains(IPv6Address.fromString("fe80::226:2dff:fefa:dcba"))); // prints true
    }

    @Test
    public void ipNetworkConstruction()
    {
        final IPv6Network range = new IPv6Network(IPv6Address.fromString("fe80::226:2dff:fefa:0"),
                                                  IPv6Address.fromString("fe80::226:2dff:fefa:ffff"));
        final IPv6Network network = IPv6Network.fromString("fe80::226:2dff:fefa:0/112");
        System.out.println(range.equals(network)); // prints true
    }

    @Test
    public void poolExample()
    {
        final IPv6AddressPool pool = new IPv6AddressPool(IPv6Address.fromString("fe80::226:2dff:fefa:0"),
                                                         IPv6Address.fromString("fe80::226:2dff:fefa:ffff"), 120);
        System.out.println(pool.isFree(IPv6Network.fromString("fe80::226:2dff:fefa:5ff/120"))); // prints true
        final IPv6AddressPool newPool = pool.allocate(IPv6Network.fromString("fe80::226:2dff:fefa:5ff/120"));
        System.out.println(newPool.isFree(IPv6Network.fromString("fe80::226:2dff:fefa:5ff/120"))); // prints false
    }

}
