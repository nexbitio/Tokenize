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

import lombok.Builder;
import lombok.Getter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.security.SecureRandom;

/**
 * An OTP key
 *
 * @author Bowser65
 * @since 10/04/20
 */
@Getter
@SuppressWarnings({"WeakerAccess", "unused"})
public class OTPKey {
    private final String key;
    private final String name;
    private final String issuer;
    private final boolean hotp;

    @Builder
    private OTPKey(@Nonnull final String name, @Nullable final String issuer, final boolean hotp) {
        this.name = name;
        this.issuer = issuer;
        this.hotp = hotp;

        final SecureRandom secureRandom = new SecureRandom();
        final char[] key = new char[16];
        for (int i = 0; i < 16; ++i)
            key[i] = Base32.ALPHABET[secureRandom.nextInt(Base32.ALPHABET.length)];
        this.key = new String(key);
    }

    /**
     * @return A Google Authenticator compliant URI for this key
     */
    public String getGoogleUri() {
        String url = "otpauth://" + (hotp ? 'h' : 't') + "otp/" + name + "?secret=" + key;
        if (issuer != null) {
            url += "&issuer=" + issuer;
        }
        return url;
    }
}
