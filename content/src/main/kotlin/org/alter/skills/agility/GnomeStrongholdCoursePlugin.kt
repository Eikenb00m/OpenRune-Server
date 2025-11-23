package org.alter.skills.agility

import org.alter.api.Skills
import org.alter.api.ext.addXp
import org.alter.api.ext.filterableMessage
import org.alter.api.ext.popRenderAnim
import org.alter.api.ext.pushRenderAnim
import org.alter.game.model.Direction
import org.alter.game.model.ForcedMovement
import org.alter.game.model.Tile
import org.alter.game.model.entity.Player
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
            player.queue {
                val distance = player.tile.getDistance(LOG_DESTINATION)
                val duration = distance * 30
                val movement = ForcedMovement.of(
                    src = player.tile,
                    dst = LOG_DESTINATION,
                    clientDuration1 = duration,
                    clientDuration2 = duration,
                    directionAngle = Direction.SOUTH.angle,
                )
                try {
                    player.lock()
                    player.filterableMessage("You walk carefully across the slippery log...")
                    player.pushRenderAnim("sequences.human_walk_logbalance")
                    player.faceTile(LOG_DESTINATION)
                    player.forceMove(this, movement)
                    player.filterableMessage("... and make it safely to the other side.")
                    player.addXp(Skills.AGILITY, LOG_BALANCE_XP)
                } finally {
                    player.popRenderAnim()
                    player.unlock()
                }
            }
        }
    }
}
