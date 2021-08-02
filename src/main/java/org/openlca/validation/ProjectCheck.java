package org.openlca.validation;

class ProjectCheck implements Runnable {

  private final Validation v;
  private boolean foundErrors = false;

  ProjectCheck(Validation v) {
    this.v = v;
  }

  @Override
  public void run() {
    if (v.hasStopped())
      return;

  }
}
