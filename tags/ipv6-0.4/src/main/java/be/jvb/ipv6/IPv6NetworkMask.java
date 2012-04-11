package be.jvb.ipv6;

import java.util.BitSet;

/**
 * Immutable representation of an IPv6 network mask. A network mask is nothing more than an IPv6 address with a continuous range of 1 bits
 * starting from the most significant bit. A network mask can also be represented as a prefix length, which is the count of these 1 bits.
 *
 * @author Jan Van Besien
 */
public final class IPv6NetworkMask
{
    private final int prefixLength;

    /**
     * Construct an IPv6 network mask from an IPv6 address. The address should be a valid network mask.
     *
     * @param iPv6Address address to use as network mask
     * @throws IllegalArgumentException if the address is not a valid network mask
     */
    public IPv6NetworkMask(final IPv6Address iPv6Address)
    {
        validateNetworkMask(iPv6Address);
        this.prefixLength = iPv6Address.numberOfLeadingOnes();
    }

    /**
     * Construct an IPv6 network mask from a prefix length. The prefix length should be in the interval ]0, 128].
     *
     * @param prefixLength prefix length
     * @throws IllegalArgumentException if the prefix length is not in the interval ]0, 128]
     */
    public IPv6NetworkMask(int prefixLength)
    {
        if (prefixLength <= 0 || prefixLength > 128)
            throw new IllegalArgumentException("prefix length should be in interval ]0, 128]");

        this.prefixLength = prefixLength;
    }

    private static void validateNetworkMask(IPv6Address addressToValidate)
    {
        final BitSet addressAsBitSet = BitSet.valueOf(new long[]{addressToValidate.getLowBits(), addressToValidate.getHighBits()});
        if (!addressAsBitSet.get(127))
        {
            throw new IllegalArgumentException(addressToValidate + " is not a valid network mask");
        }
        else
        {
            boolean firstZeroFound = false;
            for (int i = 127; i >= 0 && !firstZeroFound; i--)
            {
                if (!addressAsBitSet.get(i))
                {
                    firstZeroFound = true;

                    // a zero -> all the others should also be zero
                    for (int j = i - 1; j >= 0; j--)
                    {
                        if (addressAsBitSet.get(j))
                        {
                            throw new IllegalArgumentException(addressToValidate + " is not a valid network mask");
                        }
                    }
                }
            }
        }
    }

    public int asPrefixLength()
    {
        return prefixLength;
    }

    public IPv6Address asAddress()
    {
        if (prefixLength == 128)
        {
            return new IPv6Address(0xFFFFFFFFFFFFFFFFL, 0xFFFFFFFFFFFFFFFFL);
        }
        else if (prefixLength == 64)
        {
            return new IPv6Address(0xFFFFFFFFFFFFFFFFL, 0L);
        }
        else if (prefixLength > 64)
        {
            final int remainingPrefixLength = prefixLength - 64;
            return new IPv6Address(0xFFFFFFFFFFFFFFFFL, (0xFFFFFFFFFFFFFFFFL << (64 - remainingPrefixLength)));
        }
        else
        {
            return new IPv6Address(0xFFFFFFFFFFFFFFFFL << (64 - prefixLength), 0);
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IPv6NetworkMask that = (IPv6NetworkMask) o;

        if (prefixLength != that.prefixLength) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        return prefixLength;
    }

    @Override
    public String toString()
    {
        return asAddress().toString();
    }
}
