import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.kafka.{ConnectionCheckerSettings, ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.scaladsl.Sink
import akka.stream.{ActorMaterializer, Materializer}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.FiniteDuration
import scala.util.{Failure, Success}

object StreamsApp extends App {
  override def main(args: Array[String]): Unit = {
    println("Hello from Consumer")

    implicit val system: ActorSystem = ActorSystem("consumer-sample")
    implicit val materializer: Materializer = ActorMaterializer()

    //Time out helps in come out of indefinite retries
    val connectionCheckerSettings =
      ConnectionCheckerSettings(3, FiniteDuration(2, TimeUnit.SECONDS), 1)

    val consumerSettings =
      ConsumerSettings(system, new StringDeserializer, new StringDeserializer).withConnectionChecker(connectionCheckerSettings)

//    val consumerSettings = ConsumerSettings(system, new ByteArrayDeserializer, new StringDeserializer)
//      .withBootstrapServers("localhost:9092")
//      .withGroupId("group1")
//      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    val done = Consumer
      .plainSource(consumerSettings, Subscriptions.topics("suren-events"))
      .runWith(Sink.foreach(println)) // just print each message for debugging

    implicit val ec: ExecutionContextExecutor = system.dispatcher
    done onComplete  {
      case Success(_) => println("Done"); system.terminate()
      case Failure(err) => println(err.toString); system.terminate()
    }
  }
}
