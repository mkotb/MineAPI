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
import com.mashape.unirest.http.JsonNode
import com.mashape.unirest.http.Unirest
import com.mashape.unirest.http.exceptions.UnirestException
import io.mazenmc.mineapi.MineAPI
import io.mazenmc.mineapi.provider.Provider
import io.mazenmc.mineapi.provider.ProviderHolder
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern
import kotlin.properties.Delegates

object IdentifierProvider {
    val idPattern: Pattern = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})")
    private val value: Any = Any()
    private var identifiers: Cache<IdentifierEntry, Any> by Delegates.notNull()
    private var provider: Provider by Delegates.notNull()

    fun init() {
        var config = MineAPI.config.idConfig

        identifiers = CacheBuilder.newBuilder()
                .maximumSize(config.maxSize.toLong())
                .expireAfterAccess(config.cacheTime.toLong(), TimeUnit.MINUTES)
                .concurrencyLevel(3)
                .build()
        provider = ProviderHolder.providerBy(config.provider.toLowerCase())
        MineAPI.debug("initialized identifiers cache with ${config.maxSize} max, ${config.cacheTime} minutes for cache time")
        MineAPI.debug("using ${config.provider} provider")
    }

    fun idFor(id: String): IdentifierEntry? {
        identifiers.cleanUp()

        return identifiers.asMap().entries.firstOrNull {
            it.key.name.equals(id)
        }?.key
    }

    fun idFor(id: UUID): IdentifierEntry? {
        identifiers.cleanUp()

        return identifiers.asMap().entries.firstOrNull {
            it.key.id.equals(id)
        }?.key
    }

    fun insert(entry: IdentifierEntry) {
        identifiers.put(entry, value)
    }

    fun requestFor(name: String): Boolean {
        var response = provider.request(name) ?: return false
        insert(response)
        return true
    }

    fun requestFor(id: UUID): Boolean {
        var response = provider.request(id) ?: return false
        insert(response)
        return true
    }

    fun idPattern(): Pattern {
        return idPattern
    }
}

data class IdentifierEntry(var name: String, var id: UUID, val oldNames: MutableList<String> = ArrayList()) {
}