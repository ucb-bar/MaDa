`timescale 1ns / 1ps

module UartTestBench();
  logic clock, reset;  
  initial clock = 'b0;
  initial reset = 'b1;
  always #5 clock = ~clock;


  wire uart_rx;
  reg uart_tx;

  // no DUT, so we just connect the rx to the tx
  always @(posedge clock) begin
    if (reset) begin
      uart_tx <= 1'b1;
    end
    else begin
      uart_tx <= uart_rx;
    end
  end

  SimUart #(
    .BAUD_RATE(115200)
  ) sim_uart (
    .io_out(uart_rx),
    .io_in(uart_tx)
  );

  bit tx_finished = 0;

  initial begin
    repeat (10) @(posedge clock);
    reset = 1'b0;

    fork
      begin
        sim_uart.listen();
      end
      begin
        sim_uart.write('h41);
        sim_uart.write('h42);
        sim_uart.write('h43);
        sim_uart.write('h44);
        sim_uart.write('h45);
        sim_uart.write('h46);
        sim_uart.write('h47);
        sim_uart.write('h48);
        
        $display("TX finished");
        tx_finished = 1;
      end
    join

    forever begin
      // wait for tx to finish
      if (tx_finished) begin
        // wait additional 10 us to ensure rx is also finished
        #10000;
        $display("Testbench finished");
        $finish;
      end
      @(posedge clock);
    end
  end
endmodule
