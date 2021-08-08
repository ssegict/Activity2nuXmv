package ict.generate.mwe;

import ict.generate.mwe.GenericExecutor;
import ict.generate.mwe.ISlotWriteback;
import java.lang.reflect.Method;
import org.eclipse.xtend.lib.annotations.AccessorType;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Pure;

/**
 * Slot contents need to have a function
 * save(String path)
 * otherwise noSuchMethodError will be thrown
 */
@SuppressWarnings("all")
public class SaveToFile extends GenericExecutor implements GenericExecutor.IExecutable {
  @Accessors({ AccessorType.PUBLIC_GETTER, AccessorType.PUBLIC_SETTER })
  private String saveTo = "";
  
  public SaveToFile() {
    super();
    this.setExecutable(this);
  }
  
  @Override
  public void run(final Object source, final ISlotWriteback writeback) {
    try {
      if ((source == null)) {
        throw new NullPointerException("Cannot save a null object to disk");
      }
      Method saveMeth = source.getClass().getMethod("save", new Class[] { String.class });
      saveMeth.invoke(source, this.saveTo);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Pure
  public String getSaveTo() {
    return this.saveTo;
  }
  
  public void setSaveTo(final String saveTo) {
    this.saveTo = saveTo;
  }
}
