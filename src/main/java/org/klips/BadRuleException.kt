package org.klips

import org.klips.dsl.Rule

abstract class BadRuleException(val rule: Rule) : IllegalArgumentException(){
}