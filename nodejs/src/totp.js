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

class TOTP {
  /**
   * Used MFA tokens, mapped per secret key
   * @type {object}
   * @private
   */
  _usedMfa = {}

  /**
   * Validates a TOTP code. Uses Google Authenticator standards, and doesn't allow use of the same code twice.
   * @param {String} token User provided token
   * @param {String} secret User's secret mfa key
   * @return {Boolean} If the code is valid or not
   */
  validate (token, secret) {
    // Maybe in the future we'll allow custom configurations.
    // STEP 1: Validate args
    const tokenInt = parseInt(token, 10)
    if (!secret || !token || token.length !== 6 || isNaN(tokenInt)) return false

    // STEP 2: Compute HOTP shit
    const counter = Math.floor((Date.now() / 1000) / 30)
    console.log(counter)

    // STEP 3: Compare
    return false
  }
}

export default TOTP
