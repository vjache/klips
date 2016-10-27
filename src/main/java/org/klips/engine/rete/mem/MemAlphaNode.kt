package org.klips.engine.rete.mem

import org.klips.dsl.Fact
import org.klips.engine.Modification
import org.klips.engine.Modification.Assert
import org.klips.engine.Modification.Retire
import org.klips.engine.rete.AlphaNode
import org.klips.engine.util.Log
import java.util.*

class MemAlphaNode(log: Log, pattern:Fact) : AlphaNode(log, pattern) {
    private val set = HashSet<Fact>()
    override fun modifyCache(mdf: Modification<out Fact>): Boolean {
        val fact = mdf.arg
        when(mdf) {
            is Assert -> {
                if(fact in set)
                    return false
                set.add(fact)
                return true
            }
            is Retire -> {
                if(fact !in set)
                    return false
                set.remove(fact)
                return true
            }
        }
    }
}