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
import io.mazenmc.mineapi.responses.NameConvertResponse
import io.mazenmc.mineapi.responses.ResponseProcessor
import io.mazenmc.mineapi.utils.IdentifierProvider
import org.wasabi.protocol.http.Request
import org.wasabi.protocol.http.Response
import java.util.*

class NameConverterRoute: BaseRoute {
    override val name = "player/:uuid/name"

    override fun act(request: Request, response: Response) {
        var stringId = request.routeParams["uuid"]
        var uuid: UUID

        try {
            uuid = UUID.fromString(IdentifierProvider.idPattern().matcher(stringId)
                    .replaceAll("$1-$2-$3-$4-$5"))
        } catch (ex: IllegalArgumentException) {
            response.statusCode = 400
            var msg = ex.message

            if (msg != null)
                response.send(msg)
            else
                response.send("Invalid UUID string")

            MineAPI.verbose("${request.host} sent invalid UUID string (${stringId})")
            return
        }

        var id = IdentifierProvider.idFor(uuid)

        if (id == null && !IdentifierProvider.requestFor(uuid)) {
            response.statusCode = 400
            response.send("No player found by UUID ${uuid.toString()}")
            MineAPI.debug("Could not find player by ID ${uuid.toString()}")
            return
        }

        id = IdentifierProvider.idFor(uuid) // try again
        ResponseProcessor.process(NameConvertResponse(id!!.name, id.oldNames), response)
    }
}