package org.klips.engine.util

import org.klips.engine.rete.Node
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class Log(val workingMemory : Boolean = false,
          val agenda : Boolean = false,
          val rete : Boolean = false) {

    inline fun wmEvent(init:() -> String?) {
        if (workingMemory)
            init()?.let(::println)
    }

    inline fun agEvent(init:() -> String?) {
        if (agenda)
            init()?.let(::println)
    }

    inline fun reteEvent(init:() -> String?) {
        if (rete)
            init()?.let(::println)
    }


    val nodeActivity = object : MutableMap<Node, Pair<AtomicInteger, AtomicInteger>> by HashMap(){}
}