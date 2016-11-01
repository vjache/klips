package org.klips.engine.rete.mapdb

import org.klips.engine.Binding
import org.mapdb.BTreeMap

interface BindingDB {
    fun fetchBinding(id:Int) : Binding
}