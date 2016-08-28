package org.klips.dsl

import java.util.*

abstract class FacetBuilder {
    inner class FacetRefImpl<T : Comparable<T>>(id: String) : Facet.FacetRef<T>(id)

    private var idClock: Long = 0

    private fun newId() = "${idClock++}"

    fun <T : Comparable<T>> const(v:T): Facet<T> = Facet.ConstFacet(v)

    fun <T : Comparable<T>> ref(id: String) = FacetRefImpl<T>(id)

    infix fun <T:Comparable<T>> Facet.FacetRef<T>.gt2(t: Facet<T>) = Guard.BinaryPredicate(Guard.BinaryPredicate.Code.Gt, this, t)
    infix fun <T:Comparable<T>> Facet.FacetRef<T>.gt2(t: T) : Guard = gt2(Facet.ConstFacet(t))

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