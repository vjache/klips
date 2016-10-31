package org.klips.dsl

import org.klips.dsl.Facet.FacetRef
import java.lang.reflect.Field
import java.util.*
import kotlin.reflect.*
import kotlin.reflect.jvm.kotlinProperty

/**
 * Fact is a class representing a data piece describing
 * some aspect of a state of a world. Conceptually fact
 * is equivalent to the relation from relational algebra
 * (database theory), and cognate to a relation from a alphaBindings theory.
 *
 * To define rules, firstly one must define an application
 * domain. Technically this means that a alphaBindings of subclasses
 * of a [Fact] must be defined. Each [Fact] subclass must
 * have a primary constructor with properties of type [Facet].
 *
 * @see Facet
 */
abstract class Fact() : Cloneable {

    private val classMeta: Pair<KFunction<*>, Map<String, KProperty.Getter<*>>> = let {
            val jc = this.javaClass
            metadataByClass.getOrPut(jc){
                val kClass = jc.kotlin
                val fields = fields(jc)
                val primaryConstructor = kClass.primaryConstructor!!
                val parameters = primaryConstructor.parameters
                val primConstrParamNames = parameters.map { it.name }
                validateConstructorParameters(kClass, parameters, fields)
                val fieldsByName = fields.filter {
                    it.type == org.klips.dsl.Facet::class.java && it.name in primConstrParamNames
                }.map {
                    kotlin.Pair(it.name, it.kotlinProperty!!.getter)
                }.toMap()
                kotlin.Pair(primaryConstructor, fieldsByName)
            }
        }

    private fun validateConstructorParameters(kclass : KClass<out Fact>, parameters: List<KParameter>, fields: List<Field>) {
        val fieldNames = fields.map { it.name }
        val paramNames = parameters.map { it.name }
        val badParams  = paramNames.filter { it !in fieldNames }
        if(badParams.isNotEmpty())
            throw IllegalArgumentException(
                    "Primary constructor parameters of $kclass " +
                            "do not match fields: $badParams")
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
                addAll(fields(cls.superclass))
                addAll(cls.declaredFields)
            }
        }
        else {
            return emptyList()
        }
    }

    /**
     * All facets this fact have.
     */
    val facets : List<Facet<*>>
        get() = facets_!!

    private val facets_ : List<Facet<*>>? = null
        get(){
            if (field == null)
            {
                field = classMeta.second.map { it.value.call(this) as Facet<*> }
            }
            return field
        }

    /**
     * All those facets which are references.
     */
    @Suppress("UNCHECKED_CAST")
    val refs : List<FacetRef<*>>
    get() = facets.filter { it is FacetRef<*> } as List<FacetRef<*>>

    /**
     * Creates a new fact instance form this one but with
     * some facets replaced by others using a lambda. If
     * lambda returns 'null' then facet will not be replaced.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Fact> substitute(action:(Facet<*>) -> Facet<*>?) : T {
        val (constr, fields) = classMeta
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
        val newFact = constr.call(*arr) as T
        return newFact
    }

    override fun hashCode(): Int {
        var hc = this.javaClass.hashCode()

        val fcs = facets_!!

        for(i in fcs.indices)
            hc += (i+1) * fcs[i].hashCode()

        return hc
    }

    override fun equals(other: Any?): Boolean {
        if(other !is Fact)
            return false

        if(javaClass != other.javaClass)
            return false

        val fcs1 = facets_!!
        val fcs2 = other.facets_!!
        for(i in fcs2.indices)
            if(fcs1[i] != fcs2[i])
                return false

        return true
    }

    override fun toString() = "${this.javaClass.simpleName}${classMeta.second.mapValues { it.value.call(this) as Facet<*> }}"

}