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

import io.mazenmc.mineapi.routes.BaseRoute
import io.mazenmc.mineapi.routes.RouteRegistrar
import io.mazenmc.mineapi.utils.GsonProvider
import io.mazenmc.mineapi.utils.IdentifierProvider
import io.mazenmc.mineapi.utils.RateLimiter
import org.wasabi.app.AppServer
import java.io.File
import java.io.FileReader
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.properties.Delegates

fun main(args: Array<String>) {
    var configFile = File("config.json")

    if (!configFile.exists()) {
        Files.copy(MineConfiguration::class.java.getResourceAsStream("/config.json"),
                Paths.get(configFile.toURI()))
    }

    var config = GsonProvider.gson().fromJson(FileReader(configFile), MineConfiguration::class.java)

    MineAPI.config = config
    IdentifierProvider.init() // load
    MineAPI.server = AppServer(config.asAppConfig())
    RouteRegistrar.registerRoutes()

    MineAPI.verbose("initialized RateLimiter cache with 10000 maximum size, 1 minute expire after write, " +
            "${config.rateLimit} limit")
    MineAPI.server.start(true)
}

object MineAPI {
    val version: Int = 1
    var server: AppServer by Delegates.notNull()
    var config: MineConfiguration by Delegates.notNull()

    fun server(): AppServer {
        return server
    }

    fun config(): MineConfiguration {
        return config
    }

    fun debug(message: String) {
        if (config.debug)
            println("DEBUG: $message")
    }

    fun verbose(message: String) {
        if (config.verboseLogging)
            println("VERBOSE: $message")
    }

    fun get(route: BaseRoute) {
        server().get("/v$version/${route.name}", {
            if (RateLimiter.processRequest(request, response)) {
                route.act(request, response)
            }
        })

        verbose("Registered route ${route.javaClass.name} at ${route.name}")
    }

    fun post(route: BaseRoute) {
        server().post("/v$version/${route.name}", {
            if (RateLimiter.processRequest(request, response)) {
                route.act(request, response)
            }
        })
    }

    fun put(route: BaseRoute) {
        server().put("/v$version/${route.name}", {
            if (RateLimiter.processRequest(request, response)) {
                route.act(request, response)
            }
        })
    }
}