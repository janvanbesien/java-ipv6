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

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;

/**
 * Immutable representation of an IPv6 address.
 *
 * @author Jan Van Besien
 */
public final class IPv6Address implements Comparable<IPv6Address>
{
    private static final int N_SHORTS = 8;

    private static final int N_BYTES = 16;

    private final long highBits;

    private final long lowBits;

    IPv6Address(long highBits, long lowBits)
    {
        this.highBits = highBits;
        this.lowBits = lowBits;
    }

    /**
     * Construct an IPv6Address from two longs representing the 64 highest and 64 lowest bits. It is usually easier to construct
     * IPv6Addresses from a {@link String} or an {@link java.net.InetAddress}. The internal representation of an IPv6Address is exactly
     * these two longs though, so if you already happen to have them, this provides a very efficient way to construct an IPv6Address.
     *
     * @param highBits highest order bits
     * @param lowBits  lowest order bits
     */
    public static IPv6Address fromLongs(long highBits, long lowBits)
    {
        return new IPv6Address(highBits, lowBits);
    }

    /**
     * Create an IPv6 address from its String representation. For example "1234:5678:abcd:0000:9876:3210:ffff:ffff" or "2001::ff" or even
     * "::". IPv4-Mapped IPv6 addresses such as "::ffff:123.456.123.456" are also supported.
     *
     * @param string string representation
     * @return IPv6 address
     */
    public static IPv6Address fromString(final String string)
    {
        if (string == null)
            throw new IllegalArgumentException("can not parse [null]");

        final String withoutIPv4MappedNotation = IPv6AddressHelpers.rewriteIPv4MappedNotation(string);
        final String longNotation = IPv6AddressHelpers.expandShortNotation(withoutIPv4MappedNotation);

        final long[] longs = tryParseStringArrayIntoLongArray(string, longNotation);

        IPv6AddressHelpers.validateLongs(longs);

        return IPv6AddressHelpers.mergeLongArrayIntoIPv6Address(longs);
    }

    private static long[] tryParseStringArrayIntoLongArray(String string, String longNotation)
    {
        try
        {
            return IPv6AddressHelpers.parseStringArrayIntoLongArray(longNotation.split(":"));
        } catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("can not parse [" + string + "]");
        }
    }

    /**
     * Create an IPv6 address from a java.net.Inet6Address.
     *
     * @param inetAddress Inet6Address representation
     * @return IPv6 address
     */
    public static IPv6Address fromInetAddress(final InetAddress inetAddress)
    {
        if (inetAddress == null)
            throw new IllegalArgumentException("can not construct from [null]");

        return fromString(inetAddress.getHostAddress());
    }

    public InetAddress toInetAddress() throws UnknownHostException
    {
        return Inet6Address.getByName(toString());
    }

    /**
     * Create an IPv6 address from a byte array.
     *
     * @param bytes byte array with 16 bytes
     * @return IPv6 address
     */
    public static IPv6Address fromByteArray(final byte[] bytes)
    {
        if (bytes == null)
            throw new IllegalArgumentException("can not construct from [null]");
        if (bytes.length != N_BYTES)
            throw new IllegalArgumentException("the byte array to construct from should be 16 bytes long");

        ByteBuffer buf = ByteBuffer.allocate(N_BYTES);
        for (byte b : bytes)
        {
            buf.put(b);
        }

        buf.rewind();
        LongBuffer longBuffer = buf.asLongBuffer();
        return new IPv6Address(longBuffer.get(), longBuffer.get());
    }

    public byte[] toByteArray()
    {
        ByteBuffer byteBuffer = ByteBuffer.allocate(N_BYTES).putLong(highBits).putLong(lowBits);
        return byteBuffer.array();
    }

    /**
     * Addition. Will never overflow, but wraps around when the highest ip address has been reached.
     *
     * @param value value to add
     * @return new IPv6 address
     */
    public IPv6Address add(int value)
    {
        final long newLowBits = lowBits + value;

        if (value >= 0)
        {
            if (IPv6AddressHelpers.isLessThanUnsigned(newLowBits, lowBits))
            {
                // oops, we added something positive and the result is smaller -> overflow detected (carry over one bit from low to high)
                return new IPv6Address(highBits + 1, newLowBits);
            }
            else
            {
                // no overflow
                return new IPv6Address(highBits, newLowBits);
            }
        }
        else
        {
            if (IPv6AddressHelpers.isLessThanUnsigned(lowBits, newLowBits))
            {
                // oops, we added something negative and the result is bigger -> overflow detected (carry over one bit from high to low)
                return new IPv6Address(highBits - 1, newLowBits);
            }
            else
            {
                // no overflow
                return new IPv6Address(highBits, newLowBits);
            }
        }
    }

    /**
     * Subtraction. Will never underflow, but wraps around when the lowest ip address has been reached.
     *
     * @param value value to substract
     * @return new IPv6 address
     */
    public IPv6Address subtract(int value)
    {
        final long newLowBits = lowBits - value;

        if (value >= 0)
        {
            if (IPv6AddressHelpers.isLessThanUnsigned(lowBits, newLowBits))
            {
                // oops, we subtracted something postive and the result is bigger -> overflow detected (carry over one bit from high to low)
                return new IPv6Address(highBits - 1, newLowBits);
            }
            else
            {
                // no overflow
                return new IPv6Address(highBits, newLowBits);
            }
        }
        else
        {
            if (IPv6AddressHelpers.isLessThanUnsigned(newLowBits, lowBits))
            {
                // oops, we subtracted something negative and the result is smaller -> overflow detected (carry over one bit from low to high)
                return new IPv6Address(highBits + 1, newLowBits);
            }
            else
            {
                // no overflow
                return new IPv6Address(highBits, newLowBits);
            }
        }
    }

    /**
     * Mask the address with the given network mask.
     *
     * @param networkMask network mask
     * @return an address of which the last 128 - networkMask.asPrefixLength() bits are zero
     */
    public IPv6Address maskWithNetworkMask(final IPv6NetworkMask networkMask)
    {
        if (networkMask.asPrefixLength() == 128)
        {
            return this;
        }
        else if (networkMask.asPrefixLength() == 64)
        {
            return new IPv6Address(this.highBits, 0);
        }
        else if (networkMask.asPrefixLength() == 0)
        {
            return new IPv6Address(0, 0);
        }
        else if (networkMask.asPrefixLength() > 64)
        {
            // apply mask on low bits only
            final int remainingPrefixLength = networkMask.asPrefixLength() - 64;
            return new IPv6Address(this.highBits, this.lowBits & (0xFFFFFFFFFFFFFFFFL << (64 - remainingPrefixLength)));
        }
        else
        {
            // apply mask on high bits, low bits completely 0
            return new IPv6Address(this.highBits & (0xFFFFFFFFFFFFFFFFL << (64 - networkMask.asPrefixLength())), 0);
        }
    }

    /**
     * Calculate the maximum address with the given network mask.
     *
     * @param networkMask network mask
     * @return an address of which the last 128 - networkMask.asPrefixLength() bits are one
     */
    public IPv6Address maximumAddressWithNetworkMask(final IPv6NetworkMask networkMask)
    {
        if (networkMask.asPrefixLength() == 128)
        {
            return this;
        }
        else if (networkMask.asPrefixLength() == 64)
        {
            return new IPv6Address(this.highBits, 0xFFFFFFFFFFFFFFFFL);
        }
        else if (networkMask.asPrefixLength() > 64)
        {
            // apply mask on low bits only
            final int remainingPrefixLength = networkMask.asPrefixLength() - 64;
            return new IPv6Address(this.highBits, this.lowBits | (0xFFFFFFFFFFFFFFFFL >>> remainingPrefixLength));
        }
        else
        {
            // apply mask on high bits, low bits completely 1
            return new IPv6Address(this.highBits | (0xFFFFFFFFFFFFFFFFL >>> networkMask.asPrefixLength()), 0xFFFFFFFFFFFFFFFFL);
        }
    }

    /**
     * Returns true if the address is an IPv4-mapped IPv6 address. In these addresses, the first 80 bits are zero, the next 16 bits are one,
     * and the remaining 32 bits are the IPv4 address.
     *
     * @return true if the address is an IPv4-mapped IPv6 addresses.
     */
    public boolean isIPv4Mapped()
    {
        return this.highBits == 0 // 64 zero bits
                && (this.lowBits & 0xFFFF000000000000L) == 0 // 16 more zero bits
                && (this.lowBits & 0x0000FFFF00000000L) == 0x0000FFFF00000000L; // 16 one bits and the remainder is the IPv4 address
    }

    /**
     * @return true if the address is an IPv6 multicast address (an address in the network ff00::/8)
     */
    public boolean isMulticast()
    {
        return IPv6Network.MULTICAST_NETWORK.contains(this);
    }

    /**
     * @return true if the address is an IPv6 site-local address (an address in the network fec0::/48)
     */
    public boolean isSiteLocal()
    {
        return IPv6Network.SITE_LOCAL_NETWORK.contains(this);
    }

    /**
     * @return true if the address is an IPv6 link-local address (an address in the network fe80::/48)
     */
    public boolean isLinkLocal()
    {
        return IPv6Network.LINK_LOCAL_NETWORK.contains(this);
    }

    /**
     * Returns a string representation of the IPv6 address. It will use shorthand notation and special notation for IPv4-mapped IPv6
     * addresses whenever possible.
     *
     * @return String representation of the IPv6 address
     */
    @Override
    public String toString()
    {
        if (isIPv4Mapped())
            return toIPv4MappedAddressString();
        else
            return toShortHandNotationString();
    }

    private String toIPv4MappedAddressString()
    {
        int byteZero = (int) ((this.lowBits & 0x00000000FF000000L) >> 24);
        int byteOne = (int) ((this.lowBits & 0x0000000000FF0000L) >> 16);
        int byteTwo = (int) ((this.lowBits & 0x000000000000FF00L) >> 8);
        int byteThree = (int) ((this.lowBits & 0x00000000000000FFL));

        final StringBuilder result = new StringBuilder("::ffff:");
        result.append(byteZero).append(".").append(byteOne).append(".").append(byteTwo).append(".").append(byteThree);

        return result.toString();
    }

    private String toShortHandNotationString()
    {
        final String[] strings = toArrayOfShortStrings();

        final StringBuilder result = new StringBuilder();

        int[] shortHandNotationPositionAndLength = startAndLengthOfLongestRunOfZeroes();
        int shortHandNotationPosition = shortHandNotationPositionAndLength[0];
        int shortHandNotationLength = shortHandNotationPositionAndLength[1];

        boolean useShortHandNotation = shortHandNotationLength > 1; // RFC5952 recommends not to use shorthand notation for a single zero

        for (int i = 0; i < strings.length; i++)
        {
            if (useShortHandNotation && i == shortHandNotationPosition)
            {
                if (i == 0)
                    result.append("::");
                else
                    result.append(":");
            }
            else if (!(i > shortHandNotationPosition && i < shortHandNotationPosition + shortHandNotationLength))
            {
                result.append(strings[i]);
                if (i < N_SHORTS - 1)
                    result.append(":");
            }
        }

        return result.toString().toLowerCase();
    }

    private String[] toArrayOfShortStrings()
    {
        final short[] shorts = toShortArray();
        final String[] strings = new String[shorts.length];
        for (int i = 0; i < shorts.length; i++)
        {
            strings[i] = String.format("%x", shorts[i]);
        }
        return strings;
    }

    /**
     * @return String representation of the IPv6 address, never using shorthand notation.
     */
    public String toLongString()
    {
        final String[] strings = toArrayOfZeroPaddedstrings();
        final StringBuilder result = new StringBuilder();
        for (int i = 0; i < strings.length - 1; i++)
        {
            result.append(strings[i]).append(":");
        }

        result.append(strings[strings.length - 1]);

        return result.toString();
    }

    private String[] toArrayOfZeroPaddedstrings()
    {
        final short[] shorts = toShortArray();
        final String[] strings = new String[shorts.length];
        for (int i = 0; i < shorts.length; i++)
        {
            strings[i] = String.format("%04x", shorts[i]);
        }
        return strings;
    }

    private short[] toShortArray()
    {
        final short[] shorts = new short[N_SHORTS];

        for (int i = 0; i < N_SHORTS; i++)
        {
            if (IPv6AddressHelpers.inHighRange(i))
                shorts[i] = (short) (((highBits << i * 16) >>> 16 * (N_SHORTS - 1)) & 0xFFFF);
            else
                shorts[i] = (short) (((lowBits << i * 16) >>> 16 * (N_SHORTS - 1)) & 0xFFFF);
        }

        return shorts;
    }

    int[] startAndLengthOfLongestRunOfZeroes()
    {
        int longestConsecutiveZeroes = 0;
        int longestConsecutiveZeroesPos = -1;
        short[] shorts = toShortArray();
        for (int pos = 0; pos < shorts.length; pos++)
        {
            int consecutiveZeroesAtCurrentPos = countConsecutiveZeroes(shorts, pos);
            if (consecutiveZeroesAtCurrentPos > longestConsecutiveZeroes)
            {
                longestConsecutiveZeroes = consecutiveZeroesAtCurrentPos;
                longestConsecutiveZeroesPos = pos;
            }
        }

        return new int[]{longestConsecutiveZeroesPos, longestConsecutiveZeroes};
    }

    private int countConsecutiveZeroes(short[] shorts, int offset)
    {
        int count = 0;
        for (int i = offset; i < shorts.length && shorts[i] == 0; i++)
        {
            count++;
        }

        return count;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IPv6Address that = (IPv6Address) o;

        if (highBits != that.highBits) return false;
        if (lowBits != that.lowBits) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = (int) (lowBits ^ (lowBits >>> 32));
        result = 31 * result + (int) (highBits ^ (highBits >>> 32));
        return result;
    }

    public int compareTo(IPv6Address that)
    {
        if (this.highBits == that.highBits)
            if (this.lowBits == that.lowBits)
                return 0;
            else
                return IPv6AddressHelpers.isLessThanUnsigned(this.lowBits, that.lowBits) ? -1 : 1;
        else if (this.highBits == that.highBits)
            return 0;
        else
            return IPv6AddressHelpers.isLessThanUnsigned(this.highBits, that.highBits) ? -1 : 1;
    }

    public long getHighBits()
    {
        return highBits;
    }

    public long getLowBits()
    {
        return lowBits;
    }

    public int numberOfTrailingZeroes()
    {
        return lowBits == 0 ?
                Long.numberOfTrailingZeros(highBits) + 64 :
                Long.numberOfTrailingZeros(lowBits);
    }

    public int numberOfTrailingOnes()
    {
        // count trailing ones in "value" by counting the trailing zeroes in "value + 1"
        final IPv6Address plusOne = this.add(1);
        return plusOne.getLowBits() == 0 ?
                Long.numberOfTrailingZeros(plusOne.getHighBits()) + 64 :
                Long.numberOfTrailingZeros(plusOne.getLowBits());
    }

    public int numberOfLeadingZeroes()
    {
        return highBits == 0 ?
                Long.numberOfLeadingZeros(lowBits) + 64 :
                Long.numberOfLeadingZeros(highBits);
    }

    public int numberOfLeadingOnes()
    {
        // count leading ones in "value" by counting leading zeroes in "~ value"
        final IPv6Address flipped = new IPv6Address(~this.highBits, ~this.lowBits);
        return flipped.numberOfLeadingZeroes();
    }

}
