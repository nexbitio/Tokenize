# Tokenize for Node.js

## How to use it
**Note**: While it's simple to make an universal token format, an universal lib format is a bit more hard, so
implementation may vary a bit depending on the language. We use NodeJS in the following examples, but there are
examples written for each language in their repository.

### Generation
```js
import Tokenize from 'js-tokenize'
const tokenize = new Tokenize('Very strong and secure secret')
// Generate a non-mfa token
console.log(tokenize.generate('account_id'))
// Generate a mfa token (Will check the validity of the 2FA code automatically)
// Returns null if the upgrade failed (aka invalid mfa code)
console.log(tokenize.upgrade('non-mfa token', 'mfa_code', 'mfa_key'))
```

### Validation
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
