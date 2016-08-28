package org.klips.engine

import org.klips.dsl.Facet
import org.klips.dsl.Facet.ConstFacet
import org.klips.dsl.Facet.IntFacet
import org.klips.dsl.Fact

/////////////////////////////////////////////////////////////////////////////////
data class CellId(val id: Int) : Comparable<CellId> {
    override fun compareTo(other: CellId): Int = id.compareTo(other.id)
}

data class ActorId(val id: Int) : Comparable<ActorId> {
    override fun compareTo(other: ActorId): Int = id.compareTo(other.id)
}

data class PlayerId(val id: Int) : Comparable<PlayerId> {
    override fun compareTo(other: PlayerId): Int = id.compareTo(other.id)
}
/////////////////////////////////////////////////////////////////////////////////
enum class ActorKind{
    Aim,
    Worker,
    Solar,
    Guard,
    Comm
}

enum class PlayerColor{
    Green,
    Blue,
    Red,
    Yellow
}

enum class LandKind{
    SandDesert,
    DirtDesert,
    Mountain,
    Water
}

enum class ResourceType {
    Mushroom,
    Crystal,
    Plant
}

data class Level(val value : Float, val maxValue : Float) : Comparable<Level> {
    override fun compareTo(other: Level) = value.compareTo(other.value)
    constructor(value : Float):this(value, 100f)
    constructor():this(100f,100f)

    fun inc(dL: Float): Level {
        val newValue = value + dL
        return if(newValue < 0f)
            Level(0f, maxValue)
        else if(newValue > maxValue)
            Level(maxValue, maxValue)
        else Level(newValue, maxValue)
    }
}

/////////////////////////////////////////////////////////////////////////////////

class Adjacent(cid1: Facet<CellId>, cid2: Facet<CellId>) : Fact(cid1, cid2){
    constructor(cid1: Int, cid2:Int ):
    this(ConstFacet(CellId(cid1)), ConstFacet(CellId(cid2)))
}

class At(aid: Facet<ActorId>, cid: Facet<CellId>) : Fact(aid, cid){
    constructor(aid: Int, cid:Int ):
    this(ConstFacet(ActorId(aid)), ConstFacet(CellId(cid)))
}

class Land(cid: Facet<CellId>, type: Facet<LandKind>) : Fact(cid, type){
    constructor(cid: Int, type: LandKind):
    this(ConstFacet(CellId(cid)), ConstFacet(type))
}

class Resource(cid: Facet<CellId>,
               type: Facet<ResourceType>,
               amount: Facet<Int>) : Fact(cid, type, amount){
    constructor(cid:    Int,
                type: ResourceType,
                amount: Int):
    this(ConstFacet(CellId(cid)), ConstFacet(type), IntFacet(amount))
}

class Actor(aid: Facet<ActorId>,
            pid: Facet<PlayerId>,
            type: Facet<ActorKind>,
            energy:Facet<Level>) :
        Fact(aid, pid, type, energy){
    constructor(id: Int, pid: Int, type: ActorKind): this(id, pid, type, 100f)
    constructor(id: Int, pid: Int, type: ActorKind, energy : Float):
    this(ConstFacet(ActorId(id)),
         ConstFacet(PlayerId(pid)),
         ConstFacet(type),
         ConstFacet(Level(energy)))
}


class Player(pid: Facet<PlayerId>, color: Facet<PlayerColor>) :
        Fact(pid, color){
    constructor(pid: Int, color: PlayerColor):
    this(ConstFacet(PlayerId(pid)), ConstFacet(color))
}

/////////////////////////////////////////////////////////////////////////////////

class TapCell(cid:Facet<CellId>):
        Fact(cid){
    constructor(cid:Int):
    this(ConstFacet(CellId(cid)))
}

class TapActor(aid:Facet<ActorId>):
        Fact(aid){
    constructor(aid:Int):
    this(ConstFacet(ActorId(aid)))
}

class ActorSelected(aid:Facet<ActorId>):
        Fact(aid){
    constructor(aid:Int):
    this(ConstFacet(ActorId(aid)))
}