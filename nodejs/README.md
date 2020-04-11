# Tokenize for Node.js

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
```js
import Tokenize from 'js-tokenize'
import OTP from 'js-tokenize/otp'

const tokenize = new Tokenize('Very strong and secure secret')

console.log(tokenize.generate('account_id')) // xxxxxxxx.xxxxxxxxxxx.xxxxxxxxx
console.log(tokenize.generate('account_id', 'prefix')) // prefix.xxxxxxxx.xxxxxxxxxxx.xxxxxxxxx

// Returns the account, or null if the token is invalid
console.log(tokenize.validate('xxxxxxxx.xxxxxxxxxxx.xxxxxxxxx', () => ({ tokensValidSince: 0 })))

// Get an OTP key
console.log(OTP.generateKey()) // You just need to save the base32 key in database

// Validate an OTP code
console.log(OTP.validateTotp("013370", "xxxxxxxxxxxxxxxx"))
console.log(OTP.validateHotp("013370", "xxxxxxxxxxxxxxxx", 1))
```

## TODO
Rewrite using TypeScript
Make it available to the web

## License
Like all Tokenize implementation, this implementation is released under the BSD-3-Clause license.
