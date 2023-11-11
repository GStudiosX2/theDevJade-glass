package com.thedevjade.glass

import com.thedevjade.glass.wasm.Bindings
import com.thedevjade.glass.wasm.WebAssembly
import java.io.File
import java.util.logging.Level
import java.util.logging.LogManager

fun main(args: Array<String>) {
    var showDebugLogs = false
    var wasmFilePath = "./binaries/glass_rust_bg.wasm"

    var index = 0
    while (index < args.size) {
        val arg = args[index]

        if (arg.equals("-d", true) || arg.equals("-debug", true)) {
            showDebugLogs = true
        } else if (arg.equals("-fp", true) || arg.equals("-filePath", true)) {
            val strFp = StringBuilder()

            index++
            if (index > args.size) {
                error("Expected string after -fp")
            }

            if (args[index].startsWith('"')) {
                strFp.append(args[index].drop(1))
                while (!args[index].endsWith('"')) {
                    strFp.append(args[index])
                    index++

                    if (index > args.size) {
                        error("Unexpected end of string")
                    }
                }

                strFp.deleteCharAt(strFp.length)
            } else {
                strFp.append(args[index + 1])
            }

            wasmFilePath = strFp.toString()
        } else {
            error("Invalid flag $arg")
        }
    }

    if (showDebugLogs) {
        val rootLogger = LogManager.getLogManager().getLogger("")
        rootLogger.level = Level.ALL
        rootLogger.handlers.forEach { h -> h.level = Level.ALL }
    }

    println("Initializing..")

    val file = File(wasmFilePath)
    WebAssembly.runEnvironment(file, Bindings)
}
