package org.klips

import org.klips.dsl.Fact

class PatternNotConnectedException(part1: Set<Fact>, part2: Set<Fact>) : IllegalArgumentException() {
    override val message: String = "Patterns not connected by any reference: '$part1' vs '$part2'."
}