package org.klips.dsl

import org.junit.Test
import org.klips.ReferenceNotBoundException
import org.klips.RuleGroupNotTriggeredException
import org.klips.engine.*
import org.klips.engine.util.Log
import kotlin.test.assertFailsWith


class SameLHSTest {

    class TestRules : RuleSet(Log(workingMemory = true, agenda = true)) {
        val cid = ref<CellId>("cid")
        val cid1 = ref<CellId>("cid1")
        val cid2 = ref<CellId>("cid2")
        val aid = ref<ActorId>("aid")
        val aid1 = ref<ActorId>("aid1")
        val kind = ref<ActorKind>("kind")
        val pid = ref<PlayerId>("pid")
        val pid1 = ref<PlayerId>("pid1")
        val nrgy = ref<Level>("nrgy")
        val nrgy1 = ref<Level>("nrgy1")
        val hlth = ref<Level>("hlth")
        val hlth1 = ref<Level>("hlth1")
        val state = ref<State>("state")
        init {
            rule(name = "R1") {
                +At(aid, cid)
                +Actor(aid,pid,kind,nrgy,hlth,state)

                effect { }
            }
            rule(name = "R2") {
                +At(aid, cid)
                +Actor(aid,pid,kind,nrgy,hlth,state)

                effect { }
            }
            rule(name = "R3") {
                -At(aid, cid)
                +Actor(aid,pid,kind,nrgy,hlth,state)

                effect { }
            }
            rule(name = "R4.unreachable") {
                +At(aid, cid)
                +Actor(aid,pid,kind,nrgy,hlth,state)

                effect { }
            }
        }
    }

    @Test
    fun basicTest() {
        TestRules().input.flush ("R1", "R2", "R3") {
            +At(100, 1)
            +Actor(100, 1000, ActorKind.Aim, 100f, 100f, State.OnMarch)
        }
    }

    @Test
    fun unreachableTest() {
        assertFailsWith<RuleGroupNotTriggeredException> {
            TestRules().input.flush("R4.unreachable") {
                +At(100, 1)
                +Actor(100, 1000, ActorKind.Aim, 100f, 100f, State.OnMarch)
            }
        }
    }

}