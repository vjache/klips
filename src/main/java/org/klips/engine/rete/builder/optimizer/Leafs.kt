package org.klips.engine.rete.builder.optimizer

import org.klips.dsl.Facet
import org.klips.dsl.Fact
import org.klips.engine.Binding
import org.klips.engine.rete.AlphaNode

class Leafs : Tree {

    override val junctLeafsMap: Map<Facet.FacetRef<*>, Set<Leaf>>
    override val reteNode: AlphaNode
    override val signature = Signature(0, 0)
    val type: Class<out Fact>
    val positions: List<Int>

    constructor(reteNode: AlphaNode) {
        this.reteNode = reteNode
        type = reteNode.pattern.javaClass

        positions = mutableListOf<Int>().apply {
            reteNode.pattern.facets.forEachIndexed { i, facet ->
                if (facet is Facet.FacetRef) add(i)
            }
        }

        junctLeafsMap = mutableMapOf<Facet.FacetRef<*>, Set<Leaf>>().apply {
            reteNode.pattern.facets.forEachIndexed { i, facet ->
                if (facet is Facet.FacetRef && facet !in this) {
                    put(facet, setOf(Leaf(type, i)))
                }
            }
        }
    }

    constructor(reteNode: AlphaNode,
                type: Class<out Fact>,
                positions: List<Int>,
                junctLeafsMap: Map<Facet.FacetRef<*>, Set<Leaf>>) {
        this.reteNode = reteNode
        this.type = type
        this.positions = positions
        this.junctLeafsMap = junctLeafsMap
    }

    override fun bind_(t: Tree): Binding? {
        if (t !is Leafs)
            return null

        if (type != t.type)
            return null

        return reteNode.matcher.bind(t.reteNode.matcher.pattern)
    }

    override fun clone(): Leafs = Leafs(reteNode, type, positions, junctLeafsMap)
}