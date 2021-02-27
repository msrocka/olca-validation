package org.openlca.validation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;

class UnitValidation implements Runnable {

  private final IDatabase db;
  private final Validation validation;

  UnitValidation(Validation v) {
    this.db = db;
    this.validation = validation;
  }

  @Override
  public void run() {
    try {
      checkUnits();
    } catch (Exception e) {
      var err = Item.error("error in unit validation: " + e.getMessage());
      validation.queue.add(err);
    } finally {
      validation.queue.add(validation.FINISH);
    }

    // NativeSql.on(db).query(query, handler);
  }

  private void checkUnits() {
    if (validation.hasStopped())
      return;
    var names = new HashSet<String>();

  }

}
