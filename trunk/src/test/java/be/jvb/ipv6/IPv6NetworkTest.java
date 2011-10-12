package be.jvb.ipv6;

import org.junit.Test;

import java.util.Random;

import static be.jvb.ipv6.IPv6Address.fromString;
import static org.junit.Assert.assertEquals;

/**
 * @author Jan Van Besien
 */
public class IPv6NetworkTest
{
    @Test
    public void constructFromFirstAndLastAddress()
    {
        assertEquals(new IPv6Network(fromString("::"), 126), new IPv6Network(fromString("::1"), fromString("::2")));
        assertEquals(new IPv6Network(fromString("a:b::"), 44), new IPv6Network(fromString("a:b:c::1:1"), fromString("a:b::f:f")));
    }

    @Test
    public void stringRepresentation()
    {
        assertEquals("::/126", new IPv6Network(fromString("::"), 126).toString());
        assertEquals("a:b:c:d::/64", new IPv6Network(fromString("a:b:c:d::"), 64).toString());
    }

    @Test
    public void toStringCanBeUsedInFromStringAndViceVersa()
    {
        final int nTests = 10000;
        final Random rg = new Random();

        for (int i = 0; i < nTests; i++)
        {
            final IPv6Network network = new IPv6Network(new IPv6Address(rg.nextLong(), rg.nextLong()), rg.nextInt(128) + 1);
            assertEquals(network, IPv6Network.fromString(network.toString()));
        }
    }

}
