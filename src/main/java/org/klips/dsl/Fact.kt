package org.klips.dsl

import java.lang.reflect.Field
import kotlin.reflect.*
import kotlin.reflect.jvm.kotlinProperty

/**
 * Fact is a class representing a data piece describing
 * some aspect of a state of a world. Conceptually fact
 * is equivalent to the relation from relational algebra
 * (database theory), and cognate to a relation from a set theory.
 *
 * To define rules, firstly one must define an application
 * domain. Technically this means that a set of subclasses
 * of a [Fact] must be defined. Each [Fact] subclass must
 * have a primary constructor with properties of type [Facet].
 *
 * @see Facet
 */
abstract class Fact() : Cloneable {

    private val facetsByName: Map<String, Facet<*>> by lazy {
        val fields = classMeta.second
        fields.mapValues {
            it.value.call(this) as org.klips.dsl.Facet<*>
        }
    }

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
    val facets : List<Facet<*>> by lazy{ facetsByName.values.toList() }

    /**
     * All those facets which are references.
     */
    @Suppress("UNCHECKED_CAST")
    val refs : List<Facet.FacetRef<*>>
    get() = facets.filter { it is Facet.FacetRef<*> } as List<Facet.FacetRef<*>>

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