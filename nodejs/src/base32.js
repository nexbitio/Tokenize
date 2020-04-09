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
 * @class Base32
 * @author Bowser65
 * @since 27/07/19
 */
class Base32 {
  static alphabet = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ234567'

  static charmap = null

  static encode (data) {
    let encoded = ''
    let shift = 3
    let carry = 0
    let symbol
    let byte

    // encode each byte in buf
    for (let i = 0; i < data.length; i++) {
      byte = data[i]
      symbol = carry | (byte >> shift)
      encoded += Base32.alphabet[symbol & 0x1f]

      if (shift > 5) {
        shift -= 5
        symbol = byte >> shift
        encoded += Base32.alphabet[symbol & 0x1f]
      }

      shift = 5 - shift
      carry = byte << shift
      shift = 8 - shift
    }

    if (shift !== 3) encoded += Base32.alphabet[carry & 0x1f]
    return encoded
  }

  static decode (data) {
    Base32._charmap()
    const buf = []
    let shift = 8
    let carry = 0

    // decode string
    data.toUpperCase().split('').forEach((char) => {
      if (char === '') return
      const symbol = Base32.charmap[char] & 0xff

      shift -= 5
      if (shift > 0) {
        carry |= symbol << shift
      } else if (shift < 0) {
        buf.push(carry | (symbol >> -shift))
        shift += 8
        carry = (symbol << shift) & 0xff
      } else {
        buf.push(carry | symbol)
        shift = 8
        carry = 0
      }
    })

    if (shift !== 8 && carry !== 0) buf.push(carry)
    return Buffer.from(buf)
  }

  static _charmap () {
    if (!Base32.charmap) {
      const mappings = { 0: 14, 1: 8 }
      Base32.alphabet.split('').forEach((c, i) => { if (!(c in mappings)) mappings[c] = i })
      Base32.charmap = mappings
    }
  }
}

export default Base32
