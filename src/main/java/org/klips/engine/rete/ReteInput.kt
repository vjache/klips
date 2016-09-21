package org.klips.engine.rete

import org.klips.dsl.Fact
import org.klips.engine.Modification
import org.klips.engine.Modification.Assert
import org.klips.engine.Modification.Retire

interface ReteInput {

  fun modify(vararg mdfs: Modification<out Fact>) : ReteInput

  fun assert(vararg facts: Fact) = modify(*Array(facts.size){Assert(facts[it])})
  fun retire(vararg facts: Fact) = modify(*Array(facts.size){Retire(facts[it])})

  fun blink(vararg facts:Fact)   = assert(*facts).flush().retire(*facts).flush()

  fun flush(vararg expect:String) : ReteInput

  fun Fact.assert() = assert(this)
  fun Fact.retire() = retire(this)

  operator fun Fact.unaryPlus():Fact {
    assert(this)
    return this
  }

  operator fun Fact.unaryMinus():Fact {
    retire(this)
    return this
  }

  fun Fact.blink() = blink(this)

  fun flush(vararg expect:String, block: ReteInput.() -> Unit): ReteInput {
    block()
    return flush(*expect)
  }

}