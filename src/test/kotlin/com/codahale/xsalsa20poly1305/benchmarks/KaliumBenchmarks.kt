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

import java.util.concurrent.TimeUnit
import org.abstractj.kalium.crypto.SecretBox
import org.openjdk.jmh.annotations.*

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
class KaliumBenchmarks {
    private val box: SecretBox = SecretBox(ByteArray(32))

    @Param("100", "1024", "10240") private val size = 100
    private val nonce = ByteArray(24)
    private val plaintext = ByteArray(size)
    private val ciphertext: ByteArray = box.encrypt(nonce, plaintext)
    @Benchmark
    fun encrypt(): ByteArray {
        return box.encrypt(nonce, plaintext)
    }

    @Benchmark
    fun decrypt(): ByteArray {
        return box.decrypt(nonce, ciphertext)
    }
}
