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
package com.codahale.xsalsa20poly1305

import com.codahale.xsalsa20poly1305.tests.Generators
import com.github.nitram509.jmacaroons.crypto.neilalexander.jnacl.hsalsa20
import com.github.nitram509.jmacaroons.crypto.neilalexander.jnacl.xsalsa20
import org.junit.jupiter.api.Test
import org.quicktheories.WithQuickTheories

internal class HSalsa20Test : WithQuickTheories {
    @Test
    fun interoperability() {
        qt().forAll(
            Generators.byteArrays(16, 16),
            Generators.byteArrays(32, 32)
        )
            .check { `in`, key ->
                val a = ByteArray(32)
                val b = ByteArray(32)
                hsalsa20.crypto_core(a, `in`, key, xsalsa20.sigma)
                HSalsa20.hsalsa20(b, `in`, key)
                a.contentEquals(b)
            }
    }
}