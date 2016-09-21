package org.klips

class RuleGroupNotTriggeredException(val expect: Collection<String>) : RuntimeException() {
  override fun toString() = "Expected rule groups are not triggered: $expect"
}