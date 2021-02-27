package org.openlca.validation;

import org.openlca.core.database.derby.DerbyDatabase;

public class SimpleExample {

  public static void main(String[] args) {
    try (var db = DerbyDatabase.fromDataDir("ei2")) {
      var validation = Validation.on(db);
      validation.run();
      for (var item : validation.getItems()) {
        System.out.println(item);
      }
    }
  }
}
