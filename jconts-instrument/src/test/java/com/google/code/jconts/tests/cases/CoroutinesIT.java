package com.google.code.jconts.tests.cases;

import com.google.code.jconts.Async;
import com.google.code.jconts.Computation;
import com.google.code.jconts.Coroutines;
import com.google.code.jconts.IsCoroutine;
import org.junit.Test;

public class CoroutinesIT {
  @Test
  public void test() throws Exception {
    Coroutines.execute(task("task 1"), task("task 2"));
  }

  @Test
  public void test2() throws Exception {
    Coroutines.execute(task2("task 1"), task2("task 2"));
  }

  @IsCoroutine
  public Computation<Void> task(String data) {
    for (int i = 0; i < 100; i++) {
      System.out.println("Processed: " + data + " at step " + i);
    }

    System.out.println("After processing data: " + data);
    return Async.areturn();
  }

  @IsCoroutine
  public Computation<Void> task2(String data) {
    for (int i = 0; i < 100; i++) {
      System.out.println("Processed: " + compute(data) + " at step " + i);
    }

    System.out.println("After processing data: " + data);
    return Async.areturn();
  }

  private String compute(String data) {
    return data + " (computed)";
  }
}
