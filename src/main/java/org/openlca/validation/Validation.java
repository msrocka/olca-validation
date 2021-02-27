package org.openlca.validation;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.database.IDatabase;

public class Validation {

  final IDatabase db;
  final IdSet ids;

  private final List<Issue> collected = new ArrayList<>();
  private int maxIssues = -1;


  private Validation(IDatabase db) {
    this.db = db;
    this.ids = IdSet.of(db);
  }

  public static Validation on(IDatabase db) {
    return new Validation(db);
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

  private void push(Issue issue) {

  }

}
