package org.alter.skills.mining

import org.alter.api.Skills
import org.alter.game.pluginnew.PluginEvent
import org.alter.rscm.RSCM.getRSCM

/**
 * Handles rune essence mining specific behaviour.
 */
class RuneEssencePlugin : PluginEvent() {

    override fun init() {
        on<RockOreObtainedEvent> {
            where { rockType == RUNE_ESSENCE_ROCK_TYPE }
            then {
                val miningLevel = player.getSkills().getBaseLevel(Skills.MINING)
                if (miningLevel >= PURE_ESSENCE_LEVEL) {
                    player.attr[MiningPlugin.ORE_OVERRIDE_ATTR] = PURE_ESSENCE_ITEM
                }
            }
        }
    }

    companion object {
        private const val PURE_ESSENCE_LEVEL = 30
        private val RUNE_ESSENCE_ROCK_TYPE = getRSCM("dbrows.mining_essence")
        private val PURE_ESSENCE_ITEM = getRSCM("items.blankrune_high")
    }
}
