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
   * @param {String} secret MFA secret key ot the user
   * @param {Number} counter If not equals to -1 Tokenize will perform a HTOP check, TOTP check otherwise
   * @return {String} The upgraded token, or null if the MFA code is invalid
   */
  upgrade (token, mfa, secret, counter = -1) {
    if (
      !token.startsWith('mfa.') && (
        (counter === -1 && this._otp.validateTotp(mfa, secret)) ||
        (counter !== -1 && this._otp.validateHotp(mfa, secret, counter))
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
   * @param {Function} accountFetcher The account fetcher function. Must accept as first param the account id,
   *                                  and return an object with 'tokensValidSince' and 'hasMfa' fields.
   * @return {Boolean} If the token is valid
   */
  validate (token, accountFetcher) {
    const isMfa = token.startsWith('mfa.')
    const splitted = token.replace(/^mfa\./, '').split('.')
    if (splitted.length !== 3) return false

    const signatureStr = `${isMfa ? 'mfa.' : ''}${splitted[0]}.${splitted[1]}`
    if (splitted[2] !== this._computeHmac(signatureStr)) return false

    const accountId = Buffer.from(splitted[0], 'base64').toString('utf8')
    const genTime = Buffer.from(splitted[1], 'base64').toString('utf8')
    const accountDetails = accountFetcher(accountId)

    return genTime > accountDetails.tokensValidSince && isMfa === accountDetails.hasMfa
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
