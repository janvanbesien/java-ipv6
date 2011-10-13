package be.jvb.ipv6;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Immutable representation of a continuous range of IPv6 addresses (bounds included).
 *
 * @author Jan Van Besien
 */
public class IPv6AddressRange implements Comparable<IPv6AddressRange>, Iterable<IPv6Address>
{
    private final IPv6Address first;

    private final IPv6Address last;

    public IPv6AddressRange(IPv6Address first, IPv6Address last)
    {
        if (first.compareTo(last) > 0)
            throw new IllegalArgumentException("Cannot create ip address range with last address < first address");

        this.first = first;
        this.last = last;
    }

    public boolean contains(IPv6Address address)
    {
        return first.compareTo(address) <= 0 && last.compareTo(address) >= 0;
    }

    public boolean contains(IPv6AddressRange range)
    {
        return contains(range.first) && contains(range.last);
    }

    public boolean overlaps(IPv6AddressRange range)
    {
        return contains(range.first) || contains(range.last) || range.contains(first) || range.contains(last);
    }

    /**
     * @return an iterator which iterates all addresses in this range, in order.
     */
    @Override
    public Iterator<IPv6Address> iterator()
    {
        return new Ipv6AddressRangeIterator();
    }

    /**
     * Remove an address from the range, resulting in one, none or two new ranges. If an address outside the range is removed, this has no
     * effect. If the first or last address is removed, a single new range is returned (potentially empty if the range only contained a
     * single address). If an address somewhere else in the range is removed, two new ranges are returned.
     *
     * @param address adddress to remove from the range
     * @return list of resulting ranges
     */
    public List<IPv6AddressRange> remove(IPv6Address address)
    {
        if (address == null)
            throw new IllegalArgumentException("invalid address [null]");

        if (!contains(address))
            return Collections.singletonList(this);
        else if (address.equals(first) && address.equals(last))
            return Collections.emptyList();
        else if (address.equals(first))
            return Collections.singletonList(new IPv6AddressRange(first.add(1), last));
        else if (address.equals(last))
            return Collections.singletonList(new IPv6AddressRange(first, last.subtract(1)));
        else
            return Arrays.asList(new IPv6AddressRange(first, address.subtract(1)),
                                 new IPv6AddressRange(address.add(1), last));
    }

    /**
     * Extend the range just enough at its head or tail such that the given address is included.
     *
     * @param address address to extend the range to
     * @return new (bigger) range
     */
    public IPv6AddressRange extend(IPv6Address address)
    {
        if (address.compareTo(first) < 0)
            return new IPv6AddressRange(address, last);
        else if (address.compareTo(last) > 0)
            return new IPv6AddressRange(first, address);
        else
            return this;
    }

    /**
     * Remove a network from the range, resulting in one, none or two new ranges. If a network outside (or partially outside) the range is
     * removed, this has no effect. If the network which is removed is aligned with the beginning or end of the range, a single new ranges
     * is returned (potentially empty if the range was equal to the network which is removed from it). If a network somewhere else in the
     * range is removed, two new ranges are returned.
     *
     * @param network network to remove from the range
     * @return list of resulting ranges
     */
    public List<IPv6AddressRange> remove(IPv6Network network)
    {
        if (network == null)
            throw new IllegalArgumentException("invalid network [null]");

        if (!contains(network))
            return Collections.singletonList(this);
        else if (this.equals(network))
            return Collections.emptyList();
        else if (first.equals(network.getFirst()))
            return Collections.singletonList(new IPv6AddressRange(network.getLast().add(1), last));
        else if (last.equals(network.getLast()))
            return Collections.singletonList(new IPv6AddressRange(first, network.getFirst().subtract(1)));
        else
            return Arrays.asList(new IPv6AddressRange(first, network.getFirst().subtract(1)),
                                 new IPv6AddressRange(network.getLast().add(1), last));

    }

    @Override
    public String toString()
    {
        return first.toString() + " - " + last.toString();
    }

    @Override
    public int compareTo(IPv6AddressRange that)
    {
        if (this.first != that.first)
            return this.first.compareTo(that.first);
        else
            return this.last.compareTo(that.last);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof IPv6AddressRange)) return false;

        IPv6AddressRange that = (IPv6AddressRange) o;

        if (first != null ? !first.equals(that.first) : that.first != null) return false;
        if (last != null ? !last.equals(that.last) : that.last != null) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (last != null ? last.hashCode() : 0);
        return result;
    }

    public IPv6Address getFirst()
    {
        return first;
    }

    public IPv6Address getLast()
    {
        return last;
    }

    /**
     * @see IPv6AddressRange#iterator()
     */
    private final class Ipv6AddressRangeIterator implements Iterator<IPv6Address>
    {
        private IPv6Address current = first;

        @Override
        public boolean hasNext()
        {
            return current.compareTo(last) <= 0;
        }

        @Override
        public IPv6Address next()
        {
            if (hasNext())
                return current = current.add(1);
            else
                throw new NoSuchElementException();
        }

        @Override
        public void remove()
        {
            IPv6AddressRange.this.remove(current);
        }
    }
}
