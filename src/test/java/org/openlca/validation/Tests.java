package org.openlca.validation;

import org.openlca.core.database.Derby;
import org.openlca.core.database.IDatabase;

class Tests {

  private static IDatabase _db ;

  static IDatabase getDB() {
    if (_db != null)
      return _db;
    _db = Derby.createInMemory();
    return _db;
  }

}
