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
