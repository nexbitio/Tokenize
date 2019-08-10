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
class Tokenize {
    /**
     * Tokenize Token Format version.
     */
    private final int VERSION = 1;

    /**
     * First second of 2019, used to get shorter tokens.
     */
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
        return "";
    }

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
        return null;
    }

    /**
     * Validates if a token is valid or not.
     *
     * @param token          The token to validate
     * @param accountFetcher The function used to fetch the account. It'll receive the account id as a string
     *                       and should return the complete account entry. It'll be returned if the token is valid.
     * @return The account if the token is valid, null otherwise.
     */
    @Nullable
    public IAccount validate(@Nonnull String token, @Nonnull Function<String, IAccount> accountFetcher) {
        return null;
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
            return new String(Base64.getEncoder().encode(data));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Tokenize is unable to function if HmacSHA256 algorithm isn't present!");
        } catch (Throwable e) {
            throw new RuntimeException("is this ever reachable?");
        }

    }
}
