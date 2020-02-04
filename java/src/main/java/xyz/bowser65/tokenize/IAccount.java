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

/**
 * Interface your Account entity should implement to work with Tokenize
 *
 * @author Bowser65
 * @since 10/08/19
 */
public interface IAccount {
    /**
     * Should return the ID of the account.
     */
    String getTokenId();

    /**
     * Should return the Tokenize timestamp since when tokens are valid.
     * Use {@link Tokenize#currentTokenTime()} to get it when you're creating an account or when you
     * need to invalidate tokens
     *
     * @return Tokenize timestamp since when tokens are valid.
     */
    long tokensValidSince();
}
