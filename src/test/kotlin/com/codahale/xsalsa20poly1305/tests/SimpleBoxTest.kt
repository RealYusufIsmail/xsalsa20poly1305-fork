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
package com.codahale.xsalsa20poly1305.tests

import com.codahale.xsalsa20poly1305.Keys
import com.codahale.xsalsa20poly1305.SimpleBox
import java.util.*
import org.junit.jupiter.api.Test
import org.quicktheories.WithQuickTheories

internal class SimpleBoxTest : WithQuickTheories {
    @Test
    fun roundTrip() {
        qt().forAll(Generators.byteArrays(32, 32), Generators.byteArrays(1, 4096)).check {
            key,
            message ->
            val box = SimpleBox(key)
            box.open(box.seal(message)).let { a -> Arrays.equals(message, a) }
        }
    }

    @Test
    fun pkRoundTrip() {
        qt()
            .forAll(
                Generators.privateKeys(), Generators.privateKeys(), Generators.byteArrays(1, 4096))
            .check { privateKeyA, privateKeyB, message ->
                val publicKeyA = Keys.generatePublicKey(privateKeyA)
                val publicKeyB = Keys.generatePublicKey(privateKeyB)
                val boxA = SimpleBox(publicKeyB, privateKeyA)
                val boxB = SimpleBox(publicKeyA, privateKeyB)
                boxB.open(boxA.seal(message)).let { a -> Arrays.equals(message, a) }
            }
    }

    @Test
    fun shortMessage() {
        qt().forAll(Generators.byteArrays(32, 32), Generators.byteArrays(1, 24)).check {
            key,
            message ->
            !SimpleBox(key).open(message) != null
        }
    }
}
