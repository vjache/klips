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

  fun flush() : ReteInput

  fun Fact.assert() = assert(this)
  fun Fact.retire() = retire(this)

  fun Fact.blink() = blink(this)

  fun flush(block: ReteInput.() -> Unit): ReteInput {
    block()
    return flush()
  }

}