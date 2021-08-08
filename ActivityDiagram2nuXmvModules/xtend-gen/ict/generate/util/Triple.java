package ict.generate.util;

import ict.generate.util.Pair;
import org.eclipse.xtend.lib.annotations.AccessorType;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.Pure;

@SuppressWarnings("all")
public class Triple<V1 extends Object, V2 extends Object, V3 extends Object> extends Pair<V1, V2> {
  @Accessors({ AccessorType.PUBLIC_GETTER, AccessorType.PUBLIC_SETTER })
  private V3 third;
  
  public Triple(final V1 first, final V2 second, final V3 third) {
    super(first, second);
    this.third = third;
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
      Triple<?, ?, ?> r = ((Triple<?, ?, ?>) other);
      boolean _equals_1 = super.equals(other);
      boolean _not = (!_equals_1);
      if (_not) {
        return false;
      }
      boolean _xifexpression = false;
      if ((this.third == null)) {
        Object _third = r.getThird();
        _xifexpression = (_third == null);
      } else {
        _xifexpression = this.third.equals(r.getThird());
      }
      return _xifexpression;
    }
    return false;
  }
  
  @Override
  public int hashCode() {
    int _hashCode = super.hashCode();
    int _xifexpression = (int) 0;
    if ((this.third == null)) {
      _xifexpression = 0;
    } else {
      _xifexpression = this.third.hashCode();
    }
    int _multiply = (17 * _xifexpression);
    return (_hashCode + _multiply);
  }
  
  @Override
  public String toString() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("Triple(");
    V1 _first = this.getFirst();
    _builder.append(_first);
    _builder.append(", ");
    V2 _second = this.getSecond();
    _builder.append(_second);
    _builder.append(", ");
    _builder.append(this.third);
    _builder.append(")");
    return _builder.toString();
  }
  
  @Pure
  public V3 getThird() {
    return this.third;
  }
  
  public void setThird(final V3 third) {
    this.third = third;
  }
}
