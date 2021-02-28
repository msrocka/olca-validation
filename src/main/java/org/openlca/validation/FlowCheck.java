package org.openlca.validation;

import java.util.concurrent.atomic.AtomicBoolean;

import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;

class FlowCheck implements Runnable {

  private final Validation v;

  FlowCheck(Validation v) {
    this.v = v;
  }

  @Override
  public void run() {
    try {
      checkReferences();
      checkPropertyFactors();
    } catch (Exception e) {
      v.error("error in flow validation", e);
    } finally {
      v.workerFinished();
    }
  }

  private void checkReferences() {
    if (v.hasStopped())
      return;
    var noErrors = new AtomicBoolean(true);
    var sql = "select " +
      /* 1 */ "id, " +
      /* 2 */ "f_reference_flow_property, " +
      /* 3 */ "f_location from tbl_flows";
    NativeSql.on(v.db).query(sql, r -> {
      long id = r.getLong(1);

      long propID = r.getLong(2);
      if (!v.ids.contains(ModelType.FLOW_PROPERTY, propID)) {
        v.error(id, ModelType.FLOW,
          "invalid flow property reference @" + propID);
        noErrors.set(false);
      }

      long locID = r.getLong(3);
      if (locID != 0 && !v.ids.contains(ModelType.LOCATION, locID)) {
        v.error(id, ModelType.FLOW, "invalid location reference @" + locID);
        noErrors.set(false);
      }

      return !v.hasStopped();
    });
    if (noErrors.get()) {
      v.ok("no errors in flow references");
    }
  }

  private void checkPropertyFactors() {
    if (v.hasStopped())
      return;
    var noErrors = new AtomicBoolean(true);
    var sql = "select " +
      /* 1 */ "f_flow, " +
      /* 2 */ "f_flow_property, " +
      /* 3 */ "conversion_factor from tbl_flow_property_factors";
    NativeSql.on(v.db).query(sql, r -> {

      long flowID = r.getLong(1);
      if (!v.ids.contains(ModelType.FLOW, flowID)) {
        v.warning(
          "invalid flow reference @" + flowID + " in flow property factor");
        noErrors.set(false);
      }

      long propID = r.getLong(2);
      if (!v.ids.contains(ModelType.FLOW_PROPERTY, propID)) {
        v.error(flowID, ModelType.FLOW, "invalid flow property @" + propID);
        noErrors.set(false);
      }

      double factor = r.getDouble(3);
      if (Double.compare(factor, 0) == 0) {
        v.error(flowID, ModelType.FLOW,
          "invalid flow property factor of 0 for property @" + propID);
        noErrors.set(false);
      }
      return !v.hasStopped();
    });

    if (noErrors.get()) {
      v.ok("no errors in flow property factors");
    }
  }
}
