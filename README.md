# Tokenize
[![Build Status](https://img.shields.io/travis/Bowser65/Tokenize/master.svg?style=flat-square&logo=travis)](https://travis-ci.org/Bowser65/Tokenize)
[![License](https://img.shields.io/github/license/Bowser65/Tokenize.svg?style=flat-square)](https://github.com/learndesk/mailcheck/blob/master/LICENSE)
[![Donate](https://img.shields.io/badge/donate-Patreon-F96854.svg?style=flat-square)](https://www.patreon.com/Bowser65)

An universal and secure token generator for authentication. Available in multiple languages, supports 2FA authentication
flow.

Tokenize is proudly built without any dependency 🎉

## Format
Tokenize token format (TTF, not to be confused with TrueType Font :^)) is inspired from the [JWT](https://jwt.io/)
standard. It's capable to generate multiple valid tokens, but unlike JWT tokens invalidate them all at once.

One downside of TTF is that you need to fetch database each time to validate a token, while JWT aims to provide a
stateful token. TTF only includes the account ID, the token generation time and if the token has been generated after
a successful 2FA flow or not.

### What does it look like
```
OTQ3NjI0OTI5MjM3NDgzNTI.MTc4MzkxODI.dGhpcyBpcyBhIHZlcnkgc2VjdXJlIHNpZ25hdHVyZSB3ZHlt
----------------------- ----------- -------------------------------------------------
      Account ID         Gen. Date              HMAC SHA256 Signature

 - All parts are base64 encoded
 - MFA tokens are simply prefixed with "mfa."
 - The signature is based on everything that preceeds it (including "mfa.")
```

### How to use it
**Note**: While it's simple to make an universal token format, an universal lib format is a bit more hard, so
implementation may vary a bit depending on the language. We use NodeJS in the following examples, but there are
examples written for each language in their repository.

#### Generation
```js
import Tokenize from 'js-tokenize'
const tokenize = new Tokenize('Very strong and secure secret')
// Generate a non-mfa token
console.log(tokenize.generate('account_id'))
// Generate a mfa token (Will check the validity of the 2FA code automatically)
// Returns null if the upgrade failed (aka invalid mfa code)
console.log(tokenize.upgrade('non-mfa token', 'mfa_code', 'mfa_key'))
```

#### Validation
The object returned by our function must have "tokensValidSince" and "hasMfa" fields. Those are used to validate the
gen. date part and the mfa. prefix of the token

```js
import Tokenize from 'js-tokenize'
const tokenize = new Tokenize('Very strong and secure secret')

// Fetch account from db
// "tokensValidSince" value should be stored in db and computed using tokenize.currentTokenTime()
// the field should be updated when the password changes or when MFA is enabled/disabled
const fetchAccount = () => ({ tokensValidSince: 0, hasMfa: false })

// Returns a boolean, true if valid false otherwise
console.log(tokenize.validate(token, fetchAccount))
```

## Acknowledgements
Thanks to [AlexFlipnote](https://github.com/AlexFlipnote) for maintaining the Python version of tokenize <3
