package com.googlecode.ipv6;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Immutable representation of an IPv6 address.
 *
 * @author Jan Van Besien
 */
public final class IPv6Address implements Comparable<IPv6Address>
{
    private static final int N_SHORTS = 8;

    private final long highBits;

    private final long lowBits;

    public IPv6Address(long highBits, long lowBits)
    {
        this.highBits = highBits;
        this.lowBits = lowBits;
    }

    /**
     * Create an IPv6 address from its String representation. For example "1234:5678:abcd:0000:9876:3210:ffff:ffff" or "2001::ff" or even
     * "::".
     *
     * @param string string representation
     * @return IPv6 address
     */
    public static IPv6Address fromString(final String string)
    {
        if (string == null)
            throw new IllegalArgumentException("can not parse [null]");

        final String longNotation = IPv6AddressHelpers.expandShortNotation(string);

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
                // oops, we added something postive and the result is smaller -> overflow detected (carry over one bit from low to high)
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
     * @return String representation of the IPv6 address, using shorthand notation whenever possible.
     */
    @Override
    public String toString()
    {
        final String[] strings = toArrayOfShortStrings();

        final StringBuilder result = new StringBuilder();

        boolean shortHandNotationUsed = false;
        boolean shortHandNotationBusy = false;
        for (int i = 0; i < strings.length; i++)
        {
            if (!shortHandNotationUsed && i < N_SHORTS - 1 && IPv6AddressHelpers.isZeroString(strings[i]) && IPv6AddressHelpers
                    .isZeroString(strings[i + 1]))
            {
                shortHandNotationUsed = true;
                shortHandNotationBusy = true;
                if (i == 0)
                    result.append("::");
                else
                    result.append(":");
            }
            else if (!(IPv6AddressHelpers.isZeroString(strings[i]) && shortHandNotationBusy))
            {
                shortHandNotationBusy = false;
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

    public short[] toShortArray()
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
