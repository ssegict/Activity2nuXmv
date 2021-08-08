package ict.generate.mwe;

import ict.generate.mwe.ISlotWriteback;
import org.eclipse.emf.mwe2.runtime.workflow.IWorkflowComponent;
import org.eclipse.emf.mwe2.runtime.workflow.IWorkflowContext;
import org.eclipse.xtend.lib.annotations.AccessorType;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtext.xbase.lib.Pure;

@SuppressWarnings("all")
public class GenericExecutor implements IWorkflowComponent {
  public interface IExecutable {
    void run(final Object source, final ISlotWriteback writeback);
  }
  
  @Accessors({ AccessorType.PUBLIC_GETTER, AccessorType.PUBLIC_SETTER })
  private String inSlot = null;
  
  @Accessors({ AccessorType.PUBLIC_GETTER, AccessorType.PUBLIC_SETTER })
  private String outSlot = null;
  
  @Accessors({ AccessorType.PUBLIC_GETTER, AccessorType.PUBLIC_SETTER })
  private GenericExecutor.IExecutable executable = null;
  
  @Override
  public void invoke(final IWorkflowContext ctx) {
    Object data = ctx.get(this.inSlot);
    this.executable.run(data, new ISlotWriteback() {
      @Override
      public void putSlot(final Object o) {
        ctx.put(GenericExecutor.this.outSlot, o);
      }
    });
  }
  
  @Override
  public void postInvoke() {
  }
  
  @Override
  public void preInvoke() {
  }
  
  @Pure
  public String getInSlot() {
    return this.inSlot;
  }
  
  public void setInSlot(final String inSlot) {
    this.inSlot = inSlot;
  }
  
  @Pure
  public String getOutSlot() {
    return this.outSlot;
  }
  
  public void setOutSlot(final String outSlot) {
    this.outSlot = outSlot;
  }
  
  @Pure
  public GenericExecutor.IExecutable getExecutable() {
    return this.executable;
  }
  
  public void setExecutable(final GenericExecutor.IExecutable executable) {
    this.executable = executable;
  }
}
