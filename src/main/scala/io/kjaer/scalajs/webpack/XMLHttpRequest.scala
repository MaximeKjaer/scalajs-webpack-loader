package io.kjaer.scalajs.webpack

import org.scalajs.dom
import org.scalajs.dom.Event

import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

@js.native
@JSImport("xhr2", JSImport.Namespace)
class XMLHttpRequest extends dom.XMLHttpRequest

object XMLHttpRequest {
  // TODO configure timeout
  def get(url: String): Future[String] = {
    val xhr = new XMLHttpRequest()
    val promise = Promise[String]()
    xhr.onload = _ => promise.success(xhr.responseText)
    xhr.open("GET", url)
    xhr.send()
    promise.future
  }
}
