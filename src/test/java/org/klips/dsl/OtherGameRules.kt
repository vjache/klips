package org.klips.dsl

import org.klips.engine.Adjacent
import org.klips.engine.CellId
import org.klips.engine.util.Log

/**
 * Created by vj on 17.10.16.
 */
object OtherGameRules: RuleSet(Log()) {
    val cid = ref<CellId>("cid")
    val cid1 = ref<CellId>("cid1")
    init {
        rule {
            +Adjacent(cid, cid1)
            effect { sol ->
                println(">>>>> $sol")
            }
        }
    }

}