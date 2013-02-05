package com.googlecode.ipv6;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static com.googlecode.ipv6.IPv6Address.fromString;
import static org.junit.Assert.*;

/**
 * @author Jan Van Besien
 */
public class IPv6AddressPoolTest
{
    @Test(expected = IllegalArgumentException.class)
    public void constructUnalignedStart()
    {
        IPv6AddressPool.fromRangeAndSubnet(IPv6AddressRange.fromFirstAndLast(fromString("2001::1"), fromString("2001::ffff:ffff")),
                                           new IPv6NetworkMask(120));
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructUnalignedEnd()
    {
        IPv6AddressPool.fromRangeAndSubnet(IPv6AddressRange.fromFirstAndLast(fromString("2001::0"), fromString("2001::ffff:fffe")),
                                           new IPv6NetworkMask(120));
    }

    @Test
    public void constructAligned()
    {
        // all these are correctly aligned with the given prefix length, so none should throw exception

        IPv6AddressPool.fromRangeAndSubnet(IPv6AddressRange.fromFirstAndLast(fromString("2001::0"), fromString("2001::ffff:ffff")),
                                           new IPv6NetworkMask(120));
        IPv6AddressPool.fromRangeAndSubnet(IPv6AddressRange.fromFirstAndLast(fromString("2001::ab00"), fromString("2001::ffff:ffff")),
                                           new IPv6NetworkMask(120));
        IPv6AddressPool.fromRangeAndSubnet(IPv6AddressRange.fromFirstAndLast(fromString("2000:ffff:ffff:ffff:ffff:ffff:ffff:ff00"),
                                                                             fromString("2001::ffff:ffff")), new IPv6NetworkMask(120));
        IPv6AddressPool.fromRangeAndSubnet(IPv6AddressRange.fromFirstAndLast(fromString("2001::0"), fromString("2001::ffff:ffff")),
                                           new IPv6NetworkMask(120));
        IPv6AddressPool.fromRangeAndSubnet(IPv6AddressRange.fromFirstAndLast(fromString("2001::abcd:ef00"),
                                                                             fromString("2001::abcd:efff")), new IPv6NetworkMask(120));
    }

    @Test
    public void autoAllocateAndDeallocateSingle128()
    {
        IPv6AddressPool pool = IPv6AddressPool.fromRangeAndSubnet(IPv6AddressRange.fromFirstAndLast(fromString("::1"),
                                                                                                    fromString("::1")),
                                                                  new IPv6NetworkMask(128));
        assertFalse(pool.isExhausted());

        pool = pool.allocate();

        assertFalse(pool.isFree(IPv6Network.fromAddressAndMask(fromString("::1"), IPv6NetworkMask.fromPrefixLength(128))));
        assertTrue(pool.isExhausted());

        assertNull("allocation in exhausted range returns null", pool.allocate());

        pool = pool.deAllocate(IPv6Network.fromAddressAndMask(fromString("::1"), IPv6NetworkMask.fromPrefixLength(128)));

        assertTrue(pool.isFree(IPv6Network.fromAddressAndMask(fromString("::1"), IPv6NetworkMask.fromPrefixLength(128))));
        assertFalse(pool.isExhausted());
    }

    @Test
    public void autoAllocateMultiple128()
    {
        IPv6AddressPool pool = IPv6AddressPool.fromRangeAndSubnet(IPv6AddressRange.fromFirstAndLast(fromString("::1"),
                                                                                                    fromString("::5")),
                                                                  new IPv6NetworkMask(128));
        assertFalse(pool.isExhausted());

        for (int i = 1; i <= 5; i++)
        {
            pool = pool.allocate();
            assertFalse(pool.isFree(IPv6Network.fromAddressAndMask(fromString("::" + i), IPv6NetworkMask.fromPrefixLength(128))));
        }

        assertTrue(pool.isExhausted());
    }

    @Test
    public void autoAllocateAFew120s()
    {
        IPv6AddressPool pool = IPv6AddressPool.fromRangeAndSubnet(IPv6AddressRange.fromFirstAndLast(fromString("2001::"),
                                                                                                    fromString("2001::ffff:ffff")),
                                                                  new IPv6NetworkMask(120));
        assertFalse(pool.isExhausted());

        pool = pool.allocate();
        assertEquals(IPv6Network.fromAddressAndMask(fromString("2001::"), IPv6NetworkMask.fromPrefixLength(120)), pool.getLastAllocated());
        assertFalse(pool.isFree(IPv6Network.fromAddressAndMask(fromString("2001::"), IPv6NetworkMask.fromPrefixLength(120))));
        assertTrue(pool.isFree(IPv6Network.fromAddressAndMask(fromString("2001::100"), IPv6NetworkMask.fromPrefixLength(120))));
        assertTrue(pool.isFree(IPv6Network.fromAddressAndMask(fromString("2001::200"), IPv6NetworkMask.fromPrefixLength(120))));

        pool = pool.allocate();
        assertEquals(IPv6Network.fromAddressAndMask(fromString("2001::100"), IPv6NetworkMask.fromPrefixLength(120)),
                     pool.getLastAllocated());
        assertFalse(pool.isFree(IPv6Network.fromAddressAndMask(fromString("2001::"), IPv6NetworkMask.fromPrefixLength(120))));
        assertFalse(pool.isFree(IPv6Network.fromAddressAndMask(fromString("2001::100"), IPv6NetworkMask.fromPrefixLength(120))));
        assertTrue(pool.isFree(IPv6Network.fromAddressAndMask(fromString("2001::200"), IPv6NetworkMask.fromPrefixLength(120))));

        pool = pool.allocate();
        assertEquals(IPv6Network.fromAddressAndMask(fromString("2001::200"), IPv6NetworkMask.fromPrefixLength(120)),
                     pool.getLastAllocated());
        assertFalse(pool.isFree(IPv6Network.fromAddressAndMask(fromString("2001::"), IPv6NetworkMask.fromPrefixLength(120))));
        assertFalse(pool.isFree(IPv6Network.fromAddressAndMask(fromString("2001::100"), IPv6NetworkMask.fromPrefixLength(120))));
        assertFalse(pool.isFree(IPv6Network.fromAddressAndMask(fromString("2001::200"), IPv6NetworkMask.fromPrefixLength(120))));

        assertFalse(pool.isExhausted());
        assertTrue(pool.isFree(IPv6Network.fromAddressAndMask(fromString("2001::ffff:0"), IPv6NetworkMask.fromPrefixLength(120))));

        pool = pool.deAllocate(IPv6Network.fromAddressAndMask(fromString("2001::100"), IPv6NetworkMask.fromPrefixLength(120)));
        assertFalse(pool.isFree(IPv6Network.fromAddressAndMask(fromString("2001::"), IPv6NetworkMask.fromPrefixLength(120))));
        assertTrue(pool.isFree(IPv6Network.fromAddressAndMask(fromString("2001::100"), IPv6NetworkMask.fromPrefixLength(120))));
        assertFalse(pool.isFree(IPv6Network.fromAddressAndMask(fromString("2001::200"), IPv6NetworkMask.fromPrefixLength(120))));
    }

    @Test
    public void manuallyAllocateSingle128Available()
    {
        IPv6AddressPool pool = IPv6AddressPool.fromRangeAndSubnet(IPv6AddressRange.fromFirstAndLast(fromString("::1"),
                                                                                                    fromString("::1")),
                                                                  new IPv6NetworkMask(128));
        assertFalse(pool.isExhausted());

        pool = pool.allocate(IPv6Network.fromAddressAndMask(fromString("::1"), IPv6NetworkMask.fromPrefixLength(128)));

        assertFalse(pool.isFree(IPv6Network.fromAddressAndMask(fromString("::1"), IPv6NetworkMask.fromPrefixLength(128))));
        assertTrue(pool.isExhausted());

        assertNull("allocation in exhausted range returns null",
                   pool.allocate(IPv6Network.fromAddressAndMask(fromString("::1"), IPv6NetworkMask.fromPrefixLength(128))));
    }

    @Test(expected = IllegalArgumentException.class)
    public void manuallyAllocateSingle128OutOfRange()
    {
        final IPv6AddressPool pool = IPv6AddressPool.fromRangeAndSubnet(IPv6AddressRange.fromFirstAndLast(fromString("::1"),
                                                                                                          fromString("::1")),
                                                                        new IPv6NetworkMask(128));
        assertFalse(pool.isExhausted());

        pool.allocate(IPv6Network.fromAddressAndMask(fromString("::99"), IPv6NetworkMask.fromPrefixLength(128)));
    }

    @Test
    public void manuallyAllocateMultiple128()
    {
        IPv6AddressPool pool = IPv6AddressPool.fromRangeAndSubnet(IPv6AddressRange.fromFirstAndLast(fromString("::1"),
                                                                                                    fromString("::5")),
                                                                  new IPv6NetworkMask(128));
        assertFalse(pool.isExhausted());

        for (int i = 1; i <= 5; i++)
        {
            pool = pool.allocate(IPv6Network.fromAddressAndMask(fromString("::" + i), IPv6NetworkMask.fromPrefixLength(128)));
            assertFalse(pool.isFree(IPv6Network.fromAddressAndMask(fromString("::" + i), IPv6NetworkMask.fromPrefixLength(128))));
        }

        assertTrue(pool.isExhausted());
    }

    @Test
    public void manuallyAllocateAFew120s()
    {
        IPv6AddressPool pool = IPv6AddressPool.fromRangeAndSubnet(IPv6AddressRange.fromFirstAndLast(fromString("2001::"),
                                                                                                    fromString("2001::ffff:ffff")),
                                                                  new IPv6NetworkMask(120));
        assertFalse(pool.isExhausted());

        pool = pool.allocate(IPv6Network.fromAddressAndMask(fromString("2001::"), IPv6NetworkMask.fromPrefixLength(120)));
        assertEquals(IPv6Network.fromAddressAndMask(fromString("2001::"), IPv6NetworkMask.fromPrefixLength(120)), pool.getLastAllocated());
        assertFalse(pool.isFree(IPv6Network.fromAddressAndMask(fromString("2001::"), IPv6NetworkMask.fromPrefixLength(120))));
        assertTrue(pool.isFree(IPv6Network.fromAddressAndMask(fromString("2001::100"), IPv6NetworkMask.fromPrefixLength(120))));
        assertTrue(pool.isFree(IPv6Network.fromAddressAndMask(fromString("2001::200"), IPv6NetworkMask.fromPrefixLength(120))));

        pool = pool.allocate(IPv6Network.fromAddressAndMask(fromString("2001::200"), IPv6NetworkMask.fromPrefixLength(120)));
        assertEquals(IPv6Network.fromAddressAndMask(fromString("2001::200"), IPv6NetworkMask.fromPrefixLength(120)),
                     pool.getLastAllocated());
        assertFalse(pool.isFree(IPv6Network.fromAddressAndMask(fromString("2001::"), IPv6NetworkMask.fromPrefixLength(120))));
        assertTrue(pool.isFree(IPv6Network.fromAddressAndMask(fromString("2001::100"), IPv6NetworkMask.fromPrefixLength(120))));
        assertFalse(pool.isFree(IPv6Network.fromAddressAndMask(fromString("2001::200"), IPv6NetworkMask.fromPrefixLength(120))));

        pool = pool.allocate(IPv6Network.fromAddressAndMask(fromString("2001::100"), IPv6NetworkMask.fromPrefixLength(120)));
        assertEquals(IPv6Network.fromAddressAndMask(fromString("2001::100"), IPv6NetworkMask.fromPrefixLength(120)),
                     pool.getLastAllocated());
        assertFalse(pool.isFree(IPv6Network.fromAddressAndMask(fromString("2001::"), IPv6NetworkMask.fromPrefixLength(120))));
        assertFalse(pool.isFree(IPv6Network.fromAddressAndMask(fromString("2001::100"), IPv6NetworkMask.fromPrefixLength(120))));
        assertFalse(pool.isFree(IPv6Network.fromAddressAndMask(fromString("2001::200"), IPv6NetworkMask.fromPrefixLength(120))));

        assertFalse(pool.isExhausted());
        assertTrue(pool.isFree(IPv6Network.fromAddressAndMask(fromString("2001::ffff:0"), IPv6NetworkMask.fromPrefixLength(120))));

        pool = pool.deAllocate(IPv6Network.fromAddressAndMask(fromString("2001::100"), IPv6NetworkMask.fromPrefixLength(120)));
        assertFalse(pool.isFree(IPv6Network.fromAddressAndMask(fromString("2001::"), IPv6NetworkMask.fromPrefixLength(120))));
        assertTrue(pool.isFree(IPv6Network.fromAddressAndMask(fromString("2001::100"), IPv6NetworkMask.fromPrefixLength(120))));
        assertFalse(pool.isFree(IPv6Network.fromAddressAndMask(fromString("2001::200"), IPv6NetworkMask.fromPrefixLength(120))));
    }

    @Test
    public void allocateOnBoundariesLowBits()
    {
        for (int i = 64; i > 0; i--)
        {
            IPv6AddressPool pool = IPv6AddressPool.fromRangeAndSubnet(IPv6AddressRange.fromFirstAndLast(fromString("::"),
                                                                                                        fromString(
                                                                                                                "ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff")),
                                                                      new IPv6NetworkMask(i));
            pool = pool.allocate();
            assertEquals(IPv6Network.fromAddressAndMask(fromString("::"), IPv6NetworkMask.fromPrefixLength(i)), pool.getLastAllocated());
            pool = pool.allocate();
            assertEquals(IPv6Network.fromAddressAndMask(fromString("::").maximumAddressWithNetworkMask(new IPv6NetworkMask(i)).add(1),
                                                        IPv6NetworkMask.fromPrefixLength(i)), pool.getLastAllocated());
        }
    }

    @Test
    public void allocateOnBoundariesHighBits()
    {
        for (int i = 128; i > 64; i--)
        {
            IPv6AddressPool pool = IPv6AddressPool.fromRangeAndSubnet(IPv6AddressRange.fromFirstAndLast(fromString("::"),
                                                                                                        fromString(
                                                                                                                "::ffff:ffff:ffff:ffff")),
                                                                      new IPv6NetworkMask(i));
            pool = pool.allocate();
            assertEquals(IPv6Network.fromAddressAndMask(fromString("::"), IPv6NetworkMask.fromPrefixLength(i)), pool.getLastAllocated());
            pool = pool.allocate();
            assertEquals(IPv6Network.fromAddressAndMask(fromString("::").maximumAddressWithNetworkMask(new IPv6NetworkMask(i)).add(1),
                                                        IPv6NetworkMask.fromPrefixLength(i)), pool.getLastAllocated());
        }
    }

    @Test
    public void iterateFreeNetworks()
    {
        final IPv6AddressPool pool = IPv6AddressPool.fromRangeAndSubnet(IPv6AddressRange.fromFirstAndLast(fromString("::"),
                                                                                                          fromString(
                                                                                                                  "::ffff:ffff:ffff:ffff")),
                                                                        new IPv6NetworkMask(66));
        final Set<IPv6Network> freeNetworks = new HashSet<IPv6Network>();
        for (IPv6Network network : pool.freeNetworks())
        {
            freeNetworks.add(network);
        }

        assertEquals(4, freeNetworks.size());
        assertTrue(freeNetworks.contains(IPv6Network.fromString("::/66")));
        assertTrue(freeNetworks.contains(IPv6Network.fromString("::4000:0:0:0/66")));
        assertTrue(freeNetworks.contains(IPv6Network.fromString("::8000:0:0:0/66")));
        assertTrue(freeNetworks.contains(IPv6Network.fromString("::c000:0:0:0/66")));
    }

}
