# XSalsa20Poly1305

[![Build Status](https://secure.travis-ci.org/codahale/xsalsa20poly1305.svg)](http://travis-ci.org/codahale/xsalsa20poly1305)

A pure Java library which provides symmetric and asymmetric encryption compatible with DJB's NaCl
library and its variants (e.g. libsodium). Also includes a class compatible with RbNaCl's SimpleBox
construction, which automatically manages nonces for you in a misuse-resistant fashion.

## Add to your project

```xml
<dependency>
  <groupId>com.codahale</groupId>
  <artifactId>xsalsa20poly1305</artifactId>
  <version>0.7.0</version>
</dependency>
```

It depends on Bouncy Castle for Salsa20, XSalsa20, and Poly1305 implementations, and on Open Whisper
Systems' curve25519-java for Curve25519 operations.

## Examples

```java
import com.codahale.xsalsa20poly1305.SimpleBox;
import java.nio.charset.StandardCharsets;

class Examples {
  void asymmetricEncryption() {
    // Alice has a key pair
    byte[] alicePrivateKey = SimpleBox.generatePrivateKey();
    byte[] alicePublicKey = SimpleBox.generatePublicKey(alicePrivateKey);
    
    // Bob also has a key pair
    byte[] bobPrivateKey = SimpleBox.generatePrivateKey();
    byte[] bobPublicKey = SimpleBox.generatePublicKey(bobPrivateKey);
    
    // Bob and Alice exchange public keys. (Not pictured.)
    
    // Bob wants to send Alice a very secret message. 
    byte[] message = "this is very secret".getBytes(StandardCharsets.UTF_8);
    
    // Bob encrypts the message using Alice's public key and his own private key
    SimpleBox bobBox = new SimpleBox(alicePublicKey, bobPrivateKey);
    byte[] ciphertext = bobBox.seal(message);
    
    // Bob sends Alice this ciphertext. (Not pictured.)
    
    // Alice decrypts the message using Bob's public key and her own private key.
    SimpleBox aliceBox = new SimpleBox(bobPublicKey, alicePrivateKey);
    byte[] plaintext = aliceBox.open(ciphertext);
    
    // Now Alice has the message!
  }
 
  void symmetricEncryption() {
    // There is a single secret key.
    byte[] secretKey = SimpleBox.generateSecretKey();  
   
    // And you want to use it to store a very secret message.
    byte[] message = "this is very secret".getBytes(StandardCharsets.UTF_8);
   
    // So you encrypt it.
    SimpleBox box = new SimpleBox(secretKey);
    byte[] ciphertext = box.seal(message);
    
    // And you store it. (Not pictured.)
    
    // And then you decrypt it later.
    byte[] plaintext = box.open(ciphertext);
    
    // Now you have the message again!
  }
  
  // There is also SecretBox, which behaves much like SimpleBox but requires you to manage your own
  // nonces. More on that later.
}
```

## Misuse-Resistant Nonces

XSalsa20Poly1305 is composed of two cryptographic primitives: XSalsa20, a stream cipher, and
Poly1305, a message authentication code. In order to be secure, both require a _nonce_ -- a bit
string which can only be used once for any given key. If a nonce is re-used -- i.e., used to encrypt
two different messages -- this can have catastrophic consequences for the confidentiality and
integrity of the encrypted messages: an attacker may be able to recover plaintext messages and even
forge seemingly-valid messages. As a result, it is incredibly important that nonces be unique.

XSalsa20 uses 24-byte (192-bit) nonces, which makes the possibility of a secure random number
generator generating the same nonce twice essentially impossible, even over trillions of messages.
For normal operations, `SecretBox#nonce()` (which simply returns 24 bytes from `SecureRandom`)
should be safe to use. But because of the downside risk of nonce misuse, this library provides a
secondary function for generating misuse-resistant nonces: `SecretBox#nonce()`, which requires the
message the nonce will be used to encrypt.

`SecretBox#nonce([]byte)` uses the BLAKE2b hash algorithm, keyed with the given key and using
randomly-generated 128-bit salt and personalization parameters. If the local `SecureRandom`
implementation is functional, the hash algorithm mixes those 256 bits of entropy along with the key
and message to produce a 192-bit nonce, which will have the same chance of collision as
`SecretBox#nonce()`. In the event that the local `SecureRandom` implementation is misconfigured,
exhausted of entropy, or otherwise compromised, the generated nonce will be unique to the given
combination of key and message, thereby preserving the security of the messages. Please note that in
this event, using `SecretBox#nonce()` to encrypt messages will be deterministic -- duplicate
messages will produce duplicate ciphertexts, and this will be observable to any attackers.

Because of the catastrophic downside risk of nonce reuse, the `SimpleBox` functions use
`SecretBox#nonce([]byte)` to generate nonces.

## Performance

For small messages (i.e. ~100 bytes), it's about as fast as `libsodium`-based libraries like Kalium,
but depends only on Bouncy Castle, which is pure Java. For larger messages (i.e., ~1KiB), Kalium is
faster:

```
Benchmark                      Mode  Cnt      Score      Error  Units
KaliumBenchmarks.seal100Bytes  avgt  200   1035.706 ±   15.781  ns/op
KaliumBenchmarks.seal1K        avgt  200   2783.802 ±   18.837  ns/op
KaliumBenchmarks.seal10K       avgt  200  21238.330 ±  174.044  ns/op
OurBenchmarks.seal100Bytes     avgt  200   1458.108 ±   13.039  ns/op
OurBenchmarks.seal1K           avgt  200   8999.346 ±   81.191  ns/op
OurBenchmarks.seal10K          avgt  200  83373.701 ± 1184.940  ns/op
```
## License

Copyright © 2017 Coda Hale

Distributed under the Apache License 2.0.
