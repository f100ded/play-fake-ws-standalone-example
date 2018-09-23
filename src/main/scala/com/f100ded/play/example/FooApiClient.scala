package com.f100ded.play.example

import com.f100ded.play.example.model.Bar

import scala.concurrent.Future

trait FooApiClient {
  def getAllBars: Future[Seq[Bar]]

  def getBar(id: Int): Future[Option[Bar]]

  def addBar(user: Bar): Future[Bar]
}
