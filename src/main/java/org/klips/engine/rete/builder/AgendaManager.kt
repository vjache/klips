package org.klips.engine.rete.builder

import org.klips.engine.Binding
import org.klips.engine.Modification
import java.util.*

/**
 * Agenda manger is a driver which support a collection of activated rules
 * and decide what effect of which rule to apply to WM firstly.
 * Agenda manager could be also treated as a conflict resolver:
 *  given a set of rules activated, decide which activated rule to apply first.
 */
interface AgendaManager {
    /**
     * Return next rule to apply. Once rule is returned, usually it shouldn't be
     * returned at next call of `next()`.
     */
    fun next(): Pair<Modification<Binding>, RuleClause>?

    /**
     * Add activated rule to agenda.
     */
    fun add(solution: Modification<Binding>, ruleClause: RuleClause)

    /**
     * Remove agenda
     */
    fun remove(solution: Modification<Binding>, ruleClause: RuleClause): Boolean
}

/**
 * This is a simplest agenda manager which uses internal queue prioritized by priority & serialNo
 * of solution causing an activation.
 */
open class PriorityAgendaManager : AgendaManager {
    protected val pqueue = PriorityQueue<Pair<Modification<Binding>, RuleClause>>(100) { x, y ->
        val cmp = x.second.priority.compareTo(y.second.priority)
        if (cmp == 0)
            x.first.serialNo.compareTo(y.first.serialNo)
        else
            cmp
    }

    override fun next(): Pair<Modification<Binding>, RuleClause>? {
        if (pqueue.isEmpty())
            return null

        return pqueue.remove()
    }

    override fun add(solution: Modification<Binding>, ruleClause: RuleClause) {
        pqueue.add(solution to ruleClause)
    }

    override fun remove(solution: Modification<Binding>, ruleClause: RuleClause) = pqueue.remove(solution to ruleClause)
}

/**
 * This is a utility agenda manager which behave like a 'PriorityAgendaManager' while a priority of activated rules
 * have a value less than 'priorityThreshold' otherwise all calls delegated to the 'nextAgendaManager'.
 */
class EscalatingAgendaManager(
    private val priorityThreshold: Double,
    private val nextAgendaManager: AgendaManager) : PriorityAgendaManager() {

    override fun next(): Pair<Modification<Binding>, RuleClause>? {
        pqueue.poll()?.let { return it }

        return nextAgendaManager.next()
    }

    override fun add(solution: Modification<Binding>, ruleClause: RuleClause) {
        if (ruleClause.priority >= priorityThreshold)
            nextAgendaManager.add(solution, ruleClause)
        else
            super.add(solution, ruleClause)
    }

    override fun remove(solution: Modification<Binding>, ruleClause: RuleClause) =
        if (ruleClause.priority >= priorityThreshold)
            nextAgendaManager.remove(solution, ruleClause)
        else
            super.remove(solution, ruleClause)

}

