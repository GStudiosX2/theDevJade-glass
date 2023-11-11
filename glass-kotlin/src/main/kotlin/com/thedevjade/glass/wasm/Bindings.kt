/*
 * Copyright (c) 2023. Made by theDevJade or contributors.
 */

@file:Suppress("unused")

package com.thedevjade.glass.wasm

object Bindings {
    fun method1() {
        println("Method1 called")
    }

    fun method2(param: String) {
        println("Method2 called with param: $param")
    }
}