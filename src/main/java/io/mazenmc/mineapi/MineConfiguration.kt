/**
 * Copyright (c) 2015, Mazen Kotb <email@mazenmc.io>
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package io.mazenmc.mineapi

import org.wasabi.app.AppConfiguration

data public class MineConfiguration(val port: Int = 3000, val debug: Boolean = false, val rateLimit: Int = 100,
                                    val idConfig: IdentifierConfiguration = IdentifierConfiguration(),
                                    val verboseLogging: Boolean = false) {
    public fun asAppConfig(): AppConfiguration {
        var new = AppConfiguration()

        new.enableLogging = debug
        new.port = port

        return new
    }
}

data class IdentifierConfiguration(val cacheTime: Int = 30, val maxSize: Int = 10000)