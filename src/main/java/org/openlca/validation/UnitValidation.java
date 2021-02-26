package org.openlca.validation;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;

class UnitValidation {

  private final IDatabase db;
  private final Validation validation;

  UnitValidation(Validation v) {
    this.db = db;
    this.validation = validation;
  }

  void run() {
    // NativeSql.on(db).query(query, handler);
  }

}
