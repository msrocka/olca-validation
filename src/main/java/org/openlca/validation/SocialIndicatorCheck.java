package org.openlca.validation;

import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;

class SocialIndicatorCheck implements Runnable {

  private final Validation v;
  private boolean foundErrors;

  SocialIndicatorCheck(Validation v) {
    this.v = v;
  }

  @Override
  public void run() {
    try {
      if (v.hasStopped())
        return;
      var sql = "select " +
        /* 1 */ "id, " +
        /* 2 */ "f_activity_quantity, " +
        /* 3 */ "f_activity_unit from tbl_social_indicators";
      NativeSql.on(v.db).query(sql, r -> {
        var id = r.getLong(1);

        var propID = r.getLong(2);
        if (!v.ids.contains(ModelType.FLOW_PROPERTY, propID)) {
          v.error(id, ModelType.SOCIAL_INDICATOR,
            "invalid flow property link @" + propID);
          foundErrors = true;
        }

        var unitID = r.getLong(3);
        if (!v.ids.contains(ModelType.UNIT, unitID)) {
          v.error(id, ModelType.SOCIAL_INDICATOR,
            "invalid unit link @" + unitID);
          foundErrors = true;
        }

        return !v.hasStopped();
      });
      if (!foundErrors && !v.hasStopped()) {
        v.ok("checked social indicators");
      }
    } catch (Exception e) {
      v.error("failed to check social indicators", e);
    } finally {
      v.workerFinished();
    }
  }
}
