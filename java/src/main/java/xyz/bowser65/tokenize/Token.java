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
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Represents a Tokenize token
 *
 * @author Bowser65
 * @since 31/02/20
 */
public class Token {
    private final Tokenize tokenize;
    private final IAccount account;
    private String prefix;
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
        token.append(new String(
                Base64.getEncoder().encode(this.account.getId().getBytes(StandardCharsets.UTF_8))
        )).append('.').append(new String(
                Base64.getEncoder().encode(String.valueOf(this.genTime).getBytes(StandardCharsets.UTF_8))
        ));
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
     * @return The account this token is valid for
     */
    @Nonnull
    public IAccount getAccount() {
        return account;
    }

    /**
     * @return The prefix associated with the token
     */
    @Nullable
    public String getPrefix() {
        return prefix;
    }

    /**
     * @return The token time in seconds relative to the Tokenize Epoch
     */
    public long getGenTime() {
        return genTime;
    }
}
