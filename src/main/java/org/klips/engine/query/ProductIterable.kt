package org.klips.engine.query


class ProductIterable<T>(val left:Iterable<T>, val right:Iterable<T>) : Iterable<Pair<T,T>> {
    override fun iterator() = object : AbstractIterator<Pair<T,T>>() {
        val l = left.iterator()
        var r:Iterator<T>? = null

        var lV:T? = null
        var rV:T? = null

        tailrec override fun computeNext() {

            if(rV == null)
            {
                if(r == null || !r!!.hasNext()) {
                    r  = right.iterator()
                    if(!r!!.hasNext())
                    {
                        return done()
                    }
                    else {
                        lV = null
                        return computeNext()
                    }
                } else rV = r!!.next()
            }

            if(lV == null)
            {
                if(!l.hasNext()) {
                    done()
                    return
                } else lV = l.next()
            }

            setNext(Pair(lV!!,rV!!))
            rV = null
        }

    }

    override fun toString() = joinToString { it -> it.toString() }
}