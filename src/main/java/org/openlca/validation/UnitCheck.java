package org.openlca.validation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;
import org.openlca.util.Strings;

class UnitCheck implements Runnable {

  private final Validation v;

  UnitCheck(Validation v) {
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
    var sql = "select " +
      /* 1 */ "id, " +
      /* 2 */ "ref_id, " +
      /* 3 */ "name, " +
      /* 4 */ "conversion_factor, " +
      /* 5 */ "f_unit_group, " +
      /* 6 */ "synonyms from tbl_units";
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
        v.error(id, ModelType.UNIT,
          "has invalid conversion factor: " + factor);
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
        v.warning("duplicate unit name / synonym: " + name);
        noErrors.set(false);
        return !v.hasStopped();
      }
      names.add(name);
      var synonyms = r.getString(6);
      if (!Strings.nullOrEmpty(synonyms)) {
        Arrays.stream(synonyms.split(";"))
          .forEach(synonym -> {
            var syn = synonym.trim();
            if (Strings.notEmpty(syn)) {
              if (names.contains(syn)) {
                v.warning("duplicate unit name / synonym: " + name);
                noErrors.set(false);
              }
              names.add(syn);
            }
          });
        return !v.hasStopped();
      }



      return true;
    });
  }

}
