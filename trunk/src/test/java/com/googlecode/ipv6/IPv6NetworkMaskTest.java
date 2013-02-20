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
        assertEquals(IPv6NetworkMask.fromAddress(new IPv6Address(0xffffffffffffffffL, 0xffffffffffffffffL)), new IPv6NetworkMask(128));
        assertEquals(IPv6NetworkMask.fromAddress(new IPv6Address(0xffffffffffffffffL, 0xfffffffffffffffeL)), new IPv6NetworkMask(127));
        assertEquals(IPv6NetworkMask.fromAddress(new IPv6Address(0xffffffffffffffffL, 0xfffffffffffffffcL)), new IPv6NetworkMask(126));
        assertEquals(IPv6NetworkMask.fromAddress(new IPv6Address(0xffffffffffffffffL, 0x8000000000000000L)), new IPv6NetworkMask(65));
        assertEquals(IPv6NetworkMask.fromAddress(new IPv6Address(0xffffffffffffffffL, 0x0L)), new IPv6NetworkMask(64));
        assertEquals(IPv6NetworkMask.fromAddress(new IPv6Address(0xc000000000000000L, 0x0L)), new IPv6NetworkMask(2));
        assertEquals(IPv6NetworkMask.fromAddress(new IPv6Address(0x8000000000000000L, 0x0L)), new IPv6NetworkMask(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructInvalidFromPrefixLength_Negative()
    {
        new IPv6NetworkMask(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructInvalidFromPrefixLength_TooBig()
    {
        new IPv6NetworkMask(129);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructInvalidFromAddress()
    {
        IPv6NetworkMask.fromAddress(new IPv6Address(123L, 456L));
    }

}
