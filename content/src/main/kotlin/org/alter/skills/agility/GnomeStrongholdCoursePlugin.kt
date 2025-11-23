package org.alter.skills.agility

import org.alter.api.Skills
import org.alter.api.ext.getInteractingGameObj
import org.alter.api.ext.getVarbit
import org.alter.api.ext.getVarp
import org.alter.api.ext.setVarbit
import org.alter.api.ext.setVarp
import org.alter.game.model.Direction
import org.alter.game.model.ForcedMovement
import org.alter.game.model.Tile
import org.alter.game.model.entity.GameObject
import org.alter.game.model.entity.Player
import org.alter.game.pluginnew.PluginEvent
import org.alter.game.pluginnew.event.impl.onObjectOption

class GnomeStrongholdCoursePlugin : PluginEvent() {

    private companion object {
        private const val AGILITY_HELPER_TEMP_VARP = "varp.helper_agility_vars"
        private const val AGILITY_HELPER_PERM_VARP = "varp.helper_agility_vars_perm"
        private const val AGILITY_HELPER_CURRENT_COURSE_VARBIT = "varbit.helper_agility_current_course"
        private const val AGILITY_HELPER_HIGHLIGHTED_COURSE_VARBIT = "varbit.helper_agility_highlighted_course"
        private const val AGILITY_HELPER_HIGHLIGHTED_COURSE_REMEMBER_VARBIT = "varbit.helper_agility_highlighted_course_remember"

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
            clientDuration = 60,
            destination = { _, obj -> obj.tile.step(Direction.SOUTH, 7) },
        ),
        CourseObstacle(
            objectId = "objects.agility_gnome_net_up",
            option = "climb-over",
            animation = "sequences.agility_climb_net",
            experience = 7.5,
            clientDuration = 45,
            destination = { _, obj -> obj.tile.transform(0, 2, 1) },
        ),
        CourseObstacle(
            objectId = "objects.agility_gnome_branch_up",
            option = "climb",
            animation = "sequences.agility_climb_branch",
            experience = 5.0,
            clientDuration = 30,
            destination = { _, obj -> obj.tile.transform(0, 0, 2) },
        ),
        CourseObstacle(
            objectId = "objects.agility_gnome_rope",
            option = "walk-on",
            animation = "sequences.agility_balancing_rope",
            experience = 7.5,
            clientDuration = 72,
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
            clientDuration = 30,
            destination = { _, obj -> obj.tile.transform(0, 0, -2) },
        ),
        CourseObstacle(
            objectId = "objects.agility_gnome_net_down",
            option = "climb-over",
            animation = "sequences.agility_climb_net",
            experience = 7.5,
            clientDuration = 45,
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
            clientDuration = 54,
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
            val destination = obstacle.destination(player, obj)
            val direction = if (obstacle == obstacles.first()) Direction.SOUTH else Direction.between(player.tile, destination)
            player.faceTile(obj.tile)
            player.animate(obstacle.animation)
            val movement = ForcedMovement.of(
                src = player.tile,
                dst = destination,
                clientDuration1 = obstacle.clientDuration,
                clientDuration2 = obstacle.clientDuration,
                directionAngle = direction.angle,
            )
            player.forceMove(this, movement)
            player.addXp(Skills.AGILITY, obstacle.experience)
        }
    }

    private data class CourseObstacle(
        val objectId: String,
        val option: String,
        val animation: String,
        val experience: Double,
        val clientDuration: Int,
        val destination: (Player, GameObject) -> Tile,
    )
}
