package com.gofficer.codenames

import redux.api.*
import redux.*
import redux.api.enhancer.Middleware


interface Action

object NoAction: Action

class Action1: Action

class Action2: Action

class AddOne: Action

class SubtractOne: Action

data class Action3(val type: String, val payload: Object?): Action

val middleware = Middleware { store: Store<State>, next: Dispatcher, action: Any ->
    println("Mid 1 $action")
//    val result = next.dispatch(action)

    // triple action
    next.dispatch(action)
    next.dispatch(action)
    next.dispatch(action)
    action
}

val middleware2 = Middleware { store: Store<State>, next: Dispatcher, action: Any ->
    println("Mid 2 $action")
    next.dispatch(action)
    action
}

val actionInterfaceOnly = Middleware { store: Store<State>, next: Dispatcher, action: Any ->
    if (action !is Action) {
        println("Only allow action objects")
    } else {
        next.dispatch(action)
        action
    }
}

//val middlewares = arrayOf(middleware, middleware2)


fun main(args: Array<String>) {
    println("Test a")
//    val store = createStore(reducer, 0, applyMiddleware(middleware))

//    val store = createStore(reducer, State())
    val store = createStore(reducer, State(), applyMiddleware(actionInterfaceOnly, middleware, middleware2))

    store.subscribe {
        println("Update: ${store.getState()}")
    }


    store.dispatch("Inc")
// 1
    store.dispatch("Inc")
// 2
    store.dispatch("Dec")
// 1

    store.dispatch(Action1())

    store.dispatch(AddOne())

    store.dispatch(Action3("Inc", null))
}

data class State(val todos: Int = 1)


val reducer = Reducer { state: State, action: Any ->
    when (action) {
        is Action1 -> state.copy(todos = state.todos + 1)
        is Action2 -> state.copy(todos = state.todos - 1)
        else -> state
    }
//    when (action) {
//        "Inc" -> state.copy(todos = state.todos + 1)
//        "Dec" -> state.copy(todos = state.todos - 1)
//        else -> state
//    }
}


//
//fun applyMiddleware(vararg middlewares: Middleware<S>): Store.Enhancer<S> {
//    return Store.Enhancer { next ->
//        Store.Creator { reducer, initialState ->
//            object : Store<S> {
//                private val store = next.create(reducer, initialState)
//                private val rootDispatcher = middlewares.foldRight(store as Dispatcher) { middleware, next ->
//                    Dispatcher { action ->
//                        middleware.dispatch(this, next, action)
//                    }
//                }
//
//                override fun dispatch(action: Any) = rootDispatcher.dispatch(action)
//
//                override fun getState() = store.state
//
//                override fun replaceReducer(reducer: Reducer<S>) = store.replaceReducer(reducer)
//
//                override fun subscribe(subscriber: Subscriber) = store.subscribe(subscriber)
//            }
//        }
//    }
//}

//data class ApplicationState(
//        var activePageName: String
//)
//
//fun myReducer(state: ApplicationState, action: RAction) = when (action) {
//    is MyAction -> {
//        state.activePageName = "Changed State"
//        state
//    }
//    else -> state
//}

//
//abstract class StoreModel<S : Any> : Store<S> {
//
//    private val store by lazy { createStore() }
//
//    abstract fun createStore(): Store<S>
//
//    override fun getState() = store.getState()
//
//    override fun replaceReducer(reducer: Reducer<S>) = store.replaceReducer(reducer)
//
//    override fun subscribe(subscriber: Subscriber) = store.subscribe(subscriber)
//
//    override fun dispatch(action: Any) = store.dispatch(action)
//}
