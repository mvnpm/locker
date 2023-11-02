package io.mvnpm.maven.locker.pom;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;

public final class LockerPomFileAccessor {

  public final File file;

  private LockerPomFileAccessor(File file) {
    this.file = file;
  }

  public static LockerPomFileAccessor fromBasedir(File basedir, String filename) {
    return new LockerPomFileAccessor(new File(basedir, filename));
  }

  public Reader reader() {
    try {
      return new InputStreamReader(new FileInputStream(file), UTF_8);
    } catch (FileNotFoundException e) {
      throw new UncheckedIOException(e);
    }
  }

  public Writer writer() {
    file.getParentFile().mkdirs();
    try {
      return new OutputStreamWriter(new FileOutputStream(file), UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public boolean exists() {
    return file.exists();
  }

  public String filename() {
    return file.getAbsolutePath();
  }
}
