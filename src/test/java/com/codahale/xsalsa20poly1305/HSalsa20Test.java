package com.codahale.xsalsa20poly1305;

import static com.codahale.xsalsa20poly1305.tests.Generators.byteArrays;
import static org.quicktheories.quicktheories.QuickTheory.qt;

import com.github.nitram509.jmacaroons.crypto.neilalexander.jnacl.hsalsa20;
import com.github.nitram509.jmacaroons.crypto.neilalexander.jnacl.xsalsa20;
import java.util.Arrays;
import org.junit.Test;

public class HSalsa20Test {

  @Test
  public void interoperability() throws Exception {
    qt().forAll(byteArrays(16, 16), byteArrays(32, 32))
        .check((in, key) -> {
          final byte[] a = new byte[32];
          final byte[] b = new byte[32];
          hsalsa20.crypto_core(a, in, key, xsalsa20.sigma);
          HSalsa20.hsalsa20(b, in, key);
          return Arrays.equals(a, b);
        });
  }
}