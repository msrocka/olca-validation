package org.openlca.validation;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.derby.DerbyDatabase;

class Tests {

  private static IDatabase _db ;

  static IDatabase getDB() {
    if (_db != null)
      return _db;
    _db = DerbyDatabase.createInMemory();
    return _db;
  }

}
