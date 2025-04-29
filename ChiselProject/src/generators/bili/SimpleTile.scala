// import chisel3._
// import chisel3.util._


// class SimpleTile extends Module {
//   val io = IO(new Bundle {
//     val reset_vector = Input(UInt(32.W))
//     val debug = new DebugIO()
//   })

//   val core = Module(new Core(nVectors=2))

//   // instruction memory must be a synchronous 1 cycle read delay memory
//   val itim = Module(new Axi4MemoryWithLatency(
//     params=Axi4Params(addressWidth=12, dataWidth=32),
//     memoryFileHex="firmware.hex",
//     readLatency=2,
//     writeLatency=2,
//   ))

//   val dtim = Module(new Axi4Memory(
//     params=Axi4Params(addressWidth=12, dataWidth=32),
//     memoryFileHex="firmware.hex"
//   ))
//   // val dtim = Module(new SimAxi4LiteMemory(readDelay = 10, writeDelay = 10))
//   // val dtim = Module(new Axi4BlockMemory())
  
//   // itim connection
//   core.io.imem.connectToAxi4(itim.io.s_axi)

//   // dtim connection
//   dtim.io.s_axi <> core.io.dmem
//   core.io.reset_vector := io.reset_vector

//   // vector memory connection
//   core.io.vdmem := DontCare

//   // debug connection
//   io.debug <> core.io.debug
//   dontTouch(core.io.debug)
// }
