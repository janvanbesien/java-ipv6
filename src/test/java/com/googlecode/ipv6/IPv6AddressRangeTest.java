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

import java.math.BigInteger;

import static com.googlecode.ipv6.IPv6Address.fromString;
import static com.googlecode.ipv6.IPv6AddressRange.fromFirstAndLast;
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
        fromFirstAndLast(fromString("::2"), fromString("::1"));
    }

    @Test
    public void contains()
    {
        assertTrue(fromFirstAndLast(fromString("::1:2:3:4"), fromString("::5:6:7:8")).contains(fromString("::1:9:8:7")));
        assertTrue(fromFirstAndLast(fromString("::1:2:3:4"), fromString("::5:6:7:8")).contains(fromString("::5:6:7:8")));
        assertTrue(fromFirstAndLast(fromString("::1:2:3:4"), fromString("::5:6:7:8")).contains(fromString("::1:2:3:4")));

        assertTrue(fromFirstAndLast(fromString("1:2:3:4:5:6:7:8"), fromString("9:10:11:12:13:14:15:16"))
                           .contains(fromString("1:2:3:12:11:10:9:8")));
        assertTrue(fromFirstAndLast(fromString("1:2:3:4:5:6:7:8"), fromString("9:10:11:12:13:14:15:16"))
                           .contains(fromString("1:2:3:4:5:6:7:8")));
        assertTrue(fromFirstAndLast(fromString("1:2:3:4:5:6:7:8"), fromString("9:10:11:12:13:14:15:16"))
                           .contains(fromString("9:10:11:12:13:14:15:16")));
    }

    @Test
    public void doesNotContain()
    {
        assertFalse(fromFirstAndLast(fromString("::1:2:3:4"), fromString("::5:6:7:8")).contains(fromString("::9:9:9:9")));
        assertFalse(fromFirstAndLast(fromString("::1:2:3:4"), fromString("::5:6:7:8")).contains(fromString("::1:1:1:1")));

        assertFalse(fromFirstAndLast(fromString("1:2:3:4:5:6:7:8"), fromString("9:10:11:12:13:14:15:16"))
                            .contains(fromString("10:10:10:10:10:10:10:10:")));
        assertFalse(fromFirstAndLast(fromString("1:2:3:4:5:6:7:8"), fromString("9:10:11:12:13:14:15:16"))
                            .contains(fromString("1:1:1:1:1:1:1:1")));
    }

    @Test
    public void containsRange()
    {
        assertTrue(fromFirstAndLast(fromString("::1:2:3:4"), fromString("::5:6:7:8"))
                           .contains(fromFirstAndLast(fromString("::1:2:3:4"), fromString("::5:6:7:8"))));
        assertTrue(fromFirstAndLast(fromString("::1:2:3:4"), fromString("::5:6:7:8"))
                           .contains(fromFirstAndLast(fromString("::4:4:4:4"), fromString("::5:5:5:5"))));
    }

    @Test
    public void doesNotContainRange()
    {
        assertFalse(fromFirstAndLast(fromString("::1:2:3:4"), fromString("::5:6:7:8"))
                            .contains(fromFirstAndLast(fromString("::1:2:3:3"), fromString("::5:6:7:8"))));
        assertFalse(fromFirstAndLast(fromString("::1:2:3:4"), fromString("::5:6:7:8"))
                            .contains(fromFirstAndLast(fromString("::1:2:3:4"), fromString("::5:6:7:9"))));

        assertFalse(fromFirstAndLast(fromString("::1:2:3:4"), fromString("::5:6:7:8"))
                            .contains(fromFirstAndLast(fromString("::9:9:9:9"), fromString("::9:9:9:10"))));
    }

    @Test
    public void remove()
    {
        assertEquals(2, fromFirstAndLast(fromString("::1:2:3:4"), fromString("::5:6:7:8")).remove(fromString("::5:5:5:5"))
                .size());
        assertEquals(1, fromFirstAndLast(fromString("::1:2:3:4"), fromString("::5:6:7:8")).remove(fromString("::1:2:3:4"))
                .size());
        assertEquals(1, fromFirstAndLast(fromString("::1:2:3:4"), fromString("::5:6:7:8")).remove(fromString("::8:8:8:8"))
                .size());
        assertEquals(0, fromFirstAndLast(fromString("::1:2:3:4"), fromString("::1:2:3:4")).remove(fromString("::1:2:3:4"))
                .size());
    }

    @Test
    public void iterate()
    {
        int amountOfAddresses = 0;
        for (IPv6Address address : fromFirstAndLast(fromString("::1:2:3:4"), fromString("::1:2:3:8")))
        {
            amountOfAddresses++;
        }

        assertEquals(5, amountOfAddresses);
    }

    @Test
    public void compareTo()
    {
        final IPv6AddressRange a =
                fromFirstAndLast(fromString("aaaa:ffff:ffff:ffff:1:1:1:1"), fromString("cccc:ffff:ffff:ffff:5:5:5:5"));
        final IPv6AddressRange b =
                fromFirstAndLast(fromString("aaaa:ffff:ffff:ffff:1:1:1:1"), fromString("bbbb:ffff:ffff:ffff:5:5:5:5"));
        final IPv6AddressRange c =
                fromFirstAndLast(fromString("bbbb:ffff:ffff:ffff:1:1:1:1"), fromString("cccc:ffff:ffff:ffff:5:5:5:5"));
        final IPv6AddressRange d =
                fromFirstAndLast(fromString("bbbb:ffff:ffff:ffff:1:1:1:1"), fromString("bbbb:ffff:ffff:ffff:5:5:5:5"));

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

    @Test
    public void size()
    {
        assertEquals(BigInteger.valueOf(11), fromFirstAndLast(fromString("::"), fromString("::a")).size());
        assertEquals(BigInteger.valueOf(131074), fromFirstAndLast(fromString("::1:2:3:4"), fromString("::1:2:5:5")).size());
        assertEquals(BigInteger.valueOf(2).pow(128),
                     fromFirstAndLast(fromString("::"), fromString("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff")).size());
    }

}
