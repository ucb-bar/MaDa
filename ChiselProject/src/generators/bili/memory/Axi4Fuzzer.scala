// import chisel3._
// import chisel3.util._


// class Axi4Fuzzer(
//   params: Axi4Params = Axi4Params(),
// ) extends Module {
//   val io = IO(new Bundle {
//     val m_axi = new Axi4Bundle(params)
//   })


//   val counter = Counter(10)

//   val do_request = RegInit(false.B)

//   val address_counter = RegInit(0.U(32.W))

//   when (counter === 0.U) {
//     do_request := true.B
//   }

//   when (do_request) {
//     io.m_axi.aw.valid := true.B

//   io.m_axi.aw.valid := counter.U < 10.U
//   io.m_axi.aw.bits.addr := counter.U
//   io.m_axi.aw.bits.len := 0.U
//   io.m_axi.aw.bits.size := 3.U
//   io.m_axi.aw.bits.burst := 0.U

// }