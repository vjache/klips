package org.klips.engine.query

import org.klips.dsl.Facet.FacetRef
import org.klips.dsl.Fact
import org.klips.engine.Binding
import org.klips.engine.PatternMatcher

class FactBindingSet(pattern : Fact,
                     private val facts   : Collection<Fact>) : BindingSet {

    val matcher = PatternMatcher(pattern)

    override val refs: Set<FacetRef<*>>
        get() = matcher.refs

    override val size: Int
        get() = this.count()

    override fun isEmpty() = facts.isEmpty()

    override fun iterator() = object : AbstractIterator<Binding>(){
        val iter = facts.iterator()
        override fun computeNext() {
            while(iter.hasNext()){
                val next = iter.next()
                matcher.bind(next)?.let {
                    setNext(it)
                    return
                }
            }
            done()
        }
    }

    override fun toString() = "${javaClass.simpleName}(${matcher.pattern}, ${joinToString()})"

}