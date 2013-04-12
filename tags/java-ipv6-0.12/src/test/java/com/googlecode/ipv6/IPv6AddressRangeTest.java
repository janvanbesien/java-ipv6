/*
 * Copyright 2013 Jan Van Besien
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.googlecode.ipv6;

import junit.framework.Assert;
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
        IPv6AddressRange.fromFirstAndLast(fromString("::2"), fromString("::1"));
    }

    @Test
    public void contains()
    {
        assertTrue(IPv6AddressRange.fromFirstAndLast(fromString("::1:2:3:4"), fromString("::5:6:7:8")).contains(fromString("::1:9:8:7")));
        assertTrue(IPv6AddressRange.fromFirstAndLast(fromString("::1:2:3:4"), fromString("::5:6:7:8")).contains(fromString("::5:6:7:8")));
        assertTrue(IPv6AddressRange.fromFirstAndLast(fromString("::1:2:3:4"), fromString("::5:6:7:8")).contains(fromString("::1:2:3:4")));

        assertTrue(IPv6AddressRange.fromFirstAndLast(fromString("1:2:3:4:5:6:7:8"), fromString("9:10:11:12:13:14:15:16"))
                           .contains(fromString("1:2:3:12:11:10:9:8")));
        assertTrue(IPv6AddressRange.fromFirstAndLast(fromString("1:2:3:4:5:6:7:8"), fromString("9:10:11:12:13:14:15:16"))
                           .contains(fromString("1:2:3:4:5:6:7:8")));
        assertTrue(IPv6AddressRange.fromFirstAndLast(fromString("1:2:3:4:5:6:7:8"), fromString("9:10:11:12:13:14:15:16"))
                           .contains(fromString("9:10:11:12:13:14:15:16")));
    }

    @Test
    public void doesNotContain()
    {
        assertFalse(IPv6AddressRange.fromFirstAndLast(fromString("::1:2:3:4"), fromString("::5:6:7:8")).contains(fromString("::9:9:9:9")));
        assertFalse(IPv6AddressRange.fromFirstAndLast(fromString("::1:2:3:4"), fromString("::5:6:7:8")).contains(fromString("::1:1:1:1")));

        assertFalse(IPv6AddressRange.fromFirstAndLast(fromString("1:2:3:4:5:6:7:8"), fromString("9:10:11:12:13:14:15:16"))
                            .contains(fromString("10:10:10:10:10:10:10:10:")));
        assertFalse(IPv6AddressRange.fromFirstAndLast(fromString("1:2:3:4:5:6:7:8"), fromString("9:10:11:12:13:14:15:16"))
                            .contains(fromString("1:1:1:1:1:1:1:1")));
    }

    @Test
    public void containsRange()
    {
        assertTrue(IPv6AddressRange.fromFirstAndLast(fromString("::1:2:3:4"), fromString("::5:6:7:8"))
                           .contains(IPv6AddressRange.fromFirstAndLast(fromString("::1:2:3:4"), fromString("::5:6:7:8"))));
        assertTrue(IPv6AddressRange.fromFirstAndLast(fromString("::1:2:3:4"), fromString("::5:6:7:8"))
                           .contains(IPv6AddressRange.fromFirstAndLast(fromString("::4:4:4:4"), fromString("::5:5:5:5"))));
    }

    @Test
    public void doesNotContainRange()
    {
        assertFalse(IPv6AddressRange.fromFirstAndLast(fromString("::1:2:3:4"), fromString("::5:6:7:8"))
                            .contains(IPv6AddressRange.fromFirstAndLast(fromString("::1:2:3:3"), fromString("::5:6:7:8"))));
        assertFalse(IPv6AddressRange.fromFirstAndLast(fromString("::1:2:3:4"), fromString("::5:6:7:8"))
                            .contains(IPv6AddressRange.fromFirstAndLast(fromString("::1:2:3:4"), fromString("::5:6:7:9"))));

        assertFalse(IPv6AddressRange.fromFirstAndLast(fromString("::1:2:3:4"), fromString("::5:6:7:8"))
                            .contains(IPv6AddressRange.fromFirstAndLast(fromString("::9:9:9:9"), fromString("::9:9:9:10"))));
    }

    @Test
    public void remove()
    {
        assertEquals(2, IPv6AddressRange.fromFirstAndLast(fromString("::1:2:3:4"), fromString("::5:6:7:8")).remove(fromString("::5:5:5:5"))
                .size());
        assertEquals(1, IPv6AddressRange.fromFirstAndLast(fromString("::1:2:3:4"), fromString("::5:6:7:8")).remove(fromString("::1:2:3:4"))
                .size());
        assertEquals(1, IPv6AddressRange.fromFirstAndLast(fromString("::1:2:3:4"), fromString("::5:6:7:8")).remove(fromString("::8:8:8:8"))
                .size());
        assertEquals(0, IPv6AddressRange.fromFirstAndLast(fromString("::1:2:3:4"), fromString("::1:2:3:4")).remove(fromString("::1:2:3:4"))
                .size());
    }

    @Test
    public void iterate()
    {
        int amountOfAddresses = 0;
        for (IPv6Address address : IPv6AddressRange.fromFirstAndLast(fromString("::1:2:3:4"), fromString("::1:2:3:8")))
        {
            amountOfAddresses++;
        }

        assertEquals(5, amountOfAddresses);
    }

    @Test
    public void compareTo()
    {
        final IPv6AddressRange a =
                IPv6AddressRange.fromFirstAndLast(fromString("aaaa:ffff:ffff:ffff:1:1:1:1"), fromString("cccc:ffff:ffff:ffff:5:5:5:5"));
        final IPv6AddressRange b =
                IPv6AddressRange.fromFirstAndLast(fromString("aaaa:ffff:ffff:ffff:1:1:1:1"), fromString("bbbb:ffff:ffff:ffff:5:5:5:5"));
        final IPv6AddressRange c =
                IPv6AddressRange.fromFirstAndLast(fromString("bbbb:ffff:ffff:ffff:1:1:1:1"), fromString("cccc:ffff:ffff:ffff:5:5:5:5"));
        final IPv6AddressRange d =
                IPv6AddressRange.fromFirstAndLast(fromString("bbbb:ffff:ffff:ffff:1:1:1:1"), fromString("bbbb:ffff:ffff:ffff:5:5:5:5"));

        Assert.assertTrue(a.compareTo(b) > 0);
        Assert.assertTrue(a.compareTo(c) < 0);
        Assert.assertTrue(a.compareTo(d) < 0);
        Assert.assertTrue(b.compareTo(c) < 0);
        Assert.assertTrue(b.compareTo(d) < 0);
        Assert.assertTrue(c.compareTo(d) > 0);

        Assert.assertTrue(a.compareTo(a) == 0);
        Assert.assertTrue(b.compareTo(b) == 0);
        Assert.assertTrue(c.compareTo(c) == 0);
        Assert.assertTrue(d.compareTo(d) == 0);
    }
}
