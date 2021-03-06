import play.Project._
import scala.Some

name := """contract-management"""

version := "1.0.18"

libraryDependencies ++= Seq(
  // Select Play modules
  jdbc,      // The JDBC connection pool and the play.api.db API
  javaJdbc,  // Java database API
  filters,   // A set of built-in filters
  javaCore,  // The core Java API
  "com.typesafe" % "config" % "1.0.2",
  "org.webjars" %% "webjars-play" % "2.2.0",
  "org.webjars" % "bootstrap" % "2.3.1",
  "com.typesafe.slick" %% "slick" % "1.0.1",
  "com.github.tototoshi" %% "slick-joda-mapper" % "0.4.0",
  "postgresql" % "postgresql" % "9.1-901-1.jdbc4",
  "mysql" % "mysql-connector-java" % "5.1.19",
  "javax.mail" % "mail" % "1.4.5"
  // Add your own project dependencies in the form:
  // "group" % "artifact" % "version"
)

coffeescriptOptions := Seq("bare")

credentials += Credentials(Path.userHome / ".credentials")

publishTo := Some("Artifactory Realm" at "http://build.ntkn.com/artifactory/libs-release-local/")

play.Project.playScalaSettings
