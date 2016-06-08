package com.google.code.jconts.tests.cases;

import com.google.code.jconts.Async;
import com.google.code.jconts.Computation;
import com.google.code.jconts.IsAsync;
import com.google.code.jconts.tests.Coroutines;
import org.junit.Test;

public class CoroutinesIT {
  @Test
  public void test() throws Exception {
    Coroutines.execute(task("task 1"), task("task 2"));
  }

  @IsAsync
  public Computation<Void> task(String data) {
    for (int i = 0; i < 100; i++) {
      Async.await(Coroutines.yield());
      System.out.println("Processed: " + data + " at step " + i);
    }

    System.out.println("After processing data: " + data);
    return Async.areturn();
  }
}
