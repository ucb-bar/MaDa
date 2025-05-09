package amba

import chisel3._
import chisel3.util._


case class Axi4Params(
  /** width of the ID field */
  idWidth: Int = 4,

  /** width of the address field */
  addressWidth: Int = 32,
  
  /** width of the data field */
  dataWidth: Int = 32,
  
  /** width of the user field */
  userWidth: Int = 1,
)
