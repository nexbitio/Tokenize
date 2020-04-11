# Tokenize
[![License](https://img.shields.io/github/license/Bowser65/Tokenize.svg?style=flat-square)](https://github.com/Bowser65/Tokenize/blob/master/LICENSE)

An universal and secure token generator for authentication. Available in multiple languages, supports 2FA authentication
flow.

## Supported languages
You can get installation procedure for each language in the README.md of their folder.

| Language | Minimum language version | Token generation | Token validation | OTP validation |
|---|---|---|:---:|:---:|
| NodeJS | 10 | x | x | x |
| Java | 1.8 | x | x | x |

Want to see more languages supported? File an issue, or if you're even more epic send a PR!

## Format
Tokenize token format (TTF, not to be confused with TrueType Font :^)) is inspired from the [JWT](https://jwt.io/)
standard. It's capable to generate multiple valid tokens, and unlike with JWT tokens, you can invalidate all tokens for
an account all at once.

### Limitations
 - You can't invalidate a specific token<br>
Yes you can still do it within your app, but that's not something Tokenize does support.
 - You must ping the database everytime<br>
This is due to the invalidation part, however you can still build a cache and serve cached results to Tokenize.
 - An OTP code is only valid once<br>
This is more of a security feature than a limitation. This behavior is in place to prevent replay attacks with the same
OTP code.

### What does it look like
```
xxxxxx.OTQ3NjI0OTI5MjM3NDgzNTI.MTc4MzkxODI.dGhpcyBpcyBhIHZlcnkgc2VjdXJlIHNpZ25hdHVyZSB3ZHlt
------ ----------------------- ----------- -------------------------------------------------
Prefix       Account ID         Gen. Date              HMAC SHA256 Signature

 - All parts are base64 encoded, except the prefix.
 - Prefix can contain arbitrary data. You can pass virtually anything in this.
 - The signature is based on everything that preceeds it and a prefix "TTF.{version_number}".
```
