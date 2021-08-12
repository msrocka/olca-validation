package org.openlca.validation;

import org.openlca.core.database.Derby;

public class SimpleExample {

  public static void main(String[] args) {

    try (var db = Derby.fromDataDir("ef_secondarydata_201908")) {
      long start = System.nanoTime();
      var validation = Validation.on(db);
      validation.run();
      long time = System.nanoTime() - start;
      System.out.printf("validation took %.3f sec", (double) time / 1e9);

      for (var item : validation.getItems()) {
        System.out.println(item);
      }
    }
  }
}
