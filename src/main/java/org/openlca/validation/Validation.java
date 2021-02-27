package org.openlca.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;

import org.openlca.core.database.Daos;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.validation.Item.Type;

public class Validation implements Runnable {

  final IDatabase db;
  final IdSet ids;

  private final List<Item> items = new ArrayList<>();
  private int maxIssues = -1;
  private boolean skipWarnings = false;

  private final BlockingQueue<Item> queue = new ArrayBlockingQueue<>(100);
  private volatile boolean stopped = false;
  private final Item FINISH = Item.ok("_finished");

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
   * Cancels a running validation. It may takes a bit until the separate
   * validation workers stop after the cancel signal was sent.
   */
  public void cancel() {
    stopped = true;
  }

  public List<Item> getItems() {
    return Collections.unmodifiableList(items);
  }

  @Override
  public void run() {
    var workers = new Runnable[] { new CategoryCheck(this), new UnitCheck(this), };
    int activeWorkers = 0;
    var threads = Executors.newFixedThreadPool(8);
    for (var worker : workers) {
      activeWorkers++;
      threads.execute(worker);
    }

    while (activeWorkers > 0) {
      try {
        var item = queue.take();
        if (item == FINISH) {
          activeWorkers--;
          continue;
        }
        if (stopped || (skipWarnings && item.type == Type.WARNING)) {
          continue;
        }
        items.add(item);
        if (maxIssues > 0 && items.size() >= maxIssues) {
          stopped = true;
        }
      } catch (Exception e) {
        throw new RuntimeException("failed to get item from validation queue", e);
      }
    }
    threads.shutdown();
  }

  boolean hasStopped() {
    return stopped;
  }

  void workerFinished() {
    queue.add(FINISH);
  }

  void ok(String message) {
    queue.add(Item.ok(message));
  }

  void error(String message, Throwable e) {
    queue.add(Item.error(message, e));
  }

  void error(long id, ModelType type, String message) {
    try {
      var dao = Daos.root(db, type);
      var d = dao.getDescriptor(id);
      queue.add(Item.error(d, message));
    } catch (Exception e) {
      error("failed to get descriptor " + type + "@" + id, e);
    }
  }

  void warning(String message) {
    queue.add(Item.warning(message));
  }

}
