package com.twitter.finagle.kestrel.protocol

import com.twitter.finagle.{Codec, ClientCodec, ServerCodec}
import org.jboss.netty.channel._
import org.jboss.netty.buffer.ChannelBuffer
import com.twitter.finagle.memcached.util.ChannelBufferUtils._
import com.twitter.finagle.memcached.protocol.text.{Encoder, server, client}

class Kestrel extends Codec[Command, Response] {
  private[this] val storageCommands = collection.Set[ChannelBuffer]("set")

  override def serverCodec = new ServerCodec[Command, Response] {
    def pipelineFactory = new ChannelPipelineFactory {
      def getPipeline() = {
        val pipeline = Channels.pipeline()

//        pipeline.addLast("exceptionHandler", new ExceptionHandler)

        pipeline.addLast("decoder", new server.Decoder(storageCommands))
        pipeline.addLast("decoding2command", new DecodingToCommand)

        pipeline.addLast("encoder", new Encoder)
        pipeline.addLast("response2encoding", new ResponseToEncoding)
        pipeline
      }
    }
  }

  override def clientCodec = new ClientCodec[Command, Response] {
    def pipelineFactory = new ChannelPipelineFactory {
      def getPipeline() = {
        val pipeline = Channels.pipeline()

        pipeline.addLast("decoder", new client.Decoder)
        pipeline.addLast("decoding2response", new DecodingToResponse)

        pipeline.addLast("encoder", new Encoder)
        pipeline.addLast("command2encoding", new CommandToEncoding)
        pipeline
      }
    }
  }
}
