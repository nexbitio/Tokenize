<?php
/**
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

namespace Bowser65\Tokenize;

/**
 * Class Tokenize
 * @package Bowser65
 */
class Tokenize {
    /**
     * Tokenize Token Format version
     */
    const VERSION = 1;

    /**
     * First second of 2019, used to get shorter tokens
     */
    const TOKENIZE_EPOCH = 1546300800000;

    /**
     * Tokenize constructor.
     * @param integer $secret
     */
    function __construct (int $secret) {

    }
}
