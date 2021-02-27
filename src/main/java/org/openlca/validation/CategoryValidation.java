package org.openlca.validation;

import java.util.concurrent.atomic.AtomicBoolean;

import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;

class CategoryValidation implements Runnable {

  private final Validation v;

  CategoryValidation(Validation v) {
    this.v = v;
  }

  @Override
  public void run() {
    try {
      checkParents();
    } catch (Exception e) {
      v.error("failed to validate categories", e);
    } finally {
      v.workerFinished();
    }
  }

  private void checkParents() {
    if (v.hasStopped())
      return;
    var noErrors = new AtomicBoolean(true);
    var sql = "select id, f_category from tbl_categories";
    NativeSql.on(v.db).query(sql, r -> {
      long id = r.getLong(1);
      long parent = r.getLong(2);
      if (parent == 0 || v.ids.contains(ModelType.CATEGORY, parent)) {
        return true;
      }
      v.error(id, ModelType.CATEGORY, "parent category does not exist");
      noErrors.set(false);
      return !v.hasStopped();
    });
    if (noErrors.get()) {
      v.ok("parent categories are valid");
    }
  }

}
