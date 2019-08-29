package org.klips.dsl

import org.junit.Test
import org.klips.dsl.ActivationFilter.Both
import org.klips.engine.*
import org.klips.engine.LandKind.SandDesert
import org.klips.engine.ActorKind.*
import org.klips.engine.ActorKind.Guard
import org.klips.engine.State.*
import org.klips.engine.rete.ReteInput
import org.klips.engine.rete.builder.EscalatingAgendaManager
import org.klips.engine.rete.builder.PriorityAgendaManager
import org.klips.engine.rete.builder.RuleClause
import org.klips.engine.util.Log


class AgendaManagerTest {
    companion object {
        val aidVoid = ActorId(-1).facet
        val pid1 = PlayerId(1)
        val pid2 = PlayerId(2)

        val players = mapOf(
            pid1 to MyPlayer(pid1),
            pid2 to MyPlayer(pid2)
        )
    }

    class MyPlayer(val playerId: PlayerId) : PriorityAgendaManager(), Player {
        private var lim = 1

        override fun startTurn() {
            lim = 1
        }

        override fun next() = if (lim > 0) decide()?.also {
            super.remove(it.first, it.second)
            lim--
        } else null

        private fun decide(): Pair<Modification<Binding>, RuleClause>? {
            val asts = pqueue.filter { it.first.isAssert() }
            if (asts.isEmpty()) return null
            asts.random()?.let { first ->
                val sol = first.first
                val aid = sol[ref<ActorId>("aid")]
                if (first.second.group == "Move") {
                    val e = sol[ref<Level>("nrgy")]
                    val fromCid = sol[ref<CellId>("cid1")]
                    val toCid = sol[ref<CellId>("cid2")]
                    println("### Player $playerId decide to act: [$aid] ${first.second.group}[$e] from [$fromCid] to [$toCid]")
                } else {
                    println("### Player $playerId decide to act: [$aid] ${first.second.group}")
                }

                return first
            }
            return null
        }
    }


    class TestRules :
        RuleSet(
            Log(),
//            Log(workingMemory = true, agenda = true),
            agendaManager = EscalatingAgendaManager(10000.0,
                MultiplayerAgendaManager(players.values.toList()) { sol, _ ->
                    val pid: PlayerId = sol.fetchValue(ref<PlayerId>("pid"))
                    players[pid]!!
                })
        ) {
        val cid = ref<CellId>("cid")
        val cid1 = ref<CellId>("cid1")
        val cid2 = ref<CellId>("cid2")
        val cid3 = ref<CellId>("cid3")
        val cid4 = ref<CellId>("cid4")
        val aid = ref<ActorId>("aid")
        val aid1 = ref<ActorId>("aid1")
        val kind = ref<ActorKind>("kind")
        val land1 = ref<LandKind>("land1")
        val land2 = ref<LandKind>("land2")
        val pid = ref<PlayerId>("pid")
        val pid1 = ref<PlayerId>("pid1")
        val nrgy = ref<Level>("nrgy")
        val nrgy1 = ref<Level>("nrgy1")
        val hlth = ref<Level>("hlth")
        val hlth1 = ref<Level>("hlth1")
        val state = ref<State>("state")
        val rtype = ref<ResourceType>("rtype")

        init {
            rule(name = "Adj-Symmetry") {
                // Symmetry of adjacency
                +Adjacent(cid, cid1)
                effect {
                    +Adjacent(cid1, cid)
                }
            }

            rule(name = "Move", priority = 10000.0) {
                -At(aid, cid1)
                +Adjacent(cid1, cid2)
                -At(aidVoid, cid2)

                +Land(cid1, land1)
                +Land(cid2, land2)

                val a = -Actor(aid, pid, kind, nrgy, hlth, OnMarch.facet)

                val dE = 0.5f

                guard {
                    it[nrgy].value >= dE
                }

                effect { sol ->
                    +At(aidVoid, cid1)
                    +At(aid, cid2)
                    +a.substitute(
                        nrgy to sol[nrgy].inc(-dE).facet
                    )
                }
            }

            rule(name = "Deploy", priority = 10000.0) {
                val a = -Actor(aid, pid, kind, nrgy, hlth, OnMarch.facet)

                val dE = 0.5f

                guard {
                    it[nrgy].value >= dE
                }

                effect { sol ->
                    +a.substitute(
                        nrgy to sol[nrgy].inc(-dE).facet,
                        OnMarch.facet to Deployed.facet
                    )
                }
            }

            rule(name = "OnMarch", priority = 10000.0) {
                val a = -Actor(aid, pid, kind, nrgy, hlth, Deployed.facet)

                val dE = 0.5f

                guard {
                    it[nrgy].value >= dE
                }

                effect { sol ->
                    +a.substitute(
                        nrgy to sol[nrgy].inc(-dE).facet,
                        Deployed.facet to OnMarch.facet
                    )
                }
            }

            rule(name = "Attack", priority = 10000.0) {

                +At(aid, cid)
                +Adjacent(cid, cid1)
                +At(aid1, cid1)

                val attacker = -Actor(aid = aid, energy = nrgy, type = Guard.facet, state = OnMarch.facet, pid = pid)
                val enemy = -Actor(aid = aid1, health = hlth1, pid = pid1)

                val dE = 0.5f
                val dH = 2.0f

                guard { it[nrgy].value > dE }
                guard(pid ne pid1)

                effect { sol ->
                    +attacker.substitute(nrgy to sol[nrgy].inc(-dE).facet)
                    +enemy.substitute(hlth1 to sol[hlth1].inc(-dH).facet)
                }
            }

            rule(name = "Repair", priority = 10000.0) {

                +At(aid, cid)
                +Adjacent(cid, cid1)
                +At(aid1, cid1)

                val repairer = -Actor(aid = aid, energy = nrgy, type = Worker.facet, state = OnMarch.facet, pid = pid)
                val friend = -Actor(aid = aid1, health = hlth1, pid = pid)

                val dE = 2.0f
                val dH = 1.0f

                guard { it[nrgy].value > dE }

                effect { sol ->
                    +repairer.substitute(nrgy to sol[nrgy].inc(-dE).facet)
                    +friend.substitute(hlth1 to sol[hlth1].inc(dH).facet)
                }
            }

            rule(name = "Charge", priority = 10000.0) {

                +At(aid, cid)
                +Adjacent(cid, cid1)
                +At(aid1, cid1)

                val charger = -Actor(aid = aid, energy = nrgy, type = Solar.facet, state = Deployed.facet, pid = pid)
                val friend = -Actor(aid = aid1, health = hlth1, pid = pid)

                val dE = 2.0f
                val dE1 = 1.5f

                guard { it[nrgy].value > dE }

                effect { sol ->
                    +charger.substitute(nrgy to sol[nrgy].inc(-dE).facet)
                    +friend.substitute(hlth1 to sol[hlth1].inc(dE1).facet)
                }
            }


            rule(name = "FeedAim", priority = 10000.0) {

                val qty = ref<Int>("qty")
                val cap = ref<Level>("cap")

                +At(aid, cid)
                val rsc = -Resource(cid, rtype, qty)
                guard(qty gt 0)

                val feeder = -Actor(aid = aid, health = hlth, energy = nrgy,  pid = pid,
                    type = Worker.facet, state = Deployed.facet)
                guard { it[hlth].value > 0.0f && it[nrgy].value > 0.0f }

                +Actor(aid = aid1, type = Aim.facet, pid = pid)
                val cb = -CargoBay(aid1, rtype, cap)

                val dE = 0.1f

                effect { sol ->
                    +feeder.substitute(nrgy to sol[nrgy].inc(-dE).facet)
                    +cb.substitute(cap to sol[cap].inc(sol[qty].toFloat()).facet)
                    val qty1 = sol[qty]-1
                    if(qty1 > 0)
                        +rsc.substitute(qty to qty1.facet)
                }
            }

            rule(name = "Comm-Deployed") {
                +Actor(aid, pid, Comm.facet, nrgy, hlth, Deployed.facet)
                +At(aid, cid1)

                +Adjacent(cid1, cid2)
                +Adjacent(cid2, cid3)
//                +Adjacent(cid3, cid4)

                effect(activation = Both) {
                    !CommField(aid, cid2)
                    !CommField(aid, cid3)
//                    !CommField(aid, cid4)
                }
            }
        }
    }

    @Test
    fun basicTest() {
        with(TestRules().input) {
            flush("Adj-Symmetry") {
                createSpace()

            }

            flush("Move") {

                -At(aidVoid, cid(1, 1))
                +At(aid(1), cid(1, 1))
                +Actor(aid(1), pid1.facet, Worker.facet, Level().facet, Level().facet, OnMarch.facet)

                -At(aidVoid, cid(5, 5))
                +At(aid(3), cid(5, 5))
                +Actor(aid(3), pid1.facet, Comm.facet, Level().facet, Level().facet, OnMarch.facet)

                -At(aidVoid, cid(2, 2))
                +At(aid(2), cid(2, 2))
                +Actor(aid(2), pid2.facet, Worker.facet, Level().facet, Level().facet, OnMarch.facet)
            }

            for (i in 1..10)
                flush {}

//            flush{}
//
//            flush{}
        }

    }

    fun ReteInput.createSpace() {
        for (i in 1..10) {
            for (j in 1..10) {
                val cid1 = cid(i, j)
                +Land(cid1, SandDesert.facet)
                +At(aidVoid, cid1)
                +Adjacent(cid1, cid(i, j + 1))
                +Adjacent(cid1, cid(i + 1, j))
            }
        }
    }

    fun ReteInput.placeAims(): Pair<ActorId, ActorId> {
        val aim1 = ActorId()
        val aim2 = ActorId()

        +Actor(aim1, pid1, Aim, Deployed)
        +Actor(aim2, pid2, Aim, Deployed)

        +At(aim1.facet, cid(10, 10))
        +At(aim2.facet, cid(90, 90))

        return Pair(aim1, aim2)
    }

    val cidPool = mutableMapOf<Int, Facet.ConstFacet<CellId>>()

    fun cid(i: Int, j: Int): Facet.ConstFacet<CellId> {
        val n = i * 1000 + j
        return cidPool.getOrPut(n) {
            CellId(n).facet
        }
    }

    val aidPool = mutableMapOf<Int, Facet<ActorId>>()

    fun aid(i: Int): Facet<ActorId> {
        return aidPool.getOrPut(i) {
            ActorId(i).facet
        }
    }
}