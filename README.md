This project aims to provide limited continuations support for the Java language similar to the C# async/await.

The idea is to allow writing asynchronous code in synchronous manner, using all the available imperative constructs
like loops, conditional blocks, try...catch...finally.

Example:

    public void runTwoOperations() throws Exception {
        waitCompleted(multiAwait(executeOp("1: "), executeOp("2: ")));
    }

    @IsAsync
    public Computation<Void> executeOp(String data) {
        for (int i = 0; i < 3; i++) {
          data = await(longOperation(data));
          System.out.println("Processed: " + data);
        }
        System.out.println("After processing data: " + data);
        return areturn();
    }

Output:

    Processed: 2:  processed!
    Processed: 1:  processed! processed!
    Processed: 2:  processed! processed!
    Processed: 1:  processed! processed! processed!
    After processing data: 1:  processed! processed! processed!
    Processed: 2:  processed! processed! processed!
    After processing data: 2:  processed! processed! processed!
    As you can see, two operations are executed simultaneously. No thread is blocked while the "long operation" takes place.
