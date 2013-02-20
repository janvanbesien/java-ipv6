/*
 * Copyright 2013 Jan Van Besien
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.googlecode.ipv6;

import java.util.BitSet;

/**
 * This class contains some helpers for working with BitSets. These are generally not necessary in JDK7, since the BitSet.valueOf(long[])
 * method. However, for java-6 compatibility, we go this way.
 *
 * @author Jan Van Besien
 */
class BitSetHelpers
{
    static BitSet bitSetOf(long lowerBits, long upperBits)
    {
        final BitSet bitSet = new BitSet();
        convert(lowerBits, 0, bitSet);
        convert(upperBits, Long.SIZE, bitSet);
        return bitSet;
    }

    static void convert(long value, int bitSetOffset, BitSet bits)
    {
        int index = 0;
        while (value != 0L)
        {
            if (value % 2L != 0)
            {
                bits.set(bitSetOffset + index);
            }
            ++index;
            value = value >>> 1;
        }
    }

}
