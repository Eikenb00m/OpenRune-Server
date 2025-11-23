package org.alter.skills.agility

import org.alter.api.Skills
import org.alter.api.ext.addXp
import org.alter.api.ext.animate
import org.alter.api.ext.filterableMessage
import org.alter.api.ext.getInteractingGameObj
import org.alter.api.ext.loopAnim
import org.alter.api.ext.stopLoopAnim
import org.alter.game.model.Direction
import org.alter.game.model.ForcedMovement
import org.alter.game.model.Tile
import org.alter.game.model.entity.Player
import org.alter.game.model.move.MovementQueue
import org.alter.game.model.move.walkTo
import org.alter.game.pluginnew.PluginEvent
import org.alter.game.pluginnew.event.impl.onObjectOption

class GnomeStrongholdCoursePlugin : PluginEvent() {

    private companion object {
        private const val LOG_BALANCE_ID = 23145
        private const val OBSTACLE_NET_UP_ID = 23134
        private const val TREE_BRANCH_UP_ID = 23559
        private const val BALANCING_ROPE_ID = 23557
        private const val TREE_BRANCH_DOWN_ID = 23560
        private const val OBSTACLE_NET_DOWN_ID = 23133
        private val PIPE_IDS = intArrayOf(23138, 23139)

        private const val LOG_BALANCE_XP = 7.5
        private const val NET_XP = 7.5
        private const val BRANCH_UP_XP = 5.0
        private const val ROPE_XP = 7.5
        private const val BRANCH_DOWN_XP = 5.0
        private const val PIPE_XP = 7.5

        private val LOG_DESTINATION = Tile(2474, 3429, 0)
    }

    override fun init() {
        onObjectOption(LOG_BALANCE_ID, "walk-across") {
            val distance = player.tile.getDistance(LOG_DESTINATION)
            player.queue {
                player.lock()
                player.filterableMessage("You walk carefully across the slippery log...")
                player.loopAnim("sequences.human_walk_logbalance")
                player.walkTo(LOG_DESTINATION, MovementQueue.StepType.FORCED_WALK, detectCollision = false)
                wait(distance + 2)
                player.stopLoopAnim()
                player.filterableMessage("... and make it safely to the other side.")
                player.addXp(Skills.AGILITY, LOG_BALANCE_XP)
                player.unlock()
            }
        }

        onObjectOption(OBSTACLE_NET_UP_ID, "climb-over") {
            val obj = player.getInteractingGameObj() ?: return@onObjectOption
            val destination = Tile(obj.tile.x, obj.tile.z - 1, 1)
            val distance = player.tile.getDistance(destination)
            player.queue {
                player.lock()
                player.filterableMessage("You climb the netting...")
                player.animate("sequences.agility_climb_net")
                wait(distance)
                player.moveTo(destination)
                player.addXp(Skills.AGILITY, NET_XP)
                player.unlock()
            }
        }

        onObjectOption(TREE_BRANCH_UP_ID, "climb") {
            val obj = player.getInteractingGameObj() ?: return@onObjectOption
            val destination = Tile(obj.tile.x, obj.tile.z - 2, 2)
            val distance = player.tile.getDistance(destination)
            player.queue {
                player.lock()
                player.filterableMessage("You climb the tree...")
                player.animate("sequences.agility_climb_branch")
                wait(distance)
                player.moveTo(destination)
                player.addXp(Skills.AGILITY, BRANCH_UP_XP)
                player.filterableMessage("... to the platform above.")
                player.unlock()
            }
        }

        onObjectOption(BALANCING_ROPE_ID, "walk-on") {
            val obj = player.getInteractingGameObj() ?: return@onObjectOption
            val destination = Tile(2483, obj.tile.z, 2)
            val distance = player.tile.getDistance(destination)
            player.queue {
                player.lock()
                player.filterableMessage("You carefully cross the tightrope.")
                player.loopAnim("sequences.agility_balancing_rope")
                player.walkTo(destination, MovementQueue.StepType.FORCED_WALK, detectCollision = false)
                wait(distance)
                player.stopLoopAnim()
                player.addXp(Skills.AGILITY, ROPE_XP)
                player.filterableMessage("... to the next platform.")
                player.unlock()
            }
        }

        onObjectOption(TREE_BRANCH_DOWN_ID, "climb-down") {
            val obj = player.getInteractingGameObj() ?: return@onObjectOption
            val destination = Tile(obj.tile.x, obj.tile.z, 0)
            val distance = player.tile.height - destination.height
            player.queue {
                player.lock()
                player.filterableMessage("You climb down the tree...")
                player.animate("sequences.agility_climb_branch")
                wait(distance)
                player.moveTo(destination)
                player.addXp(Skills.AGILITY, BRANCH_DOWN_XP)
                player.filterableMessage("You land on the ground.")
                player.unlock()
            }
        }

        onObjectOption(OBSTACLE_NET_DOWN_ID, "climb-over") {
            val obj = player.getInteractingGameObj() ?: return@onObjectOption
            if (player.tile.z >= obj.tile.z) {
                player.filterableMessage("You can't climb the netting from this side.")
                return@onObjectOption
            }
            val destination = Tile(obj.tile.x, obj.tile.z + 2, 0)
            val distance = player.tile.getDistance(destination)
            player.queue {
                player.lock()
                player.filterableMessage("You climb the netting...")
                player.animate("sequences.agility_climb_net")
                wait(distance)
                player.moveTo(destination)
                player.addXp(Skills.AGILITY, NET_XP)
                player.unlock()
            }
        }

        PIPE_IDS.forEach { pipeId ->
            onObjectOption(pipeId, "squeeze-through") {
                val obj = player.getInteractingGameObj() ?: return@onObjectOption
                if (player.tile.z > obj.tile.z) {
                    return@onObjectOption
                }
                player.queue {
                    player.lock()
                    val pipeStart = Tile(obj.tile.x, obj.tile.z - 1)
                    if (player.tile != pipeStart) {
                        val distance = player.tile.getDistance(pipeStart)
                        player.walkTo(pipeStart)
                        wait(distance + 2)
                        player.faceTile(obj.tile)
                    }
                    player.filterableMessage("You squeeze into the pipe...")
                    player.animate("sequences.agility_pipe")
                    val firstMovement = ForcedMovement.of(
                        player.tile,
                        Tile(obj.tile.x, obj.tile.z + 2),
                        clientDuration1 = 10,
                        clientDuration2 = 70,
                        directionAngle = Direction.NORTH.angle,
                    )
                    wait(2)
                    player.forceMove(this, firstMovement)
                    wait(2)
                    val secondMovement = ForcedMovement.of(
                        player.tile,
                        Tile(obj.tile.x, obj.tile.z + 4),
                        clientDuration1 = 10,
                        clientDuration2 = 70,
                        directionAngle = Direction.NORTH.angle,
                    )
                    player.forceMove(this, secondMovement)
                    wait(2)
                    val thirdMovement = ForcedMovement.of(
                        player.tile,
                        Tile(obj.tile.x, obj.tile.z + 6),
                        clientDuration1 = 20,
                        clientDuration2 = 70,
                        directionAngle = Direction.NORTH.angle,
                    )
                    player.forceMove(this, thirdMovement)
                    player.addXp(Skills.AGILITY, PIPE_XP)
                    player.unlock()
                }
            }
        }
    }
}
