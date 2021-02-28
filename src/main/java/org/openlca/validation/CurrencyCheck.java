package org.openlca.validation;

import java.util.concurrent.atomic.AtomicBoolean;

import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;

class CurrencyCheck implements Runnable {

  private final Validation v;

  CurrencyCheck(Validation v) {
    this.v = v;
  }

  @Override
  public void run() {
    if (v.hasStopped())
      return;
    try {
      var noErrors = new AtomicBoolean(true);
      var sql = "select " +
        /* 1 */ "id, " +
        /* 2 */ "f_reference_currency, " +
        /* 3 */ "conversion_factor from tbl_currencies";
      NativeSql.on(v.db).query(sql, r -> {
        long id = r.getLong(1);

        long refID = r.getLong(2);
        if (!v.ids.contains(ModelType.CURRENCY, refID)) {
          v.error(id, ModelType.CURRENCY,
            "invalid link to reference currency @" + refID);
          noErrors.set(false);
        }

        double factor = r.getDouble(3);
        if (Double.compare(factor, 0) == 0) {
          v.error(id, ModelType.CURRENCY,
            "invalid conversion factor of 0 to ref. currency @" + refID);
           noErrors.set(false);
        }

        return !v.hasStopped();
      });
      if (noErrors.get()) {
        v.ok("no errors in currency factors");
      }

    } catch (Exception e) {
      v.error("error in currency validation", e);
    } finally {
      v.workerFinished();
    }
  }
}
