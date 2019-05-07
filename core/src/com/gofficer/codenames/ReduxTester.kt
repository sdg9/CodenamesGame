package com.gofficer.codenames


import com.freeletics.coredux.*
import kotlinx.coroutines.*

import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import java.util.Collections.copy

data class SomeState(val counter: Int = 0, val otherCounter: Int = 0)

suspend fun main(args: Array<String>) {
    println("Test a")
//    val testLogger by memoized { TestLogger() }

    val store = GlobalScope.createStore<SomeState, CalculatorAction>(
            name = "Calculator",
            initialState = 0,
            launchMode = CoroutineStart.DEFAULT,
            sideEffects = listOf(sideEffect),
            reducer = { currentState, newAction ->
                when (newAction) {
                    is CalculatorAction.Add -> currentState
//                    is CalculatorAction.Add -> currentState + newAction.value
                    is CalculatorAction.Deduct -> copy(counter = counter - newAction.value)
                    is CalculatorAction.Multiply -> currentState * newAction.value
                    is CalculatorAction.Divide -> currentState / newAction.value
                }
            }

    )

    store.subscribe { state -> println("Updated state: $state") }

    store.dispatch(CalculatorAction.Add(1))

//    store.dispatch(CalculatorAction.Add(1))

    store.dispatch(CalculatorAction.Add(1))


    delay(1000)
}
sealed class CalculatorAction {
    data class Add(val value: Int) : CalculatorAction()
    data class Deduct(val value: Int) : CalculatorAction()
    data class Multiply(val value: Int) : CalculatorAction()
    data class Divide(val value: Int) : CalculatorAction()
}

val logSink1 = "hi"

val sideEffect = object : SideEffect<SomeState, CalculatorAction> {
    override val name: String = "network logger"

    override fun CoroutineScope.start(
            input: ReceiveChannel<CalculatorAction>,
            stateAccessor: StateAccessor<Int>,
            output: SendChannel<CalculatorAction>,
//            logSinks = listOf(logSink1),
            logger: SideEffectLogger
    ): Job = launch(context = CoroutineName(name)) {
        for (inputAction in input) {
            logger.logSideEffectEvent {
                LogEvent.SideEffectEvent.InputAction(name, inputAction)
            }
            println("State Accesor ${stateAccessor()}")
//            println("Side effect")
            if (inputAction is CalculatorAction.Add &&
                    stateAccessor() >= 0) {
                launch {
//                    val response = makeNetworkCall()
                    val response = 200
                    logger.logSideEffectEvent {
                        LogEvent.SideEffectEvent.Custom(name, "Received network response: $response")
                    }
                    if (response == 200) {
                        val outputAction = CalculatorAction.Deduct(1)
                        logger.logSideEffectEvent { LogEvent.SideEffectEvent.DispatchingToReducer(name, outputAction) }
                        println("Sending output action $outputAction")
                        output.send(outputAction)
                    }
                }
            }
        }
    }
}
