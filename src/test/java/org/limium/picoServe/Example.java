// Distributed under Apache 2 license
// Copyright 2021 github.com/hrj

package org.limium.picoserve;

import static org.limium.picoserve.Server.*;
import java.util.Map;
import java.util.List;

public final class Example {
  public static void main(String[] args) throws java.io.IOException {
    System.out.println("Starting");
    final var executor = new java.util.concurrent.ForkJoinPool();

    // Can be used in Project Loom
    // final var executor = java.util.concurrent.Executors.newVirtualThreadExecutor();

    final var server = Server.builder()
      .port(9000)
      .backlog(5)
      .executor(executor)
      .handle(new Handler("/string", (params) -> {
        return new StringResponse(200, "hello " + params, Map.of("Content-type", List.of("text/plain")));
      }))
      .handle(new Handler("/stringWithDelay", (params) -> {
        try { Thread.sleep(10); } catch (java.lang.InterruptedException e) { System.out.println("Interrupted");}
        return new StringResponse(200, "hello", Map.of("Content-type", List.of("text/plain")));
      }))
      .handle(new Handler("/bytes", (params) -> { return new ByteResponse(200, new byte[] {0x11, 0x22, 0x33}); }))
      .build();
    server.start();
  }
}
