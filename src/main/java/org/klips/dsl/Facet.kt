package org.klips.dsl

import org.klips.dsl.Guard.BinaryPredicate.Code.*
import java.util.*

@Suppress("unused")
interface Facet<T> : Comparable<Facet<*>> {

    @Suppress("UNCHECKED_CAST")
    open class ConstFacet<T:Comparable<T>>(val value:T): Facet<T>{

        override fun compareTo(other: Facet<*>): Int {
            return if(other is ConstFacet) value.compareTo(other.value as T)
            else javaClass.name.compareTo(other.javaClass.name)
        }

        override fun match(f: Facet<*>) = when(f){
            is IntervalFacet<*> ->
                value.javaClass == f.min.javaClass &&
                    value in f.min as T .. f.max as T
            is ConstFacet<*> ->
                value.javaClass == f.value.javaClass
                    && f.value as T == value
            is FacetRef<*> -> true
            else           -> false
        }

        override fun hashCode() = value.hashCode()

        override fun equals(other: Any?): Boolean {
            if(this === other)
                return true

            return other is ConstFacet<*> && value == other.value
        }

        override fun toString() = "$value/C"
    }

    class IntervalFacet<T:Comparable<T>>(id:String, val min:T, val max:T): FacetRef<T>(id){


        init{
            if(min > max) throw IllegalArgumentException()
        }

        @Suppress("UNCHECKED_CAST")
        override fun match(f: Facet<*>) = when(f){
                is IntervalFacet<*> ->
                    min.javaClass == f.min.javaClass &&
                            f.min as T in min..max &&
                            f.max as T in min..max
                is ConstFacet<*> ->
                    min.javaClass == f.value.javaClass
                            && f.value as T in min..max
                is FacetRef<*> -> true
                else           -> false
            }
        override fun toString() = "[$min..$max]"
    }

    open class FacetRef<T:Comparable<T>>(val id: String) : Facet<T>{

        override fun compareTo(other: Facet<*>): Int {
            return if(other is FacetRef)
                id.compareTo(other.id)
            else javaClass.name.compareTo(other.javaClass.name)
        }

        fun rename(newName:String) :FacetRef<T> = FacetRef(newName)

        fun decorated(prefix:String = "", postfix:String = "") = rename("$prefix$id$postfix")

        infix fun gt(t: Facet<T>) = Guard.BinaryPredicate(Gt, this, t)
        infix fun gt(t: T) : Guard = gt(ConstFacet(t))

        infix fun lt(t: Facet<T>) = Guard.BinaryPredicate(Lt, this, t)
        infix fun lt(t: T) : Guard = gt(ConstFacet(t))

        infix fun ge(t: Facet<T>) = Guard.BinaryPredicate(Ge, this, t)
        infix fun ge(t: T) : Guard = ge(ConstFacet(t))

        infix fun le(t: Facet<T>) = Guard.BinaryPredicate(Le, this, t)
        infix fun le(t: T) : Guard = le(ConstFacet(t))

        infix fun eq(t: Facet<T>) = Guard.BinaryPredicate(Eq, this, t)
        infix fun eq(t: T) : Guard = eq(ConstFacet(t))

        infix fun ne(t: Facet<T>) = Guard.BinaryPredicate(Ne, this, t)
        infix fun ne(t: T) : Guard = ne(ConstFacet(t))

        override fun match(f: Facet<*>) = true

        override fun equals(other: Any?) =
            other === this || (other is FacetRef<*> && id == other.id)

        override fun hashCode() = id.hashCode()

        override fun toString() = "$id/?"
    }

    class IntFacet(value:Int) :
            ConstFacet<Int>(value) {}

    class LongFacet(value:Long) :
            ConstFacet<Long>(value) {}

    class FloatFacet(value:Float) :
            ConstFacet<Float>(value) {}

    class DoubleFacet(value:Double) :
            ConstFacet<Double>(value) {}

    class StringFacet(value:String) :
            ConstFacet<String>(value) {}

    class DateFacet(value: Date) :
            ConstFacet<Date>(value) {}

    class BoolFacet(value:Boolean) :
            ConstFacet<Boolean>(value) {}

    fun match(f: Facet<*>): Boolean

    fun bind(f: Facet<*>): Pair<Facet<*>, Facet<*>>? = if (match(f)) Pair(f, this) else null



}