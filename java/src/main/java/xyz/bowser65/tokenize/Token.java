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

import lombok.Getter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Represents a Tokenize token
 *
 * @author Bowser65
 * @since 31/01/20
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Token {
    private final Tokenize tokenize;
    @Getter
    private final IAccount account;
    @Getter
    private String prefix;
    @Getter
    private long genTime;

    Token(@Nonnull final Tokenize tokenize, @Nonnull final IAccount account) {
        this(tokenize, account, null, Tokenize.currentTokenTime());
    }

    Token(@Nonnull final Tokenize tokenize, @Nonnull final IAccount account, @Nullable final String prefix) {
        this(tokenize, account, prefix, Tokenize.currentTokenTime());
    }

    Token(@Nonnull final Tokenize tokenize, @Nonnull final IAccount account, final long genTime) {
        this(tokenize, account, null, genTime);
    }

    Token(@Nonnull final Tokenize tokenize, @Nonnull final IAccount account, @Nullable final String prefix, final long genTime) {
        this.tokenize = tokenize;
        this.account = account;
        this.prefix = prefix;
        this.genTime = genTime;
    }

    /**
     * @return The signed token
     */
    @Override
    public String toString() {
        final StringBuilder token = new StringBuilder();
        if (this.prefix != null) {
            token.append(this.prefix).append('.');
        }

        final byte[] rawId = Base64.getEncoder().encode(this.account.getTokenId().getBytes(StandardCharsets.UTF_8)),
                rawTime = Base64.getEncoder().encode(String.valueOf(this.genTime).getBytes(StandardCharsets.UTF_8));

        token.append(new String(rawId, 0, rawId.length, StandardCharsets.UTF_8));
        token.append('.');
        token.append(new String(rawTime, 0, rawTime.length, StandardCharsets.UTF_8));

        final String toSign = token.toString();
        token.append('.').append(tokenize.computeHmac(toSign));
        return token.toString();
    }

    /**
     * Resets the generation time for the token
     */
    public void regenerate() {
        this.genTime = Tokenize.currentTokenTime();
    }

    /**
     * Sets the prefix of the token, and resets the generation time
     *
     * @param prefix The new prefix. Cannot contain dots
     */
    public void setPrefix(String prefix) {
        if (prefix.contains(".")) {
            throw new IllegalArgumentException("Prefix cannot contain dots.");
        }
        this.prefix = prefix;
        this.genTime = Tokenize.currentTokenTime();
    }

    /**
     * @return The token age in seconds
     */
    public long getAge() {
        return genTime - Tokenize.TOKENIZE_EPOCH / 1000;
    }
}
