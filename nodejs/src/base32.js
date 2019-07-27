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
 * @author Isaac "agnoster" Wolkerstorfer
 * @see https://github.com/agnoster/base32-js
 * @licence MIT
 */
class Base32 {
  static alphabet = '0123456789abcdefghjkmnpqrtuvwxyz'
  static alias = { o: 0, i: 1, l: 1, s: 5 }
  static _table = null

  static encode (data) {
    let skip = 0
    let bits = 0
    let encoded = ''

    for (let i = 0; i < data.length;) {
      const byte = typeof data[i] === 'string' ? data[i].charCodeAt(0) : data[i]

      if (skip < 0) bits |= (byte >> (-skip))
      else bits = (byte << skip) & 248

      if (skip > 3) {
        skip -= 8
        i += 1
      } else {
        if (skip < 4) {
          encoded += Base32.alphabet[bits >> 3]
          skip += 5
        }
      }
    }

    encoded += skip < 0 ? Base32.alphabet[bits >> 3] : ''
    return encoded
  }

  static decode (data) {
    let skip = 0
    let byte = 0
    let decoded = ''

    for (let i = 0; i < data.length; i++) {
      const char = (typeof data[i] === 'string' ? String.fromCharCode(data[i]) : data[i]).toLowerCase()
      let val = Base32._lookup()[char]
      if (typeof val === 'undefined') return

      val <<= 3
      byte |= val >>> skip
      skip += 5
      if (skip >= 8) {
        decoded += String.fromCharCode(byte)
        skip -= 8
        if (skip > 0) byte = (val << (5 - skip)) & 255
        else byte = 0
      }
    }

    decoded += skip < 0 ? Base32.alphabet[byte >> 3] : ''
    return decoded
  }

  static _lookup () {
    if (!Base32._table) {
      Base32._table = {}

      for (let i = 0; i < Base32.alphabet.length; i++) {
        Base32._table[Base32.alphabet[i]] = i
      }

      for (const key in Base32.alias) {
        if (!Base32.alias.hasOwnProperty(key)) continue
        Base32._table[key] = Base32._table[Base32.alias[key].toString()]
      }
    }

    return Base32._table
  }
}

export default Base32
