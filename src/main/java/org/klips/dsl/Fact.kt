package org.klips.dsl

import org.klips.engine.Binding
import java.lang.reflect.Field
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.kotlinProperty
import kotlin.reflect.primaryConstructor

abstract class Fact() : Cloneable {

    private val facetsByName: Map<String, Facet<*>>
        get() {
            val fields = classMeta(this).second
            return fields.mapValues { it.value.call(this) as Facet<*> }
        }

    private companion object Metadata {
        val metadataByClass = mutableMapOf<
                Class<*>,
                Pair<KFunction<*>, Map<String, KProperty.Getter<*>>> >()
    }

    private fun fields(cls:Class<*>?) : List<Field>{
        if(cls != null)
        {
            return mutableListOf<Field>().apply {
                addAll(cls.declaredFields)
                addAll(fields(cls.superclass))
            }
        }
        else {
            return emptyList()
        }
    }
    private fun classMeta(obj:Any): Pair<KFunction<*>, Map<String, KProperty.Getter<*>>> {
        val jc = obj.javaClass
        return metadataByClass.getOrPut(jc){
            val fields = fields(jc)
            val kClass = jc.kotlin
            val primaryConstructor = kClass.primaryConstructor!!
            val primConstrParamNames = primaryConstructor.parameters.map { it.name }
            val fieldsByName = fields.filter {
                it.type == Facet::class.java && it.name in primConstrParamNames
            }.map {
                Pair(it.name, it.kotlinProperty!!.getter)
            }.toMap()
            Pair(primaryConstructor, fieldsByName)
        }
    }

    val facets : List<Facet<*>> by lazy{ facetsByName.values.toList() }

    @Suppress("UNCHECKED_CAST")
    val refs : List<Facet.FacetRef<*>>
    get() = facets.filter { it is Facet.FacetRef<*> } as List<Facet.FacetRef<*>>

    fun substitute(action:(Facet<*>) -> Facet<*>?) : Fact {
        val (constr, fields) = classMeta(this)
        val args = constr.parameters.map {
            val arg = fields[it.name]!!.call(this)
            if (arg is Facet<*>)
            {
                action(arg) ?: arg
            }
            else
                arg
        }
        val arr:Array<Any?> = args.toTypedArray()
        return constr.call(*arr) as Fact
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

    override fun toString() = "${this.javaClass.simpleName}$facetsByName"

}