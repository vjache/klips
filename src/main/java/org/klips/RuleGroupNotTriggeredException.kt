package org.klips

class RuleGroupNotTriggeredException(val expect: Collection<String>) : RuntimeException() {
  override val message = "Expected rule groups are not triggered: $expect"
}