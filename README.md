# picoServe

* A very simple HTTP server library,
* written in pure Java with zero dependencies,
* based on `com.sun.net.httpserver.HttpServer`,
* ready for Project Loom, by specifying an appropriate executor

This library provides 
* a convenient builder and conveniences for
* request parameters
* response headers
* string and byte array responses

## Quick start

```java
    Server.builder()
      .port(9000)
      .backlog(5)
      .GET("/string", params -> {
        return new StringResponse(200, "hello " + params);
      })
      .build()
      .start();
```

## Example

See the file `Example.java` for different variations of usage.
