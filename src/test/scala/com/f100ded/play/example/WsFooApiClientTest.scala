package com.f100ded.play.example

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.f100ded.play.example.model.Bar
import org.f100ded.play.fakews._
import org.scalatest._
import play.api.libs.json.Json
import play.api.libs.ws.DefaultBodyWritables._
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

  private val DUMMY_URL = "http://host/"

  private val DUMMY_BAR = Bar(1, "bar", 0)

  private val DUMMY_ACCESS_TOKEN = "fake_access_token"

  test("getBar: normal flow") {
    val ws = StandaloneFakeWSClient {
      case request@GET(url"http://host/bars/$id") =>
        id shouldBe "1"
        request.headers("Authorization") shouldBe Seq(s"Bearer $DUMMY_ACCESS_TOKEN")
        Ok(Json.toJson(DUMMY_BAR))
    }

    val api = new WsFooApiClient(ws, DUMMY_URL, DUMMY_ACCESS_TOKEN)
    api.getBar(1).map { result =>
      result shouldBe Some(DUMMY_BAR)
    }
  }

  test("getBar: gracefully handle 404") {
    val ws = StandaloneFakeWSClient {
      case GET(url"""http://host/bars/[\d]+""") =>
        NotFound
    }

    val api = new WsFooApiClient(ws, DUMMY_URL, DUMMY_ACCESS_TOKEN)
    api.getBar(1).map { result =>
      result shouldBe None
    }
  }

  test("getBar: gracefully handle unexpected status") {
    val ws = StandaloneFakeWSClient(InternalServerError)
    val api = new WsFooApiClient(ws, DUMMY_URL, DUMMY_ACCESS_TOKEN)
    api.getBar(1).failed.map {
      case e: FooApiClientException =>
        e.status shouldBe 500
    }
  }

  test("getBar: gracefully handle unexpected response body") {
    val ws = StandaloneFakeWSClient(Ok("This should not be here"))
    val api = new WsFooApiClient(ws, DUMMY_URL, DUMMY_ACCESS_TOKEN)
    api.getBar(1).failed.map {
      case e: FooApiClientException =>
        e.status shouldBe 200
    }
  }

  test("getAllBars: normal flow") {
    val ws = StandaloneFakeWSClient {
      case request@GET(url"http://host/bars") =>
        request.headers should contain("Authorization" -> Seq(s"Bearer $DUMMY_ACCESS_TOKEN"))
        Ok(Json.toJson(Seq(DUMMY_BAR)))
    }

    val api = new WsFooApiClient(ws, DUMMY_URL, DUMMY_ACCESS_TOKEN)
    api.getAllBars.map { result =>
      result shouldBe Seq(DUMMY_BAR)
    }
  }

  test("getAllBars: gracefully handle unexpected status") {
    val ws = StandaloneFakeWSClient(InternalServerError)
    val api = new WsFooApiClient(ws, DUMMY_URL, DUMMY_ACCESS_TOKEN)
    recoverToSucceededIf[FooApiClientException] {
      api.getAllBars
    }
  }

  test("addBar: normal flow") {
    val ws = StandaloneFakeWSClient {
      case request@POST(url"http://host/bars") =>
        request.headers should contain("Authorization" -> Seq(s"Bearer $DUMMY_ACCESS_TOKEN"))
        Created(Json.toJson(DUMMY_BAR))
    }

    val api = new WsFooApiClient(ws, DUMMY_URL, DUMMY_ACCESS_TOKEN)
    api.addBar(DUMMY_BAR).map { result =>
      result shouldBe DUMMY_BAR
    }
  }

  test("addBar: gracefully handle unexpected status") {
    val ws = StandaloneFakeWSClient(InternalServerError)
    val api = new WsFooApiClient(ws, DUMMY_URL, DUMMY_ACCESS_TOKEN)
    recoverToSucceededIf[FooApiClientException] {
      api.addBar(DUMMY_BAR)
    }
  }

  override def afterAll(): Unit = {
    Await.result(system.terminate(), Duration.Inf)
  }

}
