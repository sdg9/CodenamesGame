package com.example


import kotlin.test.*

/**
 * Tests the [SomeApplication].
 */
class LambdaExamples {

    fun basicCallback(callback: () -> Any) {
        callback()
    }

    fun oneInput(callback: (input1: String) -> Any) {
        callback("hi")
    }

    fun twoInput(callback: (input1: String, input2: String) -> Unit) {
        callback("one", "two")
    }

    fun mixedBag(someString: String, callback: (input1: String, input2: String, input3: String, input4: String) -> Any, moreString: String) {
        callback("one", "two", someString, moreString)
    }


    /**
     * This is an integration test that verifies the behaviour of a simple conversation with an empty server.
     */
    @Test
    fun testLambda() {
        println("Lambda working")
        basicCallback {
            println("Some callback")
        }

        oneInput {
            println("Got passed $it")
        }

        oneInput {input1 ->
            println("Alternative way to handle single input $input1")
        }

        twoInput { input1, input2 ->
            println("Got passed $input1 and $input2")
        }

        mixedBag("first", { input1, input2, input3, input4 ->
            println("Got $input1, $input2, $input3, and $input4")
        }, "last")

//        val someFunction = { s: String -> println(s) }

        val someFunction = { channel: String, topic: String ->
            if (channel == topic) {
                println("Match")
            } else {
                println("NoMatch")
            }
        }

        someFunction("a", "b")
        someFunction("c", "c")
    }


}