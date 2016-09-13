package org.klips.dsl

sealed class Occur {
    class Once(vararg val facets: Facet<*>)
}