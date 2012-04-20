package com.googlecode.ipv6;

import com.googlecode.ipv6.IPv6Address;
import com.googlecode.ipv6.IPv6NetworkMask;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Jan Van Besien
 */
public class IPv6NetworkMaskTest
{
    @Test
    public void constructValidNetworkMasks()
    {
        assertEquals(new IPv6NetworkMask(new IPv6Address(0xffffffffffffffffL, 0xffffffffffffffffL)), new IPv6NetworkMask(128));
        assertEquals(new IPv6NetworkMask(new IPv6Address(0xffffffffffffffffL, 0xfffffffffffffffeL)), new IPv6NetworkMask(127));
        assertEquals(new IPv6NetworkMask(new IPv6Address(0xffffffffffffffffL, 0xfffffffffffffffcL)), new IPv6NetworkMask(126));
        assertEquals(new IPv6NetworkMask(new IPv6Address(0xffffffffffffffffL, 0x8000000000000000L)), new IPv6NetworkMask(65));
        assertEquals(new IPv6NetworkMask(new IPv6Address(0xffffffffffffffffL, 0x0L)), new IPv6NetworkMask(64));
        assertEquals(new IPv6NetworkMask(new IPv6Address(0xc000000000000000L, 0x0L)), new IPv6NetworkMask(2));
        assertEquals(new IPv6NetworkMask(new IPv6Address(0x8000000000000000L, 0x0L)), new IPv6NetworkMask(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructInvalidFromPrefixLength_Zero()
    {
        new IPv6NetworkMask(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructInvalidFromPrefixLength_TooBig()
    {
        new IPv6NetworkMask(129);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructInvalidFromAddress()
    {
        new IPv6NetworkMask(new IPv6Address(123L, 456L));
    }

}
