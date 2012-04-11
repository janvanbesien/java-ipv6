package be.jvb.ipv6;

import org.junit.Test;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

import static be.jvb.ipv6.IPv6Address.fromInetAddress;
import static be.jvb.ipv6.IPv6Address.fromString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Jan Van Besien
 */
public class IPv6AddressTest
{
    @Test
    public void parseFromAllZeroes()
    {
        assertEquals("::", fromString("0000:0000:0000:0000:0000:0000:0000:0000").toString());
    }

    @Test
    public void parseFromAllZeroesShortNotation()
    {
        assertEquals("::", fromString("::").toString());
    }

    @Test
    public void parseSomeRealAddresses()
    {
        assertEquals("::1", fromString("0000:0000:0000:0000:0000:0000:0000:0001").toString());
        assertEquals("::1:0", fromString("0000:0000:0000:0000:0000:0000:0001:0000").toString());
        assertEquals("1::1:0:0:0", fromString("0001:0000:0000:0000:0001:0000:0000:0000").toString());
        assertEquals("::ffff", fromString("0000:0000:0000:0000:0000:0000:0000:ffff").toString());
        assertEquals("ffff::", fromString("ffff:0000:0000:0000:0000:0000:0000:0000").toString());
        assertEquals("2001:db8:85a3::8a2e:370:7334", fromString("2001:0db8:85a3:0000:0000:8a2e:0370:7334").toString());
    }

    @Test
    public void parseSomeRealAddressesShortNotation()
    {
        assertEquals("::1", fromString("::1").toString());
        assertEquals("::1:0", fromString("::1:0").toString());
        assertEquals("1::1:0:0:0", fromString("1::1:0:0:0").toString());
        assertEquals("::ffff", fromString("::ffff").toString());
        assertEquals("ffff::", fromString("ffff::").toString());
        assertEquals("2001:db8:85a3::8a2e:370:7334", fromString("2001:db8:85a3::8a2e:370:7334").toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseInvalid_1()
    {
        fromString(":");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseInvalid_2()
    {
        fromString(":a");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseInvalidTooShort_1()
    {
        fromString("a:");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseInvalidTooShort_2()
    {
        fromString("a:a:");
    }

    @Test(expected = IllegalArgumentException.class)
    public void parseInvalidTooLong()
    {
        fromString("a:a:a:a:a:a:a:a:a:a:a:a");
    }

    @Test
    public void constructFromInet6Address() throws UnknownHostException
    {
        final InetAddress inetAddress = Inet6Address.getByName("2001:0db8:85a3:0000:0000:8a2e:0370:7334");
        assertEquals("2001:db8:85a3::8a2e:370:7334", fromInetAddress(inetAddress).toString());
    }

    @Test
    public void convertToInet6Address() throws UnknownHostException
    {
        final InetAddress inetAddress = Inet6Address.getByName("2001:0db8:85a3:0000:0000:8a2e:0370:7334");
        assertEquals(inetAddress, fromString("2001:0db8:85a3:0000:0000:8a2e:0370:7334").toInetAddress());
    }

    @Test
    public void toStringCanBeUsedInFromStringAndViceVersa()
    {
        final int nTests = 10000;
        final Random rg = new Random();

        for (int i = 0; i < nTests; i++)
        {
            final IPv6Address address = new IPv6Address(rg.nextLong(), rg.nextLong());
            assertEquals(address, fromString(address.toString()));
        }
    }

    @Test
    public void addition()
    {
        assertEquals(fromString("::2"), fromString("::1").add(1));
        assertEquals(fromString("::1:0:0:0"), fromString("::ffff:ffff:ffff").add(1));
        assertEquals(fromString("::1:0:0:0:0"), fromString("::ffff:ffff:ffff:ffff").add(1));
        assertEquals(fromString("::1:0:0:0:1"), fromString("::ffff:ffff:ffff:ffff").add(2));
        assertEquals(fromString("::8000:0:0:0"), fromString("::7fff:ffff:ffff:ffff").add(1));
        assertEquals(fromString("::").add(Integer.MAX_VALUE).add(Integer.MAX_VALUE), fromString("::").add(Integer.MAX_VALUE).add(
                Integer.MAX_VALUE));
    }

    @Test
    public void additionOverflow()
    {
        assertEquals(fromString("::"), fromString("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff").add(1));
    }

    @Test
    public void subtraction()
    {
        assertEquals(fromString("::1"), fromString("::2").subtract(1));
        assertEquals(fromString("::ffff:ffff:ffff:ffff"), fromString("::0001:0:0:0:0").subtract(1));
        assertEquals(fromString("::ffff:ffff:ffff:fffe"), fromString("::0001:0:0:0:0").subtract(2));
        assertEquals(fromString("::7fff:ffff:ffff:ffff"), fromString("::8000:0:0:0").subtract(1));
        assertEquals(fromString("::").subtract(Integer.MAX_VALUE).subtract(Integer.MAX_VALUE), fromString("::").subtract(
                Integer.MAX_VALUE).subtract(Integer.MAX_VALUE));
    }

    @Test
    public void subtractionVersusAdditionWithRandomAddresses()
    {
        final Random random = new Random();
        final int randomInt = random.nextInt();
        final IPv6Address randomAddress = new IPv6Address(random.nextLong(), random.nextLong());
        assertEquals(randomAddress, randomAddress.add(randomInt).subtract(randomInt));
    }

    @Test
    public void subtractionVersusAdditionCornerCases()
    {
        final Random random = new Random();
        final IPv6Address randomAddress = new IPv6Address(random.nextLong(), random.nextLong());
        assertEquals(randomAddress, randomAddress.add(Integer.MAX_VALUE).subtract(Integer.MAX_VALUE));
        assertEquals(randomAddress, randomAddress.add(Integer.MIN_VALUE).subtract(Integer.MIN_VALUE));
    }

    @Test
    public void subtractionUnderflow()
    {
        assertEquals(fromString("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff"), fromString("::").subtract(1));
    }

    @Test
    public void compare()
    {
        assertTrue(0 == fromString("::").compareTo(fromString("::")));
        assertTrue(0 > fromString("::").compareTo(fromString("::1")));
        assertTrue(0 < fromString("::1").compareTo(fromString("::")));

        assertTrue(0 > fromString("::").compareTo(fromString("::ffff:ffff:ffff:ffff")));
        assertTrue(0 > fromString("::efff:ffff:ffff:ffff").compareTo(fromString("::ffff:ffff:ffff:ffff")));
        assertTrue(0 > fromString("efff:ffff:ffff:ffff:0:1:2:3").compareTo(fromString("ffff:ffff:ffff:ffff:4:5:6:7")));
    }

    @Test
    public void maskWithPrefixLength()
    {
        assertEquals(fromString("2001:0db8:85a3:0000:0000:8a2e:0370:7334"),
                fromString("2001:0db8:85a3:0000:0000:8a2e:0370:7334").maskWithNetworkMask(new IPv6NetworkMask(128)));
        assertEquals(fromString("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ff00"),
                fromString("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff").maskWithNetworkMask(new IPv6NetworkMask(120)));
        assertEquals(fromString("2001:0db8:85a3:0000:0000:8a2e:0370:7300"),
                fromString("2001:0db8:85a3:0000:0000:8a2e:0370:7334").maskWithNetworkMask(new IPv6NetworkMask(120)));
        assertEquals(fromString("2001:0db8:85a3::"),
                fromString("2001:0db8:85a3:0000:0000:8a2e:0370:7334").maskWithNetworkMask(new IPv6NetworkMask(64)));
        assertEquals(fromString("2000::"),
                fromString("2001:0db8:85a3:0000:0000:8a2e:0370:7334").maskWithNetworkMask(new IPv6NetworkMask(15)));
        assertEquals(fromString("8000::"),
                fromString("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff").maskWithNetworkMask(new IPv6NetworkMask(1)));
    }

    @Test
    public void maximumAddressWithPrefixLength()
    {
        assertEquals(fromString("2001:0db8:85a3:0000:0000:8a2e:0370:7334"),
                fromString("2001:0db8:85a3:0000:0000:8a2e:0370:7334").maximumAddressWithNetworkMask(new IPv6NetworkMask(128)));
        assertEquals(fromString("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff"),
                fromString("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ff00").maximumAddressWithNetworkMask(new IPv6NetworkMask(120)));
        assertEquals(fromString("2001:0db8:85a3:0000:0000:8a2e:0370:73ff"),
                fromString("2001:0db8:85a3:0000:0000:8a2e:0370:7300").maximumAddressWithNetworkMask(new IPv6NetworkMask(120)));
        assertEquals(fromString("2001:0db8:85a3:0000:ffff:ffff:ffff:ffff"),
                fromString("2001:0db8:85a3:0000:0000:8a2e:0370:7334").maximumAddressWithNetworkMask(new IPv6NetworkMask(64)));
        assertEquals(fromString("ffff:ffff:ffff:ffff:ffff:ffff:ffff:ffff"),
                fromString("8000::").maximumAddressWithNetworkMask(new IPv6NetworkMask(1)));
        assertEquals(fromString("7fff:ffff:ffff:ffff:ffff:ffff:ffff:ffff"),
                fromString("7fff::").maximumAddressWithNetworkMask(new IPv6NetworkMask(1)));
    }

    @Test
    public void numberOfTrailingOnes()
    {
        assertEquals(0, fromString("::").numberOfTrailingOnes());
        assertEquals(1, fromString("::1").numberOfTrailingOnes());
        assertEquals(4, fromString("::f").numberOfTrailingOnes());

        final IPv6Address addressWithLowBitsEqualToLongMaxValue = fromString("::7fff:ffff:ffff:ffff");
        assertEquals(Long.MAX_VALUE, addressWithLowBitsEqualToLongMaxValue.getLowBits());
        assertEquals(63, addressWithLowBitsEqualToLongMaxValue.numberOfTrailingOnes());
    }

    @Test
    public void numberOfLeadingOnes()
    {
        assertEquals(0, fromString("::").numberOfLeadingOnes());
        assertEquals(1, fromString("8000::").numberOfLeadingOnes());
        assertEquals(4, fromString("f000::").numberOfLeadingOnes());
        assertEquals(4, fromString("f000::f").numberOfLeadingOnes());
        assertEquals(65, fromString("ffff:ffff:ffff:ffff:8000::f").numberOfLeadingOnes());
    }

    @Test
    public void numberOfTrailingZeroes()
    {
        assertEquals(128, fromString("::").numberOfTrailingZeroes());
        assertEquals(127, fromString("8000::").numberOfTrailingZeroes());
        assertEquals(124, fromString("f000::").numberOfTrailingZeroes());
        assertEquals(0, fromString("f000::f").numberOfTrailingZeroes());
        assertEquals(63, fromString("ffff:ffff:ffff:ffff:8000::").numberOfTrailingZeroes());
    }

    @Test
    public void numberOfLeadingZeroes()
    {
        assertEquals(128, fromString("::").numberOfLeadingZeroes());
        assertEquals(0, fromString("8000::").numberOfLeadingZeroes());
        assertEquals(124, fromString("::f").numberOfLeadingZeroes());
        assertEquals(63, fromString("::1:ffff:ffff:ffff:ffff").numberOfLeadingZeroes());
    }

}
