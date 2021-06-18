package utils.crypto;

import org.bouncycastle.util.encoders.Base64;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Base64Test {
  @Test
  void encode() {
    assertEquals(Base64.toBase64String("Test".getBytes(StandardCharsets.UTF_8)), "VGVzdA==");
    assertEquals(Base64.toBase64String("A6npe@6m7a#hf&36".getBytes(StandardCharsets.UTF_8)), "QTZucGVANm03YSNoZiYzNg==");
  }

  @Test
  void decode() {
    assertEquals(new String(Base64.decode("VGVzdA=="), StandardCharsets.UTF_8), "Test");
    assertEquals(new String(Base64.decode("QTZucGVANm03YSNoZiYzNg=="), StandardCharsets.UTF_8), "A6npe@6m7a#hf&36");
  }

  @Test
  void complete() {
    String start = "LLMkznXujspfzyqxJh$&iszCxL9C3LR6qhBS^KE#v^6hqrm5nFieS#%Bt$fVKA&^j!7Np@nP7C!qR&icGejBDm%vJ@qQS#R9GZa6L2^#ivJzXX3!ZdFpjE@U8v7m^u35";

    String b64 = Base64.toBase64String(start.getBytes(StandardCharsets.UTF_8));

    assertEquals(new String(Base64.decode(b64), StandardCharsets.UTF_8), start);
  }
}