package org.openlca.validation;

import org.openlca.core.database.Derby;

public class SimpleExample {

  public static void main(String[] args) {
    try (var db = Derby.fromDataDir("ei2")) {
      var validation = Validation.on(db);
      validation.run();
      for (var item : validation.getItems()) {
        System.out.println(item);
      }
    }
  }
}
