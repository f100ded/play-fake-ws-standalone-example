package com.f100ded.play.example

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.f100ded.play.example.model.Bar
import org.f100ded.play.fakews._
import org.scalatest._
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables._

import scala.concurrent._
import scala.concurrent.duration.Duration
import scala.language.reflectiveCalls

/**
  * Tests FooApi HTTP client implementation
  */
class WsFooApiClientTest extends AsyncFunSuite with BeforeAndAfterAll with Matchers {

  implicit val system: ActorSystem = ActorSystem()

  implicit val mat: ActorMaterializer = ActorMaterializer()

  import system.dispatcher

  private val baseUrl = "http://host/"

  private val bar = Bar(1, "bar", 0)

  private val accessToken = "fake_access_token"

  test("getBar: regular case") {
    val ws = StandaloneFakeWSClient {
      case request@GET(url"http://host/bars/$id") =>
        id shouldBe "1"
        request.headers should contain("Authorization" -> Seq(s"Bearer $accessToken"))
        Ok(Json.toJson(bar))
    }

    val api = new WsFooApiClient(ws, baseUrl, accessToken)
    api.getBar(1).map { result =>
      result shouldBe Some(bar)
    }
  }

  test("getBar: 404") {
    val ws = StandaloneFakeWSClient {
      case GET(url"""http://host/bars/[\d]+""") =>
        NotFound
    }

    val api = new WsFooApiClient(ws, baseUrl, accessToken)
    api.getBar(1).map { result =>
      result shouldBe None
    }
  }

  test("getBar: unexpected code") {
    val ws = StandaloneFakeWSClient(InternalServerError)
    val api = new WsFooApiClient(ws, baseUrl, accessToken)
    recoverToSucceededIf[FooApiClientException] {
      api.getBar(1)
    }
  }

  test("getAllBars: regular case") {
    val ws = StandaloneFakeWSClient {
      case request@GET(url"http://host/bars") =>
        request.headers should contain("Authorization" -> Seq(s"Bearer $accessToken"))
        Ok(Json.toJson(Seq(bar)))
    }

    val api = new WsFooApiClient(ws, baseUrl, accessToken)
    api.getAllBars.map { result =>
      result shouldBe Seq(bar)
    }
  }

  test("getAllBars: unexpected code") {
    val ws = StandaloneFakeWSClient(InternalServerError)
    val api = new WsFooApiClient(ws, baseUrl, accessToken)
    recoverToSucceededIf[FooApiClientException] {
      api.getAllBars
    }
  }

  test("addBar: regular case") {
    val ws = StandaloneFakeWSClient {
      case request@POST(url"http://host/bars") =>
        request.headers should contain("Authorization" -> Seq(s"Bearer $accessToken"))
        Created(Json.toJson(bar))
    }

    val api = new WsFooApiClient(ws, baseUrl, accessToken)
    api.addBar(bar).map { result =>
      result shouldBe bar
    }
  }

  test("addBar: unexpected code") {
    val ws = StandaloneFakeWSClient(InternalServerError)
    val api = new WsFooApiClient(ws, baseUrl, accessToken)
    recoverToSucceededIf[FooApiClientException] {
      api.addBar(bar)
    }
  }

  override def afterAll(): Unit = {
    Await.result(system.terminate(), Duration.Inf)
  }

}
