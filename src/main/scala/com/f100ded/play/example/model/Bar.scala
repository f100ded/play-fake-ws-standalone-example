package com.f100ded.play.example.model

import play.api.libs.json.{Format, Json}

case class Bar(id: Int, name: String, epoch: Int)

object Bar {
  implicit val f: Format[Bar] = Json.format[Bar]
}
