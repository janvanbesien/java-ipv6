package be.jvb.ipv6;

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

    IPv6Address(long highBits, long lowBits)
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
    public IPv6Address add(long value)
    {
        final long newLowBits = lowBits + value;
        if ((lowBits < 0 && newLowBits >= 0) || (lowBits >= 0 && newLowBits < 0))
        {
            // sign changes indicate carry out -> carry out from low bits into high bits
            return new IPv6Address(highBits + 1, newLowBits);
        }
        else
        {
            return new IPv6Address(highBits, newLowBits);
        }
    }

    /**
     * Subtraction. Will never underflow, but wraps around when the lowest ip address has been reached.
     *
     * @param value value to substract
     * @return new IPv6 address
     */
    public IPv6Address subtract(long value)
    {
        final long newLowBits = lowBits - value;
        if ((lowBits < 0 && newLowBits >= 0) || (lowBits >= 0 && newLowBits < 0))
        {
            // sign changes indicate carry out -> carry out from high bits into low bits
            return new IPv6Address(highBits - 1, newLowBits);
        }
        else
        {
            return new IPv6Address(highBits, newLowBits);
        }
    }

    /**
     * Mask the address with the given prefix length.
     *
     * @param prefixLength prefix length
     * @return an address of which the last 128 - prefixLength bits are zero
     */
    public IPv6Address maskWithPrefixLength(int prefixLength)
    {
        if (prefixLength <= 0 || prefixLength > 128)
            throw new IllegalArgumentException("prefix length should be in interval ]0, 128]");

        if (prefixLength == 128)
        {
            return this;
        }
        else if (prefixLength == 64)
        {
            return new IPv6Address(this.highBits, 0);
        }
        else if (prefixLength > 64)
        {
            // apply mask on low bits only
            final int remainingPrefixLength = prefixLength - 64;
            return new IPv6Address(this.highBits, this.lowBits & (0xFFFFFFFFFFFFFFFFL << (64 - remainingPrefixLength)));
        }
        else
        {
            // apply mask on high bits, low bits completely 0
            return new IPv6Address(this.highBits & (0xFFFFFFFFFFFFFFFFL << (64 - prefixLength)), 0);
        }
    }

    /**
     * Calculate the maximum address with the given prefix length.
     *
     * @param prefixLength prefix length
     * @return an address of which the last 128 - prefixLength bits are one
     */
    public IPv6Address maximumAddressWithPrefixLength(int prefixLength)
    {
        if (prefixLength <= 0 || prefixLength > 128)
            throw new IllegalArgumentException("prefix length should be in interval ]0, 128]");

        if (prefixLength == 128)
        {
            return this;
        }
        else if (prefixLength == 64)
        {
            return new IPv6Address(this.highBits, 0xFFFFFFFFFFFFFFFFL);
        }
        else if (prefixLength > 64)
        {
            // apply mask on low bits only
            final int remainingPrefixLength = prefixLength - 64;
            return new IPv6Address(this.highBits, this.lowBits | (0xFFFFFFFFFFFFFFFFL >>> remainingPrefixLength));
        }
        else
        {
            // apply mask on high bits, low bits completely 1
            return new IPv6Address(this.highBits | (0xFFFFFFFFFFFFFFFFL >>> prefixLength), 0xFFFFFFFFFFFFFFFFL);
        }
    }

    @Override
    public String toString()
    {
        final String[] strings = toStringArray();

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

    private String[] toStringArray()
    {
        final short[] shorts = toShortArray();
        final String[] strings = new String[shorts.length];
        for (int i = 0; i < shorts.length; i++)
        {
            strings[i] = String.format("%X", shorts[i]);
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

    int numberOfTrailingZeroes()
    {
        return lowBits == 0 ? Long.numberOfTrailingZeros(highBits) + 64 : Long.numberOfTrailingZeros(lowBits);
    }

    int numberOfTrailingOnes()
    {
        // count trailing ones in "value" by counting the trailing zeroes in "value + 1"
        final IPv6Address plusOne = this.add(1);
        return plusOne.getLowBits() == 0 ?
               Long.numberOfTrailingZeros(plusOne.getHighBits()) + 64 :
               Long.numberOfTrailingZeros(plusOne.getLowBits());
    }

}
