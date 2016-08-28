package org.klips.engine.query

import org.klips.dsl.Fact
import org.klips.engine.*

class TestWorld(val max:Int = 10) {

    private var aidClock = 0
    val facts = mutableListOf<Fact>().apply {
        for (i in 1..max) {
            for (j in 1..max) {
                val cid = id(i, j)
                if (j < max) add(Adjacent(cid, id(i, j + 1)))
                if (i < max) add(Adjacent(cid, id(i + 1, j)))
                add(Land(cid, LandKind.SandDesert))
                val r = ResourceType.values()[cid % ResourceType.values().size]
                add(Resource(cid, r, 5))
            }
        }

        addPlayerGroup(2, 2, 1, PlayerColor.Green)

        addPlayerGroup(5, 5, 2, PlayerColor.Blue)

        addPlayerGroup(8, 8, 3, PlayerColor.Yellow)

        add(Player(4, PlayerColor.Red))
    }

    fun MutableList<Fact>.addPlayerGroup(n: Int, m: Int, pid: Int, color: PlayerColor){
        add(Player(pid, color))

        val aid1 = allocAid()
        add(Actor(aid1, pid, ActorKind.Aim))
        add(At(aid1, id(n, m)))

        val aid2 = allocAid()
        add(Actor(aid2, pid, ActorKind.Comm))
        add(At(aid2, id(n + 1, m)))

        val aid3 = allocAid()
        add(Actor(aid3, pid, ActorKind.Solar))
        add(At(aid3, id(n, m + 2)))
    }


    fun id(i:Int, j:Int) = i * max + j
    fun allocAid() = aidClock ++

}