package ict.generate.util;

import com.google.common.base.Objects;

@SuppressWarnings("all")
public class ThrowHelper {
  public enum ThrowType {
    WARN,
    
    ERROR;
  }
  
  public static void customThrow(final ThrowHelper.ThrowType type, final RuntimeException ex) {
    boolean _equals = Objects.equal(type, ThrowHelper.ThrowType.WARN);
    if (_equals) {
      ex.printStackTrace();
    } else {
      throw ex;
    }
  }
}
