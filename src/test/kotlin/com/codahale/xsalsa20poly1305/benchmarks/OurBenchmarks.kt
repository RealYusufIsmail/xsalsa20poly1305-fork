/*
 * Copyright © 2017 Coda Hale (coda.hale@gmail.com) && RealYusufIsmail && other YDWK contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package com.codahale.xsalsa20poly1305.benchmarks

import com.codahale.xsalsa20poly1305.SecretBox
import com.codahale.xsalsa20poly1305.SimpleBox
import java.util.*
import java.util.concurrent.TimeUnit
import org.openjdk.jmh.annotations.*

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
class OurBenchmarks {
    @Param("100", "1024", "10240") private val size = 100
    private val box: SecretBox = SecretBox(ByteArray(32))
    private val simpleBox: SimpleBox = SimpleBox(ByteArray(32))
    private val nonce = ByteArray(24)
    private val plaintext = ByteArray(size)
    private val boxCiphertext: ByteArray = box.seal(nonce, plaintext)
    private val simpleCiphertext: ByteArray = simpleBox.seal(plaintext)
    @Benchmark
    fun seal(): ByteArray {
        return box.seal(nonce, plaintext)
    }

    @Benchmark
    fun open(): ByteArray? {
        return box.open(nonce, boxCiphertext)
    }

    @Benchmark
    fun simpleSeal(): ByteArray {
        return simpleBox.seal(plaintext)
    }

    @Benchmark
    fun simpleOpen(): ByteArray? {
        return simpleBox.open(simpleCiphertext)
    }
}
