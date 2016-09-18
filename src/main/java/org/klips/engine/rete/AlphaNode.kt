package org.klips.engine.rete

import org.klips.dsl.Facet.FacetRef
import org.klips.dsl.Fact
import org.klips.engine.Modification
import org.klips.engine.PatternMatcher
import org.klips.engine.util.activationFailed
import org.klips.engine.util.activationHappen


abstract class AlphaNode(val pattern: Fact) : Node() {

    val matcher = PatternMatcher(pattern)

    override val refs: Set<FacetRef<*>>
        get() = matcher.refs

    fun accept(mdf:Modification<out Fact>):Boolean {
        matcher.bind(mdf.arg)?.let {
            if(modifyCache(mdf)) {
                activationHappen()
                notifyConsumers(mdf.inherit(it))
            }
            else
            {
                activationFailed()
            }
            return true
        }

        return false
    }

    abstract protected  fun modifyCache(mdf: Modification<out Fact>):Boolean

    ////////////////////////////////////////////////////////
//
//    override fun equals(other: Both?): Boolean {
//        if(other !is AlphaNode) return false
//
//        return pattern.equals(other.pattern)
//    }
//
//    override fun hashCode(): Int {
//        return pattern.hashCode()
//    }
//
    override fun toString() = "A-Node($pattern) [${System.identityHashCode(this)}]"
}