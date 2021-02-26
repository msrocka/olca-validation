package org.openlca.validation;

import javax.persistence.Table;

import org.openlca.core.model.ModelType;

public class Main {

  public static void main(String[] args) {
    for (var type : ModelType.values()) {
      var clazz = type.getModelClass();
      if (clazz == null)
        continue;
      System.out.printf("Pair.of(ModelType.%s, \"%s\"),%n",
          type.name(),
          type.getModelClass().getAnnotation(Table.class).name());
    }
  }

}
