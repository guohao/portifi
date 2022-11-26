package org.gh.portifi

import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer

class InboundHandlerInitializer(private val specs: List<ProxySpec>) : ChannelInitializer<Channel>() {
    override fun initChannel(ch: Channel) {
        ch.pipeline()
            .addLast(FrontHandler(specs[0].port()))
            .remove(this)
    }
}
