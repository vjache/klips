package org.klips.dsl

import org.klips.dsl.Facet.IntervalFacet
import org.klips.dsl.Guard.BinaryPredicate.Code.*
import org.klips.dsl.Guard.Junction.And
import org.klips.dsl.Guard.Junction.Or
import org.klips.engine.Binding
import org.klips.engine.Modification

sealed class Guard
{
    abstract class Predicate : Guard()

    class OnceBy(val group: String, vararg val facets: Facet<*>) : Guard() {
        override fun eval(cache: MutableMap<Any, Any>, env: Modification<Binding>): Boolean {
            val identity = Pair(group, env.inherit(facets.map{ env.arg[it] }))

            if(cache[identity] != null)
                return false

            cache[identity] = identity

            return true
        }
    }

    class BinaryPredicate<T : Comparable<T>>(
            val code: Code,
            val left: Facet<T>,
            val right: Facet<T>) : Predicate(){
        override fun eval(cache:MutableMap<Any,Any>, env: Modification<Binding>) = when (code) {
            Eq -> compare(env) == 0
            Ne -> compare(env) != 0
            Gt -> compare(env) > 0
            Ge -> compare(env) >= 0
            Le -> compare(env) <= 0
            Lt -> compare(env) < 0
            In -> {
                if(right !is IntervalFacet<T>)
                    false
                else {
                    val leftVal = env.arg.fetchValue2(left)
                    leftVal >= right.min && leftVal <= right.max
                }
            }
        }

        private fun compare(env: Modification<Binding>): Int {
            fun asConst(arg:Facet<*>) : Facet.ConstFacet<*>{
                return when (arg){
                    is Facet.ConstFacet -> arg
                    is Facet.FacetRef -> env.arg[arg] as Facet.ConstFacet<*>
                    else -> throw IllegalArgumentException("Expected const or ref: $arg")
                }
            }

            return asConst(left).compareTo(asConst(right))
        }

        enum class Code {  Eq, Ne, Gt, Lt, Ge, Le, In }
    }
//    class UnaryPredicate<T>(
//            val code: Code,
//            val arg: Facet<T>) : Predicate() {
//        override fun eval(env: Binding) = when(code) {
//        }
//
//        enum class Code {  Odd, Even }
//    }

    abstract class Operator : Guard()

    enum class Junction { And, Or }

    class BinaryJunction(
            val code:Junction,
            val left: Guard, val right: Guard) : Operator() {
        override fun eval(cache:MutableMap<Any,Any>, env: Modification<Binding>) = when(code){
            And -> left.eval(cache, env) && right.eval(cache, env)
            Or  -> left.eval(cache, env) || right.eval(cache, env)
        }
    }

    class MultiJunction(val code:Junction, vararg args: Guard) : Operator()
    {
        override fun eval(cache:MutableMap<Any,Any>, env: Modification<Binding>) = when(code){
            And -> juncts.all { it.eval(cache, env) }
            Or  -> juncts.any { it.eval(cache, env) }
        }

        val juncts = mutableListOf(*args)
    }

    class NotOperator(val arg: Guard) : Operator(){
        override fun eval(cache:MutableMap<Any,Any>, env: Modification<Binding>) = !arg.eval(cache, env)
    }

    class LambdaGuard(private val lambda: (Modification<Binding>) -> Boolean) : Guard() {
        override fun eval(cache:MutableMap<Any,Any>, env: Modification<Binding>) = lambda(env)
    }

    ////////////////////////////////////

    infix fun and(t: Guard) = BinaryJunction(And, this, t)
    infix fun or (t: Guard) = BinaryJunction(Or,  this, t)

    fun not() = NotOperator(this)

    abstract fun eval(cache:MutableMap<Any,Any>, env:Modification<Binding>) : Boolean
}