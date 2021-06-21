package utils;

import org.bouncycastle.util.encoders.Base64;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

class ImageCompressorTest {

  @Test
  void compress() throws IOException {
    System.out.println(System.getProperty("user.dir"));
    String image = Base64.toBase64String(Files.readAllBytes(Paths.get("src/test/java/utils/image.jpg")));
    String newImage = ImageCompressor.compress(image);

    System.out.println(newImage);
  }
}