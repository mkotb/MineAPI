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

import io.mazenmc.mineapi.responses.NameConvertResponse
import io.mazenmc.mineapi.responses.ResponseProcessor
import io.mazenmc.mineapi.utils.IdentifierProvider
import org.wasabi.http.Request
import org.wasabi.http.Response
import java.util.*

public class NameConverterRoute: BaseRoute {
    override val name = "player/:uuid/name"

    override fun act(request: Request, response: Response) {
        var stringId = request.routeParams["uuid"]
        var uuid: UUID

        try {
            uuid = UUID.fromString("${stringId.substring(0, 7)}-${stringId.substring(7, 11)}" +
                    "-${stringId.substring(11, 15)}-${stringId.substring(15, 19)}-${stringId.substring(19, 31)}")
        } catch (ex: IllegalArgumentException) {
            response.statusCode = 400
            var msg = ex.getMessage()

            if (msg != null)
                response.send(msg)
            else
                response.send("Invalid UUID string")
            return
        }

        var id = IdentifierProvider.idFor(uuid)

        if (id == null && !IdentifierProvider.requestFor(uuid)) {
            response.statusCode = 500
            response.send("Internal server error")
            return
        }

        id = IdentifierProvider.idFor(uuid) // try again

        if (id == null) {
            return
        }

        ResponseProcessor.process(NameConvertResponse(id.name, id.oldNames), response)
    }
}