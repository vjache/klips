## Kotlin Language Integrated Production System (KLIPS)

### Keywords
Rule Engine, Logical Programming, Declarative Programming, Rete, AI Engine, DSL
  
### Motivation 
This library is inspired by the CLIPS (C Language Integrated Production 
System), hence KLIPS does for Kotlin the same thing as CLIPS for C. 
Why Kotlin? Kotlin is from JVM clan and more expressive than Java but 
simpler than Scala. Kotlin have a good enough capabilities to design a 
DSL allowing more or less smooth mix of rule declarations with 'ordinary' 
Kotlin code. Other reason is that Kotlin become a 'weapon' of choice for 
more and more programmers for an Android devices. I believe this library 
would be a helper for programmers developing AIs for their games for 
Android.
 
### Introduction
KLIPS provides DSL to specify a production rules and a rule engine 
to trigger them. Rule have its _left hand side_ (LHS) which specifies a 
condition under which a rule will be activated, and a _right hand side_ 
(RHS) which specifies a _what to do_ if rule decided to apply. Engine 
solves a system of a rules against working memory in a reactive way i.e. 
when it is changed. Working memory (WM) is a set of facts. Facts are 
pieces of a data describing the state of a world of a particular program, 
hence they are specific for each program and defined by a programmer. 
Probably, it is more convenient for some domains to treat facts as an 
events. Facts are pushed into WM as soon as they become known and if 
conditions of some rules are satisfied then those rules become activated. 
All activated rules form a collection called an agenda and at some point 
a conflict resolution policy is applied. The purpose of a conflict 
resolution is to decide which activated rule must be applied first. All 
these concepts will be explained in more details bellow.
  
### Fact
Fact is a data piece describing some aspect of a state of a world of 
your program. Fact is quite similar to the concept of relation from 
'Relational Algebra'. Technically it is a subclass of the class 
`org.kotlin.dsl.Fact` with some additional requirement about fields. 
Lets see example:
```kotlin
class Actor(val id:Facet<Int>, val nrgy:Facet<Int>, val type:Facet<ActorType>) : Fact()
class At(val actorId:Facet<Int>, val cellId:Facet<Int>) : Fact()
class Adjacent(val cellId1:Facet<Int>, val cellId2:Facet<Int>) : Fact()
class Cell(val id:Facet<Int>, val resourceAmount:Facet<Double>) : Fact()

class MoveCommand(val actorId:Facet<Int>, val cellId:Facet<Int>) : Fact()
```
Please note that the primary constructors contain fields of type 
`org.kotlin.dsl.Facet`. Facets are placeholders which may contain a 
concrete constant or a reference (a variable if you wish). Facets 
introduced due to we need to be able to reuse the same fact classes to 
specify concrete data and patterns which contain references, this 
became more clear bellow at the 'Rule' topic. 
So the facts above, describe a simple domain in which may live some 
actors, which may be located at some cell (tile), and some cells may be 
adjacent and some other not (topology of space).
Now we put to WM some facts to initialize our world:

```kotlin
input.flush {
    +Cell(0.facet, 0.5.facet) // Define cell 0 with some amount of resources
    +Cell(1.facet, 0.5.facet)
    +Cell(2.facet, 0.5.facet)
    +Cell(3.facet, 0.facet)
    +Cell(4.facet, 0.facet)
    
    +Adjacent(0.facet, 1.facet) // Define cells 0 and 1 as adjacent
    +Adjacent(0.facet, 2.facet) // Define cells 0 and 2 as adjacent
    +Adjacent(0.facet, 3.facet) // Define cells 0 and 3 as adjacent
    +Adjacent(0.facet, 4.facet) // Define cells 0 and 4 as adjacent

    +Actor(1.facet, 100.facet, ActorType.Worker.facet) // Define actor of type Worker with energy 100 units
    +At(1.facet, 0.facet) // Place actor at cell 0
}
```
The instruction `input.flush` is an API call which modifies a WM state, 
please do not bother about that for now, but pay some attention to unary 
plus before constructor call and `<some const>.facet` notation. There 
are two basic operations against WM: `assert` fact and `retire` fact. The 
unary plus is a synonym for an assert operation and means add fact to WM. 
The unary minus before fact would be a synonym for retire operation i.e. 
remove fact from WM. So, when we want to make program become aware about 
some piece of data about world we do assert a fact, and if a fact is no 
valid any more we do retire a fact. The notation `<some const>.facet` is 
used to make a constant of type `T` become a `FacetConst<T>`.

### Rule
Now lets add some rules to our world. Suppose we want to have a rule to 
move actors through the world space i.e. cells. Such a rule may sound 
like this: 
```text
If command 'move some actor to some cell' issued, 
and if the target cell is adjacent to the current cell of an actor, 
and if an actor have an energy level greater than 5 units, 
than place actor on target cell and decrease the energy level of that actor by 5 units.
```

Such a rule may be described using KLIPS DSL as follows:

```kotlin
// Move rule v1
rule {
    // LHS: begin
    
    // Create references
    val aid = ref<Int>("aid") // Actor ID reference with name "aid". If name is not specified it will be generagted which is not very convenient for debug printing.
    val cid = ref<Int>("cid")
    val cid0 = ref<Int>("cid0")
    val nrge = ref<Int>("nrgy")
    val type = ref<ActorType>("type")
    
    // Describe pattern
    +MoveCommand(aid, cid)
    +Actor(aid, nrgy, type)
    +At(aid, cid0)
    +Adjacent(cid, cid0)
    
    // Additional constraint -- energy level must be greater than 5 units
    guard(nrgy gt 5.facet)
    
    // LHS: end
    // RHS enclosed by effect call
    effect { sol ->
       -At(aid, cid0) // Actor not at cell cid0 any more
       +At(aid, cid)  // Actor become at cell cid from now
       
       -Actor(aid, nrgy, type) // Actor data become invalid
       +Actor(aid, (sol[nrgy] - 5).facet, type) // Actor data updated with lower energy level
       
       -MoveCommand(aid, cid) // Move command dismissed
    }
}
```
As you can see the rule declaration is done by notation like:

```kotlin
rule {
    ...        // facts which form a pattern (rule precondition)
    guard(...) // guard -- additonal boolean constraint against references (FacetRef's)
    effect { sol ->  // sol is a solution i.e. map FacetRef -> FacetConst
    ... // asserted and retired facts
    }
}
```
The pattern of a rule, i.e. LHS, consists of a facts constructors prefixed 
with unary plus or unary minus. The unary plus before the fact constructor 
means that fact is a part of a pattern. Unary minus before the fact means 
that fact constructor is a part of a pattern but the appropriate bound fact 
will be automatically retired when rule `effect{}` part (RHS) will be 
applied to WM. So if we have a rule:
```kotlin
rule {
    +F1(x)
    effect {
        -F1(x)
        +F1(func(x))
    }
}
```
Then we can rewrite it as:
```kotlin
rule {
    -F1(x)
    effect {
        +F1(func(x))
    }
}
``` 
Hence the `move rule v1` above may be rewritten in a shorter form as: 
```kotlin
val aid = ref<Int>("aid") // Actor ID reference with name "aid". If name is not specified it will be generagted which is not very convenient for debug printing.
val cid = ref<Int>("cid")
val cid0 = ref<Int>("cid0")
val nrge = ref<Int>("nrgy")
val type = ref<ActorType>("type")

// Move rule v2
rule {
  
    -MoveCommand(aid, cid)
    -Actor(aid, nrgy, type)
    -At(aid, cid0)
    +Adjacent(cid, cid0)
    
    guard(nrgy gt 5.facet)
    
    effect { sol ->
       +At(aid, cid)  // Actor become at cell cid from now
       +Actor(aid, (sol[nrgy] - 5).facet, type) // Actor data updated with lower energy level    
    }
}
```
The primary goal of an engine is to find such a binding of references 
which is satisfying a pattern. Such a binding called 'solution'. Note 
that solution satisfies also a guard optionally existing in an LHS. 
Remember school? It is very similar like finding a solution for a system 
of an equations, where an equations are facts and variables are references.

After solution is found then the references in an `effect{...}` are 
substituted with constants and asserted or retired against WM. But there 
are cases when we want to explicitly specify the constant facet for some 
fact at `effect{...}` like from example above:
```kotlin
    effect { sol ->
       +Actor(aid, (sol[nrgy] - 5).facet, type)    
    }
```
the expression `(sol[nrgy] - 5).facet` does the computation of a new 
value of an energy level decreasing the old one it by 5 and converting 
an ordinary Kotlin value to `FacetConst` by calling an extension 
property `facet`. As you can see, to get the Kotlin value 
(i.e. not FacetConst<T> but T) bound to particular reference we use 
`sol[ref]` call.

### Computation semantics
TBD
