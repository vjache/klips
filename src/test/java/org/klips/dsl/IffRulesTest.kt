package org.klips.dsl

import org.junit.Test
import org.klips.dsl.ActivationFilter.Both
import org.klips.engine.Adjacent
import org.klips.engine.CellId
import org.klips.engine.util.Log


class IffRulesTest {

    @Test
    fun adjacencySymmetry() {
        val rs = rules(Log(workingMemory = true, agenda = true)) {
            val cid = ref<CellId>("cid")
            val cid1 = ref<CellId>("cid1")
            rule("Adj-Symmetry") {
                +Adjacent(cid, cid1)
                effect(activation = Both) {
                    !Adjacent(cid1, cid)
                }
            }
        }

        rs.input.flush("Adj-Symmetry") {
            +Adjacent(CellId(0).facet, CellId(1).facet)
        }

        rs.input.flush("Adj-Symmetry") {
            -Adjacent(CellId(0).facet, CellId(1).facet)
        }
    }


}