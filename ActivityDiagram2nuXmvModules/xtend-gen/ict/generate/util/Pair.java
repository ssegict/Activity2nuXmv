package ict.generate.util;

import org.eclipse.xtend.lib.annotations.AccessorType;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Pure;

@SuppressWarnings("all")
public class Pair<V1 extends Object, V2 extends Object> {
  @Accessors({ AccessorType.PUBLIC_GETTER, AccessorType.PUBLIC_SETTER })
  private V1 first;
  
  @Accessors({ AccessorType.PUBLIC_GETTER, AccessorType.PUBLIC_SETTER })
  private V2 second;
  
  public Pair(final V1 first, final V2 second) {
    this.first = first;
    this.second = second;
  }
  
  @Override
  public boolean equals(final Object other) {
    if ((other == null)) {
      return false;
    }
    if ((this == other)) {
      return true;
    }
    boolean _equals = this.getClass().equals(other.getClass());
    if (_equals) {
      Pair<?, ?> otherPair = ((Pair<?, ?>) other);
      boolean _xifexpression = false;
      if ((this.first == null)) {
        _xifexpression = (otherPair.first == null);
      } else {
        _xifexpression = this.first.equals(otherPair.first);
      }
      boolean isEqual = _xifexpression;
      if ((!isEqual)) {
        return false;
      }
      boolean _xifexpression_1 = false;
      if ((this.second == null)) {
        Object _second = otherPair.getSecond();
        _xifexpression_1 = (_second == null);
      } else {
        _xifexpression_1 = this.second.equals(otherPair.getSecond());
      }
      return _xifexpression_1;
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    int _xifexpression = (int) 0;
    if ((this.first == null)) {
      _xifexpression = 0;
    } else {
      int _hashCode = this.first.hashCode();
      int _xifexpression_1 = (int) 0;
      if ((this.second == null)) {
        _xifexpression_1 = 0;
      } else {
        _xifexpression_1 = this.second.hashCode();
      }
      int _multiply = (17 * _xifexpression_1);
      _xifexpression = (_hashCode + _multiply);
    }
    return _xifexpression;
  }
  
  @Override
  public String toString() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Pair(");
    _builder.append(this.first);
    _builder.append(", ");
    _builder.append(this.second);
    _builder.append(")");
    return _builder.toString();
  }
  
  @Pure
  public V1 getFirst() {
    return this.first;
  }
  
  public void setFirst(final V1 first) {
    this.first = first;
  }
  
  @Pure
  public V2 getSecond() {
    return this.second;
  }
  
  public void setSecond(final V2 second) {
    this.second = second;
  }
}
