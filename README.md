# picoServe

* A very simple HTTP server library,
* written in pure Java with zero dependencies,
* based on `com.sun.net.httpserver.HttpServer`,
* ready for Project Loom, by specifying an appropriate executor (see `Example.java`)
* a convenient builder with conveniences for
  * request parameters
  * response headers
  * string and byte array responses

## Quick start

```java
    Server.builder()
      .port(9000)
      .backlog(5)
      .GET("/string", request -> {
        return new StringResponse(200, "hello " + request.getQueryParams());
      })
      .build()
      .start();
```

## Example

See the file `Example.java` for different variations of usage.
