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
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

public object IdentifierProvider {
    private val idPattern: Pattern = Pattern.compile("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})")
    private val value: Any = Any()
    private val identifiers: Cache<IdentifierEntry, Any> = CacheBuilder.newBuilder()
            .maximumSize(10000)
            .expireAfterAccess(30, TimeUnit.MINUTES)
            .concurrencyLevel(3)
            .build()

    public fun idFor(id: String): IdentifierEntry? {
        identifiers.cleanUp()

        return identifiers.asMap().keySet().firstOrNull {
            it.name.equals(id, true)
        }
    }

    public fun idFor(id: UUID): IdentifierEntry? {
        identifiers.cleanUp()

        return identifiers.asMap().keySet().firstOrNull {
            it.id.equals(id)
        }
    }

    public fun insert(entry: IdentifierEntry) {
        identifiers.put(entry, value)
    }

    public fun requestFor(name: String): Boolean {
        var response = Unirest.get("https://api.mojang.com/users/profiles/minecraft/${name}")
                .asJson()

        if (response.getStatus() == 204) {
            return false
        }

        var stringId = response.getBody().getObject().getString("id")
        stringId = idPattern.matcher(stringId).replaceAll("$1-$2-$3-$4-$5")

        insert(IdentifierEntry(name, UUID.fromString(stringId)))
        return true
    }

    public fun requestFor(id: UUID): Boolean {
        var response = Unirest.get("https://api.mojang.com/user/profiles/${id.toString().replace("-", "")}/names")
                .asJson()

        var names = response.getBody().getArray()
        var entry = IdentifierEntry(names.getJSONObject(0).getString("name"), id)

        if (names.length() > 1) {
            for (i in 1..names.length()) {
                var oldName = names.getJSONObject(i)

                entry.oldNames.put(oldName.getString("name"), oldName.getLong("changedToAt"))
            }
        }

        insert(entry)
        return true
    }

    public fun idPattern(): Pattern {
        return idPattern
    }
}

data public class IdentifierEntry(var name: String, var id: UUID, val oldNames: MutableMap<String, Long> = HashMap()) {
}