package org.alter.objects.door

import org.alter.api.cfg.Sound
import org.alter.api.ext.*
import org.alter.game.Server
import org.alter.game.model.World
import org.alter.game.model.attr.AttributeKey
import org.alter.game.model.collision.WALL_DIAGONAL
import org.alter.game.model.entity.GameObject
import org.alter.game.model.entity.Player
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

class DoorPlugin(
    r: PluginRepository, world: World,
    server: Server) : KotlinPlugin(r, world, server) {

    val STICK_STATE = AttributeKey<DoorStickState>()

    init {
        loadService(DoorService())

        onWorldInit {
            world.getService(DoorService::class.java)?.let { service ->
                service.doors.forEach { door ->
                    r.bindObject(door.opened, 1) {
                        val obj = player.getInteractingGameObj()
                        val newDoor = world.closeDoor(obj, closed = door.closed, invertTransform = obj.type == WALL_DIAGONAL)
                        copyStickVars(obj, newDoor)
                        player.playSound(Sound.CLOSE_DOOR_SFX)
                    }

                    r.bindObject(door.closed, 1) {
                        val obj = player.getInteractingGameObj()
                        val newDoor = world.openDoor(obj, opened = door.opened, invertTransform = obj.type == WALL_DIAGONAL)
                        copyStickVars(obj, newDoor)
                        player.playSound(Sound.OPEN_DOOR_SFX)
                    }
                }

                service.doubleDoors.forEach { doors ->
                    r.bindObject(doors.closed.left, 1) {
                        handleDoubleDoors(player, player.getInteractingGameObj(), doors, open = true)
                    }

                    r.bindObject(doors.closed.right, 1) {
                        handleDoubleDoors(player, player.getInteractingGameObj(), doors, open = true)
                    }

                    r.bindObject(doors.opened.left, 1) {
                        handleDoubleDoors(player, player.getInteractingGameObj(), doors, open = false)
                    }

                    r.bindObject(doors.opened.right, 1) {
                        handleDoubleDoors(player, player.getInteractingGameObj(), doors, open = false)
                    }
                }
            }
        }
    }

    fun handleDoubleDoors(p: Player, obj: GameObject, doors: DoubleDoorSet, open: Boolean) {
        val left = obj.internalID == doors.opened.left || obj.internalID == doors.closed.left
        val right = obj.internalID == doors.opened.right || obj.internalID == doors.closed.right

        check(left || right)

        val otherDoorId = if (open) {
            if (left) doors.closed.right else doors.closed.left
        } else {
            if (left) doors.opened.right else doors.opened.left
        }
        val otherDoor = getNeighbourDoor(world, obj, otherDoorId) ?: return

        if (open) {
            val door1 = world.openDoor(obj, opened = if (left) doors.opened.left else doors.opened.right, invertRot = left)
            val door2 = world.openDoor(otherDoor, opened = if (left) doors.opened.right else doors.opened.left, invertRot = right)
            copyStickVars(obj, door1)
            copyStickVars(obj, door2)
            p.playSound(Sound.OPEN_DOOR_SFX)
        } else {
            val door1 = world.closeDoor(obj, closed = if (left) doors.closed.left else doors.closed.right, invertRot = left, invertTransform = left)
            val door2 = world.closeDoor(otherDoor, closed = if (left) doors.closed.right else doors.closed.left, invertRot = right, invertTransform = right)
            copyStickVars(obj, door1)
            copyStickVars(obj, door2)
            p.playSound(Sound.CLOSE_DOOR_SFX)
        }
    }

    fun getNeighbourDoor(world: World, obj: GameObject, otherDoor: Int): GameObject? {
        val tile = obj.tile

        for (x in -1..1) {
            for (z in -1..1) {
                if (x == 0 && z == 0) {
                    continue
                }
                val transform = tile.transform(x, z)
                val tileObj = world.getObject(transform, type = obj.type)
                if (tileObj?.internalID == otherDoor) {
                    return tileObj
                }
            }
        }
        return null
    }

    fun copyStickVars(from: GameObject, to: GameObject) {
        if (from.attr.has(STICK_STATE)) {
            to.attr[STICK_STATE] = from.attr[STICK_STATE]!!
        }
    }
}