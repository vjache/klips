package org.klips.dsl

import org.klips.engine.*
import org.klips.engine.State.Deployed
import org.klips.engine.State.OnMarch
import org.klips.engine.util.Log

class TapRules : RuleSet(Log(workingMemory = true, agenda = true)) {

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

        rule(name = "Adj-Symmetry") { // Symmetry of adjacency
            +Adjacent(cid, cid1)
            effect {
                +Adjacent(cid1, cid)
            }
        }

        rule(name = "Move") { // On Move

            -TapCell(cid)
            +Adjacent(cid, cid1)
            +At(aid, cid1)
            -Actor(aid, pid, kind, nrgy, hlth, state)
            +ActorSelected(aid)

            guard { it[nrgy].value > 3 }

            effect { sol ->
                val v = sol[nrgy].inc(-3f).facet
                +Actor(aid, pid, kind, v, hlth, state)
                onMove = true
            }
        }

        rule(name = "Attack") { // On Attack

            +TapActor(aid)
            -Actor(aid, pid, kind)
            -At(aid, cid)
            +Adjacent(cid, cid1)
            +At(aid1, cid1)
            +Actor(aid1, pid1, kind1, nrgy1, hlth1)
            +ActorSelected(aid1)

            guard { it[nrgy1].value > 3 }

            effect {
                onAttack = true
            }
        }

        rule(name = "Deploy") { // On Deploy

            -TapActor(aid)
            -Actor(aid, pid, kind, nrgy, hlth, OnMarch.facet)
            -ActorSelected(aid)

            effect { sol ->
                val v = sol[nrgy].inc(-3f)
                +Actor(aid, pid, kind, v.facet, hlth, Deployed.facet)
                onDeploy = true
            }
        }


    }
}