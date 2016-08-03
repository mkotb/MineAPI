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
package io.mazenmc.mineapi.provider

import com.mashape.unirest.http.Unirest
import io.mazenmc.mineapi.MineAPI
import io.mazenmc.mineapi.utils.IdentifierEntry
import java.util.*

class MCAPIProvider: Provider {
    override fun request(id: UUID): IdentifierEntry? {
        var url = "https://mcapi.ca/name/uuid/${id.toString().replace("-", "")}"
        var response = Unirest.get(url)
                .asJson()

        if (response.status != 200) {
            MineAPI.debug("response from $url threw ${response.status}: ${response.statusText}")
            return null
        }

        var obj = response.body.`object`
        return IdentifierEntry(obj.getString("name"), id)
    }

    override fun request(name: String): IdentifierEntry? {
        var url = "https://mcapi.ca/uuid/player/$name"
        var response = Unirest.get(url)
                .asJson()

        if (response.status != 200) {
            MineAPI.debug("response from $url threw ${response.status}: ${response.statusText}")
            return null
        }

        var obj = response.body.`object`
        return IdentifierEntry(name, UUID.fromString(obj.getString("uuid_formatted")))
    }
}