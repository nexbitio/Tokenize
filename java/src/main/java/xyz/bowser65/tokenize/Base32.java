/*
 * Copyright (c) 2020 Bowser65, All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package xyz.bowser65.tokenize;

import java.util.Arrays;

class Base32 {
    static final char[] ALPHABET = {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
            'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '2', '3', '4', '5', '6', '7'
    };

    @SuppressWarnings("DuplicateExpressions")
    static byte[] decode(final String base32) {
        final int[] b = new int[8];
        final byte[] bytes = new byte[base32.length()];
        final int count = base32.length() >> 3 << 3;
        int byteIndex = 0, i = 0;

        for (; i < count; ) {
            final int limit = i + 8;
            for (; i < limit; i++) {
                b[i % 8] = indexOf(base32.charAt(i));
            }

            bytes[byteIndex++] = (byte) ((b[0] << 3 | b[1] >>> 2) & 255);
            bytes[byteIndex++] = (byte) ((b[1] << 6 | b[2] << 1 | b[3] >>> 4) & 255);
            bytes[byteIndex++] = (byte) ((b[3] << 4 | b[4] >>> 1) & 255);
            bytes[byteIndex++] = (byte) ((b[4] << 7 | b[5] << 2 | b[6] >>> 3) & 255);
            bytes[byteIndex++] = (byte) ((b[6] << 5 | b[7]) & 255);
        }

        final int remain = base32.length() - count;
        if (remain == 2 || remain == 4 || remain == 5 || remain == 7) {
            b[0] = indexOf(base32.charAt(i++));
            b[1] = indexOf(base32.charAt(i++));
            bytes[byteIndex++] = (byte) ((b[0] << 3 | b[1] >>> 2) & 255);
        }
        if (remain == 4 || remain == 5 || remain == 7) {
            b[2] = indexOf(base32.charAt(i++));
            b[3] = indexOf(base32.charAt(i++));
            bytes[byteIndex++] = (byte) ((b[1] << 6 | b[2] << 1 | b[3] >>> 4) & 255);
        }
        if (remain == 5 || remain == 7) {
            b[4] = indexOf(base32.charAt(i++));
            bytes[byteIndex++] = (byte) ((b[3] << 4 | b[4] >>> 1) & 255);
        }
        if (remain == 7) {
            b[5] = indexOf(base32.charAt(i++));
            b[6] = indexOf(base32.charAt(i));
            bytes[byteIndex] = (byte) ((b[4] << 7 | b[5] << 2 | b[6] >>> 3) & 255);
        }

        return Arrays.copyOf(bytes, byteIndex);
    }

    private static int indexOf(final char c) {
        for (int i = 0; i < ALPHABET.length; i++) {
            if (ALPHABET[i] == c) return i;
        }
        return -1;
    }
}
