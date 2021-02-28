package org.openlca.validation;

import java.util.concurrent.atomic.AtomicBoolean;

import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;

class FlowPropertyCheck implements Runnable {

  private final Validation v;

  FlowPropertyCheck(Validation v) {
    this.v = v;
  }

  @Override
  public void run() {
    try {
      checkReferences();
    } catch (Exception e) {
      v.error("error in flow property validation", e);
    } finally {
      v.workerFinished();
    }
  }

  private void checkReferences() {
    if (v.hasStopped())
      return;
    var noErrors = new AtomicBoolean(true);
    var sql = "select id, f_unit_group from tbl_flow_properties";
    NativeSql.on(v.db).query(sql, r -> {
      long id = r.getLong(1);
      long groupID = r.getLong(2);
      if (!v.ids.contains(ModelType.UNIT_GROUP, groupID)) {
        v.error(id, ModelType.FLOW_PROPERTY,
          "invalid link to unit group @" + groupID);
        noErrors.set(false);
      }
      return !v.hasStopped();
    });
    if (noErrors.get()) {
      v.ok("no errors in flow property references");
    }
  }
}
