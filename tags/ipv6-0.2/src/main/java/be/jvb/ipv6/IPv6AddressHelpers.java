package be.jvb.ipv6;

import java.util.Arrays;

/**
 * Helper methods used by IPv6Address.
 *
 * @author Jan Van Besien
 */
public final class IPv6AddressHelpers
{
    static long[] parseStringArrayIntoLongArray(String[] strings)
    {
        final long[] longs = new long[strings.length];
        for (int i = 0; i < strings.length; i++)
        {
            longs[i] = Long.parseLong(strings[i], 16);
        }
        return longs;
    }

    static void validateLongs(long[] longs)
    {
        if (longs.length != 8)
            throw new IllegalArgumentException("an IPv6 address should contain 8 shorts [" + Arrays.toString(longs) + "]");

        for (long l : longs)
        {
            if (l < 0) throw new IllegalArgumentException("each element should be positive [" + Arrays.toString(longs) + "]");
            if (l > 0xFFFF) throw new IllegalArgumentException("each element should be less than 0xFFFF [" + Arrays.toString(longs) + "]");
        }
    }

    static IPv6Address mergeLongArrayIntoIPv6Address(long[] longs)
    {
        long high = 0L;
        long low = 0L;

        for (int i = 0; i < longs.length; i++)
        {
            if (inHighRange(i))
                high |= (longs[i] << ((longs.length - i - 1) * 16));
            else
                low |= (longs[i] << ((longs.length - i - 1) * 16));
        }

        return new IPv6Address(high, low);
    }

    static boolean inHighRange(int shortNumber)
    {
        return shortNumber >= 0 && shortNumber < 4;
    }

    static String expandShortNotation(String string)
    {
        if (!string.contains("::"))
        {
            return string;
        }
        else if (string.equals("::"))
        {
            return generateZeroes(8);
        }
        else
        {
            final int numberOfColons = countOccurrences(string, ':');
            if (string.startsWith("::"))
                return string.replace("::", generateZeroes((7 + 2) - numberOfColons));
            else if (string.endsWith("::"))
                return string.replace("::", ":" + generateZeroes((7 + 2) - numberOfColons));
            else
                return string.replace("::", ":" + generateZeroes((7 + 2 - 1) - numberOfColons));
        }
    }

    public static int countOccurrences(String haystack, char needle)
    {
        int count = 0;
        for (int i = 0; i < haystack.length(); i++)
        {
            if (haystack.charAt(i) == needle)
            {
                count++;
            }
        }
        return count;
    }

    public static String generateZeroes(int number)
    {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < number; i++)
        {
            builder.append("0:");
        }

        return builder.toString();
    }

    static boolean isZeroString(String string)
    {
        return "0".equals(string);
    }

    static boolean isLessThanUnsigned(long a, long b)
    {
        return (a < b) ^ ((a < 0) != (b < 0));
    }
}
