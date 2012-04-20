package com.googlecode.ipv6;

import com.googlecode.ipv6.IPv6Address;
import com.googlecode.ipv6.IPv6AddressRange;
import org.junit.Test;

import static com.googlecode.ipv6.IPv6Address.fromString;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Jan Van Besien
 */
public class IPv6AddressRangeTest
{
    @Test(expected = IllegalArgumentException.class)
    public void constructInvalid()
    {
        new IPv6AddressRange(fromString("::2"), fromString("::1"));
    }

    @Test
    public void contains()
    {
        assertTrue(new IPv6AddressRange(fromString("::1:2:3:4"), fromString("::5:6:7:8")).contains(fromString("::1:9:8:7")));
        assertTrue(new IPv6AddressRange(fromString("::1:2:3:4"), fromString("::5:6:7:8")).contains(fromString("::5:6:7:8")));
        assertTrue(new IPv6AddressRange(fromString("::1:2:3:4"), fromString("::5:6:7:8")).contains(fromString("::1:2:3:4")));

        assertTrue(new IPv6AddressRange(fromString("1:2:3:4:5:6:7:8"), fromString("9:10:11:12:13:14:15:16"))
                           .contains(fromString("1:2:3:12:11:10:9:8")));
        assertTrue(new IPv6AddressRange(fromString("1:2:3:4:5:6:7:8"), fromString("9:10:11:12:13:14:15:16"))
                           .contains(fromString("1:2:3:4:5:6:7:8")));
        assertTrue(new IPv6AddressRange(fromString("1:2:3:4:5:6:7:8"), fromString("9:10:11:12:13:14:15:16"))
                           .contains(fromString("9:10:11:12:13:14:15:16")));
    }

    @Test
    public void doesNotContain()
    {
        assertFalse(new IPv6AddressRange(fromString("::1:2:3:4"), fromString("::5:6:7:8")).contains(fromString("::9:9:9:9")));
        assertFalse(new IPv6AddressRange(fromString("::1:2:3:4"), fromString("::5:6:7:8")).contains(fromString("::1:1:1:1")));

        assertFalse(new IPv6AddressRange(fromString("1:2:3:4:5:6:7:8"), fromString("9:10:11:12:13:14:15:16"))
                            .contains(fromString("10:10:10:10:10:10:10:10:")));
        assertFalse(new IPv6AddressRange(fromString("1:2:3:4:5:6:7:8"), fromString("9:10:11:12:13:14:15:16"))
                            .contains(fromString("1:1:1:1:1:1:1:1")));
    }

    @Test
    public void containsRange()
    {
        assertTrue(new IPv6AddressRange(fromString("::1:2:3:4"), fromString("::5:6:7:8"))
                           .contains(new IPv6AddressRange(fromString("::1:2:3:4"), fromString("::5:6:7:8"))));
        assertTrue(new IPv6AddressRange(fromString("::1:2:3:4"), fromString("::5:6:7:8"))
                           .contains(new IPv6AddressRange(fromString("::4:4:4:4"), fromString("::5:5:5:5"))));
    }

    @Test
    public void doesNotContainRange()
    {
        assertFalse(new IPv6AddressRange(fromString("::1:2:3:4"), fromString("::5:6:7:8"))
                            .contains(new IPv6AddressRange(fromString("::1:2:3:3"), fromString("::5:6:7:8"))));
        assertFalse(new IPv6AddressRange(fromString("::1:2:3:4"), fromString("::5:6:7:8"))
                            .contains(new IPv6AddressRange(fromString("::1:2:3:4"), fromString("::5:6:7:9"))));

        assertFalse(new IPv6AddressRange(fromString("::1:2:3:4"), fromString("::5:6:7:8"))
                            .contains(new IPv6AddressRange(fromString("::9:9:9:9"), fromString("::9:9:9:10"))));
    }

    @Test
    public void remove()
    {
        assertEquals(2, new IPv6AddressRange(fromString("::1:2:3:4"), fromString("::5:6:7:8")).remove(fromString("::5:5:5:5")).size());
        assertEquals(1, new IPv6AddressRange(fromString("::1:2:3:4"), fromString("::5:6:7:8")).remove(fromString("::1:2:3:4")).size());
        assertEquals(1, new IPv6AddressRange(fromString("::1:2:3:4"), fromString("::5:6:7:8")).remove(fromString("::8:8:8:8")).size());
        assertEquals(0, new IPv6AddressRange(fromString("::1:2:3:4"), fromString("::1:2:3:4")).remove(fromString("::1:2:3:4")).size());
    }

    @Test
    public void iterate()
    {
        int amountOfAddresses = 0;
        for (IPv6Address address : new IPv6AddressRange(fromString("::1:2:3:4"), fromString("::1:2:3:8")))
        {
            amountOfAddresses++;
        }

        assertEquals(5, amountOfAddresses);
    }

}
