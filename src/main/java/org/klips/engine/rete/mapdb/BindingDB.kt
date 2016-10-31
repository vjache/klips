package org.klips.engine.rete.mapdb

import org.klips.engine.Binding
import org.mapdb.BTreeMap

interface BindingDB {
    val bindings : BTreeMap<Int, Binding>
}