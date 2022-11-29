package io.github.gh.portifi.grpc

import io.grpc.Channel
import io.grpc.StatusRuntimeException
import io.grpc.examples.helloworld.GreeterGrpc
import io.grpc.examples.helloworld.HelloReply
import io.grpc.examples.helloworld.HelloRequest
import org.slf4j.LoggerFactory

class HelloWorldClient(channel: Channel) {

    private val blockingStub = GreeterGrpc.newBlockingStub(channel)
    private val logger = LoggerFactory.getLogger(HelloWorldClient::class.java.name)

    /** Say hello to server.  */
    fun greet(name: String) {
        logger.info("Will try to greet $name ...")
        val request = HelloRequest.newBuilder().setName(name).build()
        val response: HelloReply = try {
            blockingStub!!.sayHello(request)
        } catch (e: StatusRuntimeException) {
            logger.warn("RPC failed: ${e.status}")
            return
        }
        logger.info("Greeting: ${response.message}")
    }
}
