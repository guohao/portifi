/*
 * Copyright 2020 gRPC authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import io.grpc.Grpc
import io.grpc.InsecureServerCredentials
import io.grpc.Server
import io.grpc.examples.helloworld.GreeterGrpc
import io.grpc.examples.helloworld.HelloReply
import io.grpc.examples.helloworld.HelloRequest
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger(HelloWorldServer::class.java.name)

class HelloWorldServer(private val port: Int) {
    val server: Server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
        .addService(HelloWorldService())
        .build()

    fun start() {
        server.start()
        log.info("Server started, listening on $port")
        Runtime.getRuntime().addShutdownHook(
            Thread {
                println("*** shutting down gRPC server since JVM is shutting down")
                this@HelloWorldServer.stop()
                println("*** server shut down")
            },
        )
    }

    private fun stop() {
        server.shutdown()
    }

    internal class HelloWorldService : GreeterGrpc.GreeterImplBase() {
        override fun sayHello(request: HelloRequest, responseObserver: StreamObserver<HelloReply>) {
            val reply = HelloReply.newBuilder()
                .setMessage("Hello ${request.name}")
                .build()
            responseObserver.onNext(reply)
            responseObserver.onCompleted()
        }
    }
}
