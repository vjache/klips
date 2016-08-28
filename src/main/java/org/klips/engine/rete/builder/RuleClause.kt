package org.klips.engine.rete.builder

import org.klips.dsl.Fact

data class RuleClause(val pattern  : Set<Fact>,
                      val trigger  : Trigger,
                      val group    : String,
                      val priority : Double)   {
  constructor(pattern  : Set<Fact>,
              trigger  : Trigger) : this(pattern, trigger, "*", 0.0)

  fun replacePattern(newPattern  : Set<Fact>) = RuleClause(newPattern,trigger,group,priority)
}