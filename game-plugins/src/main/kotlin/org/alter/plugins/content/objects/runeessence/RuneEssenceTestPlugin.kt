package org.alter.plugins.content.objects.runeessence

import org.alter.api.ext.message
import org.alter.game.Server
import org.alter.game.World
import org.alter.game.plugin.KotlinPlugin
import org.alter.game.plugin.PluginRepository

class RuneEssenceTestPlugin(
    r: PluginRepository,
    world: World,
    server: Server
) : KotlinPlugin(r, world, server) {

    init {
        val runeEssenceObject = "objects.blankrunestone"

        if (objHasOption(obj = runeEssenceObject, option = "Mine Rune Essence")) {
            onObjOption(obj = runeEssenceObject, option = "Mine Rune Essence") {
                player.message("Rune essence test event triggered.")
            }
        }
    }
}
