package org.klips

import org.klips.dsl.Facet
import org.klips.dsl.Fact
import org.klips.dsl.Rule

class RHSFactNotBoundException(
        val fact: Fact,
        val refs:Collection<Facet.FacetRef<*>>,
        rule:Rule) : BadRuleException(rule) {

    override val message: String = "RHS not bound by LHS in rule '${rule.group}'. RHS of rule '${rule.group}' have fact '$fact' which have references $refs not bound with LHS."
}