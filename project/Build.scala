import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "contracts"
    val appVersion      = "1.0.1"

    val appDependencies = Seq(
      // Add your project dependencies here,
			"postgresql" % "postgresql" % "9.1-901-1.jdbc4",
			"mysql" % "mysql-connector-java" % "5.1.19",
			"javax.mail" % "mail" % "1.4.5"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
			coffeescriptOptions := Seq("bare"),
			credentials += Credentials(Path.userHome / ".credentials"),
			publishTo := Some("Artifactory Realm" at "http://build.ntkn.com/artifactory/libs-release-local/")

    )

		parallelExecution in Test := false
}

