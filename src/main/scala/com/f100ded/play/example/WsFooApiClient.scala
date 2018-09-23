package com.f100ded.play.example

import com.f100ded.play.example.model.Bar
import org.f100ded.scalaurlbuilder.URLBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.JsonBodyReadables._
import play.api.libs.ws.JsonBodyWritables._
import play.api.libs.ws.StandaloneWSClient

import scala.concurrent.{ExecutionContext, Future}

class WsFooApiClient(wsClient: StandaloneWSClient, baseUrl: String, accessToken: String)
                    (implicit ec: ExecutionContext) extends FooApiClient {

  private val urlBars = URLBuilder(baseUrl).withPathSegments("bars")

  override def getAllBars: Future[Seq[Bar]] = {
    val url = urlBars.toString()
    ws(url).get().map {
      case response if response.status == 200 =>
        response.body[JsValue].as[Seq[Bar]]
      case response =>
        throw FooApiClientException.unexpectedStatus(response.status, url)
    }
  }

  override def getBar(id: Int): Future[Option[Bar]] = {
    val url = urlBars.withPathSegments(s"$id").toString()
    ws(url).get().map {
      case response if response.status == 200 =>
        Some(response.body[JsValue].as[Bar])
      case response if response.status == 404 =>
        None
      case response =>
        throw FooApiClientException.unexpectedStatus(response.status, url)
    }
  }

  override def addBar(bar: Bar): Future[Bar] = {
    val url = urlBars.toString()
    ws(url).post(Json.toJson(bar)).map {
      case response if response.status == 200 || response.status == 201 =>
        response.body[JsValue].as[Bar]
      case response =>
        throw FooApiClientException.unexpectedStatus(response.status, url)
    }
  }

  private def ws(url: String) = {
    wsClient.url(url).withHttpHeaders("Authorization" -> s"Bearer $accessToken")
  }
}
