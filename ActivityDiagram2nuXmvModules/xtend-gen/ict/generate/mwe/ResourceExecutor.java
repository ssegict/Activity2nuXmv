package ict.generate.mwe;

import ict.generate.mwe.ISlotWriteback;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.mwe2.runtime.workflow.IWorkflowComponent;
import org.eclipse.emf.mwe2.runtime.workflow.IWorkflowContext;
import org.eclipse.xtend.lib.annotations.AccessorType;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Pure;

@SuppressWarnings("all")
public class ResourceExecutor implements IWorkflowComponent {
  public interface IResourceExecutable {
    void run(final List<Resource> resources, final ISlotWriteback writeback);
  }
  
  @Accessors({ AccessorType.PUBLIC_GETTER, AccessorType.PUBLIC_SETTER })
  private String inSlot = null;
  
  @Accessors({ AccessorType.PUBLIC_GETTER, AccessorType.PUBLIC_SETTER })
  private String outSlot = null;
  
  @Accessors({ AccessorType.PUBLIC_GETTER, AccessorType.PUBLIC_SETTER })
  private ResourceExecutor.IResourceExecutable executable = null;
  
  @Override
  public void invoke(final IWorkflowContext ctx) {
    Object data = ctx.get(this.inSlot);
    if ((data == null)) {
      throw new IllegalStateException((("Slot \'" + this.inSlot) + "\' was empty!"));
    }
    ArrayList<Resource> resources = new ArrayList<Resource>();
    if ((data instanceof Iterable)) {
      for (final Object object2 : ((Iterable)data)) {
        if ((object2 instanceof Resource)) {
          resources.add(((Resource)object2));
        } else {
          StringConcatenation _builder = new StringConcatenation();
          _builder.append("Slot contents was not a Resource but a \'");
          String _simpleName = object2.getClass().getSimpleName();
          _builder.append(_simpleName);
          _builder.append("\'");
          _builder.newLineIfNotEmpty();
          throw new IllegalStateException(_builder.toString());
        }
      }
    } else {
      if ((data instanceof Resource)) {
        resources.add(((Resource)data));
      } else {
        StringConcatenation _builder_1 = new StringConcatenation();
        _builder_1.append("Slot contents was not a Resource but a \'");
        String _simpleName_1 = data.getClass().getSimpleName();
        _builder_1.append(_simpleName_1);
        _builder_1.append("\'");
        _builder_1.newLineIfNotEmpty();
        throw new IllegalStateException(_builder_1.toString());
      }
    }
    final Function1<Resource, Boolean> _function = (Resource it) -> {
      return Boolean.valueOf(it.getURI().isFile());
    };
    List<Resource> _list = IterableExtensions.<Resource>toList(IterableExtensions.<Resource>filter(resources, _function));
    this.executable.run(_list, new ISlotWriteback() {
      @Override
      public void putSlot(final Object o) {
        ctx.put(ResourceExecutor.this.outSlot, o);
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
  public ResourceExecutor.IResourceExecutable getExecutable() {
    return this.executable;
  }
  
  public void setExecutable(final ResourceExecutor.IResourceExecutable executable) {
    this.executable = executable;
  }
}
