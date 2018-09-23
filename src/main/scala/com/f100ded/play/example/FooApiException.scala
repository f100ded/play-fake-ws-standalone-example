package com.f100ded.play.example

class FooApiException private(val status: Int, message: String) extends RuntimeException(message)

object FooApiException {
  def unexpectedStatus(status: Int, url: String): FooApiException = {
    new FooApiException(status, s"Unexpected status code at $url")
  }
}
