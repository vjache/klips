package org.klips

import org.klips.dsl.Facet
import org.klips.engine.Binding

class ReferenceNotBoundException(val ref : Facet.FacetRef<*>, binding: Binding)
: FacetNotBoundException(ref, binding){
    override val message = "Reference $ref is not bound in $binding."
}