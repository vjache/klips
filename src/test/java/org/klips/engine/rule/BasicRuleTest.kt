package org.klips.engine.rule

import org.klips.engine.*
import org.klips.engine.Modification.Assert
import org.junit.Test

class BasicRuleTest : RuleTestCommon() {


    @Test
    fun rule_Actor_At_Land() {
        testTriggered(
                Actor(ref("aid"), ref("pid"), ref("kind"), ref("nrgy"), ref("hlth"), ref("state")),
                Land(ref("cid"), ref("land")),
                At(ref("aid"), ref("cid")))
        {
            arrayOf(Assert(Actor(1, 100, ActorKind.Aim)),
                    Assert(Land(10, LandKind.DirtDesert)),
                    Assert(At(1, 10)))
        }
    }


    @Test
    fun rule_Actor_At_Resource() {
        testTriggered(
                Actor(ref("aid"), ref("pid"), ref("kind"), ref("nrgy"), ref("hlth"), ref("state")),
                At(ref("aid"), ref("cid")),
                Resource(ref("cid"), ref("type"), ref("amount")))
        {
            arrayOf(Assert(Actor(1, 100, ActorKind.Aim)),
                    Assert(Resource(10, ResourceType.Crystal, 85000)),
                    Assert(At(1, 10)),
                    Assert(Resource(10, ResourceType.Mushroom, 2100)))
        }
    }

    @Test
    fun rule_Adjacent_Actor() {
        testTriggered(
                Actor(ref("aid"), ref("pid"), ref("kind"), ref("nrgy"), ref("hlth"), ref("state")),
                At(ref("aid"), ref("cid")),
                Actor(ref("aid1"), ref("pid1"), ref("land1"), ref("nrgy1"), ref("hlth1"), ref("state1")),
                At(ref("aid1"), ref("cid1")),
                Adjacent(ref("cid"), ref("cid1")))
        {
            arrayOf(Assert(Actor(1, 100, ActorKind.Aim)),
                    Assert(At(1, 10)),
                    Assert(Actor(2, 101, ActorKind.Worker)),
                    Assert(At(2, 11)),
                    Assert(Adjacent(10, 11)),
                    Assert(Adjacent(11, 10)),
                    Assert(Actor(3, 101, ActorKind.Worker)),
                    Assert(At(3, 12)),
                    Assert(Adjacent(10, 12)),
                    Assert(Adjacent(12, 10)))
        }
    }

}

