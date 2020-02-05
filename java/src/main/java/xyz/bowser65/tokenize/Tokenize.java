/*
 * Tokenize, universal and secure token generator for authentication
 * Copyright (C) 2019-present Bowser65
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

package xyz.bowser65.tokenize;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Base64;

/**
 * Tokenize main class
 *
 * @author Bowser65
 * @since 10/08/19
 */
@SuppressWarnings("WeakerAccess")
public class Tokenize {
    /**
     * Tokenize Token Format version.
     */
    public static final int VERSION = 1;

    /**
     * First millisecond of 2019, used to get shorter tokens.
     */
    public static final long TOKENIZE_EPOCH = 1546300800000L;

    /**
     * Secret used to sign tokens.
     */
    private final byte[] secret;

    public Tokenize(final byte[] secret) {
        this.secret = secret;
    }

    @Nonnull
    public Token generateToken(@Nonnull final IAccount account) {
        return this.generateToken(account, null);
    }

    @Nonnull
    public Token generateToken(@Nonnull final IAccount account, @Nullable final String prefix) {
        return new Token(this, account, prefix, currentTokenTime());
    }

    /**
     * Validates a token
     *
     * @param token          The token to validate
     * @param accountFetcher An AccountFetcher instance that will be used to fetch
     *                       an account with the associated ID string
     * @return A {@link Token}, or null if no account is associated or if the token
     *         has been revoked
     * @throws SignatureException If the token signature is invalid
     */
    @Nullable
    public Token validateToken(@Nonnull final String token, @Nonnull AccountFetcher accountFetcher)
            throws SignatureException {
        final String[] parts = token.split("\\.");
        String prefix, encodedAccount, encodedTime;
        boolean signatureValid;

        if (parts.length != 3 && parts.length != 4) {
            throw new IllegalArgumentException("Invalid token: expected 3 or 4 parts, got " + parts.length);
        }

        if (parts.length == 4) {
            prefix = parts[0];
            encodedAccount = parts[1];
            encodedTime = parts[2];
            signatureValid = computeHmac(prefix + '.' + encodedAccount + '.' + encodedTime).equals(parts[3]);
        } else {
            prefix = null;
            encodedAccount = parts[0];
            encodedTime = parts[1];
            signatureValid = computeHmac(encodedAccount + '.' + encodedTime).equals(parts[2]);
        }

        if (!signatureValid) {
            throw new SignatureException("Invalid signature");
        }

        final String accountId = new String(
                Base64.getDecoder().decode(encodedAccount.getBytes(StandardCharsets.UTF_8)));
        final long tokenTime = Long
                .parseLong(new String(Base64.getDecoder().decode(encodedTime.getBytes(StandardCharsets.UTF_8))));
        final IAccount account = accountFetcher.fetchAccount(accountId);

        if (account != null && tokenTime > account.tokensValidSince()) {
            return new Token(this, account, prefix, tokenTime);
        }
        return null;
    }

    /**
     * @return Current token time based on the Tokenize Epoch
     */
    public static long currentTokenTime() {
        return (System.currentTimeMillis() - TOKENIZE_EPOCH) / 1000;
    }

    String computeHmac(final String string) {
        try {
            final Mac hmac = Mac.getInstance("HmacSHA256");
            final SecretKeySpec key = new SecretKeySpec(secret, "HmacSHA256");
            hmac.init(key);

            final byte[] data = hmac.doFinal(("TTF." + VERSION + "." + string).getBytes(StandardCharsets.UTF_8));
            return new String(Base64.getEncoder().encode(data)).replace("=", "");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Tokenize is unable to function if HmacSHA256 algorithm isn't present!");
        } catch (Throwable e) {
            throw new RuntimeException("is this ever reachable?");
        }
    }
}
