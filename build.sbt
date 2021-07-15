name := "TinyRV32I"

version := "0.1"

scalaVersion := "2.11.12"

crossScalaVersions := Seq("2.11.12", "2.12.4")

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases")
)

val defaultVersions = Map(
  "chisel3" -> "3.1.+",
  "chisel-iotesters" -> "1.3-SNAPSHOT",
)

libraryDependencies ++= Seq("chisel3","chisel-iotesters").map { dep: String =>
  "edu.berkeley.cs" %% dep % sys.props.getOrElse(dep + "Version", defaultVersions(dep))
}
