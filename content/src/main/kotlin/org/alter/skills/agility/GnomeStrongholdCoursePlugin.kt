package org.alter.skills.agility

import org.alter.api.Skills
import org.alter.api.ext.getInteractingGameObj
import org.alter.api.ext.getVarbit
import org.alter.api.ext.getVarp
import org.alter.api.ext.loopAnim
import org.alter.api.ext.setVarbit
import org.alter.api.ext.setVarp
import org.alter.api.ext.stopLoopAnim
import org.alter.api.ext.filterableMessage
import org.alter.game.model.Direction
import org.alter.game.model.ForcedMovement
import org.alter.game.model.Tile
import org.alter.game.model.entity.GameObject
import org.alter.game.model.entity.Player
import org.alter.game.model.move.MovementQueue
import org.alter.game.pluginnew.PluginEvent
import org.alter.game.pluginnew.event.impl.onObjectOption

class GnomeStrongholdCoursePlugin : PluginEvent() {

    private companion object {
        private const val AGILITY_HELPER_TEMP_VARP = "varp.helper_agility_vars"
        private const val AGILITY_HELPER_PERM_VARP = "varp.helper_agility_vars_perm"
        private const val AGILITY_HELPER_CURRENT_COURSE_VARBIT = "varbits.helper_agility_current_course"
        private const val AGILITY_HELPER_HIGHLIGHTED_COURSE_VARBIT = "varbits.helper_agility_highlighted_course"
        private const val AGILITY_HELPER_HIGHLIGHTED_COURSE_REMEMBER_VARBIT = "varbits.helper_agility_highlighted_course_remember"

        private const val GNOME_STRONGHOLD_COURSE_ID = 0
        private const val GNOME_STRONGHOLD_HELPER_TEMP_VALUE = 24608
        private const val AGILITY_HELPER_PERM_ENABLED = 2
    }

    private val obstacles = listOf(
        CourseObstacle(
            objectId = "objects.gnome_log_balance1",
            option = "walk-across",
            animation = "sequences.human_walk_logbalance",
            experience = 7.5,
            clientDuration1 = 60,
            clientDuration2 = 60,
            endTile = Tile(x = 2474, z = 3429, height = 0),
            destination = { _, obj -> obj.tile.step(Direction.SOUTH, 7) },
            movementType = MovementType.FORCED_WALK,
            loopAnimation = true,
            startMessage = "You walk carefully across the slippery log...",
            endMessage = "... and make it safely to the other side.",
        ),
        CourseObstacle(
            objectId = "objects.agility_gnome_net_up",
            option = "climb-over",
            animation = "sequences.agility_climb_net",
            experience = 7.5,
            clientDuration1 = 45,
            clientDuration2 = 45,
            destination = { _, obj -> obj.tile.transform(0, 2, 1) },
        ),
        CourseObstacle(
            objectId = "objects.agility_gnome_branch_up",
            option = "climb",
            animation = "sequences.agility_climb_branch",
            experience = 5.0,
            clientDuration1 = 30,
            clientDuration2 = 30,
            destination = { _, obj -> obj.tile.transform(0, 0, 2) },
        ),
        CourseObstacle(
            objectId = "objects.agility_gnome_rope",
            option = "walk-on",
            animation = "sequences.agility_balancing_rope",
            experience = 7.5,
            clientDuration1 = 72,
            clientDuration2 = 72,
            destination = { player, obj ->
                val direction = if (player.tile.x <= obj.tile.x) Direction.WEST else Direction.EAST
                obj.tile.transform(0, 0, 2).step(direction, 6)
            },
        ),
        CourseObstacle(
            objectId = "objects.agility_gnome_branch_down",
            option = "climb-down",
            animation = "sequences.agility_climb_branch",
            experience = 5.0,
            clientDuration1 = 30,
            clientDuration2 = 30,
            destination = { _, obj -> obj.tile.transform(0, 0, -2) },
        ),
        CourseObstacle(
            objectId = "objects.agility_gnome_net_down",
            option = "climb-over",
            animation = "sequences.agility_climb_net",
            experience = 7.5,
            clientDuration1 = 45,
            clientDuration2 = 45,
            destination = { player, obj ->
                val direction = if (player.tile.z >= obj.tile.z) Direction.SOUTH else Direction.NORTH
                obj.tile.step(direction, 2)
            },
        ),
        CourseObstacle(
            objectId = "objects.agility_gnome_pipe",
            option = "squeeze-through",
            animation = "sequences.agility_pipe",
            experience = 7.5,
            clientDuration1 = 54,
            clientDuration2 = 54,
            destination = { player, obj ->
                val direction = if (player.tile.x <= obj.tile.x) Direction.EAST else Direction.WEST
                obj.tile.step(direction, 3)
            },
        ),
    )

    override fun init() {
        obstacles.forEach { obstacle ->
            onObjectOption(obstacle.objectId, obstacle.option) {
                val gameObject = player.getInteractingGameObj() ?: return@onObjectOption
                syncAgilityHelperVars(player)
                handleObstacle(player, gameObject, obstacle)
            }
        }
    }

    private fun syncAgilityHelperVars(player: Player) {
        if (player.getVarp(AGILITY_HELPER_PERM_VARP) != AGILITY_HELPER_PERM_ENABLED) {
            player.setVarp(AGILITY_HELPER_PERM_VARP, AGILITY_HELPER_PERM_ENABLED)
        }
        if (player.getVarp(AGILITY_HELPER_TEMP_VARP) != GNOME_STRONGHOLD_HELPER_TEMP_VALUE) {
            player.setVarp(AGILITY_HELPER_TEMP_VARP, GNOME_STRONGHOLD_HELPER_TEMP_VALUE)
        }

        if (player.getVarbit(AGILITY_HELPER_CURRENT_COURSE_VARBIT) != GNOME_STRONGHOLD_COURSE_ID) {
            player.setVarbit(AGILITY_HELPER_CURRENT_COURSE_VARBIT, GNOME_STRONGHOLD_COURSE_ID)
        }
        if (player.getVarbit(AGILITY_HELPER_HIGHLIGHTED_COURSE_VARBIT) != GNOME_STRONGHOLD_COURSE_ID) {
            player.setVarbit(AGILITY_HELPER_HIGHLIGHTED_COURSE_VARBIT, GNOME_STRONGHOLD_COURSE_ID)
        }
        if (player.getVarbit(AGILITY_HELPER_HIGHLIGHTED_COURSE_REMEMBER_VARBIT) != GNOME_STRONGHOLD_COURSE_ID) {
            player.setVarbit(AGILITY_HELPER_HIGHLIGHTED_COURSE_REMEMBER_VARBIT, GNOME_STRONGHOLD_COURSE_ID)
        }
    }

    private fun handleObstacle(player: Player, obj: GameObject, obstacle: CourseObstacle) {
        player.queue {
            val destination = obstacle.endTile ?: obstacle.destination(player, obj)
            val direction = if (obstacle == obstacles.first()) Direction.SOUTH else Direction.between(player.tile, destination)
            player.faceTile(obj.tile)
            player.lock()
            when (obstacle.movementType) {
                MovementType.FORCED_WALK -> {
                    val distance = player.tile.getDistance(destination)
                    obstacle.startMessage?.let(player::filterableMessage)
                    if (obstacle.loopAnimation) player.loopAnim(obstacle.animation) else player.animate(obstacle.animation)
                    player.forceWalkStraight(destination, direction)
                    wait(distance + 2)
                    if (obstacle.loopAnimation) player.stopLoopAnim()
                    obstacle.endMessage?.let(player::filterableMessage)
                }

                MovementType.FORCED_MOVEMENT -> {
                    player.animate(obstacle.animation)
                    val movement = ForcedMovement.of(
                        src = player.tile,
                        dst = destination,
                        clientDuration1 = obstacle.clientDuration1,
                        clientDuration2 = obstacle.clientDuration2,
                        directionAngle = direction.angle,
                    )
                    player.forceMove(this, movement)
                }
            }
            player.unlock()
            player.addXp(Skills.AGILITY, obstacle.experience)
        }
    }

    private data class CourseObstacle(
        val objectId: String,
        val option: String,
        val animation: String,
        val experience: Double,
        val clientDuration1: Int,
        val clientDuration2: Int,
        val endTile: Tile? = null,
        val destination: (Player, GameObject) -> Tile,
        val movementType: MovementType = MovementType.FORCED_MOVEMENT,
        val loopAnimation: Boolean = false,
        val startMessage: String? = null,
        val endMessage: String? = null,
    )

    private enum class MovementType {
        FORCED_WALK,
        FORCED_MOVEMENT,
    }

    private fun Player.forceWalkStraight(destination: Tile, direction: Direction) {
        movementQueue.clear()
        var current = tile
        while (current != destination) {
            current = current.step(direction, 1)
            movementQueue.addStep(current, MovementQueue.StepType.FORCED_WALK)
        }
    }
}
