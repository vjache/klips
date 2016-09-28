package org.klips.dsl

import org.klips.PatternNotConnectedException
import org.klips.dsl.ActivationFilter.Both
import org.klips.engine.*
import org.klips.engine.ActorKind.Aim
import org.klips.engine.ActorKind.Guard
import org.klips.engine.Modification.Assert
import org.klips.engine.Modification.Retire
import org.klips.engine.State.Deployed
import org.klips.engine.State.OnMarch
import org.klips.engine.util.Log
import java.lang.Math.*
import kotlin.test.assertFailsWith

class GameRules : RuleSet(Log()) {

    val triggered = mutableListOf<String>()


    val cid = ref<CellId>("cid")
    val cid1 = ref<CellId>("cid1")
    val cid2 = ref<CellId>("cid2")
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

        rule(name = "CreateAgent") { // On Move
            -CreateAgentCommand(aid, cid1, kind)
            +Actor(aid = aid, type = Aim.facet, state = Deployed.facet, pid = pid)
            +At(aid, cid)
            +Adjacent(cid, cid1)

            effect { sol ->
                val newAid = ActorId()
                +Actor(newAid, sol[pid], sol[kind])
                +At(newAid.facet, cid1)
            }
        }

        rule(name = "Move") { // On Move

            -MoveCommand(aid, cid1)
            val cf = +CommField(aid1,cid1)
            -At(aid, cid)
            +Adjacent(cid, cid1)
            val a = -Actor(aid = aid, energy = nrgy, state = OnMarch.facet)

            guard {
                it[nrgy].value > 3
            }

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
            val a = -Actor(aid = aid, energy = nrgy, type = Guard.facet, state = OnMarch.facet, pid = pid)
            val a1 = -Actor(aid = aid1, health = hlth1, pid = pid1)

            guard { it[nrgy].value > 5 }
            guard(pid ne pid1)

            effect { sol ->
                +a.substitute(nrgy to sol[nrgy].inc(-5f).facet)
                +a1.substitute(hlth1 to sol[hlth1].inc(-2f).facet)
            }
        }

        rule(name = "Charge") {
            -ChargeCommand(aid, aid1)
            +At(aid, cid)
            +At(aid1, cid1)
            +Adjacent(cid, cid1)
            val a = -Actor(aid = aid, energy = nrgy, type = ActorKind.Solar.facet)
            val a1 = -Actor(aid = aid1, energy = nrgy1)

            guard { it[nrgy].value > 5 }

            effect { sol ->
                +a.substitute(nrgy to sol[nrgy].inc(-5f).facet)
                +a1.substitute(nrgy1 to sol[nrgy1].inc(4f).facet)
            }
        }

        rule(name = "Repair") {
            -RepairCommand(aid, aid1)
            +At(aid, cid)
            +At(aid1, cid1)
            +Adjacent(cid, cid1)
            val a = -Actor(aid = aid, energy = nrgy, type = ActorKind.Worker.facet)
            val a1 = -Actor(aid = aid1, health = hlth1)

            guard { it[nrgy].value > 5 }

            effect { sol ->
                +a.substitute(nrgy to sol[nrgy].inc(-5f).facet)
                +a1.substitute(hlth1 to sol[hlth1].inc(2f).facet)
            }
        }

        rule(name = "Scrap") {
            -ScrapCommand(aid, aid1)
            +At(aid, cid)
            +At(aid1, cid1)
            +Adjacent(cid, cid1)
            +Actor(aid = aid, type = ActorKind.Aim.facet)
            -Actor(aid = aid1)

            effect { }
        }

        rule(name = "FeedAim") {
            -FeedAimCommand(aid, aid1)
            +At(aid, cid)
            +At(aid1, cid1)
            +Adjacent(cid, cid1)
            +Actor(aid = aid, type = ActorKind.Worker.facet)
            -Actor(aid = aid1, type = ActorKind.Aim.facet)

            effect { }
        }

        rule(name = "TurnStart.Solar.Charge") {
            +Turn(pid)
            val a = -Actor(pid = pid, aid = aid, energy = nrgy, type = ActorKind.Solar.facet)

            onceBy(pid, aid)

            effect { sol ->
                +a.substitute(nrgy to sol[nrgy].inc(5f).facet)
            }
        }

        rule(name = "Comm.Field.OnMarche") {
            +Actor(aid = aid, type = ActorKind.Comm.facet, state = State.OnMarch.facet)
            +At(aid = aid, cid = cid)
            +Adjacent(cid1 = cid, cid2 = cid1)
            effect(activation = Both) { sol ->
                when (sol)
                {
                    is Assert -> +CommField(aid, cid1)
                    is Retire -> -CommField(aid, cid1)
                }
            }
        }

        rule(name = "Comm.Field.Deployed") {
            +Actor(aid = aid, type = ActorKind.Comm.facet, state = State.Deployed.facet)
            +At(aid = aid, cid = cid)
            +Adjacent(cid1 = cid, cid2 = cid1)
            +Adjacent(cid1 = cid1, cid2 = cid2)

            guard(cid ne cid2)

            effect(activation = Both) { sol ->
                when (sol)
                {
                    is Assert -> {
                        +CommField(aid, cid2)
                        +CommField(aid, cid1)
                    }
                    is Retire -> {
                        -CommField(aid, cid2)
                        -CommField(aid, cid1)
                    }
                }
            }
        }

        rule(name = "Guards.Health.Interchange") {
            +Turn(pid = pid)
            val a  = -Actor(pid = pid, aid = aid,  health = hlth,  type = ActorKind.Guard.facet, state = State.Deployed.facet)
            val a1 = -Actor(pid = pid, aid = aid1, health = hlth1, type = ActorKind.Guard.facet, state = State.Deployed.facet)
            +At(aid = aid, cid = cid)
            +At(aid = aid1, cid = cid1)
            +Adjacent(cid1 = cid, cid2 = cid1)

            onceBy(aid, aid1)
            onceBy(aid1, aid)

            effect { sol ->
                val h  = sol[hlth].value
                val h1 = sol[hlth1].value

                val dh  = (h - h1)/2
                val dha = abs(dh)

                +a.substitute(hlth to (sol[hlth].inc(-copySign(min(5f, dha), dh))).facet)
                +a1.substitute(hlth1 to (sol[hlth1].inc(copySign(min(5f, dha), dh))).facet)
            }
        }

        rule(name = "RefNotBound.Exception") {
            +Actor(aid = ActorId(171717).facet)

            effect { sol ->
                sol[pid]
            }
        }

        rule(name = "ConcurrencyTest1.Rule1") {
            -Actor(aid = aid)
            +MoveCommand(aid, CellId(101010).facet)

            effect { sol ->
                triggered.add("ConcurrencyTest1.Rule1")
            }
        }

        rule(name = "ConcurrencyTest1.Rule2") {
            -Actor(aid = aid)
            +AttackCommand(aid, ActorId(121212).facet)

            effect { sol ->
                triggered.add("ConcurrencyTest1.Rule2")
            }
        }

        rule(name = "ConcurrencyTest2.Rule") {
            -Actor(aid = aid, pid = PlayerId(109109109).facet)
            +RepairCommand(aid, aid1)

            effect { sol ->
                triggered.add("ConcurrencyTest2.Rule")
            }
        }

        assertFailsWith<PatternNotConnectedException> {
            rule(name = "NotConnected") {
                -Actor(aid = aid, pid = PlayerId(109109109).facet)
                +RepairCommand(ref("x"), aid1)

                effect { sol ->
                    triggered.add("ConcurrencyTest2.Rule")
                }
            }
        }

    }
}