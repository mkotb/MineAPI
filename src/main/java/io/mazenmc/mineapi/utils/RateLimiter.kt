package io.mazenmc.mineapi.utils

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import io.mazenmc.mineapi.MineAPI
import org.wasabi.http.Request
import org.wasabi.http.Response
import java.util.concurrent.TimeUnit

public object RateLimiter {
    private val limitData: Cache<String, RateData> = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .concurrencyLevel(5)
            .build()

    public fun processRequest(request: Request, response: Response): Boolean {
        limitData.cleanUp();

        if (!limitData.asMap().contains(request.host)) {
            limitData.put(request.host, RateData())
        }

        var data = limitData.getIfPresent(request.host)

        ++data.requests
        data.lastRequest = System.currentTimeMillis()

        var canRequest = data.requests >= MineAPI.config().rateLimit

        if (!canRequest) {
            response.statusCode = 429
            response.send("")
        }

        return canRequest
    }
}

data class RateData(var requests: Int = 0, var lastRequest: Long = System.currentTimeMillis())