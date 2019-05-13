package com.gofficer.codenames.desktop

//sealed class CalculatorAction {
//    data class Add(val value: Int) : CalculatorAction()
//    data class Deduct(val value: Int) : CalculatorAction()
//    data class Multiply(val value: Int) : CalculatorAction()
//    data class Divide(val value: Int) : CalculatorAction()
//}
//
//
//fun create(coroutineScope: CoroutineScope): Store<State, com.gofficer.codenames.redux.Action> = coroutineScope
//        .createStore(
//                name = "Pagination State Machine",
//                initialState = State.LoadingFirstPageState,
//                logSinks = loggers.toList(),
//                sideEffects = listOf(
//                        loadFirstPageSideEffect,
//                        loadNextPageSideEffect,
//                        showAndHideLoadingErrorSideEffect
//                ),
//                reducer = ::reducer
//        )
//
//val store = coroutineScope.createStore<Int, CalculatorAction>(
//        name = "Calculator",
//        initialState = 0,
//        reducer = { currentState, newAction ->
//            when (newAction) {
//                is CalculatorAction.Add -> currentState + newAction.value
//                is CalculatorAction.Deduct -> currentState - newAction.value
//                is CalculatorAction.Multiply -> currentState * newAction.value
//                is CalculatorAction.Divide -> currentState / newAction.value
//            }
//        }
//)


fun main(args: Array<String>) {
    println("Test")
}
