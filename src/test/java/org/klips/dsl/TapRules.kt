package org.klips.dsl

import org.klips.dsl.RuleSet
import org.klips.dsl.get
import org.klips.engine.*
import org.klips.engine.Modification.Assert
import org.klips.engine.Modification.Retire
import org.klips.dsl.ActivationFilter.Both

class TapRules : RuleSet() {

    var onMove = false
    var onAttack = false

    val cid = ref<CellId>("cid")
    val cid1 = ref<CellId>("cid1")
    val aid = ref<ActorId>("aid")
    val aid1 = ref<ActorId>("aid1")
    val kind = ref<ActorKind>("kind")
    val kind1 = ref<ActorKind>("kind1")
    val pid = ref<PlayerId>("pid")
    val pid1 = ref<PlayerId>("pid1")
    val nrgy = ref<Level>("nrgy")
    val nrgy1 = ref<Level>("nrgy1")

    init {

        rule { // Symmetry of adjacency
            Adjacent(cid, cid1).assert()
            effect {
                Adjacent(cid1, cid).assert()
            }
        }

        rule { // On Move
            TapCell(cid).retire()
            Adjacent(cid, cid1).assert()
            At(aid, cid1).assert()
            Actor(aid, pid, kind, nrgy).retire()
            ActorSelected(aid).assert()

            //guard(nrgy gt Level(3f))

            guard { it[nrgy].value > 3 }

            effect(activation = Both) { sol ->
                when (sol) {
                    is Assert -> {
                        val v = sol[nrgy].inc(-3f)
                        Actor(aid, pid, kind, const(v)).assert()
                        onMove = true
                        //println("ASSERT: Actor[$v] ${sol[aid]} move on tile ${sol[cid]}.")
                    }
                    is Retire -> {}
                        //println("RETIRE: Actor ${sol[aid]} move on tile ${sol[cid]}.")
                }
            }
        }

        rule { // On Attack
            TapActor(aid).retire()
            retire(
                    Actor(aid, pid, kind, nrgy),
                    At(aid, cid))
            assert(Adjacent(cid, cid1),
                    At(aid1, cid1),
                    Actor(aid1, pid1, kind1, nrgy1),
                    ActorSelected(aid1))
            effect(activation = Both) { sol ->
                when (sol) {
                    is Assert -> {
                        onAttack = true
                        //println("ASSERT: Actor ${sol[aid1]} attack actor ${sol[aid]}.")
                    }
                    is Retire -> {}
                    //println("RETIRE: Actor ${sol[aid1]} attack actor ${sol[aid]}.")
                }
            }
        }

    }
}