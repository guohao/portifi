# portifi

[![Java CI with Gradle](https://github.com/guohao/portifi/actions/workflows/gradle.yml/badge.svg)](https://github.com/guohao/portifi/actions/workflows/gradle.yml)
[![License](https://img.shields.io/github/license/guohao/portifi)](https://opensource.org/licenses/Apache-2.0)

A reverse proxy supports binding multiple protocols at a same port

## Features

- Export service at **SAME** port with multiple protocols:HTTP1.1/ HTTP/2(gRPC)/ redis
- Use as a SDK or deploy as a standalone proxy

## Usage Scenarios

- **Protocol Upgrade:** Migrate protocols effortlessly with minor code changes. HTTP/1.1 -> gRPC etc.
- **Unary Gateway:** Act as an all-in-one gateway to hide a complex system topology
- **Negotiator Component:** Interact with heterogeneous systems

## Quick Start

You can take a look at [examples](examples) and run a `portifi` server quickly.

Or read this quick start.

### Prerequisite

- JDK: 8 or higher

Add `Portifi` to your project's `build.gradle.kts`.

### Gradle(KTS)
```kotlin
dependencies {
    implementation("io.github.gh:portifi:x.y.z")
}
```

Start `Portifi` server at port `9999`.

```kotlin
fun main() {
    ProxySpecBuilder(8080)
        .protocol(Protocol.HTTP1_1)
        .build()
        .asServer()
        .start()
}
```

Start a backend HTTP server at 8080, here is the simplest way with python3.
### Python3
```shell
python3 -m http.server 8080
```

Run a test
```shell
curl localhost:9999
```
