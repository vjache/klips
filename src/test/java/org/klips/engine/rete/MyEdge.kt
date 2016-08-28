package org.klips.engine.rete

class MyEdge<A,B,C>(val triple:Triple<A,B,C>){

    constructor(a:A, b:B, c:C):this(Triple(a,b,c))

    override fun equals(other: Any?): Boolean {
        if (other !is MyEdge<*, *, *>) return false

        return other.triple.equals(this.triple)
    }

    override fun hashCode(): Int {
        return MyEdge::class.java.hashCode() + triple.hashCode()
    }

    override fun toString() = triple.third.toString()
}