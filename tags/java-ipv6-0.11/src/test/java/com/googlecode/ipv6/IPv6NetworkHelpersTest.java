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

import static com.googlecode.ipv6.IPv6Address.fromString;
import static junit.framework.Assert.assertEquals;

/**
 * @author Jan Van Besien
 */
public class IPv6NetworkHelpersTest
{
    @Test
    public void longestPrefixLength()
    {
        assertEquals(128, IPv6NetworkHelpers.longestPrefixLength(fromString("::1"), fromString("::1")));
        assertEquals(127, IPv6NetworkHelpers.longestPrefixLength(fromString("::"), fromString("::1")));
        assertEquals(127, IPv6NetworkHelpers.longestPrefixLength(fromString("::1"), fromString("::")));
        assertEquals(126, IPv6NetworkHelpers.longestPrefixLength(fromString("::1"), fromString("::2")));

        assertEquals(0, IPv6NetworkHelpers.longestPrefixLength(fromString("::"), fromString("ffff::")));
        assertEquals(32, IPv6NetworkHelpers.longestPrefixLength(fromString("ffff:ffff::"), fromString("ffff:ffff:8000::")));
        assertEquals(65, IPv6NetworkHelpers.longestPrefixLength(fromString("ffff:ffff::8000:2:3:4"), fromString("ffff:ffff::C000:2:3:4")));
    }
}
