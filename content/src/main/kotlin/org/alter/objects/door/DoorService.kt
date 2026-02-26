package org.alter.objects.door

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.openrune.filesystem.Cache
import gg.rsmod.util.ServerProperties
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import org.alter.api.ext.appendToString
import org.alter.game.Server.Companion.logger
import org.alter.game.model.World
import org.alter.game.service.Service
import org.alter.rscm.RSCM.getRSCM
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
        val singleDoorFile = Paths.get(serviceProperties.get("single-doors") ?: "data/cfg/doors/single-doors.json")
        val doubleDoorsFile = Paths.get(serviceProperties.get("double-doors") ?: "data/cfg/doors/double-doors.json")

        Files.newBufferedReader(singleDoorFile).use { reader ->
            val doors = Gson().fromJson<ObjectArrayList<Door>>(reader, object : TypeToken<ObjectArrayList<Door>>() {}.type)
            this.doors.addAll(doors.map { Door(closed = getRSCM(it.closed), opened = getRSCM(it.opened)) })
        }

        Files.newBufferedReader(doubleDoorsFile).use { reader ->
            val doors = Gson().fromJson<ObjectArrayList<DoubleDoorSet>>(reader, object : TypeToken<ObjectArrayList<DoubleDoorSet>>() {}.type)
            this.doubleDoors.addAll(
                doors.map {
                    DoubleDoorSet(
                        opened = DoubleDoor(left = getRSCM(it.opened.left), right = getRSCM(it.opened.right)),
                        closed = DoubleDoor(left = getRSCM(it.closed.left), right = getRSCM(it.closed.right)),
                    )
                },
            )
        }

        logger.info { "Loaded ${doors.size.appendToString("single door")} and ${doubleDoors.size.appendToString("double door")}." }
    }
}
