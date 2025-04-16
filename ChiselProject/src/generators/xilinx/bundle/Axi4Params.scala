import chisel3._
import chisel3.util._


/**
  * AXI4 protocol type definition.
  */
object Axi4Protocol {
  /** AXI4 protocol. */
  val AXI4 = "AXI4"

  /** AXI4-Lite protocol. */
  val AXI4LITE = "AXI4LITE"
}

case class Axi4Params(
  /** width of the ID field */
  idWidth: Int = 4,

  /** width of the address field */
  addressWidth: Int = 32,
  
  /** width of the data field */
  dataWidth: Int = 32,
  
  /** width of the user field */
  userWidth: Int = 1,
  
  /** protocol type */
  protocol: String = Axi4Protocol.AXI4
)
