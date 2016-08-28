package org.klips.dsl

import org.klips.engine.Binding
import org.klips.engine.SimpleBinding

abstract class Fact(vararg  facets:Facet<*>) : Cloneable{
    private var facets_ : Array<Facet<*>> = Array(facets.size){ facets[it] }
    val facets : Array<Facet<*>>
        get(){ return facets_}

    @Suppress("UNCHECKED_CAST")
    val refs : List<Facet.FacetRef<*>>
    get() = facets.filter { it is Facet.FacetRef<*> } as List<Facet.FacetRef<*>>

    public override fun clone() = super.clone() as Fact

    fun substitute(action:(Facet<*>) -> Facet<*>?):Fact{
        val clone = this.clone()
        val copy = clone.facets_.copyOf()
        for(i in copy.indices){
            val new = action(copy[i])
            if(new != null)
                copy[i] = new
        }
        clone.facets_ = copy
        return clone
    }

    fun substitute(what:Facet<*>, with:Facet<*>) = substitute{
        when(it){
            what -> with
            else -> null
        }
    }

    fun substitute(data:Binding) = substitute{
        data[it]
    }

    override fun hashCode(): Int {
        var hc = this.javaClass.hashCode()

        for(i in facets.indices)
            hc += (i+1) * facets[i].hashCode()

        return hc
    }

    override fun equals(other: Any?): Boolean {
        if(other !is Fact)
            return false

        if(javaClass != other.javaClass)
            return false

        for(i in other.facets.indices)
            if(facets[i] != other.facets[i])
                return false

        return true
    }

    override fun toString() =  facets.joinToString (
            prefix = "${javaClass.simpleName}(",postfix = ")"){ "$it" }
}