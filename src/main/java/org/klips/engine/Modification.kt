package org.klips.engine


sealed class Modification<T> (val arg:T) {

    class Assert<T>(arg:T) : Modification<T>(arg) {
        override fun inverse()           = Retire(arg)
        override fun <A> inherit(arg: A) = Assert(arg)
    }
    class Retire<T>(arg:T) : Modification<T>(arg) {
        override fun inverse()           = Assert(arg)
        override fun <A> inherit(arg: A) = Retire(arg)
    }

    abstract fun inverse(): Modification<T>
    abstract fun <A> inherit(arg: A): Modification<A>

    override fun toString() = "${this.javaClass.simpleName}($arg)"

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true

        if (other !is Modification<*>)
            return false

        val b1 = this is Assert
        val b2 = other is Assert<*>
        if (b1 != b2) return false

        return this.arg!!.equals(other.arg)
    }

    override fun hashCode(): Int {
        return this.javaClass.hashCode() + arg!!.hashCode()
    }
}