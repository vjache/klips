package org.klips.engine.rete

import org.klips.engine.Binding
import org.klips.engine.Modification

@FunctionalInterface
interface Consumer { fun consume(source: Node, mdf: Modification<Binding>)}