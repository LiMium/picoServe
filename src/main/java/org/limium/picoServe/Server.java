package org.limium.picoserve;

import java.net.InetSocketAddress;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

public final class Server {
  private final HttpServer server;

  static interface Processor {
    public String process();
  }

  static class Handler {
    public final String path;
    public final Processor processor;
    public Handler(final String path, final Processor processor) {
      this.path = path;
      this.processor = processor;
    }
  }

  public Server(InetSocketAddress addr, int backlog, List<Handler> handlers) throws IOException {
    this.server = HttpServer.create(addr, backlog);
    for (final var handler: handlers) {
      System.out.println("Registering handler");
      this.server.createContext(handler.path, new HttpHandler() {
        public void handle(final HttpExchange exchange) {
          try(final var os = exchange.getResponseBody()) {
            final var response =  handler.processor.process();
            final var bytes = response.getBytes();
            exchange.sendResponseHeaders(200, bytes.length);
            os.write(bytes);
            os.close();
          } catch (IOException ioe) {
            System.out.println("Error: " + ioe);
          }
        }
      });
    }
  }

  public void start() {
    this.server.start();
  }

  public void stop(int delay) {
    this.server.stop(delay);
  }

  public static ServerBuilder builder() {
    return new ServerBuilder();
  }

  public static void main(String[] args) throws java.io.IOException {
    System.out.println("Starting");
    /*var server = new Server(new InetSocketAddress(9000), 5, List.of(
      new Handler("/", () -> { return "hello"; })
    ));*/
    var server = Server.builder()
      .port(9000)
      .backlog(5)
      .handle(new Handler("/", () -> { return "hello"; }))
      .build();
    server.start();
  }

  public static class ServerBuilder {
    private InetSocketAddress mAddress = new InetSocketAddress(9000);
    private int backlog = 5;
    private List<Handler> handlers = new LinkedList<Handler>();

    public ServerBuilder port(final int port) {
      mAddress = new InetSocketAddress(port);
      return this;
    }
    public ServerBuilder backlog(final int backlog) {
      this.backlog = backlog;
      return this;
    }
    public ServerBuilder address(final InetSocketAddress addr) {
      mAddress = addr;
      return this;
    }
    public ServerBuilder handle(final Handler handler) {
      handlers.add(handler);
      return this;
    }
    public Server build() throws IOException {
      return new Server(mAddress, backlog, handlers);
    }
  }
}
