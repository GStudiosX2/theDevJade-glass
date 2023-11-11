/*
 * Copyright (c) 2023. Made by theDevJade or contributors.
 */

@file:Suppress("NAME_SHADOWING")

package com.thedevjade.glass.wasm

import io.github.kawamuray.wasmtime.*
import io.github.kawamuray.wasmtime.WasmFunctions.Consumer0
import io.github.kawamuray.wasmtime.wasi.WasiCtxBuilder
import java.io.File
import java.util.*
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.javaType

fun KType?.valType(): List<Val.Type>? {
    if (this == null) return null

    return when (this.javaType) {
        Int::class.java -> listOf(Val.Type.I32)
        Long::class.java -> listOf(Val.Type.I64)
        Float::class.java -> listOf(Val.Type.F32)
        Double::class.java -> listOf(Val.Type.F64)
        Short::class.java -> listOf(Val.Type.I32) // for some reason wasmtime doesn't have an I16
        String::class.java -> listOf(Val.Type.I32, Val.Type.I32)
        else -> null
    }
}

fun KFunction<*>.extern(store: Store<Void>, bindings: Any): Extern {
    // TODO: return values
    val parameters = this.parameters
    val args: MutableList<Val.Type> = ArrayList()

    parameters.forEach { param ->
        if (param.kind != KParameter.Kind.INSTANCE) {
            param.type.valType()?.let {
                args.addAll(it)
            }
        }
    }

    return Extern.fromFunc(Func(store, FuncType.emptyResults(*args.toTypedArray())) { caller, params, _ ->
        val args: MutableList<Any> = mutableListOf()

        var i = 0
        var fnParamI = 0
        while (i < params.size) {
            args.add(when (parameters[fnParamI + 1].type.javaType) {
                String::class.java -> { // special case for String
                    caller.getExport("memory").get().memory().use { mem ->
                        val ptr = params[i].i32()
                        val bytes = ByteArray(params[++i].i32())
                        for (i2 in bytes.indices) {
                            bytes[i2] = mem.buffer(store).get(ptr + i2)
                        }
                        String(bytes)
                    }
                }

                else -> params[i].value
            })
            i++; fnParamI++
        }

        this.call(bindings, *args.toTypedArray())
    })
}

object WebAssembly {

    fun runEnvironment(pathToBinary: File, bindings: Any) {
        val functions: List<KFunction<*>> = getMethodsFromKObject(bindings::class.java)

        WasiCtxBuilder().build().use { ctx ->
            Store.withoutData(ctx).use { store ->
                store.engine().use { engine ->
                    Module.fromFile(engine, pathToBinary.absolutePath).use { module ->
                        val imports: MutableList<Extern> = ArrayList()
                        for (function in functions) {
                            imports.add(function.extern(store, bindings))
                        }

                        Instance(store, module, imports).use { instance ->
                            instance.getFunc(store, "run").get().use { f ->
                                val fn: Consumer0 = WasmFunctions.consumer(store, f)
                                fn.accept()
                            }
                        }

                        imports.forEach { it.func().close() }
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