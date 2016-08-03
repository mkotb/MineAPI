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
package io.mazenmc.mineapi.utils

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import io.mazenmc.mineapi.MineAPI
import org.wasabi.protocol.http.Request
import org.wasabi.protocol.http.Response
import java.util.concurrent.TimeUnit

object RateLimiter {
    private val limitData: Cache<String, RateData> = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .concurrencyLevel(5)
            .build()

    fun processRequest(request: Request, response: Response): Boolean {
        limitData.cleanUp();

        if (!limitData.asMap().contains(request.host)) {
            limitData.put(request.host, RateData())
        }

        var data = limitData.getIfPresent(request.host)

        ++data!!.requests
        data.lastRequest = System.currentTimeMillis()

        var rateLimit = MineAPI.config().rateLimit
        var canRequest = data.requests < rateLimit

        if (!canRequest) {
            response.statusCode = 429
            response.send("")
            MineAPI.debug("Rate limited ${request.host}, is ${data.requests - rateLimit} over the rate limit. " +
                    "(${data.requests},$rateLimit)")
        }

        return canRequest
    }
}

data class RateData(var requests: Int = 0, var lastRequest: Long = System.currentTimeMillis())