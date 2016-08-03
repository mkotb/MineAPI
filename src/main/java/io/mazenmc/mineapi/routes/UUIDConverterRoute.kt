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
package io.mazenmc.mineapi.routes

import io.mazenmc.mineapi.MineAPI
import io.mazenmc.mineapi.responses.ResponseProcessor
import io.mazenmc.mineapi.responses.UUIDConvertResponse
import io.mazenmc.mineapi.utils.IdentifierProvider
import org.wasabi.protocol.http.Request
import org.wasabi.protocol.http.Response

class UUIDConverterRoute : BaseRoute {
    override val name: String = "player/:name/uuid"

    override fun act(request: Request, response: Response) {
        var name = request.routeParams["name"]
        var id = IdentifierProvider.idFor(name!!)

        if (id == null && !IdentifierProvider.requestFor(name)) {
            response.statusCode = 400
            response.send("No player found by name $name")
            MineAPI.debug("Could not find player by name $name")
            return
        }

        id = IdentifierProvider.idFor(name) // retry, should be not null
        ResponseProcessor.process(UUIDConvertResponse(id!!.id.toString()), response)
    }
}