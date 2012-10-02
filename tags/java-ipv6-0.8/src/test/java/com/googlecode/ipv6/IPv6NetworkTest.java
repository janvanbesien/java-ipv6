package com.googlecode.ipv6;

import org.junit.Test;

import java.util.Random;

import static com.googlecode.ipv6.IPv6Address.fromString;
import static org.junit.Assert.assertEquals;

/**
 * @author Jan Van Besien
 */
public class IPv6NetworkTest
{
    @Test
    public void constructFromTwoAddresses()
    {
        assertEquals(IPv6Network.fromAddressAndMask(fromString("::"), IPv6NetworkMask.fromPrefixLength(126)),
                     IPv6Network.fromTwoAddresses(fromString("::1"), fromString("::2")));
        assertEquals(IPv6Network.fromAddressAndMask(fromString("a:b::"), IPv6NetworkMask.fromPrefixLength(44)),
                     IPv6Network.fromTwoAddresses(fromString("a:b:c::1:1"), fromString("a:b::f:f")));
    }

    @Test
    public void stringRepresentation()
    {
        assertEquals("::/126", IPv6Network.fromAddressAndMask(fromString("::"), IPv6NetworkMask.fromPrefixLength(126)).toString());
        assertEquals("a:b:c:d::/64", IPv6Network.fromAddressAndMask(fromString("a:b:c:d::"), IPv6NetworkMask.fromPrefixLength(64))
                .toString());
    }

    @Test
    public void toStringCanBeUsedInFromStringAndViceVersa()
    {
        final int nTests = 10000;
        final Random rg = new Random();

        for (int i = 0; i < nTests; i++)
        {
            final IPv6Network network = IPv6Network.fromAddressAndMask(new IPv6Address(rg.nextLong(), rg.nextLong()),
                                                                       IPv6NetworkMask.fromPrefixLength(rg.nextInt(128) + 1));
            assertEquals(network, IPv6Network.fromString(network.toString()));
        }
    }

    @Test
    public void constructAndVerifyPrefixLength()
    {
        assertEquals(1, IPv6Network.fromString("a:b:c::/1").getNetmask().asPrefixLength());
        assertEquals(63, IPv6Network.fromString("a:b:c::/63").getNetmask().asPrefixLength());
        assertEquals(64, IPv6Network.fromString("a:b:c::/64").getNetmask().asPrefixLength());
        assertEquals(65, IPv6Network.fromString("a:b:c::/65").getNetmask().asPrefixLength());
        assertEquals(127, IPv6Network.fromString("a:b:c::/127").getNetmask().asPrefixLength());
        assertEquals(128, IPv6Network.fromString("a:b:c::/128").getNetmask().asPrefixLength());
    }

    @Test
    public void constructAndVerifyNetmask()
    {
        assertEquals(IPv6NetworkMask.fromAddress(new IPv6Address(0x8000000000000000L, 0x0L)),
                     IPv6Network.fromString("a:b:c::/1").getNetmask());

        assertEquals(IPv6NetworkMask.fromAddress(new IPv6Address(0xfffffffffffffffeL, 0x0L)),
                     IPv6Network.fromString("a:b:c::/63").getNetmask());

        assertEquals(IPv6NetworkMask.fromAddress(new IPv6Address(0xffffffffffffffffL, 0x0L)),
                     IPv6Network.fromString("a:b:c::/64").getNetmask());

        assertEquals(IPv6NetworkMask.fromAddress(new IPv6Address(0xffffffffffffffffL, 0x8000000000000000L)),
                     IPv6Network.fromString("a:b:c::/65").getNetmask());

        assertEquals(IPv6NetworkMask.fromAddress(new IPv6Address(0xffffffffffffffffL, 0xfffffffffffffffeL)),
                     IPv6Network.fromString("a:b:c::/127").getNetmask());

        assertEquals(IPv6NetworkMask.fromAddress(new IPv6Address(0xffffffffffffffffL, 0xffffffffffffffffL)),
                     IPv6Network.fromString("a:b:c::/128").getNetmask());
    }

}
