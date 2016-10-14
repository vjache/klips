package org.klips.engine.util

import org.klips.engine.rete.Node
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class Log(val name : String = Random().nextInt().toString(),
          val workingMemory : Boolean = false,
          val agenda : Boolean = false,
          val rete : Boolean = false) {

    inline fun wmEvent(init:() -> String?) {
        if (workingMemory)
            init()?.let { println2(it) }
    }

    inline fun agEvent(init:() -> String?) {
        if (agenda)
            init()?.let { println2(it) }
    }

    inline fun reteEvent(init:() -> String?) {
        if (rete)
            init()?.let { println2(it) }
    }


    val nodeActivity = object : MutableMap<Node, Pair<AtomicInteger, AtomicInteger>> by HashMap(){}

    fun println2(str:String) = println("[$name] $str")
}