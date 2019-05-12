package com.example


import io.ktor.application.*
import io.ktor.server.testing.*
import kotlinx.coroutines.*
import kotlin.system.measureTimeMillis
import kotlin.test.*

/**
 * Tests the [SomeApplication].
 */
class CoroutineExamples {

//    fun asyncFetch(): Deferred<Int> {
//        GlobalScope.launch {
//            delay(10000)
//        }
//
//        return 1
//    }

    suspend fun doSomethingUsefulOne(): Int {
        delay(1000L) // pretend we are doing something useful here
        return 13
    }

    suspend fun doSomethingUsefulTwo(): Int {
        delay(1000L) // pretend we are doing something useful here, too
        return 29
    }

    fun somethingUsefulOneAsync() = GlobalScope.async {
        doSomethingUsefulOne()
    }

    // The result type of somethingUsefulTwoAsync is Deferred<Int>
    fun somethingUsefulTwoAsync() = GlobalScope.async {
        doSomethingUsefulTwo()
    }

    suspend fun concurrentSum(): Int = coroutineScope {
        val one = async { doSomethingUsefulOne() }
        val two = async { doSomethingUsefulTwo() }
        one.await() + two.await()
    }

    /**
     * This is an integration test that verifies the behaviour of a simple conversation with an empty server.
     */
    @Test
    fun testJoinRoomMessaging() {
        // First we create a [TestApplicationEngine] that includes the module [Application.main],
        // this executes that function and thus installs all the features and routes to this test application.
        withTestApplication(Application::main) {

            // Keeps a log array that will hold all the events we want to check later at once.
//            val log = arrayListOf<String>()
//
//            // We perform a test websocket connection to this route. Effectively acting as a client.
//            // The [incoming] parameter allows to receive frames, while the [outgoing] allows to send frames to the server.
//            handleWebSocketConversation("/room/1?userId=1234") { incoming, outgoing ->
//                // We then receive two messages (the message notifying that the member joined, and the message we sent echoed to us)
//
//                log += (incoming.receive() as Frame.Text).readText()
////                for (n in 0 until 2) {
////                    log += (incoming.receive() as Frame.Text).readText()
////                }
////            }

            runBlocking {
                val time = measureTimeMillis {
                    val one = doSomethingUsefulOne()
                    val two = doSomethingUsefulTwo()
                    println("The answer is ${one + two}")
                }
                println("Round1 $time")
            }

            runBlocking {
                val time = measureTimeMillis {
                    val one = async { doSomethingUsefulOne() }
                    val two = async { doSomethingUsefulTwo() }
                    println("The answer is ${one.await() + two.await()}")
                }
                println("Round 2 $time")
            }


            runBlocking {
                val time = measureTimeMillis {
                    val one = async(start = CoroutineStart.LAZY) { doSomethingUsefulOne() }
                    val two = async(start = CoroutineStart.LAZY) { doSomethingUsefulTwo() }
                    one.start()
                    two.start()
                    println("The answer is ${one.await() + two.await()}")
                }
                println("Round 3 $time")
            }

            val time = measureTimeMillis {
                // we can initiate async actions outside of a coroutine
                val one = somethingUsefulOneAsync()
                val two = somethingUsefulTwoAsync()
                // but waiting for a result must involve either suspending or blocking.
                // here we use `runBlocking { ... }` to block the main thread while waiting for the result
                runBlocking {
                    println("The answer is ${one.await() + two.await()}")
                }
            }
            println("Round 4 $time")

            runBlocking {
                val time5 = measureTimeMillis {
                    println("The answer is ${concurrentSum()}")
                }
                println("Round 5 $time5")
            }

//            runBlocking {
//                val retVal = someAsyncTask()
//                println("RetVal $retVal")
//                assertEquals(1, 2)
//            }
//
//            GlobalScope.launch {
//                delay(1000)
//                println("Hello")
//
//                assertEquals(1, 2)
//            }

//            Thread.sleep(1100)
//            assertEquals(
//                listOf("{\"name\":\"user1\",\"type\":\"USER_CONNECTED\"}"),
//                log
//            )
        }
    }


}