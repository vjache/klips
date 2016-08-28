package org.klips.engine.query

import org.klips.dsl.Facet
import org.klips.engine.Binding
import org.klips.engine.ComposeBinding
import org.klips.engine.EmptyBinding
import org.klips.engine.query.JoinBindingSet.JoinType.*

class JoinBindingSet(l: BindingSet, r: BindingSet, private val type: JoinType) : BindingSet {

    enum class JoinType {
        Inner,
        LeftOuter,
        RightOuter,
        FullOuter
    }

    constructor(l: BindingSet, r: BindingSet) : this(l, r, Inner)

    private val leftSorted by lazy {
        SortedBindingSet(l, commonRefs)
    }

    private val rightSorted by lazy {
        SortedBindingSet(r, commonRefs)
    }

    val commonRefs = l.refs.intersect(r.refs).toList().sorted()

    override val refs: Set<Facet.FacetRef<*>>
            = l.refs.union(r.refs)

    override val size: Int
        get() = count()

    override fun isEmpty() = !iterator().hasNext()

    override fun iterator(): Iterator<Binding> {
        return object : AbstractIterator<Binding>() {
            val bc = BindingComparator(commonRefs)
            val leftIter = leftSorted.iterator()
            val rightIter = rightSorted.iterator()

            var vL: Binding? = null
            var vR: Binding? = null

            var prod: Iterator<Pair<Binding, Binding>>? = null

            tailrec override fun computeNext() {
                val p = prod
                p?.let {
                    if (p.hasNext()) {
                        setNext(ComposeBinding(p.next()))
                        return
                    } else {
                        prod = null
                    }
                }

                if (vL == null && leftIter.hasNext())
                    vL = leftIter.next()

                if (vR == null && rightIter.hasNext())
                    vR = rightIter.next()

                if ((vL == null && vR == null) ||
                    (vL == null && (type == LeftOuter  || type == Inner)) ||
                    (vR == null && (type == RightOuter || type == Inner))) {
                    return done()
                }

                if(vL == null && (type == RightOuter || type == FullOuter))
                {
                    setNext(ComposeBinding(EmptyBinding(), vR!!))
                    vR = null
                    return
                }

                if(vR == null && (type == LeftOuter || type == FullOuter))
                {
                    setNext(ComposeBinding(vL!!, EmptyBinding()))
                    vL = null
                    return
                }

                val cmp = bc.compare(vL, vR)
                if (cmp == 0) {

                    val (l, vL1) = leftIter.nextWhile(vL!!)  { bc.compare(it, vL) == 0 }
                    val (r, vR1) = rightIter.nextWhile(vR!!) { bc.compare(it, vR) == 0 }

                    prod = ProductIterable(l, r).iterator()

                    vL = vL1
                    vR = vR1
                    return computeNext()
                } else if (cmp < 0) { // Left element leaves
                    when(type){
                        RightOuter,
                        Inner -> {
                            vL = null
                            return computeNext()
                        }
                        else ->
                        {
                            setNext(ComposeBinding(vL!!, EmptyBinding()))
                            vL = null
                        }
                    }
                } else { // Right element leaves
                    when(type) {
                        LeftOuter,
                        Inner -> {
                            vR = null
                            return computeNext()
                        }
                        else -> {
                            setNext(ComposeBinding(EmptyBinding(), vR!!))
                            vR = null
                        }
                    }
                }
            }
        }
    }

    override fun toString() =
            joinToString(
                    separator = ",\r\n\t",
                    prefix = "${javaClass.simpleName}($commonRefs, ${leftSorted.size}, ${rightSorted.size}, \r\n\t",
                    postfix = ")")

    fun <T> Iterator<T>.nextWhile(first: T, predicate: (t: T) -> Boolean): Pair<MutableList<T>, T?> {
        val res = mutableListOf(first)
        while (hasNext()) {
            val v = next()
            if (!predicate(v)) return Pair(res, v)
            res.add(v)
        }
        return Pair(res, null)
    }
}

