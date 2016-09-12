package org.klips.dsl

import org.klips.engine.*
import org.klips.engine.ActorKind.Guard
import org.klips.engine.State.Deployed
import org.klips.engine.State.OnMarch

class GameRules : RuleSet() {

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

            -MoveCommand(aid, cid1)
            -UnaryCommand(aid)
            -At(aid, cid)
            +Adjacent(cid, cid1)
            val a = -Actor(aid = aid, energy = nrgy)

            guard { it[nrgy].value > 3 }

            effect { sol ->
                val v = sol[nrgy].inc(-3f).facet
                +a.substitute(nrgy to v)
                +At(aid, cid1)
            }
        }



        rule(name = "Deploy") {
            -DeployCommand(aid)
            val a = -Actor(aid = aid, energy = nrgy, state = OnMarch.facet)
            guard { it[nrgy].value > 3 }
            effect { sol ->
                +a.substitute(
                        nrgy to sol[nrgy].inc(-3f).facet,
                        OnMarch.facet to Deployed.facet)
            }
        }

        rule(name = "Undeploy") {
            -UndeployCommand(aid)
            val a = -Actor(aid = aid, energy = nrgy, state = Deployed.facet)
            guard { it[nrgy].value > 3 }
            effect { sol ->
                +a.substitute(
                        nrgy to sol[nrgy].inc(-3f).facet,
                        Deployed.facet to OnMarch.facet)
            }
        }

        rule(name = "Attack") { // On Move

            -AttackCommand(aid, aid1)
            //-UnaryCommand(aid)
            +At(aid, cid)
            +Adjacent(cid, cid1)
            +At(aid1, cid1)
            val a  = -Actor(aid = aid, energy = nrgy, type = Guard.facet, state = OnMarch.facet, pid = pid)
            val a1 = -Actor(aid = aid1, health = hlth1, pid = pid1)

            guard { it[nrgy].value > 5 }
            guard (pid ne pid1)

            effect { sol ->
                +a.substitute(nrgy to sol[nrgy].inc(-5f).facet)
                +a1.substitute(hlth1 to sol[hlth1].inc(-2f).facet)
            }
        }

        rule (name = "Charge"){
            -ChargeCommand(aid, aid1)
            +At(aid,cid)
            +At(aid1,cid1)
            +Adjacent(cid,cid1)
            val a  = -Actor(aid = aid,  energy = nrgy, type = ActorKind.Solar.facet)
            val a1 = -Actor(aid = aid1, energy = nrgy1)

            guard { it[nrgy].value > 5 }

            effect { sol ->
                +a.substitute(nrgy to sol[nrgy].inc(-5f).facet)
                +a1.substitute(nrgy1 to sol[nrgy1].inc(4f).facet)
            }
        }

    }
}