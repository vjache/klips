package org.klips.engine.rete.db

import org.klips.engine.Binding

interface BindingRepo {
    fun fetchBinding(id:Int) : Binding
}