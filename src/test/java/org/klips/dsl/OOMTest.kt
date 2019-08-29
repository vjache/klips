package org.klips.dsl

import org.junit.Test
import org.klips.engine.*
import org.klips.engine.util.Log


class OOMTest {

    class TestRules : RuleSet(Log(workingMemory = true, agenda = true)) {
        val cid = ref<CellId>("cid")
        val cid1 = ref<CellId>("cid1")
        val cid2 = ref<CellId>("cid2")
        val aid = ref<ActorId>("aid")
        val aid1 = ref<ActorId>("aid1")
        val kind = ref<ActorKind>("kind")
        val kind1 = ref<ActorKind>("land1")
        val pid = ref<PlayerId>("pid")
        val pid1 = ref<PlayerId>("pid1")
        val nrgy = ref<Level>("nrgy")
        val nrgy1 = ref<Level>("nrgy1")
        val hlth = ref<Level>("hlth")
        val hlth1 = ref<Level>("hlth1")
        val state = ref<State>("state")
        val state1 = ref<State>("state1")
        init {
            rule(name = "R1") {
                -At(aid, cid)
                +Actor(aid,pid,kind,nrgy,hlth,state)
                +Actor(aid1,pid,kind1,nrgy1,hlth1,state1)

                effect { }
            }
        }
    }

    @Test
    fun basicTest() {
        TestRules().input.flush ("R1") {
            +At(100, 1)
            +Actor(100, 1000, ActorKind.Aim, 100f, 100f, State.OnMarch)
        }
    }

}