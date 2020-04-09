# Tokenize for Node.js

# NOTE:
This implementation is out of date. You shouldn't use it in a production application.

---------------------

## Installation
```
With PNPM:
pnpm i js-tokenize

With Yarn:
yarn add js-tokenize

With NPM:
npm i js-tokenize
```

## How to use it
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

## TODO
Update impl
Rewrite using TypeScript

# License
Like all Tokenize implementation, this implementation is released under the BSD-3-Clause license.
