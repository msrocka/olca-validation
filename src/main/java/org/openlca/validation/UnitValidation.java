package org.openlca.validation;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;
import org.openlca.util.Strings;

class UnitValidation implements Runnable {

  private final Validation v;

  UnitValidation(Validation v) {
    this.v = v;
  }

  @Override
  public void run() {
    try {
      checkUnits();
    } catch (Exception e) {
      v.error("error in unit validation", e);
    } finally {
      v.workerFinished();
    }

    // NativeSql.on(db).query(query, handler);
  }

  private void checkUnits() {
    if (v.hasStopped())
      return;
    var noErrors = new AtomicBoolean(true);
    var names = new HashSet<String>();
    var sql = "select id, ref_id, name, conversion_factor, f_unit_group, synonyms";
    NativeSql.on(v.db).query(sql, r -> {
      long id = r.getLong(1);

      var refID = r.getString(2);
      if (Strings.nullOrEmpty(refID)) {
        v.error(id, ModelType.UNIT, "has no reference ID");
        noErrors.set(false);
        return !v.hasStopped();
      }

      var name = r.getString(3);
      if (Strings.nullOrEmpty(name)) {
        v.error(id, ModelType.UNIT, "has empty name");
        noErrors.set(false);
        return !v.hasStopped();
      }

      var factor = r.getDouble(4);
      if (factor <= 0) {
        v.error(id, ModelType.UNIT, "has invalid conversion factor: " + factor);
        noErrors.set(false);
        return !v.hasStopped();
      }

      var groupID = r.getLong(5);
      if (!v.ids.contains(ModelType.UNIT_GROUP, groupID)) {
        v.error(id, ModelType.UNIT, "no unit group @" + groupID);
        noErrors.set(false);
        return !v.hasStopped();
      }

      // name warning after errors
      if (names.contains(name)) {

      }

      return true;
    });
  }

}
