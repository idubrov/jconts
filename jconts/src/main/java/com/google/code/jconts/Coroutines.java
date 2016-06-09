package com.google.code.jconts;

import com.google.code.jconts.util.EmptyContinuation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Coroutines {

  private static final List<Runnable> jobs = new ArrayList<Runnable>();

  public static void execute(Computation<Void>... tasks) {
    Computation<List<Object>> multiTasks = Async.multiAwait(tasks);
    multiTasks.execute(new EmptyContinuation<List<Object>>());
    Random scheduler = new Random();
    while (!jobs.isEmpty()) {
      Runnable job = jobs.remove(scheduler.nextInt(jobs.size()));
      job.run();
    }
  }

  public static Computation<Void> yield() {
    return new Computation<Void>() {
      @Override
      public void execute(final Continuation<? super Void> cont) {
        jobs.add(new Runnable() {
          @Override
          public void run() {
            cont.invoke(null);
          }
        });
      }
    };
  }

  public static <T> Computation<T> yield(final T result) {
    return new Computation<T>() {
      @Override
      public void execute(final Continuation<? super T> cont) {
        jobs.add(new Runnable() {
          @Override
          public void run() {
            cont.invoke(result);
          }
        });
      }
    };
  }
}
