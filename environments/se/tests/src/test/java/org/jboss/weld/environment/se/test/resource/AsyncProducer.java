package org.jboss.weld.environment.se.test.resource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

public class AsyncProducer {
  @ApplicationScoped
  @Produces
  public CompletionStage<AsyncValue> getAsyncValue(){
    CompletableFuture<AsyncValue> future = new CompletableFuture<AsyncValue>();
    // Either produce it here with a delay, or in EEResourceInjectionIgnoredTest

//    ExecutorService executor = Executors.newSingleThreadExecutor();
//    executor.submit(
//          new Runnable() {
//             public void run() {
//               try {
//                Thread.sleep(3000);
//              } catch (InterruptedException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//              }
//                future.complete(new AsyncValueImpl(null));
//             }
//          });

    return future;
  }

}
