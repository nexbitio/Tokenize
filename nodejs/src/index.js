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
   * First second of 2019, used to generate shorter tokens
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
  }

  /**
   * Generates a new token for a given account
   * @param {String} accountId The account id this token belongs to
   * @return {String}
   */
  generate (accountId) {
    return ''
  }

  /**
   * Upgrades a non-mfa token to a mfa token.
   * Validation of the mfa code is automatically handled by the library
   * @param {String} token Non-mfa token (NOTE: Validity won't be checked!)
   * @param {String} mfa User-provided 6 digit code
   * @param {String} key MFA secret key ot the user
   * @return {String} The upgraded token, or null if the MFA code is invalid
   */
  upgrade (token, mfa, key) {
    return ''
  }

  /**
   * Validates a token
   * @param {String} token The provided token
   * @param {Function} accountFetcher The account fetcher function. Must accept as first param the account id,
   *                                  and return an object with 'tokensValidSince' and 'hasMfa' fields.
   * @return {Boolean}
   */
  validate (token, accountFetcher) {
    return false
  }

  /**
   * Returns the current token time based on the Tokenize Epoch
   * @return {Number} Current token time
   */
  currentTokenTime () {
    return Math.floor((Date.now() - this.TOKENIZE_EPOCH) / 1000)
  }
}

export default Tokenize
