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

import { createHmac } from 'crypto'
import OTP from './otp'

/**
 * @class Tokenize
 * @author Bowser65
 * @since 27/07/19
 */
class Tokenize {
  /**
   * Tokenize Token Format version
   * @type {Number}
   * @const
   */
  VERSION = 1

  /**
   * First second of 2019, used to get shorter tokens
   * @type {Number}
   * @const
   */
  TOKENIZE_EPOCH = 1546300800000

  /**
   * Tokenize constructor
   * @param {String} secret Secret used to sign the tokens
   */
  constructor (secret) {
    this._secret = secret
    this._otp = new OTP()
  }

  /**
   * Generates a new token for a given account
   * @param {String} accountId The account id this token belongs to
   * @return {String}
   */
  generate (accountId) {
    const accountPart = Buffer.from(accountId).toString('base64')
    const timePart = Buffer.from(this.currentTokenTime().toString()).toString('base64')
    const token = `${accountPart}.${timePart}`.replace(/=/g, '')
    const signature = this._computeHmac(token)
    return `${token}.${signature}`
  }

  /**
   * Upgrades a non-mfa token to a mfa token.
   * Validation of the mfa code is automatically handled by the library. Tokenize doesn't allow usage of the same
   * token twice to enhance user's security (MITM or replay attacks)
   * @param {String} token Non-mfa token (NOTE: Validity won't be checked!)
   * @param {String} mfa User-provided 6 digit code
   * @param {String} secret MFA secret bound to the user
   * @param {Number} counter If null Tokenize will perform a TOTP check, HOTP check otherwise
   * @return {String} The upgraded token, or null if the MFA code is invalid
   */
  upgrade (token, mfa, secret, counter = null) {
    if (
      !token.startsWith('mfa.') && (
        (counter === null && this._otp.validateTotp(mfa, secret)) ||
        (counter !== null && this._otp.validateHotp(mfa, secret, counter))
      )
    ) {
      const parts = token.split('.')
      const upgraded = `mfa.${parts[0]}.${parts[1]}`
      const signature = this._computeHmac(upgraded)
      return `${upgraded}.${signature}`
    }
    return null
  }

  /**
   * Validates a token
   * @param {String} token The provided token
   * @param {Function} accountFetcher The function used to fetch the account. It'll receive the account id as a string
   *                                  and should return an object with 'tokensValidSince' and 'hasMfa' fields.
   *                                  It'll be returned if the token is valid.
   * @param {Boolean} ignoreMfa Whether or not MFA check should be performed. If true, only non-mfa tokens will be
   *                            accepted. Can be used to make "ticket tokens", where the user entered correct
   *                            credentials but didn't performed MFA check
   * @return {object|null} The account if the token is valid, null otherwise.
   */
  validate (token, accountFetcher, ignoreMfa) {
    const isMfa = token.startsWith('mfa.')
    const splitted = token.replace(/^mfa\./, '').split('.')
    if (splitted.length !== 3) return false

    const signatureStr = `${isMfa ? 'mfa.' : ''}${splitted[0]}.${splitted[1]}`
    if (splitted[2] !== this._computeHmac(signatureStr)) return false

    const accountId = Buffer.from(splitted[0], 'base64').toString('utf8')
    const genTime = Buffer.from(splitted[1], 'base64').toString('utf8')
    const accountDetails = accountFetcher(accountId)

    return genTime > accountDetails.tokensValidSince && (!ignoreMfa && isMfa) === accountDetails.hasMfa ? accountDetails : null
  }

  /**
   * Returns the current token time based on the Tokenize Epoch
   * @return {Number} Current token time
   */
  currentTokenTime () {
    return Math.floor((Date.now() - this.TOKENIZE_EPOCH) / 1000)
  }

  /**
   * Signs a string with the HMAC-SHA256 algorithm
   * @param {String} string string to sign
   * @return {String} Base64 digest without padding
   * @private
   */
  _computeHmac (string) {
    return createHmac('sha256', this._secret).update(`TTF.${this.VERSION}.${string}`).digest('base64').replace(/=/g, '')
  }
}

export default Tokenize
