package be.jvb.ipv6;

import org.junit.Test;

import static be.jvb.ipv6.IPv6Address.fromString;
import static junit.framework.Assert.assertEquals;

/**
 * @author Jan Van Besien
 */
public class IPv6NetworkHelpersTest
{
    @Test
    public void longestPrefixLength()
    {
        assertEquals(128, IPv6NetworkHelpers.longestPrefixLength(fromString("::1"), fromString("::1")));
        assertEquals(127, IPv6NetworkHelpers.longestPrefixLength(fromString("::"), fromString("::1")));
        assertEquals(127, IPv6NetworkHelpers.longestPrefixLength(fromString("::1"), fromString("::")));
        assertEquals(126, IPv6NetworkHelpers.longestPrefixLength(fromString("::1"), fromString("::2")));

        assertEquals(0, IPv6NetworkHelpers.longestPrefixLength(fromString("::"), fromString("ffff::")));
        assertEquals(32, IPv6NetworkHelpers.longestPrefixLength(fromString("ffff:ffff::"), fromString("ffff:ffff:8000::")));
        assertEquals(65, IPv6NetworkHelpers.longestPrefixLength(fromString("ffff:ffff::8000:2:3:4"), fromString("ffff:ffff::C000:2:3:4")));
    }
}
