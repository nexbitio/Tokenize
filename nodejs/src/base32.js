/**
 * Tokenize, universal and secure token generator for authentication
 * Copyright (C) 2019 Bowser65
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
