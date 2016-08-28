package org.klips.engine.rete.builder.optimizer

import org.klips.dsl.Facet
import org.klips.engine.Binding
import org.klips.engine.ComposeBinding
import org.klips.engine.rete.BetaNode

class Fork : Tree {

    override val reteNode: BetaNode
    override val signature: Signature
    override val junctLeafsMap: Map<Facet.FacetRef<*>, Set<Leaf>>

    var left: Tree
    var right: Tree

    constructor(reteNode: BetaNode) {
        this.reteNode = reteNode

        left  = makeTree(reteNode.left)
        right = makeTree(reteNode.right)

//        junctLeafsMap = mutableMapOf<Facet.FacetRef<*>, Set<Leaf>>().apply {
//            left.junctLeafsMap.keys.union(right.junctLeafsMap.keys).forEach { facet ->
//                left.junctLeafsMap[facet]?.let { ls1 ->
//                    right.junctLeafsMap[facet]?.let { ls2 ->
//                        ls1.union(ls2)
//                    }
//                }
//            }
//        }
        junctLeafsMap = mutableMapOf<Facet.FacetRef<*>, MutableSet<Leaf>>().apply {
            reteNode.refs.forEach { facet ->
                val leafs = getOrPut(facet) { mutableSetOf()}

                left.junctLeafsMap[facet]?.let {
                    leafs.addAll(it)
                }

                right.junctLeafsMap[facet]?.let {
                    leafs.addAll(it)
                }
            }
        }

        signature = Signature(
                1 + left.signature.nonLeafCount + right.signature.nonLeafCount,
                Math.max(left.signature.deep, right.signature.deep))

    }

    constructor(reteNode: BetaNode, signature: Signature, left: Tree, right: Tree, junctLeafsMap: Map<Facet.FacetRef<*>, Set<Leaf>>) {
        this.reteNode = reteNode
        this.signature = signature
        this.left = left
        this.right = right
        this.junctLeafsMap = junctLeafsMap
    }

    override fun bind(t: Tree): Binding? {
        if (t !is Fork)
            return null

        if (signature != t.signature)
            return null

        if (junctLeafs != t.junctLeafs)
            return null

        left.bind(t.left)?.let { bLeft ->
            right.bind(t.right)?.let {bRight ->
                return ComposeBinding(bLeft, bRight)
            }
        }

        left.bind(t.right)?.let { bLeft ->
            right.bind(t.left)?.let {bRight ->
                return ComposeBinding(bLeft, bRight)
            }
        }

        return null
    }

    override fun clone(): Fork = Fork(reteNode, signature, left, right, junctLeafsMap)
    ////////////////////////////////////////////////////////////////////


}