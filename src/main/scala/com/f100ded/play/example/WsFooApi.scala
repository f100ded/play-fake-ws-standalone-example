package com.f100ded.play.example

import com.f100ded.play.example.model.Bar
import org.f100ded.scalaurlbuilder.URLBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.JsonBodyReadables._
import play.api.libs.ws.JsonBodyWritables._
import play.api.libs.ws.StandaloneWSClient

import scala.concurrent.{ExecutionContext, Future}

class WsFooApi(ws: StandaloneWSClient, baseUrl: String)
              (implicit ec: ExecutionContext) extends FooApi {

  private val urlUsers = URLBuilder(baseUrl).withPathSegments("users")

  override def getAllBars: Future[Seq[Bar]] = {
    val url = urlUsers.toString()
    ws.url(url).get().map {
      case response if response.status == 200 =>
        response.body[JsValue].as[Seq[Bar]]
      case response =>
        throw FooApiException.unexpectedStatus(response.status, url)
    }
  }

  override def getBar(id: Int): Future[Option[Bar]] = {
    val url = urlUsers.withPathSegments(s"$id").toString()
    ws.url(url).get().map {
      case response if response.status == 200 =>
        Some(response.body[JsValue].as[Bar])
      case response if response.status == 404 =>
        None
      case response =>
        throw FooApiException.unexpectedStatus(response.status, url)
    }
  }

  override def addBar(user: Bar): Future[Bar] = {
    val url = urlUsers.toString()
    ws.url(url).post(Json.toJson(user)).map {
      case response if response.status == 200 || response.status == 201 =>
        response.body[JsValue].as[Bar]
      case response =>
        throw FooApiException.unexpectedStatus(response.status, url)
    }
  }
}
