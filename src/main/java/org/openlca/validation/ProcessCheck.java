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
      checkProcessDocs();
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

  private void checkProcessDocs() {
    if (v.hasStopped())
      return;
    var sql = "select " +
      /* 1 */ "p.id, " +
      /* 2 */ "doc.f_reviewer, " +
      /* 3 */ "doc.f_data_generator, " +
      /* 4 */ "doc.f_dataset_owner, " +
      /* 5 */ "doc.f_data_documentor, " +
      /* 6 */ "doc.f_publication from tbl_processes p inner join " +
              "tbl_process_docs doc on p.f_process_doc = doc.id";
    var refs = new String[]{
      "reviewer",
      "data generator",
      "data set owner",
      "data documentor",
      "publication",
    };

    NativeSql.on(v.db).query(sql, r -> {
      var id = r.getLong(1);

      for (int i = 0; i < refs.length; i++) {
        var refID = r.getLong(i + 2);
        if (refID == 0)
          continue;
        var type = i == 4
          ? ModelType.SOURCE
          : ModelType.ACTOR;
        if (!v.ids.contains(type, refID)) {
          v.warning(id, ModelType.PROCESS,
            "invalid reference to " + refs[i] + " @" + refID);
          foundErrors = true;
        }
      }
      return !v.hasStopped();
    });


  }

}
