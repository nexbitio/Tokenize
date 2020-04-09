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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Tokenize main class
 *
 * @author Bowser65
 * @since 10/08/19
 */
@SuppressWarnings({"WeakerAccess", "unused"})
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
     * Validates a token synchronously.
     *
     * @param token          The token to validate.
     * @param accountFetcher The account fetcher used to retrieve the account.
     * @return The token, or {@code null} if there is no account associated or if the token has been revoked.
     * @throws SignatureException If the token signature is invalid.
     */
    @Nullable
    public Token validateToken(@Nonnull final String token, @Nonnull AccountFetcher accountFetcher) throws SignatureException {
        final String[] parts = parseToken(token);
        final long tokenTime = Long.parseLong(parts[2]);
        final IAccount account = accountFetcher.fetchAccount(parts[1]);
        if (account != null && tokenTime > account.tokensValidSince()) {
            return new Token(this, account, parts[0], tokenTime);
        }
        return null;
    }

    /**
     * Validates a token asynchronously.
     *
     * @param token          The token to validate.
     * @param accountFetcher The account fetcher used to retrieve the account.
     * @return A {@link CompletionStage}.
     * @throws SignatureException If the token signature is invalid.
     */
    @Nullable
    public CompletionStage<Token> validateToken(@Nonnull final String token, @Nonnull AsyncAccountFetcher accountFetcher) throws SignatureException {
        final CompletableFuture<Token> future = new CompletableFuture<>();
        final String[] parts = parseToken(token);
        final long tokenTime = Long.parseLong(parts[2]);
        accountFetcher.fetchAccount(parts[1]).thenAccept(account -> {
            if (account != null && tokenTime > account.tokensValidSince()) {
                future.complete(new Token(this, account, parts[0], tokenTime));
            }
            future.complete(null);
        });
        return future;
    }

    private String[] parseToken(@Nonnull final String token) throws SignatureException {
        final String[] parts = token.split("\\.");
        String[] parsed = new String[4];
        if (parts.length != 3 && parts.length != 4) {
            throw new IllegalArgumentException("Invalid token: expected 3 or 4 parts, got " + parts.length);
        }

        int index = 0;
        boolean signatureValid;
        if (parts.length == 4) {
            signatureValid = computeHmac(parts[0] + '.' + parts[1] + '.' + parts[2]).equals(parts[3]);
            parsed[0] = parts[0];
            index++;
        } else {
            signatureValid = computeHmac(parts[0] + '.' + parts[1]).equals(parts[2]);
            parsed[0] = null;
        }

        if (!signatureValid) {
            throw new SignatureException("Invalid signature");
        }

        parsed[1] = new String(Base64.getDecoder().decode(parts[index].getBytes(StandardCharsets.UTF_8)));
        parsed[2] = new String(Base64.getDecoder().decode(parts[1 + index].getBytes(StandardCharsets.UTF_8)));
        return parsed;
    }

    /**
     * @return Current token time based on the Tokenize Epoch.
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
