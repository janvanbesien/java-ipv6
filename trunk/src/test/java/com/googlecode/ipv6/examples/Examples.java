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

package com.googlecode.ipv6.examples;

import com.googlecode.ipv6.*;
import org.junit.Test;

import java.util.Iterator;

/**
 * Some examples also featured in the online documentation. This class is in a separate package on purpose, such that we make sure only to
 * call methods of the public API.
 *
 * @author Jan Van Besien
 */
public class Examples
{
    @Test
    public void ipAddressConstruction()
    {
        final IPv6Address iPv6Address = IPv6Address.fromString("fe80::226:2dff:fefa:cd1f");
        final IPv6Address iPv4MappedIPv6Address = IPv6Address.fromString("::ffff:192.168.0.1");
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
        final IPv6AddressRange range = IPv6AddressRange.fromFirstAndLast(IPv6Address.fromString("fe80::226:2dff:fefa:cd1f"),
                                                                         IPv6Address.fromString("fe80::226:2dff:fefa:ffff"));
        System.out.println(range.contains(IPv6Address.fromString("fe80::226:2dff:fefa:dcba"))); // prints true
    }

    @Test
    public void ipAddressRangeConvertToSubnets()
    {
        final IPv6AddressRange range = IPv6AddressRange.fromFirstAndLast(IPv6Address.fromString("::1:ffcc"),
                                                                         IPv6Address.fromString("::2:0"));

        Iterator<IPv6Network> subnetsIterator = range.toSubnets();
        while (subnetsIterator.hasNext())
            System.out.println(subnetsIterator.next());

        // prints ::1:ffcc/126 ::1:ffd0/124 ::1:ffe0/123 ::2:0/128 (i.e. the minimal set of networks that define the original range)
    }

    @Test
    public void ipNetworkConstruction()
    {
        final IPv6AddressRange range = IPv6AddressRange.fromFirstAndLast(IPv6Address.fromString("fe80::226:2dff:fefa:0"),
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

    @Test
    public void ipNetworkSplitInSmallerSubnets()
    {
        final IPv6Network network = IPv6Network.fromString("1:2:3:4:5:6:7:0/120");

        Iterator<IPv6Network> splits = network.split(IPv6NetworkMask.fromPrefixLength(124));
        while (splits.hasNext())
            System.out.println(splits.next());

        // prints 1:2:3:4:5:6:7:0/124, 1:2:3:4:5:6:7:10/124, 1:2:3:4:5:6:7:20/124, ... until 1:2:3:4:5:6:7:f0/124 (16 in total)
    }

    @Test
    public void ipNetworkNotationChoices()
    {
        IPv6Network prefixLengthNotation = IPv6Network.fromString("::1/16");
        IPv6Network addressNotation =
                IPv6Network.fromAddressAndMask(IPv6Address.fromString("::"), IPv6NetworkMask.fromAddress(IPv6Address.fromString("ffff::")));
        System.out.println(prefixLengthNotation.equals(addressNotation)); // prints true
        System.out.println(prefixLengthNotation); // prints ::/16
        System.out.println(prefixLengthNotation.getFirst() + "/" + prefixLengthNotation.getNetmask().asAddress()); // prints ::/ffff::
    }

    @Test(expected = IllegalArgumentException.class)
    public void ipNetworkMaskConstruction()
    {
        final IPv6NetworkMask slash40Network = IPv6NetworkMask.fromPrefixLength(40);
        System.out.println(slash40Network.asAddress()); // prints ffff:ffff:ff00::
        System.out.println(slash40Network.asPrefixLength()); // prints 40

        final IPv6NetworkMask slash40NetworkConstructedFromAddressNotation = IPv6NetworkMask.fromAddress(
                IPv6Address.fromString("ffff:ffff:ff00::"));
        System.out.println(slash40Network.equals(slash40NetworkConstructedFromAddressNotation)); // prints true

        final IPv6NetworkMask invalidNetworkMask = IPv6NetworkMask.fromAddress(IPv6Address.fromString("0fff::")); // fails
    }

    @Test
    public void ipAddressNetworkMasking()
    {
        final IPv6Address iPv6Address = IPv6Address.fromString("fe80::226:2dff:fefa:cd1f");

        final IPv6Address masked = iPv6Address.maskWithNetworkMask(IPv6NetworkMask.fromPrefixLength(40));
        System.out.println(masked.toString()); // prints fe80::

        final IPv6Address maximum = iPv6Address.maximumAddressWithNetworkMask(IPv6NetworkMask.fromPrefixLength(40));
        System.out.println(maximum.toString()); // prints fe80:0:ff:ffff:ffff:ffff:ffff:ffff
    }

    @Test
    public void poolExample()
    {
        final IPv6AddressPool pool = IPv6AddressPool.fromRangeAndSubnet(
                IPv6AddressRange.fromFirstAndLast(IPv6Address.fromString("fe80::226:2dff:fefa:0"),
                                                  IPv6Address.fromString("fe80::226:2dff:fefa:ffff")),
                IPv6NetworkMask.fromPrefixLength(120));
        System.out.println(pool.isFree(IPv6Network.fromString("fe80::226:2dff:fefa:5ff/120"))); // prints true

        final IPv6AddressPool newPool = pool.allocate(IPv6Network.fromString("fe80::226:2dff:fefa:5ff/120"));
        System.out.println(newPool.isFree(IPv6Network.fromString("fe80::226:2dff:fefa:5ff/120"))); // prints false
    }

}
