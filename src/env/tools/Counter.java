package tools;

import cartago.Artifact;
import cartago.OPERATION;
import cartago.ObsProperty;

public class Counter extends Artifact {
  void init(int initialValue) {
    defineObsProperty("count", initialValue);
  }

  @OPERATION
  void inc() {
    ObsProperty prop = getObsProperty("count");
    prop.updateValue(prop.intValue()+1);
    signal("tick");
  }
}

