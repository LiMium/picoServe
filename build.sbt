name := "picoserve"

scalaVersion := "2.13.3"

fork := true

scalacOptions := Seq( "-opt:l:inline", "-opt-inline-from:scala.**", "-opt-inline-from:org.limium.**")

