package be.jvb.ipv6.examples;

import be.jvb.ipv6.*;
import org.junit.Test;

/**
 * Some examples also featured in the online documentation. This class is in a separate package on purpose, such that we make sure only
 * to call methods of the public API.
 *
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
    public void ipNetworkCalculation()
    {
        final IPv6Network strangeNetwork = IPv6Network.fromString("fe80::226:2dff:fefa:cd1f/43");

        System.out.println(strangeNetwork.getFirst()); // prints fe80::
        System.out.println(strangeNetwork.getLast()); // prints fe80:0:1f:ffff:ffff:ffff:ffff:ffff
        System.out.println(strangeNetwork.getNetmask().asPrefixLength()); // prints 43
        System.out.println(strangeNetwork.getNetmask().asAddress()); // prints ffff:ffff:ffe0::
    }

    @Test(expected = IllegalArgumentException.class)
    public void ipNetworkMaskConstruction()
    {
        final IPv6NetworkMask slash40Network = new IPv6NetworkMask(40);
        System.out.println(slash40Network.asAddress()); // prints ffff:ffff:ff00::
        System.out.println(slash40Network.asPrefixLength()); // prints 40

        final IPv6NetworkMask slash40NetworkConstructedFromAddressNotation = new IPv6NetworkMask(IPv6Address.fromString("ffff:ffff:ff00::"));
        System.out.println(slash40Network.equals(slash40NetworkConstructedFromAddressNotation)); // prints true

        final IPv6NetworkMask invalidNetworkMask = new IPv6NetworkMask(IPv6Address.fromString("0fff::")); // fails
    }

    @Test
    public void ipAddressNetworkMasking()
    {
        final IPv6Address iPv6Address = IPv6Address.fromString("fe80::226:2dff:fefa:cd1f");

        final IPv6Address masked = iPv6Address.maskWithNetworkMask(new IPv6NetworkMask(40));
        System.out.println(masked.toString()); // prints fe80::

        final IPv6Address maximum = iPv6Address.maximumAddressWithNetworkMask(new IPv6NetworkMask(40));
        System.out.println(maximum.toString()); // prints fe80:0:ff:ffff:ffff:ffff:ffff:ffff
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
