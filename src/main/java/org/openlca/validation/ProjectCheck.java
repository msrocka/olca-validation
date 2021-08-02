package org.openlca.validation;

import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;

class ProjectCheck implements Runnable {

  private final Validation v;
  private boolean foundErrors = false;

  ProjectCheck(Validation v) {
    this.v = v;
  }

  @Override
  public void run() {
    try {

    } catch (Exception e) {
      v.error("error in flow validation", e);
    } finally {
      v.workerFinished();
    }
  }

  private void checkProjectRefs() {
    if (v.hasStopped())
      return;
    var sql = "select " +
      /* 1 */ "id, " +
      /* 2 */ "f_impact_method, " +
      /* 3 */ "f_nwset from tbl_projects";
    NativeSql.on(v.db).query(sql, r -> {
      var projectId = r.getLong(1);

      var methodId = r.getLong(2);
      if (!v.ids.containsOrZero(ModelType.IMPACT_METHOD, methodId)) {
        v.error(projectId, ModelType.PROJECT,
          "invalid reference to impact method @" + methodId);
        foundErrors = true;
      }

      var nwSetId = r.getLong(3);
      if (!v.ids.containsOrZero(ModelType.NW_SET, nwSetId)) {
        v.error(projectId, ModelType.PROJECT,
          "invalid reference to nw-set @" + nwSetId);
        foundErrors = true;
      }
      return !v.hasStopped();
    });
  }
}
