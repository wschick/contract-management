package models

// From http://danieldietrich.net/?p=47

import play.api.mvc.PathBindable

abstract class BindableEnum extends Enumeration {

  implicit def bindableEnum = new PathBindable[Value] {

    def bind(key: String, value: String) =
      values.find(_.toString.toLowerCase == value.toLowerCase) match {
        case Some(v) => Right(v)
        case None => Left("Unknown url path segment '" + value + "'")
      }

    def unbind(key: String, value: Value) = value.toString.toLowerCase

  }

}
