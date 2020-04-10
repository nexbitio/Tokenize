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
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Utility class to handle OTP authentication tokens
 *
 * @author Bowser65
 * @since 09/04/20
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class OTPUtils {
    private static final Map<String, String> lastUsedCodes = new HashMap<>();

    public synchronized static boolean validateHotp(@Nonnull final String token, @Nonnull final String secret, final long counter) {
        if (lastUsedCodes.getOrDefault(secret, "").equals(token)) return false;
        if (!Pattern.matches("^[0-9]{6}$", token)) throw new IllegalArgumentException("Token must be 6 digits.");
        if (computeToken(secret, counter).equals(token)) {
            lastUsedCodes.put(secret, token);
            return true;
        }
        return false;
    }

    public synchronized static boolean validateTotp(@Nonnull final String token, @Nonnull final String secret) {
        return validateHotp(token, secret, System.currentTimeMillis() / 30 / 1000);
    }

    private synchronized static String computeToken(final String secret, long counter) {
        try {
            final byte[] key = Base32.decode(secret);
            final byte[] buf = new byte[8];
            for (int i = 0; i < 8; i++) {
                buf[7 - i] = (byte) (counter & 0xff);
                counter = counter >> 8;
            }
            final Mac hmac = Mac.getInstance("HmacSHA1");
            hmac.init(new SecretKeySpec(key, "HmacSHA1"));
            final byte[] digest = hmac.doFinal(buf);
            final int offset = digest[digest.length - 1] & 0xf;
            final int code = (digest[offset] & 0x7f) << 24 |
                    (digest[offset + 1] & 0xff) << 16 |
                    (digest[offset + 2] & 0xff) << 8 |
                    (digest[offset + 3] & 0xff);
            final String str = String.format("%6s", code).replace(' ', '0');
            return str.substring(str.length() - 6);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Tokenize is unable to function if HmacSHA1 algorithm isn't present!", e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException("is this ever reachable?", e);
        }
    }
}
