package org.klips.engine.rete.builder

import org.klips.dsl.Fact
import org.klips.engine.query.BindingSet
import org.klips.engine.query.FactBindingSet
import org.klips.engine.query.JoinBindingSet
import org.klips.engine.query.JoinBindingSet.JoinType

object Util {

    fun select(facts:Collection<Fact>, patt:Collection<Fact>, joinType: JoinType = JoinType.Inner) = patt.map {
            FactBindingSet(it, facts)
        }.reduce<BindingSet, FactBindingSet> { l, r ->  JoinBindingSet(l,r, joinType)}

}