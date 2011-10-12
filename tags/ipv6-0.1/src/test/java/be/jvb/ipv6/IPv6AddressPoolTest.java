package be.jvb.ipv6;

import org.junit.Test;

import static be.jvb.ipv6.IPv6Address.fromString;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Jan Van Besien
 */
public class IPv6AddressPoolTest
{
    @Test(expected = IllegalArgumentException.class)
    public void constructUnalignedStart()
    {
        new IPv6AddressPool(fromString("2001::1"), fromString("2001::ffff:ffff"), 120);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructUnalignedEnd()
    {
        new IPv6AddressPool(fromString("2001::0"), fromString("2001::ffff:fffe"), 120);
    }

    @Test
    public void constructAligned()
    {
        // all these are correctly aligned with the given prefix length, so none should throw exception

        new IPv6AddressPool(fromString("2001::0"), fromString("2001::ffff:ffff"), 120);
        new IPv6AddressPool(fromString("2001::ab00"), fromString("2001::ffff:ffff"), 120);
        new IPv6AddressPool(fromString("2000:ffff:ffff:ffff:ffff:ffff:ffff:ff00"), fromString("2001::ffff:ffff"), 120);
        new IPv6AddressPool(fromString("2001::0"), fromString("2001::ffff:ffff"), 120);
        new IPv6AddressPool(fromString("2001::abcd:ef00"), fromString("2001::abcd:efff"), 120);
    }

    @Test
    public void autoAllocateAndDeallocateSingle128()
    {
        IPv6AddressPool pool = new IPv6AddressPool(fromString("::1"), fromString("::1"), 128);
        assertFalse(pool.isExhausted());

        pool = pool.allocate();

        assertFalse(pool.isFree(new IPv6Network(fromString("::1"), 128)));
        assertTrue(pool.isExhausted());

        assertNull("allocation in exhausted range returns null", pool.allocate());

        pool = pool.deAllocate(new IPv6Network(fromString("::1"), 128));

        assertTrue(pool.isFree(new IPv6Network(fromString("::1"), 128)));
        assertFalse(pool.isExhausted());
    }

    @Test
    public void autoAllocateMultiple128()
    {
        IPv6AddressPool pool = new IPv6AddressPool(fromString("::1"), fromString("::5"), 128);
        assertFalse(pool.isExhausted());

        for (int i = 1; i <= 5; i++)
        {
            pool = pool.allocate();
            assertFalse(pool.isFree(new IPv6Network(fromString("::" + i), 128)));
        }

        assertTrue(pool.isExhausted());
    }

    @Test
    public void autoAllocateAFew120s()
    {
        IPv6AddressPool pool = new IPv6AddressPool(fromString("2001::"), fromString("2001::ffff:ffff"), 120);
        assertFalse(pool.isExhausted());

        pool = pool.allocate();
        assertFalse(pool.isFree(new IPv6Network(fromString("2001::"), 120)));
        assertTrue(pool.isFree(new IPv6Network(fromString("2001::100"), 120)));
        assertTrue(pool.isFree(new IPv6Network(fromString("2001::200"), 120)));

        pool = pool.allocate();
        assertFalse(pool.isFree(new IPv6Network(fromString("2001::"), 120)));
        assertFalse(pool.isFree(new IPv6Network(fromString("2001::100"), 120)));
        assertTrue(pool.isFree(new IPv6Network(fromString("2001::200"), 120)));

        pool = pool.allocate();
        assertFalse(pool.isFree(new IPv6Network(fromString("2001::"), 120)));
        assertFalse(pool.isFree(new IPv6Network(fromString("2001::100"), 120)));
        assertFalse(pool.isFree(new IPv6Network(fromString("2001::200"), 120)));

        assertFalse(pool.isExhausted());
        assertTrue(pool.isFree(new IPv6Network(fromString("2001::ffff:0"), 120)));

        pool = pool.deAllocate(new IPv6Network(fromString("2001::100"), 120));
        assertFalse(pool.isFree(new IPv6Network(fromString("2001::"), 120)));
        assertTrue(pool.isFree(new IPv6Network(fromString("2001::100"), 120)));
        assertFalse(pool.isFree(new IPv6Network(fromString("2001::200"), 120)));
    }

    @Test
    public void manuallyAllocateSingle128Available()
    {
        IPv6AddressPool pool = new IPv6AddressPool(fromString("::1"), fromString("::1"), 128);
        assertFalse(pool.isExhausted());

        pool = pool.allocate(new IPv6Network(fromString("::1"), 128));

        assertFalse(pool.isFree(new IPv6Network(fromString("::1"), 128)));
        assertTrue(pool.isExhausted());

        assertNull("allocation in exhausted range returns null", pool.allocate(new IPv6Network(fromString("::1"), 128)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void manuallyAllocateSingle128OutOfRange()
    {
        final IPv6AddressPool pool = new IPv6AddressPool(fromString("::1"), fromString("::1"), 128);
        assertFalse(pool.isExhausted());

        pool.allocate(new IPv6Network(fromString("::99"), 128));
    }

    @Test
    public void manuallyAllocateMultiple128()
    {
        IPv6AddressPool pool = new IPv6AddressPool(fromString("::1"), fromString("::5"), 128);
        assertFalse(pool.isExhausted());

        for (int i = 1; i <= 5; i++)
        {
            pool = pool.allocate(new IPv6Network(fromString("::" + i), 128));
            assertFalse(pool.isFree(new IPv6Network(fromString("::" + i), 128)));
        }

        assertTrue(pool.isExhausted());
    }

    @Test
    public void manuallyAllocateAFew120s()
    {
        IPv6AddressPool pool = new IPv6AddressPool(fromString("2001::"), fromString("2001::ffff:ffff"), 120);
        assertFalse(pool.isExhausted());

        pool = pool.allocate(new IPv6Network(fromString("2001::"), 120));
        assertFalse(pool.isFree(new IPv6Network(fromString("2001::"), 120)));
        assertTrue(pool.isFree(new IPv6Network(fromString("2001::100"), 120)));
        assertTrue(pool.isFree(new IPv6Network(fromString("2001::200"), 120)));

        pool = pool.allocate(new IPv6Network(fromString("2001::200"), 120));
        assertFalse(pool.isFree(new IPv6Network(fromString("2001::"), 120)));
        assertTrue(pool.isFree(new IPv6Network(fromString("2001::100"), 120)));
        assertFalse(pool.isFree(new IPv6Network(fromString("2001::200"), 120)));

        pool = pool.allocate(new IPv6Network(fromString("2001::100"), 120));
        assertFalse(pool.isFree(new IPv6Network(fromString("2001::"), 120)));
        assertFalse(pool.isFree(new IPv6Network(fromString("2001::100"), 120)));
        assertFalse(pool.isFree(new IPv6Network(fromString("2001::200"), 120)));

        assertFalse(pool.isExhausted());
        assertTrue(pool.isFree(new IPv6Network(fromString("2001::ffff:0"), 120)));

        pool = pool.deAllocate(new IPv6Network(fromString("2001::100"), 120));
        assertFalse(pool.isFree(new IPv6Network(fromString("2001::"), 120)));
        assertTrue(pool.isFree(new IPv6Network(fromString("2001::100"), 120)));
        assertFalse(pool.isFree(new IPv6Network(fromString("2001::200"), 120)));
    }

}
