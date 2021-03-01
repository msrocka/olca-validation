package org.openlca.validation;

import static org.junit.Assert.*;

import gnu.trove.set.hash.TLongHashSet;
import org.junit.Test;

public class EmptyLongSetTest {

  @Test
  public void testEmptySet() {
    var empty = new TLongHashSet(0);
    assertTrue(empty.isEmpty());
    for (long i = 0; i < 100; i++) {
      assertFalse(empty.contains(i));
    }
  }

}
