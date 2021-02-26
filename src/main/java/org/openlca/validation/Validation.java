package org.openlca.validation;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.database.IDatabase;

public class Validation {

  final IDatabase db;
  final List<Issue> errors = new ArrayList<>();
  final List<Issue> warnings = new ArrayList<>();
  boolean withWarnings = false;
  int maxIssues = -1;


  private Validation(IDatabase db) {
    this.db = db;
  }

  public static Validation on(IDatabase db) {
    return new Validation(db);
  }

  public Validation withWarnings(boolean b) {
    this.withWarnings = b;
    return this;
  }

  public Validation withMaxIssues(int c) {
    this.maxIssues = c;
    return this;
  }

  public List<Issue> validate() {
    // TODO

    if (!withWarnings)
      return errors;
    var issues = new ArrayList<Issue>(
      errors.size() + warnings.size());
    issues.addAll(errors);
    issues.addAll(warnings);
    return issues;
  }


}
