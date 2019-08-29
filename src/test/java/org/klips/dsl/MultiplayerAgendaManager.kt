package org.klips.dsl

import org.klips.engine.Binding
import org.klips.engine.Modification
import org.klips.engine.rete.builder.AgendaManager
import org.klips.engine.rete.builder.RuleClause

interface Player : AgendaManager {
    fun startTurn()
}
class MultiplayerAgendaManager(
    val players: List<Player>,
    val getPlayer: (Binding, RuleClause) -> AgendaManager)
    : AgendaManager {

    class NoSuchPlayerException(msg: String) : Exception(msg)

    private var queue = players.iterator()
    private var player = queue.next()

    override fun next(): Pair<Modification<Binding>, RuleClause>? {
        while (true) {

            player.next()?.let { return it }

            if (queue.hasNext()) {
                player = queue.next().apply { startTurn() }
            }
            else {
                queue = players.iterator()
                return null
            }
        }
    }

    override fun add(solution: Modification<Binding>, ruleClause: RuleClause) {
        val player = getPlayer(solution.arg, ruleClause)
        if (!players.contains(player))
            throw NoSuchPlayerException("$player")
        player.add(solution, ruleClause)
    }

    override fun remove(solution: Modification<Binding>, ruleClause: RuleClause): Boolean {
        val player = getPlayer(solution.arg, ruleClause)
        if (!players.contains(player))
            throw NoSuchPlayerException("$player")
        return player.remove(solution, ruleClause)
    }

}