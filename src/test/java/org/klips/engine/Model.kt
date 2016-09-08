@file:Suppress("unused")

package org.klips.engine

import org.klips.dsl.Facet
import org.klips.dsl.Facet.ConstFacet
import org.klips.dsl.Facet.IntFacet
import org.klips.dsl.Fact
import org.klips.dsl.ref

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
enum class ActorKind {
    Aim,
    Worker,
    Solar,
    Guard,
    Comm
}

enum class PlayerColor {
    Green,
    Blue,
    Red,
    Yellow
}

enum class LandKind {
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

enum class State {
    OnMarch,
    Deployed
}

data class Level(val value: Float, val maxValue: Float) : Comparable<Level> {
    override fun compareTo(other: Level) = value.compareTo(other.value)

    constructor(value: Float) : this(value, 100f)
    constructor() : this(100f, 100f)

    fun inc(dL: Float): Level {
        val newValue = value + dL
        return if (newValue < 0f)
            Level(0f, maxValue)
        else if (newValue > maxValue)
            Level(maxValue, maxValue)
        else Level(newValue, maxValue)
    }
}

/////////////////////////////////////////////////////////////////////////////////

class Adjacent(val cid1: Facet<CellId> = ref(), val cid2: Facet<CellId> = ref()) : Fact() {
    constructor(cid1: Int, cid2: Int) :
    this(ConstFacet(CellId(cid1)), ConstFacet(CellId(cid2)))
}

class At(val aid: Facet<ActorId> = ref(), val cid: Facet<CellId> = ref()) : Fact() {
    constructor(aid: Int, cid: Int) :
    this(ConstFacet(ActorId(aid)), ConstFacet(CellId(cid)))
}

class Land(val cid: Facet<CellId> = ref(), val type: Facet<LandKind> = ref()) : Fact() {
    constructor(cid: Int, type: LandKind) :
    this(ConstFacet(CellId(cid)), ConstFacet(type))
}

class Resource(val cid: Facet<CellId> = ref(),
               val type: Facet<ResourceType> = ref(),
               val amount: Facet<Int> = ref()) : Fact() {
    constructor(cid: Int,
                type: ResourceType,
                amount: Int) :
    this(ConstFacet(CellId(cid)), ConstFacet(type), IntFacet(amount))
}

class Actor(val aid:    Facet<ActorId> = ref(),
            val pid:    Facet<PlayerId> = ref(),
            val type:   Facet<ActorKind> = ref(),
            val energy: Facet<Level> = ref(),
            val health: Facet<Level> = ref(),
            val state:  Facet<State> = ref()) : Fact() {


    constructor(id: Int, pid: Int, type: ActorKind) :
    this(id, pid, type, 100f, 100f, State.OnMarch)

    constructor(id: Int, pid: Int, type: ActorKind, energy: Float, health: Float, state: State) :
    this(ConstFacet(ActorId(id)),
            ConstFacet(PlayerId(pid)),
            ConstFacet(type),
            ConstFacet(Level(energy)),
            ConstFacet(Level(health)),
            ConstFacet(state))

}


class Player(val pid: Facet<PlayerId> = ref(), val color: Facet<PlayerColor> = ref()) :
        Fact() {
    constructor(pid: Int, color: PlayerColor) :
    this(ConstFacet(PlayerId(pid)), ConstFacet(color))
}

/////////////////////////////////////////////////////////////////////////////////
// UI events
/////////////////////////////////////////////////////////////////////////////////

class TapCell(val cid: Facet<CellId> = ref()) :
        Fact() {
    constructor(cid: Int) :
    this(ConstFacet(CellId(cid)))
}

class TapActor(val aid: Facet<ActorId> = ref()) :
        Fact() {
    constructor(aid: Int) :
    this(ConstFacet(ActorId(aid)))
}

class ActorSelected(val aid: Facet<ActorId> = ref()) :
        Fact() {
    constructor(aid: Int) :
    this(ConstFacet(ActorId(aid)))
}


/////////////////////////////////////////////////////////////////////////////////
// Command events
/////////////////////////////////////////////////////////////////////////////////

open class UnaryCommand(
        val actingAgent: Facet<ActorId> = ref()) : Fact()

open class AgentToAgentCommand(
        actingAgent: Facet<ActorId> = ref(),
        val passiveAgentId: Facet<ActorId> = ref()) : UnaryCommand(actingAgent)

class MoveCommand(
        actingAgent: Facet<ActorId> = ref(),
        val targetCell: Facet<CellId>  = ref()) :
        UnaryCommand(actingAgent)

class CreateAgentCommand(
        actingAgent : Facet<ActorId> = ref(),
        val cellId    : Facet<CellId>  = ref(),
        val agentType : Facet<ActorKind>   = ref()) :
        UnaryCommand( actingAgent )

class DeployCommand(
        actingAgent : Facet<ActorId> = ref()) :
        UnaryCommand(actingAgent)

class UndeployCommand(
        actingAgent : Facet<ActorId> = ref()) :
        UnaryCommand(actingAgent)

class AttackCommand(
        actingAgent: Facet<ActorId> = ref(),
        passiveAgentId: Facet<ActorId> = ref()) :
        AgentToAgentCommand(
                actingAgent,
                passiveAgentId)

class ChargeCommand(
        actingAgent: Facet<ActorId> = ref(),
        passiveAgentId: Facet<ActorId> = ref()) :
        AgentToAgentCommand(
                actingAgent,
                passiveAgentId)

class FeedAimCommand(
        actingAgent: Facet<ActorId> = ref(),
        passiveAgentId: Facet<ActorId> = ref()) :
        AgentToAgentCommand(
                actingAgent,
                passiveAgentId)

class RepairCommand(
        actingAgent: Facet<ActorId> = ref(),
        passiveAgentId: Facet<ActorId> = ref()) :
        AgentToAgentCommand(
                actingAgent,
                passiveAgentId)

class ScrapCommand(
        actingAgent: Facet<ActorId> = ref(),
        passiveAgentId: Facet<ActorId> = ref()) :
        AgentToAgentCommand(
                actingAgent,
                passiveAgentId)