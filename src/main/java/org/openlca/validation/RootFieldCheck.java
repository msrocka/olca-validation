package org.openlca.validation;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.persistence.Table;

import org.openlca.core.database.NativeSql;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.ModelType;
import org.openlca.util.Strings;

class RootFieldCheck implements Runnable {

  private final Validation v;
  private Set<String> _libs;

  RootFieldCheck(Validation v) {
    this.v = v;
  }

  @Override
  public void run() {
    try {
      for (var type : ModelType.values()) {
        var clazz = type.getModelClass();
        if (clazz == null)
          continue;
        if (CategorizedEntity.class.isAssignableFrom(clazz)) {
          check(type);
        }
      }
    } catch (Exception e) {
      v.error("failed to check basic fields", e);
    } finally {
      v.workerFinished();
    }
  }

  private void check(ModelType type) {
    if (v.hasStopped())
      return;
    var table = type.getModelClass().getAnnotation(Table.class);
    if (table == null)
      return;

    var noErrors = new AtomicBoolean(true);
    var sql = "select " +
      /* 1 */ "id, " +
      /* 2 */ "ref_id, " +
      /* 3 */ "name, " +
      /* 4 */ "f_category, " +
      /* 5 */ "library from " + table.name();
    NativeSql.on(v.db).query(sql, r -> {
      long id = r.getLong(1);

      var refID = r.getString(2);
      if (Strings.nullOrEmpty(refID)) {
        v.error(id, type, "has no reference ID");
        noErrors.set(false);
      }

      var name = r.getString(3);
      if (Strings.nullOrEmpty(name)) {
        v.warning(id, type, "has an empty name");
        noErrors.set(false);
      }

      var category = r.getLong(4);
      if (category != 0
          && !v.ids.contains(ModelType.CATEGORY, category)) {
        v.error(id, type, "invalid category link @" + category);
        noErrors.set(false);
      }

      var library = r.getString(5);
      if (Strings.notEmpty(library)) {
        if (!libraries().contains(library)) {
          v.error(id, type, "points to unlinked library @" + library);
          noErrors.set(false);
        }
      }
      return !v.hasStopped();
    });

    if (noErrors.get()) {
      v.ok("no errors in basic fields of type "
           + type.getModelClass().getSimpleName()
           + "; table " + table.name());
    }
  }

  private Set<String> libraries() {
    if (_libs != null)
      return _libs;
    _libs = v.db.getLibraries();
    return _libs;
  }

}
