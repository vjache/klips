package org.klips.engine.rete.builder.optimizer

import org.klips.dsl.Fact

data class Leaf(val type: Class<out Fact>, val position: Int)