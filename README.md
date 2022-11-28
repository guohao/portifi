# portifi

A reverse proxy supports binding multiple protocols at a same port

## Features

- Export service at **SAME** port with multiple protocols:HTTP1.1/ HTTP/2(gRPC)/ redis
- Use as a SDK or deploy as a standalone proxy

## Usage Scenarios

- **Protocol Upgrade:** Migrate protocols effortlessly with minor code changes. HTTP/1.1 -> gRPC etc.
- **Unary Gateway:** Act as an all-in-one gateway to export multiple protocols
- **Negotiator Component:** Interact with heterogeneous systems

## Quick Start

You can take a look [examples](examples) and run a `portifi` server quickly.

Or read this quick start.

### Prerequisite

- JDK: 8 or higher

Add `Portifi` to your project with `maven` or `gradle`.

### Gradle(KTS)
```kotlin
dependencies {
    implementation("io.github.gh:portifi:x.y.z")
}
```

Start a `Portifi` server at port `9999` with `java` or `kotlin`.

### Java
```java
public class Main {
    public static void main(String[] args) {
        ProxySpec spec = new ProxySpecBuilder(8080, Protocol.HTTP1_1, "localhost", false).build();
        Portifi server = PortifiKt.asServer(spec);
        server.start(9999);
    }
}
```

### Kotlin
```kotlin
fun main() {
    ProxySpecBuilder(8080)
        .protocol(Protocol.HTTP1_1)
        .build()
        .asServer()
        .start()
}
```

Start a simple HTTP backend server at 8080, here is the simplest way with python.
### Python2
```shell
$ python -m SimpleHTTPServer 8080
```

### Python3
```shell
$ python -m http. server 8080
```

Run a test
```shell
$curl localhost:9999
```
