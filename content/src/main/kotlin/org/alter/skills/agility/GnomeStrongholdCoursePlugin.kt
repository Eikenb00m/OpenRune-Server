package org.alter.skills.agility

import org.alter.api.Skills
import org.alter.api.ext.addXp
import org.alter.api.ext.filterableMessage
import org.alter.api.ext.loopAnim
import org.alter.api.ext.stopLoopAnim
import org.alter.game.model.Tile
import org.alter.game.model.entity.Player
import org.alter.game.model.move.MovementQueue
import org.alter.game.model.move.walkTo
import org.alter.game.pluginnew.PluginEvent
import org.alter.game.pluginnew.event.impl.onObjectOption

class GnomeStrongholdCoursePlugin : PluginEvent() {

    private companion object {
        private const val LOG_BALANCE_ID = 23145

        private const val LOG_BALANCE_XP = 7.5

        private val LOG_DESTINATION = Tile(2474, 3429, 0)
    }

    override fun init() {
        onObjectOption(LOG_BALANCE_ID, "walk-across") {
            val distance = player.tile.getDistance(LOG_DESTINATION)
            player.queue {
                player.filterableMessage("You walk carefully across the slippery log...")
                player.loopAnim("sequences.human_walk_logbalance")
                player.walkTo(LOG_DESTINATION, MovementQueue.StepType.FORCED_WALK, detectCollision = false)
                player.lock()
                wait(distance + 2)
                player.stopLoopAnim()
                player.filterableMessage("... and make it safely to the other side.")
                player.addXp(Skills.AGILITY, LOG_BALANCE_XP)
                player.unlock()
            }
        }
    }
}
