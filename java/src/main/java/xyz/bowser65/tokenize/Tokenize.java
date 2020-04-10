/*
 * Copyright (c) 2020 Bowser65, All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package xyz.bowser65.tokenize;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
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
            throw new RuntimeException("Tokenize is unable to function if HmacSHA256 algorithm isn't present!", e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("is this ever reachable?", e);
        }
    }
}
