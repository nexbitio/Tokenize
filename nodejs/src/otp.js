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

import { createHmac, randomBytes } from 'crypto'
import Base32 from './base32'

/**
 * @class OTP
 * @author Bowser65
 * @since 27/07/19
 */
class OTP {
  /**
   * Set used in key generation
   * @type {String}
   * @private
   */
  _set = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz!@#$%^&*()<>?/[]{},.:;'

  /**
   * Used MFA tokens, mapped per secret key
   * @type {object}
   * @private
   */
  _usedMfa = {}

  validateHotp (token, secret, counter) {
    if (!this._usedMfa[secret]) this._usedMfa[secret] = []
    if (this._usedMfa[secret].includes(token)) return false

    const counterInt = parseInt(counter, 10) || 0
    if (!secret || !token || token.length !== 6 || isNaN(parseInt(token, 10))) return false

    if (this._computeHotp(secret, counterInt) === token) {
      this._usedMfa[secret].push(token)
      return true
    }
    return false
  }

  validateTotp (token, secret) {
    return this.validateHotp(token, secret, Math.floor(Date.now() / 30 / 1000))
  }

  generateKey (name = 'Secret Key', issuer = null, hotp = false) {
    const bytes = randomBytes(32)
    const string = Array(32)
      .fill(i => this._set[Math.floor(bytes[i] / 255.0 * (this._set.length - 1))])
      .map((f, i) => f(i))
      .join('')

    const key = {
      raw: string,
      base32: Base32.encode(string)
    }

    return Object.defineProperty(key, {
      value: 'google_url',
      writable: false,
      get: () => `otpauth://${hotp ? 'h' : 't'}otp/${name}?secret=${this.base32}${issuer ? `&issuer=${issuer}` : ''}`
    })
  }

  _computeHotp (secret, counter) {
    // Compute digest
    secret = Base32.decode(secret)

    let tmp = counter
    const buf = Buffer.alloc(8)
    for (let i = 0; i < 8; i++) {
      buf[7 - i] = tmp & 0xff
      tmp = tmp >> 8
    }

    const digest = createHmac('sha1', secret).update(buf).digest()

    // Do shit
    const offset = digest[digest.length - 1] & 0xf
    let code = (digest[offset] & 0x7f) << 24 |
      (digest[offset + 1] & 0xff) << 16 |
      (digest[offset + 2] & 0xff) << 8 |
      (digest[offset + 3] & 0xff)

    code = new Array(7).join('0') + code.toString(10)
    return code.substr(-6)
  }
}

export default OTP
