package org.openlca.validation;

import java.util.Arrays;
import java.util.HashSet;

import gnu.trove.set.hash.TLongHashSet;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;
import org.openlca.util.Strings;

class UnitCheck implements Runnable {

  private final Validation v;
  private boolean foundErrors = false;

  UnitCheck(Validation v) {
    this.v = v;
  }

  @Override
  public void run() {
    try {
      var unitIDs = checkUnits();
      checkGroups(unitIDs);
      if (!foundErrors && !v.hasStopped()) {
        v.ok("checked units and unit groups");
      }
    } catch (Exception e) {
      v.error("error in unit validation", e);
    } finally {
      v.workerFinished();
    }
  }

  private TLongHashSet checkUnits() {
    if (v.hasStopped())
      return new TLongHashSet(0);
    var unitIDs = new TLongHashSet();
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
      unitIDs.add(id);

      var refID = r.getString(2);
      if (Strings.nullOrEmpty(refID)) {
        v.error(id, ModelType.UNIT, "has no reference ID");
        foundErrors = true;
      }

      var name = r.getString(3);
      if (Strings.nullOrEmpty(name)) {
        v.error(id, ModelType.UNIT, "has empty name");
        foundErrors = true;
      }

      var factor = r.getDouble(4);
      if (factor <= 0) {
        v.error(id, ModelType.UNIT,
          "has invalid conversion factor: " + factor);
        foundErrors = true;
      }

      var groupID = r.getLong(5);
      if (!v.ids.contains(ModelType.UNIT_GROUP, groupID)) {
        v.error(id, ModelType.UNIT, "no unit group @" + groupID);
        foundErrors = true;
      }

      // name warning after errors
      if (names.contains(name)) {
        v.warning("duplicate unit name / synonym: " + name);
        foundErrors = true;
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
                foundErrors = true;
              }
              names.add(syn);
            }
          });
      }
      return !v.hasStopped();
    });

    return unitIDs;
  }

  private void checkGroups(TLongHashSet unitIDs) {
    if (v.hasStopped())
      return;

    var sql = "select " +
      /* 1 */ "id, " +
      /* 2 */ "f_reference_unit, " +
      /* 3 */ "f_default_flow_property from tbl_unit_groups";
    NativeSql.on(v.db).query(sql, r -> {

      var id = r.getLong(1);
      var unitID = r.getLong(2);
      if (!unitIDs.contains(unitID)) {
        v.error(id, ModelType.UNIT_GROUP,
          "invalid reference unit @" + unitID);
        foundErrors = true;
      }

      var propID = r.getLong(3);
      if (propID != 0 && !v.ids.contains(ModelType.FLOW_PROPERTY, propID)) {
        v.warning(id, ModelType.UNIT_GROUP,
          "invalid link to default property @" + propID);
        foundErrors = true;
      }

      return !v.hasStopped();
    });
  }

}
