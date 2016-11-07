package org.klips.engine.util

class ListSet<E>(private val listInit:() -> List<E>) : AbstractCollection<E>(), Set<E> {
    override val collection: List<E>
        get() = list!!

    private val list : List<E>? = null
        get() {
            if (field == null)
                field = listInit()
            return field
        }

    override fun iterator(): Iterator<E> = ListIterator(list!!)
}
class ListIterator<E>(val list:List<E>) : AbstractIterator<E>() {
    var i = 0
    val sz = list.size
    override fun computeNext() {
        if (i == sz)
            done()
        else {
            setNext(list[i])
            i++
        }
    }
}

class MappedIterator<E0, E>(val iter:Iterator<E0>, val mapFunc:(E0) -> E) : AbstractIterator<E>() {
    override fun computeNext() {
        if (!iter.hasNext())
            done()
        else
            setNext(mapFunc(iter.next()))
    }

}

class MappedCollectionSet<V1,V2>(val former:Collection<V1>, val mapFunc:(V1) -> V2) : Set<V2>
{
    override val size: Int
        get() = former.size

    override fun contains(element: V2): Boolean = former.find { mapFunc(it) == element } != null

    override fun containsAll(elements: Collection<V2>): Boolean = elements.all { it in this }

    override fun isEmpty(): Boolean = former.isEmpty()

    override fun iterator(): Iterator<V2> = MappedIterator(former.iterator(), mapFunc)

}

abstract class AbstractCollection<E>() : Collection<E> {
    protected abstract val collection : Collection<E>

    override val size : Int
        get() = collection.size

    override fun contains(element: E): Boolean = element in collection

    override fun containsAll(elements: Collection<E>): Boolean = elements.all { it in this }

    override fun isEmpty(): Boolean = collection.isEmpty()
}

fun <K,V> MutableMap<K,V>.putIfAbsent(key:K, vlaue:V): V? {
    val v = get(key)
    if(v == null){
        put(key, vlaue)
        return null
    } else {
        return v
    }
}