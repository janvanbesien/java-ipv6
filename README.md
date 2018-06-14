## Java IPv6 Library (v0.16)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.googlecode.java-ipv6/java-ipv6/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.googlecode.java-ipv6/java-ipv6)
[![Javadoc](https://javadoc-emblem.rhcloud.com/doc/com.googlecode.java-ipv6/java-ipv6/badge.svg)](http://www.javadoc.io/doc/com.googlecode.java-ipv6/java-ipv6)

java-ipv6 is a Java library for IPv6 related concepts such as IPv6 addresses, network masks, address pools, etc.

### IPv6Address

IPv6Address represents an IPv6 address.

```Java
    final IPv6Address iPv6Address = IPv6Address.fromString("fe80::226:2dff:fefa:cd1f");
    final IPv6Address iPv4MappedIPv6Address = IPv6Address.fromString("::ffff:192.168.0.1");
```

Internally, the IPv6Address uses two long values to store the IPv6 address.

IPv6Address can be used to make simple calculations on IPv6 addresses, such as addition and subtraction.

```Java
    final IPv6Address iPv6Address = IPv6Address.fromString("fe80::226:2dff:fefa:cd1f");
    final IPv6Address next = iPv6Address.add(1);
    final IPv6Address previous = iPv6Address.subtract(1);
    System.out.println(next.toString()); // prints fe80::226:2dff:fefa:cd20
    System.out.println(previous.toString()); // prints fe80::226:2dff:fefa:cd1e
```

### IPv6AddressRange

IPv6AddressRange represents a continuous range of consecutive IPv6 addresses.

```Java
    final IPv6AddressRange range = IPv6AddressRange.fromFirstAndLast(IPv6Address.fromString("fe80::226:2dff:fefa:cd1f"),
                                                                     IPv6Address.fromString("fe80::226:2dff:fefa:ffff"));
    System.out.println(range.contains(IPv6Address.fromString("fe80::226:2dff:fefa:dcba"))); // prints true
```

IPv6AddressRange contains methods to iterate over all the addresses in the range. Ranges can be compared with other ranges by 
checking if they overlap or if one range contains the other range.

### IPv6Network

An IPv6Network is a range (extends IPv6AddressRange) that can be expressed as a network address and a prefix length.

```Java
    final IPv6AddressRange range = IPv6AddressRange.fromFirstAndLast(IPv6Address.fromString("fe80::226:2dff:fefa:0"),
                                                                     IPv6Address.fromString("fe80::226:2dff:fefa:ffff"));
    final IPv6Network network = IPv6Network.fromString("fe80::226:2dff:fefa:0/112");
    System.out.println(range.equals(network)); // prints true
```

Note that every IPv6Network is also an IPv6AddressRange, but not all IPv6AddressRanges are valid IPv6Networks. It is possible to 
construct an IPv6Network from a range in between a first address and a last address, but than the smallest possible IPv6Network 
(i.e. the one with the longest prefix length) which contains the given first and last addresses will be constructed. The resulting 
network thus contains the same or more addresses as the requested range.

IPv6Network can be used for IPv6 network address calculations.

```Java
    final IPv6Network strangeNetwork = IPv6Network.fromString("fe80::226:2dff:fefa:cd1f/43");
    
    System.out.println(strangeNetwork.getFirst()); // prints fe80::
    System.out.println(strangeNetwork.getLast()); // prints fe80:0:1f:ffff:ffff:ffff:ffff:ffff
    System.out.println(strangeNetwork.getNetmask().asPrefixLength()); // prints 43
    System.out.println(strangeNetwork.getNetmask().asAddress()); // prints ffff:ffff:ffe0::
```

A particularly interesting calculation is to split an IPv6Network in smaller subnets.

```Java
    final IPv6Network network = IPv6Network.fromString("1:2:3:4:5:6:7:0/120");
    
    Iterator<IPv6Network> splits = network.split(IPv6NetworkMask.fromPrefixLength(124));
    while (splits.hasNext())
        System.out.println(splits.next());
    
    // prints 1:2:3:4:5:6:7:0/124, 1:2:3:4:5:6:7:10/124, 1:2:3:4:5:6:7:20/124, ... until 1:2:3:4:5:6:7:f0/124 (16 in total)
```

An IPv6AddressRange (which doesn't necessarily align with a single network) can be converted to a set of networks (subnets). This 
will give you the minimal set of non overlapping, consecutive networks that define the same set of IPv6Addresses as the original 
range. Here is an example:

```Java
    IPv6AddressRange range = IPv6AddressRange.fromFirstAndLast(IPv6Address.fromString("::1:ffcc"),
                                                               IPv6Address.fromString("::2:0"));
    
    Iterator<IPv6Network> subnetsIterator = range.toSubnets();
    while (subnetsIterator.hasNext())
        System.out.println(subnetsIterator.next());
    
    // prints the networks ::1:ffcc/126, ::1:ffd0/124, ::1:ffe0/123 and ::2:0/128
```

### IPv6NetworkMask

An IPv6NetworkMask is merely a wrapper around the integer in the interval 0 (exclusive) until 128 (inclusive) which represents the
prefix length of an IPv6Network.

```Java
    final IPv6NetworkMask slash40Network = IPv6NetworkMask.fromPrefixLength(40);
    System.out.println(slash40Network.asAddress()); // prints ffff:ffff:ff00::
    System.out.println(slash40Network.asPrefixLength()); // prints 40
    
    final IPv6NetworkMask slash40NetworkConstructedFromAddressNotation = IPv6NetworkMask.fromAddress(IPv6Address.fromString("ffff:ffff:ff00::"));
    System.out.println(slash40Network.equals(slash40NetworkConstructedFromAddressNotation)); // prints true
    
    final IPv6NetworkMask invalidNetworkMask = IPv6NetworkMask.fromAddress(IPv6Address.fromString("0fff::")); // fails
```

It can be used for some more advanced calculations on IPv6Address, such as masking addresses with a prefix length.

```Java
    final IPv6Address iPv6Address = IPv6Address.fromString("fe80::226:2dff:fefa:cd1f");
    
    final IPv6Address masked = iPv6Address.maskWithNetworkMask(IPv6NetworkMask.fromPrefixLength(40));
    System.out.println(masked.toString()); // prints fe80::
    
    final IPv6Address maximum = iPv6Address.maximumAddressWithNetworkMask(IPv6NetworkMask.fromPrefixLength(40));
    System.out.println(maximum.toString()); // prints fe80:0:ff:ffff:ffff:ffff:ffff:ffff
```

### IPv6AddressPool

An IPv6AddressPool is like a range (extends IPv6AddressRange) of which certain subnets are "allocated" and others are "free".

```Java
    final IPv6AddressPool pool = IPv6AddressPool.fromRangeAndSubnet(
        IPv6AddressRange.fromFirstAndLast(IPv6Address.fromString("fe80::226:2dff:fefa:0"),
                                          IPv6Address.fromString("fe80::226:2dff:fefa:ffff")),
        IPv6NetworkMask.fromPrefixLength(120));
    System.out.println(pool.isFree(IPv6Network.fromString("fe80::226:2dff:fefa:5ff/120"))); // prints true
    
    final IPv6AddressPool newPool = pool.allocate(IPv6Network.fromString("fe80::226:2dff:fefa:5ff/120"));
    System.out.println(newPool.isFree(IPv6Network.fromString("fe80::226:2dff:fefa:5ff/120"))); // prints false
```

### And Much More

Much more can be done with these types. Have a look at the javadoc and sources to get an idea of the possibilities. If you have 
additional functionality you would like to see in this library, do not hesitate to let us know.

## Download

Available via maven central.

    <dependency>
        <groupId>com.googlecode.java-ipv6</groupId>
        <artifactId>java-ipv6</artifactId>
        <version>0.17</version>
    </dependency>

## Prerequisites

This library requires Java 6.

## What's new

See WhatsNew.
