package org.openlca.validation;

import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ParameterScope;
import org.openlca.expressions.FormulaInterpreter;
import org.openlca.util.Strings;

class FormulaCheck implements Runnable {

  private final Validation v;
  private boolean foundErrors = false;

  FormulaCheck(Validation v) {
    this.v = v;
  }

  @Override
  public void run() {
    try {
      var interpreter = buildInterpreter();
      checkParameterFormulas(interpreter);
      if (!foundErrors && !v.hasStopped()) {
        v.ok("checked formulas");
      }
    } catch (Exception e) {
      v.error("error in formula validation", e);
    } finally {
      v.workerFinished();
    }
  }

  private FormulaInterpreter buildInterpreter() {
    var interpreter = new FormulaInterpreter();
    var sql = "select " +
      /* 1 */ "scope, " +
      /* 2 */ "f_owner, " +
      /* 3 */ "name, " +
      /* 4 */ "is_input_param, " +
      /* 5 */ "value," +
      /* 6 */ "formula from tbl_parameters";
    NativeSql.on(v.db).query(sql, r -> {

      // parse the parameter scope
      var _str = r.getString(1);
      var paramScope = _str == null
        ? ParameterScope.GLOBAL
        : ParameterScope.valueOf(_str);

      // get the interpreter scope
      long owner = r.getLong(2);
      if (paramScope == ParameterScope.GLOBAL) {
        owner = 0L;
      }
      var scope = owner == 0
        ? interpreter.getGlobalScope()
        : interpreter.getOrCreate(owner);

      // bind the parameter value or formula
      var name = r.getString(3);
      boolean isInput = r.getBoolean(4);
      if (isInput) {
        // value
        scope.bind(name, r.getDouble(5));
      } else {
        // formula
        scope.bind(name, r.getString(6));
      }

      return true;
    });
    return interpreter;
  }

  private void checkParameterFormulas(FormulaInterpreter interpreter) {
    if (v.hasStopped())
      return;

    var sql = "select " +
      /* 1 */ "id, " +
      /* 2 */ "name, " +
      /* 3 */ "scope, " +
      /* 4 */ "f_owner, " +
      /* 5 */ "is_input_param, " +
      /* 6 */ "formula from tbl_parameters";

    NativeSql.on(v.db).query(sql, r -> {

      boolean isInput = r.getBoolean(5);
      if (isInput)
        return !v.hasStopped();

      long paramId = r.getLong(1);

      // parse the parameter scope
      var _str = r.getString(3);
      var paramScope = _str == null
        ? ParameterScope.GLOBAL
        : ParameterScope.valueOf(_str);
      var formula = r.getString(6);

      // check formulas of global parameters
      if (paramScope == ParameterScope.GLOBAL) {
        if (Strings.nullOrEmpty(formula)) {
          v.error(paramId, ModelType.PARAMETER, "empty formula");
          foundErrors = true;
          return !v.hasStopped();
        }
        try {
          interpreter.getGlobalScope().eval(formula);
        } catch (Exception e) {
          v.error(paramId, ModelType.PARAMETER,
            "formula error in '" + formula + "': " + e.getMessage());
          foundErrors = true;
        }
        return !v.hasStopped();
      }

      // check formulas of local parameters
      var paramName = r.getString(2);
      var modelId = r.getLong(4);
      var modelType = paramScope == ParameterScope.IMPACT
        ? ModelType.IMPACT_CATEGORY
        : ModelType.PROCESS;

      if (Strings.nullOrEmpty(formula)) {
        v.error(modelId, modelType, "empty formula of parameter '"
          + paramName + "'");
        foundErrors = true;
        return !v.hasStopped();
      }

      try {
        var scope = interpreter.getScopeOrGlobal(modelId);
        scope.eval(formula);
      } catch (Exception e) {
        v.error(modelId, modelType,
          "formula error in parameter '" + paramName + "': " + e.getMessage());
        foundErrors = true;
      }
      return !v.hasStopped();

    });
  }


}
