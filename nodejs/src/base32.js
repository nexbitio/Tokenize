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

/**
 * NOTE: This class is PARTIALLY compliant with RFC 4648 and only implements required Base32 operations for OTP secrets.
 * @class Base32
 * @author Bowser65
 * @since 27/07/19
 */
class Base32 {
  static charMap = {
    /* eslint-disable object-property-newline */
    A: 0, B: 1, C: 2, D: 3, E: 4, F: 5, G: 6, H: 7, I: 8,
    J: 9, K: 10, L: 11, M: 12, N: 13, O: 14, P: 15, Q: 16,
    R: 17, S: 18, T: 19, U: 20, V: 21, W: 22, X: 23, Y: 24,
    Z: 25, 2: 26, 3: 27, 4: 28, 5: 29, 6: 30, 7: 31
    /* eslint-enable object-property-newline */
  }

  static alphabet = Object.keys(Base32.charMap)

  // Inspired from https://github.com/emn178/hi-base32.
  static decode (base32) {
    let i, b1, b2, b3, b4, b5, b6, b7, b8
    let str = ''
    const count = base32.length >> 3 << 3

    for (i = 0; i < count;) {
      [ b1, b2, b3, b4, b5, b6, b7, b8 ] = Array(8).fill(null).map(() => Base32.charMap[base32.charAt(i++)])
      str += String.fromCharCode((b1 << 3 | b2 >>> 2) & 255) +
        String.fromCharCode((b2 << 6 | b3 << 1 | b4 >>> 4) & 255) +
        String.fromCharCode((b4 << 4 | b5 >>> 1) & 255) +
        String.fromCharCode((b5 << 7 | b6 << 2 | b7 >>> 3) & 255) +
        String.fromCharCode((b7 << 5 | b8) & 255)
    }

    const remain = base32.length - count
    /* eslint-disable no-duplicate-case */
    // noinspection JSDuplicateCaseLabel
    switch (remain) {
      case 2:
      case 4:
      case 5:
      case 7:
        b1 = Base32.charMap[base32.charAt(i++)]
        b2 = Base32.charMap[base32.charAt(i++)]
        str += String.fromCharCode((b1 << 3 | b2 >>> 2) & 255)
        break
      case 4:
      case 5:
      case 7:
        b3 = Base32.charMap[base32.charAt(i++)]
        b4 = Base32.charMap[base32.charAt(i++)]
        str += String.fromCharCode((b2 << 6 | b3 << 1 | b4 >>> 4) & 255)
        break
      case 5:
      case 7:
        b5 = Base32.charMap[base32.charAt(i++)]
        str += String.fromCharCode((b4 << 4 | b5 >>> 1) & 255)
        break
      case 7:
        b6 = Base32.charMap[base32.charAt(i++)]
        b7 = Base32.charMap[base32.charAt(i++)]
        str += String.fromCharCode((b5 << 7 | b6 << 2 | b7 >>> 3) & 255)
        break
    }
    /* eslint-enable no-duplicate-case */
    return str
  }
}

export default Base32
