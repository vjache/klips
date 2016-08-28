package org.klips.engine.rete.builder

import org.klips.dsl.Fact
import org.klips.engine.Binding
import org.klips.engine.Modification

interface Trigger {
    fun fire(solution: Modification<Binding>, addEffect: (Modification<Fact>) -> Unit)
}