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
 * Common configuration trait for Chisel projects
 */
trait ChiselModule extends ScalaModule with ScalafmtModule { m =>
  // Configure Scala version based on Chisel version
  override def scalaVersion = "2.13.16"

  // Scala compiler options
  override def scalacOptions = Seq(
    "-language:reflectiveCalls",  // Enable reflective calls
    "-deprecation",               // Emit warning for deprecated features
    "-feature",                   // Emit warning for usages of features that should be imported explicitly
    "-Xcheckinit"                // Add runtime checks for uninitialized values
  )

  // Define Chisel dependencies based on version
  override def ivyDeps = Agg(
    ivy"org.chipsalliance::chisel:7.0.0-RC1",
  )

  // Add Chisel compiler plugin
  override def scalacPluginIvyDeps = Agg(
    ivy"org.chipsalliance:::chisel-plugin:7.0.0-RC1",
  )

  /**
   * Test configuration object that includes ScalaTest support
   */
  object test extends ScalaTests with TestModule.ScalaTest with ScalafmtModule {
    // Add test dependencies
    override def ivyDeps = super.ivyDeps() ++ Agg(
      ivy"org.scalatest::scalatest::3.2.19",
      ivy"edu.berkeley.cs::chiseltest:6.0.0"
    )
  }
}


object builder extends ChiselModule

object `package-vivado-ips` extends ChiselModule { m =>
  override def moduleDeps = Seq(builder)
}

object `package-delta-soc` extends ChiselModule { m =>
  override def moduleDeps = Seq(builder, `package-vivado-ips`)
}

object `package-chipyard-wrapper` extends ChiselModule { m =>
  override def moduleDeps = Seq(builder, `package-vivado-ips`)
}

