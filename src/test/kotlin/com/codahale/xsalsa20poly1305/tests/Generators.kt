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

import java.util.*
import org.quicktheories.core.Gen
import org.quicktheories.impl.Constraint

interface Generators {
    companion object {
        fun byteArrays(minLength: Int, maxLength: Int): Gen<ByteArray> {
            val gen: Gen<ByteArray> =
                Gen<ByteArray> { prng ->
                    val bytes =
                        ByteArray(
                            prng
                                .next(Constraint.between(minLength.toLong(), maxLength.toLong()))
                                .toInt())
                    for (i in bytes.indices) {
                        bytes[i] = prng.next(Constraint.between(0, 255)).toByte()
                    }
                    bytes
                }
            return gen.describedAs(Arrays::toString)
        }

        fun privateKeys(): Gen<ByteArray> {
            return byteArrays(32, 32).map { privateKey: ByteArray -> clamp(privateKey) }
        }

        fun clamp(privateKey: ByteArray): ByteArray {
            privateKey[0] = (privateKey[0].toInt() and 248.toByte().toInt()).toByte()
            privateKey[31] = (privateKey[31].toInt() and 127.toByte().toInt()).toByte()
            privateKey[31] = (privateKey[31].toInt() or 64.toByte().toInt()).toByte()
            return privateKey
        }
    }
}
