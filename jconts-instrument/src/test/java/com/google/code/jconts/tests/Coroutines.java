package com.google.code.jconts.tests;

import com.google.code.jconts.Async;
import com.google.code.jconts.Computation;
import com.google.code.jconts.Continuation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Coroutines {

  private static final List<Continuation<?>> jobs = new ArrayList<Continuation<?>>();

  public static void execute(Computation<Void>... tasks) {
    Computation<List<Object>> multiTasks = Async.multiAwait(tasks);
    multiTasks.execute(new EmptyContinuation<List<Object>>());
    Random scheduler = new Random();
    while (!jobs.isEmpty()) {
      Continuation<?> job = jobs.remove(scheduler.nextInt(jobs.size()));
      job.invoke(null);
    }
  }

  public static Computation<Void> yield() {
    return new Computation<Void>() {
      @Override
      public void execute(Continuation<? super Void> cont) {
        jobs.add(cont);
      }
    };
  }
}
