package org.alter.objects.door

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.toml.TomlFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.openrune.filesystem.Cache
import gg.rsmod.util.ServerProperties
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import org.alter.api.ext.appendToString
import org.alter.game.Server.Companion.logger
import org.alter.game.model.World
import org.alter.game.service.Service
import java.nio.file.Files
import java.nio.file.Paths

/**
 * @author Tom <rspsmods@gmail.com>
 */
class DoorService : Service {
    val doors = ObjectArrayList<Door>()

    val doubleDoors = ObjectArrayList<DoubleDoorSet>()

    override fun init(
        cache: Cache,
        server: org.alter.game.Server,
        world: World,
        serviceProperties: ServerProperties,
    ) {
        val configFile = Paths.get(serviceProperties.get("doors-config") ?: "data/cfg/doors/doors.toml")

        val mapper = jacksonObjectMapper(TomlFactory())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

        Files.newBufferedReader(configFile).use { reader ->
            val config = mapper.readValue(reader, DoorConfig::class.java)
            this.doors.addAll(config.singleDoors)
            this.doubleDoors.addAll(config.doubleDoors)
        }

        logger.info { "Loaded ${doors.size.appendToString("single door")} and ${doubleDoors.size.appendToString("double door")}." }
    }
}

private data class DoorConfig(
    val singleDoors: List<Door> = emptyList(),
    val doubleDoors: List<DoubleDoorSet> = emptyList(),
)
