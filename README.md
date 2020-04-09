# Tokenize
[![License](https://img.shields.io/github/license/Bowser65/Tokenize.svg?style=flat-square)](https://github.com/Bowser65/Tokenize/blob/master/LICENSE)

An universal and secure token generator for authentication. Available in multiple languages, supports 2FA authentication
flow.

## Supported languages
You can get installation procedure for each language in the README.md of their folder.

| Language | Minimum language version | Token generation & validation | OTP validation |
|---|---|:---:|:---:|
| NodeJS | 10 | x | x |
| Java | 1.8 | x | Soon™️ |
| C++ | 17 | Soon™️ | Soon™️ |

Want to see more languages supported? File an issue, or if you're even more epic send a PR!

## Format
Tokenize token format (TTF, not to be confused with TrueType Font :^)) is inspired from the [JWT](https://jwt.io/)
standard. It's capable to generate multiple valid tokens, and unlike with JWT tokens, you can invalidate all tokens for
an account all at once.

### Limitations
 - You can't invalidate a specific token
Yes you can still do it within your app, but that's not something Tokenize does support.
 - You must ping the database everytime
This is due to the invalidation part, however you can still build a cache and serve cached results to Tokenize.
 - One TOTP code is only valid once
This is more of a security feature than a limitation. This behaviour is in place to prevent replay attacks with the same
TOTP code.

### What does it look like
```
xxxxxx.OTQ3NjI0OTI5MjM3NDgzNTI.MTc4MzkxODI.dGhpcyBpcyBhIHZlcnkgc2VjdXJlIHNpZ25hdHVyZSB3ZHlt
------ ----------------------- ----------- -------------------------------------------------
Prefix       Account ID         Gen. Date              HMAC SHA256 Signature

 - All parts are base64 encoded, except the prefix.
 - Prefix can contain arbitrary data. You can pass virtually anything in this.
 - The signature is based on everything that preceeds it and a prefix "TTF.{version_number}".
```

### How to use it
**Note**: The following examples uses the NodeJS lib, as I (Bowser65) am more comfortable with this language. There are
language-specific examples for each Tokenize implementation in their respective directory.

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
