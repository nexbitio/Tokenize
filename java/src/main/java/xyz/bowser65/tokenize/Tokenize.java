/*
 * Tokenize, universal and secure token generator for authentication
 * Copyright (C) 2019 Bowser65
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
import java.util.Base64;
import java.util.function.Function;

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
    @SuppressWarnings("FieldCanBeLocal")
    private final int VERSION = 1;

    /**
     * First second of 2019, used to get shorter tokens.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final long TOKENIZE_EPOCH = 1546300800000L;

    /**
     * Secret used to sign tokens.
     */
    private final byte[] secret;

    public Tokenize(@Nonnull String secret) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Generates a token for a given account id.
     *
     * @param accountId ID of the account.
     * @return A valid, non-mfa token.
     */
    @Nonnull
    public String generate(@Nonnull String accountId) {
        final String accountPart = new String(Base64.getEncoder().encode(accountId.getBytes(StandardCharsets.UTF_8)));
        final String timePart = new String(Base64.getEncoder().encode(String.valueOf(currentTokenTime()).getBytes(StandardCharsets.UTF_8)));
        final String firstPart = (accountPart + "." + timePart).replace("=", "");
        final String signaturePart = computeHmac(firstPart);
        return firstPart + "." + signaturePart;
    }

    /**
     * @see Tokenize#upgrade(String, String, String, Integer)
     */
    @Nullable
    public String upgrade(@Nonnull String token, @Nonnull String mfa, @Nonnull String secret) {
        return upgrade(token, mfa, secret, null);
    }

    /**
     * Upgrades a token and turns it into a mfa token.
     *
     * @param token   The non-mfa token of the user.
     * @param mfa     User-provided 6 digit code.
     * @param secret  MFA secret bound to the user.
     * @param counter If null Tokenize will perform a TOTP check, HOTP check otherwise.
     * @return A valid, mfa token or null if the upgrade failed (Invalid MFA code).
     */
    @Nullable
    public String upgrade(@Nonnull String token, @Nonnull String mfa, @Nonnull String secret, @Nullable Integer counter) {
        // @todo: otp
        return null;
    }

    /**
     * @see Tokenize#validate(String, Function, boolean)
     */
    @Nullable
    public IAccount validate(@Nonnull String token, @Nonnull Function<String, IAccount> accountFetcher) {
        return validate(token, accountFetcher, false);
    }

    /**
     * Validates if a token is valid or not.
     *
     * @param token          The token to validate
     * @param accountFetcher The function used to fetch the account. It'll receive the account id as a string
     *                       and should return the complete account entry. It'll be returned if the token is valid.
     * @param ignoreMfa      Whether or not MFA check should be performed. If true, only non-mfa tokens will be
     *                       accepted. Can be used to make "ticket tokens", where the user entered correct credentials
     *                       but didn't performed MFA check
     * @return The account if the token is valid, null otherwise.
     */
    @Nullable
    public IAccount validate(@Nonnull String token, @Nonnull Function<String, IAccount> accountFetcher, boolean ignoreMfa) {
        final boolean isMfa = token.startsWith("mfa.");
        final String[] splitted = token.replace("mfa.", "").split("\\.");
        if (splitted.length != 3) return null;

        final StringBuilder builder = new StringBuilder();
        if (isMfa) builder.append("mfa.");
        builder.append(splitted[0]).append(".").append(splitted[1]);

        final String signature = computeHmac(builder.toString());
        if (splitted[2].equals(signature)) return null;

        final long tokenTime = Long.valueOf(new String(Base64.getDecoder().decode(splitted[1])));
        final IAccount account = accountFetcher.apply(new String(Base64.getDecoder().decode(splitted[0])));

        return tokenTime > account.tokensValidSince() && (!ignoreMfa && isMfa) == account.hasMfa() ? account : null;
    }

    /**
     * @return Current token time based on the Tokenize Epoch
     */
    public long currentTokenTime() {
        return (System.currentTimeMillis() - TOKENIZE_EPOCH) / 1000;
    }

    private String computeHmac(String string) {
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
