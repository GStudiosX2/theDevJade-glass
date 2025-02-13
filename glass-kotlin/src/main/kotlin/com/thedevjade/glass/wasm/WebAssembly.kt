/*
 * Copyright (c) 2023. Made by theDevJade or contributors.
 */

package com.thedevjade.glass.wasm

import io.github.kawamuray.wasmtime.*
import io.github.kawamuray.wasmtime.WasmFunctions.Consumer0
import io.github.kawamuray.wasmtime.wasi.WasiCtx
import io.github.kawamuray.wasmtime.wasi.WasiCtxBuilder
import java.io.File
import kotlin.reflect.KFunction
import kotlin.reflect.full.memberFunctions

object WebAssembly {

    fun runEnvironment(pathToBinary: File, bindings: Any) {
        val functions: List<KFunction<*>> = getMethodsFromKObject(bindings::class.java)

        WasiCtxBuilder().build().use { ctx ->
            Store.withoutData(ctx).use { store ->
                store.engine().use { engine ->
                    Linker(engine).use { linker ->
                        WasiCtx.addToLinker(linker)
                        Module.fromFile(engine, pathToBinary.absolutePath).use { module ->
                            val wasmCtx = WasmContext(pathToBinary, store, bindings)
                            for (function in functions) {
                                linker.define(store, "kotlin_module", function.name, function.extern(wasmCtx))
                            }

                            linker.module(store, "", module)

                            linker.get(store, "", "run").get().func().use { f ->
                                val fn: Consumer0 = WasmFunctions.consumer(store, f)
                                fn.accept()
                            }

                            // cleanup
                            linker.externsOfModule(store, "kotlin_module").forEach {
                                if (it.extern().type() === Extern.Type.FUNC) {
                                    it.extern().func().close()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getMethodsFromKObject(clazz: Class<out Any>): List<KFunction<*>> {
        return clazz.kotlin.memberFunctions
            .filterNot { it.name in setOf("equals", "hashCode", "toString") }
            .map { it }
    }
    
}