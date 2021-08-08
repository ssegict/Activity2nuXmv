package ict.generate.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.eclipse.xtext.xbase.lib.Exceptions;

@SuppressWarnings("all")
public class FileAccess {
  public static class IterableFileWrapper implements Iterable<String> {
    private final File file;
    
    public IterableFileWrapper(final String path) {
      this(new File(path));
    }
    
    public IterableFileWrapper(final File file) {
      this.file = file;
    }
    
    @Override
    public Iterator<String> iterator() {
      return new FileAccess.IterableFile(this.file);
    }
  }
  
  public static class IterableFile implements Iterator<String> {
    private final BufferedReader br;
    
    private String nextBuffer = null;
    
    public IterableFile(final File file) {
      try {
        FileReader _fileReader = new FileReader(file);
        BufferedReader _bufferedReader = new BufferedReader(_fileReader);
        this.br = _bufferedReader;
      } catch (Throwable _e) {
        throw Exceptions.sneakyThrow(_e);
      }
    }
    
    @Override
    public boolean hasNext() {
      try {
        if ((this.nextBuffer != null)) {
          return true;
        }
        this.nextBuffer = this.br.readLine();
        return (this.nextBuffer != null);
      } catch (Throwable _e) {
        throw Exceptions.sneakyThrow(_e);
      }
    }
    
    @Override
    public String next() {
      boolean _hasNext = this.hasNext();
      boolean _not = (!_hasNext);
      if (_not) {
        throw new NoSuchElementException();
      }
      String temp = this.nextBuffer;
      this.nextBuffer = null;
      return temp;
    }
  }
  
  public static void writeFile(final String path, final String content) {
    try {
      BufferedWriter wr = FileAccess.getWriter(path);
      wr.append(content);
      wr.close();
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public static BufferedWriter getWriter(final String path) {
    try {
      boolean _exists = new File(path).getParentFile().exists();
      boolean _not = (!_exists);
      if (_not) {
        new File(path).getParentFile().mkdirs();
      }
      File _file = new File(path);
      FileWriter _fileWriter = new FileWriter(_file);
      return new BufferedWriter(_fileWriter);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public static String readFile(final String path) {
    File _file = new File(path);
    return FileAccess.readFile(_file);
  }
  
  public static String readFile(final File file) {
    StringBuilder fcont = new StringBuilder();
    FileAccess.IterableFileWrapper _readFileLine = FileAccess.readFileLine(file);
    for (final String line : _readFileLine) {
      fcont.append(line).append(System.lineSeparator());
    }
    return fcont.toString();
  }
  
  public static FileAccess.IterableFileWrapper readFileLine(final String path) {
    return new FileAccess.IterableFileWrapper(path);
  }
  
  public static FileAccess.IterableFileWrapper readFileLine(final File file) {
    return new FileAccess.IterableFileWrapper(file);
  }
}
