package org.klips.dsl

import java.util.*

/**
 * This abstract class is a context which support facet
 * creation operators.This class is not intended to be used directly.
 */
abstract class FacetBuilder {
    inner class FacetRefImpl<T : Comparable<T>>(id: String) : Facet.FacetRef<T>(id)

    private var idClock: Long = 0

    private fun newId() = "${idClock++}"

    /**
     * Create facet constant. Facet constant wraps particular value.
     *
     * @see Facet
     */
    fun <T : Comparable<T>> const(v:T): Facet<T> = Facet.ConstFacet(v)

    /**
     * Create named facet reference. Facet reference used to construct
     * a fact pattern. Several fact patterns form a complex pattern
     * in which all occurrences of the same reference must be bound to
     * the same value ([Facet.ConstFacet]).
     *
     * @see Facet
     */
    fun <T : Comparable<T>> ref(id: String) = FacetRefImpl<T>(id)

    fun intRef()                    = intRef(newId())
    fun intRef(id:String)           = FacetRefImpl<Int>(id)
    fun intRefs(vararg ids: String) = Array(ids.size, { intRef(ids[it]) })
    fun const(v : Int)              = Facet.IntFacet(v)

    fun strRef()                    = strRef(newId())
    fun strRef(id:String)           = FacetRefImpl<String>(id)
    fun strRefs(vararg ids: String) = Array(ids.size, { strRef(ids[it]) })
    fun const(v : String)           = Facet.StringFacet(v)

    fun floatRef()                    = floatRef(newId())
    fun floatRef(id:String)           = FacetRefImpl<Float>(id)
    fun floatRefs(vararg ids: String) = Array(ids.size, { floatRef(ids[it]) })
    fun const(v : Float)              = Facet.FloatFacet(v)

    fun dateRef()                    = dateRef(newId())
    fun dateRef(id:String)           = FacetRefImpl<Float>(id)
    fun dateRefs(vararg ids: String) = Array(ids.size, { dateRef(ids[it]) })
    fun const(v : Date)              = Facet.DateFacet(v)
}