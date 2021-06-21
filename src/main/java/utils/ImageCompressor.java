package utils;

import org.bouncycastle.util.encoders.Base64;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;

public class ImageCompressor {
  public static String compress(String base64Image) throws IOException {
    BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(Base64.decode(base64Image)));

    Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
    ImageWriter writer = writers.next();

    ByteArrayOutputStream bas = new ByteArrayOutputStream();
    writer.setOutput(ImageIO.createImageOutputStream(bas));

    ImageWriteParam param = writer.getDefaultWriteParam();

    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
    param.setCompressionQuality(0.1f);  // Change the quality value you prefer
    writer.write(null, new IIOImage(bufferedImage, null, null), param);

    String base64ImageCompressed = Base64.toBase64String(bas.toByteArray());

    bas.close();
    writer.dispose();

    return base64ImageCompressed;
  }
}
