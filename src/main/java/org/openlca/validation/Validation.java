package org.openlca.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.openlca.core.database.IDatabase;
import org.openlca.validation.Item.Type;

public class Validation implements Runnable {

  final IDatabase db;
  final IdSet ids;

  private final List<Item> items = new ArrayList<>();
  private int maxIssues = -1;
  private boolean skipWarnings = false;

  final BlockingQueue<Item> queue = new ArrayBlockingQueue<>(100);
  private volatile boolean stopped = false;
  final Item FINISH = Item.ok("_finished");

  private Validation(IDatabase db) {
    this.db = db;
    this.ids = IdSet.of(db);
  }

  public static Validation on(IDatabase db) {
    return new Validation(db);
  }

  public Validation maxItems(int c) {
    this.maxIssues = c;
    return this;
  }

  public Validation skipWarnings(boolean b) {
    this.skipWarnings = b;
    return this;
  }

  /**
   * Cancels a running validation. This does not mean that the validation is
   * canceled
   */
  public void cancel() {
    stopped = true;
  }

  public List<Item> getItems() {
    return Collections.unmodifiableList(items);
  }

  boolean hasStopped() {
    return stopped;
  }

  @Override
  public void run() {
    int activeWorkers = 0;

    while (activeWorkers > 0) {
      try {
        var item = queue.take();
        if (item == FINISH) {
          activeWorkers--;
          continue;
        }
        if (stopped
          || (skipWarnings && item.type == Type.WARNING)) {
          continue;
        }
        items.add(item);
        if (maxIssues > 0 && items.size() >= maxIssues) {
          stopped = true;
        }
      } catch (Exception e) {
        throw new RuntimeException(
          "failed to get item from validation queue", e);
      }
    }
  }
}
