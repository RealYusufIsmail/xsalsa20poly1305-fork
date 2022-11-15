/*
 * Copyright © 2017 Coda Hale (coda.hale@gmail.com)
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
import com.codahale.xsalsa20poly1305.SecretBox
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.quicktheories.WithQuickTheories
import java.util.*
import java.util.function.Supplier
import java.util.stream.Collectors
import java.util.stream.IntStream
import javax.swing.Box
import kotlin.test.assertNotNull

internal class SecretBoxTest : WithQuickTheories {
    @Test
    fun shortKey() {
        qt().forAll(Generators.byteArrays(1, 31))
            .checkAssert { key ->
                assertThrows<IllegalArgumentException> {
                    if (key != null) {
                        SecretBox(key)
                    } else {
                        //fail the test
                        throw IllegalArgumentException()
                    }
                }
            }
    }

    @Test
    fun roundTrip() {
        qt().withExamples(1)
            .withShrinkCycles(1)
            .forAll(Generators.byteArrays(32, 32), Generators.byteArrays(24, 24), Generators.byteArrays(1, 4096))
            .check { key, nonce, message ->
                assertNotNull(key)
                assertNotNull(message)
                val box = SecretBox(key)
                box.open(nonce, box.seal(nonce, message))
                    .let { a -> Arrays.equals(message, a) }
            }
    }

    @Test
    fun pkRoundTrip() {
        qt().forAll(
            Generators.privateKeys(),
            Generators.privateKeys(),
            Generators.byteArrays(24, 24),
            Generators.byteArrays(1, 4096)
        )
            .check { privateKeyA, privateKeyB, nonce, message ->
                val publicKeyA = Keys.generatePublicKey(privateKeyA)
                val publicKeyB = Keys.generatePublicKey(privateKeyB)
                val boxA = SecretBox(publicKeyB, privateKeyA)
                val boxB = SecretBox(publicKeyA, privateKeyB)
                assertNotNull(message)
                boxB.open(nonce, boxA.seal(nonce, message))
                    .let { a -> Arrays.equals(message, a) }
            }
    }

    @Test
    fun badKey() {
        qt().forAll(
            Generators.byteArrays(32, 32),
            Generators.byteArrays(24, 24),
            Generators.byteArrays(1, 4096),
            Generators.byteArrays(32, 32)
        )
            .assuming { keyA, nonce, message, keyB -> !Arrays.equals(keyA, keyB) }
            .check { keyA, nonce, message, keyB ->
                assertNotNull(keyA)
                assertNotNull(keyB)
                assertNotNull(message)
                val ciphertext: ByteArray = SecretBox(keyA).seal(nonce, message)
                val plaintext: ByteArray? = SecretBox(keyB).open(nonce, ciphertext)
                !plaintext != null
            }
    }

    @Test
    fun badNonce() {
        qt().forAll(
            Generators.byteArrays(32, 32),
            Generators.byteArrays(24, 24),
            Generators.byteArrays(1, 4096),
            Generators.byteArrays(24, 24)
        )
            .assuming { key, nonceA, message, nonceB -> !Arrays.equals(nonceA, nonceB) }
            .check { key, nonceA, message, nonceB ->
                val box = SecretBox(key)
                !box.open(nonceB, box.seal(nonceA, message)) != null
            }
    }

    @Test
    fun badCiphertext() {
        qt().forAll(
            Generators.byteArrays(32, 32),
            Generators.byteArrays(24, 24),
            Generators.byteArrays(1, 4096),
            integers().allPositive()
        )
            .check { key, nonce, message, v ->
                val box = SecretBox(key)
                val ciphertext: ByteArray = box.seal(nonce, message)
                // flip a single random bit of plaintext
                var mask = (1 shl v % 8).toByte()
                if (mask.toInt() == 0) {
                    mask = 1
                }
                ciphertext[v % ciphertext.size] = (ciphertext[v % ciphertext.size].toInt() xor mask.toInt()).toByte()
                !box.open(nonce, ciphertext) != null
            }
    }

    @Test
    fun randomNonce() {
        val box = SecretBox(ByteArray(32))
        val nonces = IntStream.range(0, 1000).mapToObj { _: Int -> box.nonce() }.collect(Collectors.toList())
        qt().forAll(integers().between(1, 1000), integers().between(1, 1000))
            .assuming { x, y -> x != y }
            .check { x, y -> !Arrays.equals(nonces[x - 1], nonces[y - 1]) }
        qt().forAll(integers().all()).check { box.nonce().size == 24 }
    }

    @Test
    fun misuseResistantNonce() {
        qt().forAll(Generators.byteArrays(32, 32), Generators.byteArrays(1, 4096))
            .check { key, message ->
                val box = SecretBox(key)
                box.nonce(message).size == 24
            }
    }

    @Test
    fun fromUsToLibSodium() {
        qt().forAll(Generators.byteArrays(32, 32), Generators.byteArrays(24, 24), Generators.byteArrays(1, 4096))
            .check { key, nonce, message ->
                val c: ByteArray = SecretBox(key).seal(nonce, message)
                val theirBox = org.abstractj.kalium.crypto.SecretBox(key)
                val p: ByteArray? = tryTo(Supplier { theirBox.decrypt(nonce, c) })
                p.let { a -> Arrays.equals(message, a) }
            }
    }

    @Test
    fun fromLibSodiumToUs() {
        qt().forAll(Generators.byteArrays(32, 32), Generators.byteArrays(24, 24), Generators.byteArrays(1, 4096))
            .check { key, nonce, message ->
                val c: ByteArray = org.abstractj.kalium.crypto.SecretBox(key).encrypt(nonce, message)
                val p: ByteArray? = SecretBox(key).open(nonce, c)
                p.let { a -> Arrays.equals(message, a) }
            }
    }

    @Test
    fun pkFromUsToLibSodium() {
        qt().forAll(
            Generators.privateKeys(),
            Generators.privateKeys(),
            Generators.byteArrays(24, 24),
            Generators.byteArrays(1, 4096)
        )
            .check { privateKeyA, privateKeyB, nonce, message ->
                val publicKeyA = Keys.generatePublicKey(privateKeyA)
                val publicKeyB = Keys.generatePublicKey(privateKeyB)
                val ourBox = SecretBox(publicKeyB, privateKeyA)
                val c: ByteArray = ourBox.seal(nonce, message)
                val theirBox = org.abstractj.kalium.crypto.Box(publicKeyA, privateKeyB)
                val p: ByteArray? = tryTo(Supplier { theirBox.decrypt(nonce, c) })
                p.let { a -> Arrays.equals(message, a) }
            }
    }

    @Test
    fun pkFromLibSodiumToUs() {
        qt().forAll(
            Generators.privateKeys(),
            Generators.privateKeys(),
            Generators.byteArrays(24, 24),
            Generators.byteArrays(1, 4096)
        )
            .check { privateKeyA, privateKeyB, nonce, message ->
                val publicKeyA = Keys.generatePublicKey(privateKeyA)
                val publicKeyB = Keys.generatePublicKey(privateKeyB)
                val theirBox = org.abstractj.kalium.crypto.Box(publicKeyB, privateKeyA)
                val c: ByteArray = theirBox.encrypt(nonce, message)
                val ourBox = SecretBox(publicKeyA, privateKeyB)
                ourBox.open(nonce, c).let { a -> Arrays.equals(message, a) }
            }
    }

    private fun <T> tryTo(f: Supplier<T>): T? {
        return try {
            f.get()
        } catch (e: RuntimeException) {
            null
        }
    }
}

operator fun ByteArray?.not(): Any {
    return this != null
}
