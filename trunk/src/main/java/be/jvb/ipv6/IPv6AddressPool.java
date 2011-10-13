package be.jvb.ipv6;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Immutable representation of an IPv6 address pool.
 * <p/>
 * An IPv6 address pool is like an IPv6 address range in which some addresses are "free" and some are "allocated". Think "dhcp server".
 * Addresses are allocated in whole subnet blocks at once. These subnet blocks have a predefined prefix length for the whole allocatable
 * range.
 *
 * @author Jan Van Besien
 */
public final class IPv6AddressPool
{
    private final IPv6AddressRange underlyingRange;

    private final SortedSet<IPv6AddressRange> freeRanges;

    private final int prefixLength;

    /**
     * Create a pool in between the given first and last address (inclusive) which is completely free. The given prefix length is the prefix
     * length used for allocating subnets from this range. The whole range should be "aligned" on a multiple of subnets of this prefix
     * length (i.e. there should not be a waste of space in the beginning or end which is smaller than one subnet of the given prefix
     * length).
     *
     * @param first        first ip address of the range
     * @param last         last ip address of the range
     * @param prefixLength prefix length with which to allocate subnets from this range
     */
    public IPv6AddressPool(final IPv6Address first, final IPv6Address last, final int prefixLength)
    {
        // in the beginning, all is free
        this(first, last, prefixLength, new TreeSet<IPv6AddressRange>(Arrays.asList(new IPv6AddressRange(first, last))));
    }

    /**
     * Private constructor to construct a pool with a given set of free ranges.
     *
     * @param first        first ip address of the range
     * @param last         last ip address of the range
     * @param prefixLength prefix length with which to allocate subnets from this range
     * @param freeRanges   free ranges in the allocatable IP address range
     */
    private IPv6AddressPool(final IPv6Address first, final IPv6Address last, final int prefixLength,
                            final SortedSet<IPv6AddressRange> freeRanges)
    {
        this.underlyingRange = new IPv6AddressRange(first, last);

        this.prefixLength = prefixLength;
        this.freeRanges = Collections.unmodifiableSortedSet(freeRanges);

        validateFreeRanges(first, last, freeRanges);
        validateRangeIsMultipleOfSubnetsOfGivenPrefixLength(first, last, prefixLength);
    }

    private void validateFreeRanges(IPv6Address first, IPv6Address last, SortedSet<IPv6AddressRange> toValidate)
    {
        if (!toValidate.isEmpty() && !checkWithinBounds(first, last, toValidate))
            throw new IllegalArgumentException("invalid free ranges: not all within bounds of overall range");

        // TODO: some more validations would be usefull. For example the free ranges should be defragmented and non overlapping etc
    }

    private boolean checkWithinBounds(IPv6Address first, IPv6Address last, SortedSet<IPv6AddressRange> toValidate)
    {
        return (toValidate.first().getFirst().compareTo(first) >= 0 && toValidate.last().getLast().compareTo(last) <= 0);
    }

    private void validateRangeIsMultipleOfSubnetsOfGivenPrefixLength(IPv6Address first, IPv6Address last, int prefixLength)
    {
        final int allocatableBits = 128 - prefixLength;

        if (first.numberOfTrailingZeroes() < allocatableBits)
            throw new IllegalArgumentException(
                    "range [" + this + "] is not aligned with prefix length [" + prefixLength + "], first address should end with " +
                    allocatableBits + " zero bits");

        if (last.numberOfTrailingOnes() < allocatableBits)
            throw new IllegalArgumentException(
                    "range [" + this + "] is not aligned with prefix length [" + prefixLength + "], last address should end with " +
                    allocatableBits + " one bits");
    }

    /**
     * Allocate the first available subnet from the pool.
     *
     * @return resulting pool
     */
    public IPv6AddressPool allocate()
    {
        if (!isExhausted())
        {
            // get the first range of free subnets, and take the first subnet of that range
            final IPv6AddressRange firstFreeRange = freeRanges.first();
            final IPv6Network allocated = new IPv6Network(firstFreeRange.getFirst(), prefixLength);

            return doAllocate(allocated, firstFreeRange);
        }
        else
        {
            // exhausted
            return null;
        }
    }

    /**
     * Allocate the given subnet from the pool.
     *
     * @param toAllocate subnet to allocate from the pool
     * @return resulting pool
     */
    public IPv6AddressPool allocate(IPv6Network toAllocate)
    {
        if (!contains(toAllocate))
            throw new IllegalArgumentException(
                    "can not allocate network which is not contained in the pool to allocate from [" + toAllocate + "]");

        if (toAllocate.getPrefixLength() != this.prefixLength)
            throw new IllegalArgumentException("can not allocate network with prefix length /" + toAllocate.getPrefixLength() +
                                               " from a pool configured to hand out subnets with prefix length /" + prefixLength);

        // go find the range that contains the requested subnet
        final IPv6AddressRange rangeToAllocateFrom = findFreeRangeContaining(toAllocate);

        if (rangeToAllocateFrom != null)
        {
            // found a range in which this subnet is free, allocate it
            return doAllocate(toAllocate, rangeToAllocateFrom);
        }
        else
        {
            // requested subnet not free
            return null;
        }
    }

    private IPv6AddressRange findFreeRangeContaining(IPv6Network toAllocate)
    {
        // split around the subnet to allocate
        final SortedSet<IPv6AddressRange> head = freeRanges.headSet(toAllocate);
        final SortedSet<IPv6AddressRange> tail = freeRanges.tailSet(toAllocate);

        // the range containing the network to allocate is either the first of the tail, or the last of the head, or it doesn't exist
        if (!head.isEmpty() && head.last().contains(toAllocate))
        {
            return head.last();
        }
        else if (!tail.isEmpty() && tail.first().contains(toAllocate))
        {
            return tail.first();
        }
        else
        {
            return null;
        }
    }

    /**
     * Private helper method to perform the allocation of a subnet within one of the free ranges.
     *
     * @param toAllocate          subnet to allocate
     * @param rangeToAllocateFrom free range to allocate from
     * @return resulting pool
     */
    private IPv6AddressPool doAllocate(final IPv6Network toAllocate, final IPv6AddressRange rangeToAllocateFrom)
    {
        assert freeRanges.contains(rangeToAllocateFrom);
        assert rangeToAllocateFrom.contains(toAllocate);

        final TreeSet<IPv6AddressRange> newFreeRanges = new TreeSet<IPv6AddressRange>(this.freeRanges);

        // remove range from free ranges
        newFreeRanges.remove(rangeToAllocateFrom);

        // from the range, remove the allocated subnet
        final List<IPv6AddressRange> newRanges = rangeToAllocateFrom.remove(toAllocate);

        // and add the resulting ranges as new free ranges
        newFreeRanges.addAll(newRanges);

        return new IPv6AddressPool(getFirst(), getLast(), prefixLength, newFreeRanges);
    }

    /**
     * Give a network back to the pool (de-allocate).
     *
     * @param toDeAllocate network to de-allocate
     */
    public IPv6AddressPool deAllocate(final IPv6Network toDeAllocate)
    {
        if (!contains(toDeAllocate))
        {
            throw new IllegalArgumentException(
                    "Network to de-allocate[" + toDeAllocate + "] is not contained in this allocatable range [" + this + "]");
        }

        // find ranges just in front or after the network to deallocate. These are the ranges to merge with to prevent fragmentation.
        final IPv6AddressRange freeRangeBeforeNetwork = findFreeRangeBefore(toDeAllocate);
        final IPv6AddressRange freeRangeAfterNetwork = findFreeRangeAfter(toDeAllocate);

        final TreeSet<IPv6AddressRange> newFreeRanges = new TreeSet<IPv6AddressRange>(this.freeRanges);

        if ((freeRangeBeforeNetwork == null) && (freeRangeAfterNetwork == null))
        {
            // nothing to "defragment"
            newFreeRanges.add(toDeAllocate);
        }
        else
        {
            if ((freeRangeBeforeNetwork != null) && (freeRangeAfterNetwork != null))
            {
                // merge two existing ranges
                newFreeRanges.remove(freeRangeBeforeNetwork);
                newFreeRanges.remove(freeRangeAfterNetwork);
                newFreeRanges.add(new IPv6AddressRange(freeRangeBeforeNetwork.getFirst(), freeRangeAfterNetwork.getLast()));
            }
            else if (freeRangeBeforeNetwork != null)
            {
                // append
                newFreeRanges.remove(freeRangeBeforeNetwork);
                newFreeRanges.add(new IPv6AddressRange(freeRangeBeforeNetwork.getFirst(), toDeAllocate.getLast()));
            }
            else /*if (freeRangeAfterNetwork != null)*/
            {
                // prepend
                newFreeRanges.remove(freeRangeAfterNetwork);
                newFreeRanges.add(new IPv6AddressRange(toDeAllocate.getFirst(), freeRangeAfterNetwork.getLast()));
            }
        }

        return new IPv6AddressPool(getFirst(), getLast(), prefixLength, newFreeRanges);
    }

    /**
     * Private helper method to find the free range just before the given network.
     */
    private IPv6AddressRange findFreeRangeBefore(IPv6Network network)
    {
        for (IPv6AddressRange freeRange : freeRanges)
        {
            if (freeRange.getLast().add(1).equals(network.getFirst()))
            {
                return freeRange;
            }
        }

        // not found
        return null;
    }

    /**
     * Private helper method to find the free range just after the given address.
     */
    private IPv6AddressRange findFreeRangeAfter(IPv6Network network)
    {
        for (IPv6AddressRange freeRange : freeRanges)
        {
            if (freeRange.getFirst().subtract(1).equals(network.getLast()))
            {
                return freeRange;
            }
        }

        // not found
        return null;
    }

    /**
     * @return true if no subnets are free in this pool, false otherwize
     */
    public boolean isExhausted()
    {
        return freeRanges.isEmpty();
    }

    public boolean isFree(final IPv6Network network)
    {
        if (network == null)
            throw new IllegalArgumentException("network invalid [null]");

        if (network.getPrefixLength() != prefixLength)
            throw new IllegalArgumentException(
                    "network of prefix length [" + network.getPrefixLength() + "] can not be free in a pool which uses prefix length [" +
                    prefixLength + "]");

        // find a free range that contains the network
        for (IPv6AddressRange freeRange : freeRanges)
        {
            if (freeRange.contains(network))
            {
                return true;
            }
        }

        // nothing found
        return false;
    }

    // delegation methods

    public boolean contains(IPv6Address address)
    {
        return underlyingRange.contains(address);
    }

    public boolean contains(IPv6AddressRange range)
    {
        return underlyingRange.contains(range);
    }

    public boolean overlaps(IPv6AddressRange range)
    {
        return underlyingRange.overlaps(range);
    }

    public IPv6Address getFirst()
    {
        return underlyingRange.getFirst();
    }

    public IPv6Address getLast()
    {
        return underlyingRange.getLast();
    }
}
