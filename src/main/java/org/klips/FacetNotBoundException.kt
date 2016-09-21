package org.klips

import org.klips.dsl.Facet
import org.klips.engine.Binding

open class FacetNotBoundException(val facet : Facet<*>, val binding: Binding) : IllegalArgumentException(){
    override val message: String = "Facet $facet is not bound in $binding."
}