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
import org.quicktheories.WithQuickTheories
import kotlin.test.assertEquals

internal class KeysTest : WithQuickTheories {
    @Test
    fun generateSecretKey() {
        val message = "this is a test".toByteArray(java.nio.charset.StandardCharsets.UTF_8)
        val key = Keys.generateSecretKey()
        val box = SecretBox(key)
        val secondBox = org.abstractj.kalium.crypto.SecretBox(key)
        val n: ByteArray = box.nonce(message)
        assertEquals(message.size + 16, secondBox.encrypt(n, message).size)
    }

    @Test
    fun generateKeyPair() {
        val privateKeyA = Keys.generatePrivateKey()
        val publicKeyA = Keys.generatePublicKey(privateKeyA)
        val privateKeyB = Keys.generatePrivateKey()
        val publicKeyB = Keys.generatePublicKey(privateKeyB)
        assertEquals(32, privateKeyA.size)
    }

    @Test
    fun sharedSecrets() {
        qt().forAll(
            Generators.privateKeys(),
            Generators.privateKeys()
        )
            .check { privateKeyA, privateKeyB ->
                val publicKeyA = Keys.generatePublicKey(privateKeyA)
                val publicKeyB = Keys.generatePublicKey(privateKeyB)
                val secretAB = Keys.sharedSecret(publicKeyA, privateKeyB)
                val secretBA = Keys.sharedSecret(publicKeyB, privateKeyA)
                secretAB.contentEquals(secretBA)
            }
    }
}