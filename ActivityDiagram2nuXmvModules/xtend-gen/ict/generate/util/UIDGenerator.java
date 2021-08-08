package ict.generate.util;

import java.util.HashMap;
import org.eclipse.xtend2.lib.StringConcatenation;

/**
 * Helper class that assigns every object given an ID
 */
@SuppressWarnings("all")
public class UIDGenerator {
  private static long currentID = 0;
  
  private static HashMap<Object, Long> mappings = new HashMap<Object, Long>();
  
  public static Long getUID(final Object obj) {
    boolean _containsKey = UIDGenerator.mappings.containsKey(obj);
    if (_containsKey) {
      return UIDGenerator.mappings.get(obj);
    }
    UIDGenerator.mappings.put(obj, Long.valueOf(UIDGenerator.currentID));
    return Long.valueOf(UIDGenerator.currentID++);
  }
  
  public static String getxmiUID(final Object obj) {
    Long uid = UIDGenerator.getUID(obj);
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("_");
    String _format = String.format("%020d", uid);
    _builder.append(_format);
    return _builder.toString();
  }
  
  public static String getNewxmiUID() {
    return UIDGenerator.getxmiUID(new Object());
  }
}
