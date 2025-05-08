/**
 * build.sc
 * 
 * This is the build configuration file for the Chisel project.
 * It defines the project structure and its dependencies.
 */

// Import Mill build tool dependencies
import mill._
import mill.define.Sources
import mill.modules.Util
import mill.scalalib.scalafmt.ScalafmtModule
import mill.scalalib.TestModule.ScalaTest
import mill.scalalib._
// Import BSP (Build Server Protocol) support
import mill.bsp._


/**
 * Main build definition for the Chisel project.
 * This object defines the project structure and its dependencies.
 */
object ChiselProject extends ScalaModule with ScalafmtModule { m =>
  // Flag to switch between Chisel 3.x and Chisel 6.x
  val useChisel3 = false

  // Configure Scala version based on Chisel version
  override def scalaVersion = if (useChisel3) "2.13.10" else "2.13.15"

  // Scala compiler options
  override def scalacOptions = Seq(
    "-language:reflectiveCalls",  // Enable reflective calls
    "-deprecation",               // Emit warning for deprecated features
    "-feature",                   // Emit warning for usages of features that should be imported explicitly
    "-Xcheckinit"                // Add runtime checks for uninitialized values
  )

  // Define Chisel dependencies based on version
  override def ivyDeps = Agg(  
    if (useChisel3)
      ivy"edu.berkeley.cs::chisel3:3.6.0"
    else
      ivy"org.chipsalliance::chisel:6.7.0"
  )

  // Add Chisel compiler plugin
  override def scalacPluginIvyDeps = Agg(
    if (useChisel3)
      ivy"edu.berkeley.cs:::chisel3-plugin:3.6.0"
    else
      ivy"org.chipsalliance:::chisel-plugin:6.7.0"
  )

  /**
   * Test configuration object that includes ScalaTest support
   */
  object test extends ScalaTests with TestModule.ScalaTest with ScalafmtModule {
    // Add test dependencies
    override def ivyDeps = super.ivyDeps() ++ Agg(
      ivy"org.scalatest::scalatest::3.2.19",
      if (useChisel3)
        ivy"edu.berkeley.cs::chiseltest:0.6.0"
      else
        ivy"edu.berkeley.cs::chiseltest:6.0.0"
    )
  }

  // Configure additional Maven repositories for dependency resolution
  def repositoriesTask = Task.Anon {
    Seq(
      coursier.MavenRepository("https://repo.scala-sbt.org/scalasbt/maven-releases"),
      coursier.MavenRepository("https://oss.sonatype.org/content/repositories/releases"),
      coursier.MavenRepository("https://oss.sonatype.org/content/repositories/snapshots")
    ) ++ super.repositoriesTask()
  }
}
