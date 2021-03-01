package org.openlca.validation;

import gnu.trove.set.hash.TLongHashSet;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;

class ProcessCheck implements Runnable {

  private final Validation v;
  private boolean foundErrors = false;

  ProcessCheck(Validation v) {
    this.v = v;
  }

  @Override
  public void run() {
    try {
      checkProcessRefs();
      if (!foundErrors) {
        v.ok("checked processes");
      }
    } catch (Exception e) {
      v.error("error in process validation", e);
    } finally {
      v.workerFinished();
    }
  }

  private TLongHashSet checkProcessRefs() {
    if (v.hasStopped())
      return new TLongHashSet(0);
    var sql = "select " +
      /* 1 */ "id, " +
      /* 2 */ "f_quantitative_reference, " +
      /* 3 */ "f_location, " +
      /* 4 */ "f_dq_system, " +
      /* 5 */ "f_exchange_dq_system, " +
      /* 6 */ "f_social_dq_system from tbl_processes";
    var qRefs = new TLongHashSet();
    NativeSql.on(v.db).query(sql, r -> {
      long id = r.getLong(1);

      var qref = r.getLong(2);
      if (qref == 0) {
        v.warning(id, ModelType.PROCESS, "no quantitative reference");
        foundErrors = true;
      } else {
        qRefs.add(qref);
      }

      var locID = r.getLong(3);
      if (locID != 0 && !v.ids.contains(ModelType.LOCATION, locID)) {
        v.error(id, ModelType.PROCESS, "invalid location @" + locID);
        foundErrors = true;
      }

      for (long i = 4; i < 7; i++) {
        var dqID = r.getLong(4);
        if (dqID != 0 && !v.ids.contains(ModelType.DQ_SYSTEM, dqID)) {
          v.error(id, ModelType.PROCESS, "invalid DQ system @" + dqID);
          foundErrors = true;
        }
      }

      return !v.hasStopped();
    });
    return qRefs;
  }

}
