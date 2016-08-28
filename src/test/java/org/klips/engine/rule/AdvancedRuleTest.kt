package org.klips.engine.rule

import org.klips.engine.*
import org.klips.engine.Modification.Assert
import org.klips.engine.query.*
import org.klips.engine.rete.builder.ReteBuilderStrategy
import org.klips.engine.rete.builder.StrategyOneMem
import org.junit.Test

class AdvancedRuleTest : RuleTestCommon() {

    @Test
    fun multipleRules() {

        createReteBuilder().input.assert(
                Actor(1, 100, ActorKind.Aim),
                Land(10, LandKind.DirtDesert),
                At(1, 10),
                Resource(10, ResourceType.Mushroom, 2100),
                Actor(2, 100, ActorKind.Comm),
                At(2, 11),
                Adjacent(10, 11))

    }

    fun createReteBuilder(): ReteBuilderStrategy {
        val aid  = ref<ActorId>("aid")
        val pid  = ref<PlayerId>("pid")
        val cid  = ref<CellId>("cid")
        val type = ref<ActorKind>("kind")
        val land = ref<LandKind>("land")
        val nrgy  = ref<Level>("nrgy")

        val rule1 = createRule(
                Actor(aid, pid, type, nrgy),
                Land(cid, land),
                At(aid, cid))
        { sol, effect ->
            println("Triggered Land rule: $sol")
        }

        val rule2 = createRule(
                Actor(aid, pid, type, nrgy),
                At(aid, cid),
                Resource(cid, ref("type"), ref("amount")))
        { sol, effect ->
            println("Triggered Resource rule: $sol")
        }


        val aid1 = ref<ActorId>("aid1")
        val pid1 = ref<PlayerId>("pid1")
        val cid1 = ref<CellId>("cid1")
        val type1 = ref<ActorKind>("kind1")
        val nrgy1  = ref<Level>("nrgy1")

        val rule3 = createRule(
                Actor(aid, pid, type, nrgy),
                At(aid, cid),
                Actor(aid1, pid1, type1, nrgy1),
                At(aid1, cid1),
                Adjacent(cid, cid1))
        { sol, effect ->
            println("Triggered Adjacent-Actors rule: $sol")
        }

        return StrategyOneMem(rule1, rule2, rule3)
    }

}

