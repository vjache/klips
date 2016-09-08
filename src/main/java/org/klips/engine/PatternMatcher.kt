package org.klips.engine

import org.klips.dsl.Facet
import org.klips.dsl.Facet.FacetRef
import org.klips.dsl.Fact
import java.util.*

class PatternMatcher(val pattern: Fact) {

    private val patternIndex = LinkedHashMap<Facet<*>, MutableSet<Int>>(pattern.facets.size)
    private val refIndex       : Map<Facet<*>, MutableSet<Int>>
    private val refIndexBind   : Map<FacetRef<*>, Int>
    private val constIndex     : Map<Facet<*>, MutableSet<Int>>
    private val patternJavaClass = pattern.javaClass

    val refs : Set<FacetRef<*>>
        get() = refIndexBind.keys

    init{
        for(i in pattern.facets.indices)
        {
            val facet = pattern.facets[i]
            if(facet in patternIndex) patternIndex[facet]!!.add(i)
            else patternIndex[facet] = mutableSetOf(i)
        }

        refIndex   = patternIndex.filter { it.key is FacetRef<*> } // TODO: #1 See bellow
        constIndex = patternIndex.filter { it.key is Facet.ConstFacet<*> }

        refIndexBind   = mutableMapOf<FacetRef<*>, Int>().apply{
            for(e in refIndex) {
                val key = e.key
                if(key is FacetRef<*>) // TODO: #1 Probably need to make stronger typisation for refIndex.keys
                    put(key, e.value.first())
            }
        }

    }

    fun match(fact: Fact): Boolean {

        val actJavaClass = fact.javaClass

        if(!patternJavaClass.isAssignableFrom(actJavaClass))
            return false

        for (i in 0..pattern.facets.size - 1) {
            val f = pattern.facets[i]

            if (!f.match(fact.facets[i])) return false
        }

        return refIndex.all {
            var x:Facet<*>? = null
            for(i in it.value)
            {
                if(x == null)
                    x = fact.facets[i]
                else if(x != fact.facets[i])
                    return false
            }
            return true
        }
    }

    fun bind(fact: Fact): Binding? {
        if(!match(fact)) return null

        return FactBinding(fact, refIndexBind)
    }

}