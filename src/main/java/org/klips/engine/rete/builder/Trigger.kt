package org.klips.engine.rete.builder

import org.klips.dsl.ActivationFilter
import org.klips.dsl.Fact
import org.klips.engine.Binding
import org.klips.engine.Modification

interface Trigger {
    fun checkGuard(cache: MutableMap<Any,Any>,
                   solution: Modification<Binding>):Boolean
    fun filter() : ActivationFilter
    fun fire(cache:MutableMap<Any,Any>, solution: Modification<Binding>, addEffect: (Modification<Fact>) -> Unit)
}