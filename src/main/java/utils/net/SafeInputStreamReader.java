package utils.net;

import utils.Config;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class SafeInputStreamReader extends InputStreamReader {
  Logger logger = Config.getLogger(SafeInputStreamReader.class);

  private long bytesRead;

  // This class makes it so users can't send HUGE files and DOS
  public SafeInputStreamReader(InputStream in) {
    super(in, StandardCharsets.UTF_8);

    bytesRead = 0;
  }

  @Override
  public int read() throws IOException {
    return readBytes(super.read());
  }

  @Override
  public int read(char[] var1) throws IOException {
    return readBytes(super.read(var1, 0, var1.length));
  }

  @Override
  public int read(char[] var1, int var2, int var3) throws IOException {
    return readBytes(super.read(var1, var2, var3));
  }

  @Override
  public synchronized void reset() throws IOException {
    super.reset();
    bytesRead = 0;
  }

  @Override
  public String toString() {
    String string = new BufferedReader(this)
        .lines()
        .collect(Collectors.joining());

    try {
      this.close();
    } catch (IOException e) {
      logger.severe("Failed to close stream.");
      return string;
    }

    return string;
  }

  /*
    UTILS
  */
  private int readBytes(int lastRead) throws IOException {
    bytesRead += lastRead;

    if (bytesRead > Config.maxRequestSize)
      throw new IOException("Max file size passed.");

    return lastRead;
  }
}
