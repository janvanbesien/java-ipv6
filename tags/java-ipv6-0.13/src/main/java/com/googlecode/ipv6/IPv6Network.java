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

/**
 * Immutable representation of an IPv6 network based on an address and a prefix length. An IPv6 network is also an IPv6 address range (but
 * not all ranges are valid networks).
 *
 * @author Jan Van Besien
 */
public final class IPv6Network extends IPv6AddressRange
{
    public static final IPv6Network MULTICAST_NETWORK = fromString("ff00::/8");

    public static final IPv6Network SITE_LOCAL_NETWORK = fromString("fec0::/48");

    public static final IPv6Network LINK_LOCAL_NETWORK = fromString("fe80::/64");


    private final IPv6Address address;

    private final IPv6NetworkMask networkMask;

    /**
     * Construct from address and network mask.
     *
     * @param address     address
     * @param networkMask network mask
     */
    private IPv6Network(IPv6Address address, IPv6NetworkMask networkMask)
    {
        super(address.maskWithNetworkMask(networkMask), address.maximumAddressWithNetworkMask(networkMask));

        this.address = address.maskWithNetworkMask(networkMask);
        this.networkMask = networkMask;
    }

    /**
     * Create an IPv6 network from an IPv6Address and an IPv6NetworkMask
     *
     * @param address     IPv6 address (the network address or any other address within the network)
     * @param networkMask IPv6 network mask
     * @return IPv6 network
     */
    public static IPv6Network fromAddressAndMask(IPv6Address address, IPv6NetworkMask networkMask)
    {
        return new IPv6Network(address, networkMask);
    }

    /**
     * Create an IPv6 network from the two addresses within the network. This will construct the smallest possible network ("longest prefix
     * length") which contains both addresses.
     *
     * @param one address one
     * @param two address two, should be bigger than address one
     */
    public static IPv6Network fromTwoAddresses(IPv6Address one, IPv6Address two)
    {
        final IPv6NetworkMask longestPrefixLength = IPv6NetworkMask.fromPrefixLength(IPv6NetworkHelpers.longestPrefixLength(one, two));
        return new IPv6Network(one.maskWithNetworkMask(longestPrefixLength), longestPrefixLength);
    }

    /**
     * Create an IPv6 network from its String representation. For example "1234:5678:abcd:0:0:0:0:0/64" or "2001::ff/128".
     *
     * @param string string representation
     * @return IPv6 network
     */
    public static IPv6Network fromString(String string)
    {
        if (string.indexOf('/') == -1)
        {
            throw new IllegalArgumentException("Expected format is network-address/prefix-length");
        }

        final String networkAddressString = parseNetworkAddress(string);
        int prefixLength = parsePrefixLength(string);

        final IPv6Address networkAddress = IPv6Address.fromString(networkAddressString);

        return fromAddressAndMask(networkAddress, new IPv6NetworkMask(prefixLength));
    }

    private static String parseNetworkAddress(String string)
    {
        return string.substring(0, string.indexOf('/'));
    }

    private static int parsePrefixLength(String string)
    {
        try
        {
            return Integer.parseInt(string.substring(string.indexOf('/') + 1));
        } catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Prefix length should be a positive integer");
        }
    }

    @Override
    public String toString()
    {
        return address.toString() + "/" + networkMask.asPrefixLength();
    }

    /**
     * @return like <code>toString</code> but without using shorthand notations for addresses
     */
    public String toLongString()
    {
        return address.toLongString() + "/" + networkMask.asPrefixLength();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        IPv6Network that = (IPv6Network) o;

        if (address != null ? !address.equals(that.address) : that.address != null) return false;
        if (networkMask != null ? !networkMask.equals(that.networkMask) : that.networkMask != null) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (networkMask != null ? networkMask.hashCode() : 0);
        return result;
    }

    public IPv6NetworkMask getNetmask()
    {
        return networkMask;
    }
}
