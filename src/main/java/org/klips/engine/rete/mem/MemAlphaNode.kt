package org.klips.engine.rete.mem

import org.klips.dsl.Fact
import org.klips.engine.Binding
import org.klips.engine.Modification
import org.klips.engine.Modification.Assert
import org.klips.engine.Modification.Retire
import org.klips.engine.rete.AlphaNode
import org.klips.engine.util.Log
import java.util.*

class MemAlphaNode(log: Log, pattern:Fact) : AlphaNode(log, pattern) {
    private val set = HashSet<Binding>()
    override fun modifyCache(mdf: Modification<out Binding>): Binding? {
        val binding = mdf.arg
        when(mdf) {
            is Assert -> {
                if(binding in set)
                    return null
                set.add(binding)
                return binding
            }
            is Retire -> {
                if(binding !in set)
                    return null
                set.remove(binding)
                return binding
            }
        }
    }
}