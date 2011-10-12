package be.jvb.ipv6;

import java.util.BitSet;

/**
 * Helper methods used by IPv6Network.
 *
 * @author Jan Van Besien
 */
public class IPv6NetworkHelpers
{
    static int longestPrefixLength(IPv6Address first, IPv6Address last)
    {
        final BitSet firstBits = BitSet.valueOf(new long[]{first.getLowBits(), first.getHighBits()});
        final BitSet lastBits = BitSet.valueOf(new long[]{last.getLowBits(), last.getHighBits()});

        return countLeadingSimilarBits(firstBits, lastBits);
    }

    private static int countLeadingSimilarBits(BitSet firstBits, BitSet lastBits)
    {
        int result = 0;
        for (int i = 127; i >= 0 && (firstBits.get(i) == lastBits.get(i)); i--)
        {
            result++;
        }

        return result;
    }
}
