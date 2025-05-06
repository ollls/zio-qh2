import zio.{ZIO, ZLayer}
import zio.ZIOAppDefault
import zio.stream.ZStream
import io.quartz.QuartzH2Server
import io.quartz.http2._
import io.quartz.http2.model.{Method, ContentType, Request, Response}
import io.quartz.http2.model.Method._
import io.quartz.http2.routes.HttpRouteIO
import io.quartz.http2.routes.WebFilter

import ch.qos.logback.classic.Level
import zio.logging.backend.SLF4J

import io.quartz.http2.model.StatusCode
import java.io.File

import java.time.LocalDate
import com.github.plokhotnyuk.jsoniter_scala.macros._
import com.github.plokhotnyuk.jsoniter_scala.core._

case class User(uid: String, givenName: String, sn: String, dob: LocalDate)
given userCodec: JsonValueCodec[User] = JsonCodecMaker.make

object Run extends ZIOAppDefault {

  override val bootstrap =
    zio.Runtime.removeDefaultLoggers ++ SLF4J.slf4j ++ zio.Runtime.enableWorkStealing

  // The WebFilter condition is set to 'true', meaning all requests will pass through. If set to a condition like r.uri.getPath().endsWith(".html"), only matching requests would be allowed.
  val filter: WebFilter[Any] = (r: Request) =>
    ZIO.succeed(
      Either.cond(
        true,
        r,
        Response
          .Error(StatusCode.Forbidden)
          .asText("Access denied to: " + r.uri.getPath())
      )
    )

  val R: HttpRouteIO[Any] = { case GET -> Root / "user" =>
    val user = User("jdoe", "John", "Doe", LocalDate.of(1867, 5, 3))
    val json = writeToString[User](user)
    ZIO.succeed(Response.Ok().asText(json).contentType(ContentType.JSON))
  }

  def run =
    for {
      _ <- zio.Console.printLine(
        "****************************************************************************************"
      )
      _ <- zio.Console.printLine("\u001B[31mquartz-h2 doc: https://ollls.github.io/zio-quartz-h2-doc/\u001B[0m")
      _ <- zio.Console.printLine(
        "****************************************************************************************"
      )

      args <- this.getArgs
      // sbt "run --trace"
      _ <- ZIO.when(args.find(_ == "--debug").isDefined)(ZIO.attempt(QuartzH2Server.setLoggingLevel(Level.DEBUG)))
      _ <- ZIO.when(args.find(_ == "--error").isDefined)(ZIO.attempt(QuartzH2Server.setLoggingLevel(Level.ERROR)))
      _ <- ZIO.when(args.find(_ == "--trace").isDefined)(ZIO.attempt(QuartzH2Server.setLoggingLevel(Level.TRACE)))
      _ <- ZIO.when(args.find(_ == "--off").isDefined)(ZIO.attempt(QuartzH2Server.setLoggingLevel(Level.OFF)))

      ctx <- QuartzH2Server.buildSSLContext("TLS", "keystore.jks", "password")
      // exitCode <- new QuartzH2Server("0.0.0.0", 8080, 16000, null).startIO_linuxOnly(1, R, filter)
      // exitCode <- new QuartzH2Server("0.0.0.0", 8443, 16000, ctx).startIO_linuxOnly(1, R, filter)
      // exitCode <- new QuartzH2Server("0.0.0.0", 8443, 16000, ctx).startIO(R, filter, sync = true)
      // exitCode <- new QuartzH2Server("0.0.0.0", 8443, 16000, ctx).startIO(R, filter, sync = true)
      // exitCode <- new QuartzH2Server("0.0.0.0", 8080, 16000, null).startIO(R, filter, sync = false)
      exitCode <- new QuartzH2Server("0.0.0.0", 8443, 16000, ctx, incomingWinSize = 512000)
        .startIO(R, filter, sync = false)
    } yield (exitCode)
}
