package be.jvb.ipv6;

import static be.jvb.ipv6.IPv6NetworkHelpers.longestPrefixLength;

/**
 * Immutable representation of an IPv6 network based on an address and a prefix length. An IPv6 network is also an IPv6 address range (but
 * not all ranges are valid networks).
 *
 * @author Jan Van Besien
 */
public final class IPv6Network extends IPv6AddressRange
{
    private final IPv6Address address;

    private final int prefixLength;

    /**
     * Construct from address and prefix length.
     *
     * @param address      address
     * @param prefixLength prefix length, in range ]0, 128]
     */
    public IPv6Network(IPv6Address address, int prefixLength)
    {
        super(address.maskWithPrefixLength(prefixLength), address.maximumAddressWithPrefixLength(prefixLength));

        this.address = address.maskWithPrefixLength(prefixLength);
        this.prefixLength = prefixLength;
    }

    /**
     * Construct from first and last address. This will construct the smallest possible network ("longest prefix length") which contains
     * both addresses.
     *
     * @param first first address
     * @param last  last address
     */
    public IPv6Network(IPv6Address first, IPv6Address last)
    {
        super(first.maskWithPrefixLength(longestPrefixLength(first, last)),
              first.maximumAddressWithPrefixLength(longestPrefixLength(first, last)));

        this.prefixLength = longestPrefixLength(first, last);
        this.address = this.getFirst();
    }

    /**
     * Create an IPv6 network from its String representation. For example "1234:5678:abcd:0:0:0:0:0/64" or "2001::ff/128".
     *
     * @param string string representation
     * @return IPv6 address
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

        return new IPv6Network(networkAddress, prefixLength);
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
        return address.toString() + "/" + prefixLength;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IPv6Network that = (IPv6Network) o;

        if (prefixLength != that.prefixLength) return false;
        if (address != null ? !address.equals(that.address) : that.address != null) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = address != null ? address.hashCode() : 0;
        result = 31 * result + prefixLength;
        return result;
    }

    public int getPrefixLength()
    {
        return prefixLength;
    }

    public IPv6Address getNetmask()
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
}
