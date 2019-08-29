package org.klips.engine.rule

import org.klips.engine.*
import org.klips.engine.rete.builder.ReteBuilderStrategy
import org.klips.engine.rete.mem.StrategyOneMem
import org.junit.Test

class AdvancedRuleTest : RuleTestCommon() {

    var rule1Cnt = 0
    var rule2Cnt = 0
    var rule3Cnt = 0

    @Test
    fun multipleRules() {

        createReteBuilder().input.assert(
                Actor(1, 100, ActorKind.Aim),
                Land(10, LandKind.DirtDesert),
                At(1, 10),
                Resource(10, ResourceType.Mushroom, 2100),
                Actor(2, 100, ActorKind.Comm),
                At(2, 11),
                Adjacent(10, 11)).flush()

        assert(rule1Cnt > 0)
        assert(rule2Cnt > 0)
        assert(rule3Cnt > 0)
    }

    fun createReteBuilder(): ReteBuilderStrategy {
        val aid  = ref<ActorId>("aid")
        val pid  = ref<PlayerId>("pid")
        val cid  = ref<CellId>("cid")
        val type = ref<ActorKind>("kind")
        val land = ref<LandKind>("land")
        val nrgy  = ref<Level>("nrgy")
        val hlth  = ref<Level>("hlth")
        val state = ref<State>("state")

        val rule1 = createRule(
                Actor(aid, pid, type, nrgy, hlth, state),
                Land(cid, land),
                At(aid, cid))
        { sol, effect ->
            rule1Cnt ++
            println("Triggered Land rule: $sol")
        }

        val rule2 = createRule(
                Actor(aid, pid, type, nrgy, hlth, state),
                At(aid, cid),
                Resource(cid, ref("type"), ref("amount")))
        { sol, effect ->
            rule2Cnt ++
            println("Triggered Resource rule: $sol")
        }


        val aid1 = ref<ActorId>("aid1")
        val pid1 = ref<PlayerId>("pid1")
        val cid1 = ref<CellId>("cid1")
        val type1 = ref<ActorKind>("land1")
        val nrgy1  = ref<Level>("nrgy1")
        val hlth1  = ref<Level>("hlth1")
        val state1 = ref<State>("state1")

        val rule3 = createRule(
                Actor(aid, pid, type, nrgy, hlth, state),
                At(aid, cid),
                Actor(aid1, pid1, type1, nrgy1, hlth1, state1),
                At(aid1, cid1),
                Adjacent(cid, cid1))
        { sol, effect ->
            rule3Cnt ++
            println("Triggered Adjacent-Actors rule: $sol")
        }

        return StrategyOneMem(rule1, rule2, rule3)
    }

}

