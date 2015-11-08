# v0.17-SNAPSHOT

  * core types are now java.io.Serializable, see Issue #19
  
# v0.16

  * Javadoc fix, see Issue #11
  * IPv6Address.setBit() has a bug, see Issue #14
  * IllegalArgumentException when parsing IPv6 address with scope_id, see Issue #15
  * IPv6NetworkMask.asAddress() returns wrong value if prefixLength is 0, see Issue #16
  * IPv6AddressRange.toSubnets() fix

# v0.15

  * new feature: deaggregate IPv6AddressRange in subnets, see Issue #9

# v0.14

  * IPv6AddressRange.size() method added, see Issue #8
  * new feature: split IPv6Network in smaller subnets, see Issue #10

# v0.13

  * bugfix for Issue #7

# v0.12

  * added IPv6Address.isMulticast method
  * added IPv6Address.isLinkLocal method
  * added IPv6Address.isSiteLocal method
  * added IPv6Address.isIPv4Mapped method
  * support IPv4-Mapped addresses in toString and fromString (e.g. ::ffff:192.168.0.1)

# v0.11

  * construct IPv6Address from byte array
  * convert IPv6Address to byte array
  * construct IPv6Address from two longs (like the internal representation)
  * in toString: use shorthand notation for zeroes on the longest run of zeroes, in stead of on the first run of zeroes

# v0.10

  * allow network with zero prefix length (e.g. ::/0)