package com.f100ded.play.example

class FooApiClientException private(val status: Int, message: String) extends RuntimeException(message)

object FooApiClientException {
  def unexpectedStatus(status: Int, url: String): FooApiClientException = {
    new FooApiClientException(status, s"Unexpected status code at $url")
  }
}
