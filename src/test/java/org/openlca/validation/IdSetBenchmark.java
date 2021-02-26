package org.openlca.validation;

import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.model.ModelType;

public class IdSetBenchmark {

  public static void main(String[] args) {
    try (var db = DerbyDatabase.fromDataDir("ei22")) {
      for (int i = 0; i < 10; i++) {
        var start = System.currentTimeMillis();
        var ids = IdSet.of(db);
        var time = System.currentTimeMillis() - start;
        var count = 0;
        for (var type : ModelType.values()) {
          count += ids.allOf(type).size();
        }
        System.out.printf(
            "collected %d ids in %.3f seconds%n",
            count, time / 1000.0);
      }
    }
  }

}
