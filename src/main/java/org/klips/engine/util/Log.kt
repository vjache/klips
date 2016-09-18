package org.klips.engine.util

class Log(val workingMemory : Boolean = false,
          val agenda : Boolean = false,
          val rete : Boolean = false) {

    inline fun wmEvent(init:() -> String) {
        if (workingMemory)
            println(init())
    }

    inline fun agEvent(init:() -> String) {
        if (agenda)
            println(init())
    }

    inline fun reteEvent(init:() -> String) {
        if (rete)
            println(init())
    }
}