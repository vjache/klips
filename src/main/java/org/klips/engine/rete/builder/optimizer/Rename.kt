package org.klips.engine.rete.builder.optimizer

import org.klips.dsl.Facet
import org.klips.engine.Binding
import org.klips.engine.SimpleBinding
import org.klips.engine.rete.ProxyNode

class Rename : Tree {

    override val reteNode: ProxyNode
    override val signature: Signature

    override val junctLeafsMap: Map<Facet.FacetRef<*>, Set<Leaf>>
    get() {
        return tree.junctLeafsMap.mapKeys {
            val facet = reteNode.renamingBinding[it.key]
            facet as Facet.FacetRef<*>
        }
    }

    var tree: Tree

    constructor(pnode: ProxyNode) {
        this.reteNode = pnode

        tree = makeTree(pnode.node)

        signature = Signature(
                1 + tree.signature.nonLeafCount,
                1 + tree.signature.deep)

    }

    constructor(reteNode: ProxyNode, signature: Signature, tree: Tree){
        this.reteNode  = reteNode
        this.signature = signature
        this.tree      = tree
    }

    override fun clone() = Rename(reteNode, signature, tree)

    override fun bind(t: Tree): Binding? {
        if (t !is Rename)
            return null

        if (signature != t.signature)
            return null

        if (junctLeafs != t.junctLeafs)
            return null

        return tree.bind(t.tree)?.let{ binding ->
            val data = binding.map {
                Pair(reteNode.renamingBinding[it.key]!!,
                        t.reteNode.renamingBinding[it.value]!!)
            }
            SimpleBinding(data)
        }
    }

}