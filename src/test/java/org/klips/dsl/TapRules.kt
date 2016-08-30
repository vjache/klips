package org.klips.dsl

import org.klips.engine.*

class TapRules : RuleSet() {

    var onMove = false
    var onAttack = false
    var onDeploy = false


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
    val hlth = ref<Level>("hlth")
    val hlth1 = ref<Level>("hlth1")
    val state = ref<State>("state")
    val state1 = ref<State>("state1")



    init {

        rule(group = "Adj-Symmetry") { // Symmetry of adjacency
            Adjacent(cid, cid1).assert()
            effect {
                Adjacent(cid1, cid).assert()
            }
        }

        rule(group = "Move") { // On Move

            TapCell(cid).retire()
            Adjacent(cid, cid1).assert()
            At(aid, cid1).assert()
            Actor(aid, pid, kind, nrgy, hlth, state).retire()
            ActorSelected(aid).assert()

            //guard(nrgy gt Level(3f))

            guard { it[nrgy].value > 3 }

            effect { sol ->
                val v = sol[nrgy].inc(-3f)
                Actor(aid, pid, kind, const(v), hlth, state).assert()
                onMove = true
            }
        }

        rule(group = "Attack") { // On Attack

            TapActor(aid).retire()

            retire( Actor(aid, pid, kind, nrgy, hlth, state),
                    At(aid, cid))

            assert(Adjacent(cid, cid1),
                    At(aid1, cid1),
                    Actor(aid1, pid1, kind1, nrgy1, hlth1, state1),
                    ActorSelected(aid1))

            guard { it[nrgy1].value > 3 }

            effect {
                onAttack = true
            }
        }

        rule(group = "Deploy") { // On Deploy

            TapActor(aid).retire()
            Actor(aid, pid, kind, nrgy, hlth, const(State.OnMarch)).retire()
            ActorSelected(aid).retire()

            effect { sol ->
                val v = sol[nrgy].inc(-3f)
                assert( Actor(aid, pid, kind, const(v), hlth, const(State.Deployed)) )
                onDeploy = true
            }
        }


    }
}