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

    val refs : Set<FacetRef<*>>
        get() = refIndexBind.keys

    init{
        for(i in pattern.facets.indices)
        {
            val facet = pattern.facets[i]
            if(facet in patternIndex) patternIndex[facet]!!.add(i)
            else patternIndex[facet] = mutableSetOf(i)
        }

        refIndex   = patternIndex.filter { it.key is FacetRef<*> }
        constIndex = patternIndex.filter { it.key is Facet.ConstFacet<*> }

        refIndexBind   = mutableMapOf<FacetRef<*>, Int>().apply{
            for(e in refIndex) {
                val key = e.key
                if(key is FacetRef<*>)
                    put(key, e.value.first())
            }
        }

    }

    fun match(fact: Fact): Boolean {
        if(fact.javaClass != pattern.javaClass)
            return false
        for(i in 0 .. pattern.facets.size - 1)
        {
            val f = pattern.facets[i]

            if(!fact.facets[i].match(f)) return false
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