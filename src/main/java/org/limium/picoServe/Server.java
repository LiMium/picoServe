package org.limium.picoserve;

import java.net.InetSocketAddress;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.LinkedList;
import java.util.concurrent.Executor;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

public final class Server {
  private final HttpServer server;

  static interface Response {
    public int getCode();
    public byte[] getBytes();
    public Map<String, List<String>> getResponseHeaders();
  }

  static class ByteResponse implements Response {
    private final int code;
    private final byte[] bytes;
    private final Map<String, List<String>> responseHeaders;

    public ByteResponse(final int code, final byte[] bytes) {
      this.code = code;
      this.bytes = bytes;
      this.responseHeaders = null;
    }

    public ByteResponse(final int code, final byte[] bytes, final Map<String, List<String>> responseHeaders) {
      this.code = code;
      this.bytes = bytes;
      this.responseHeaders = responseHeaders;
    }

    public int getCode() { return this.code; }
    public byte[] getBytes() { return this.bytes; }
    public Map<String, List<String>> getResponseHeaders() {
      return this.responseHeaders;
    }
  }

  static class StringResponse extends ByteResponse {
    public StringResponse(final int code, final String msg) {
      super(code, msg.getBytes());
    }

    public StringResponse(final int code, final String msg, final Map<String, List<String>> responseHeaders) {
      super(code, msg.getBytes(), responseHeaders);
    }
  }

  static interface Processor {
    public Response process();
  }

  static class Handler {
    public final String path;
    public final Processor processor;
    public Handler(final String path, final Processor processor) {
      this.path = path;
      this.processor = processor;
    }
  }

  public Server(final InetSocketAddress addr, final int backlog, final List<Handler> handlers, final Executor executor) throws IOException {
    this.server = HttpServer.create(addr, backlog);
    this.server.setExecutor(executor);
    for (final var handler: handlers) {
      System.out.println("Registering handler for " + handler.path);
      this.server.createContext(handler.path, new HttpHandler() {
        public void handle(final HttpExchange exchange) {
          try(final var os = exchange.getResponseBody()) {
            final var response =  handler.processor.process();
            final var headersToSend = response.getResponseHeaders();
            if (headersToSend != null) {
              final var responseHeaders = exchange.getResponseHeaders();
              responseHeaders.putAll(headersToSend);
            }
            final var bytes = response.getBytes();
            final var code = response.getCode();
            exchange.sendResponseHeaders(code, bytes.length);
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
    var server = Server.builder()
      .port(9000)
      .backlog(5)
      .handle(new Handler("/string", () -> {
        return new StringResponse(200, "hello", Map.of("Content-type", List.of("text/plain")));
      }))
      .handle(new Handler("/bytes", () -> { return new ByteResponse(200, new byte[] {0x11, 0x22, 0x33}); }))
      .build();
    server.start();
  }

  public static class ServerBuilder {
    private InetSocketAddress mAddress = new InetSocketAddress(9000);
    private int backlog = 5;
    private List<Handler> handlers = new LinkedList<Handler>();
    private Executor executor = null;

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
    public ServerBuilder executor(final Executor executor) {
      this.executor = executor;
      return this;
    }
    public Server build() throws IOException {
      return new Server(mAddress, backlog, handlers, executor);
    }
  }
}
