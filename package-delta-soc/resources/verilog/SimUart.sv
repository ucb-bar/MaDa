package SimUart;

// Define an enum for logging modes
typedef enum int {
  LOGGING_NONE = 0,
  LOGGING_DEBUG = 10,
  LOGGING_INFO = 20
} logging_mode_t;

endpackage

/**
 * SimUart is a simple UART simulator that can be used to test the UART module.
 * It is used to send and receive bytes to and from the UART module.
 * 
 * @param BAUD_RATE The baud rate of the UART.
 * @param BAUD_PERIOD The period of the baud rate.
 */
module SimUartConsole #(
  parameter int BAUD_RATE = 115_200,
  parameter int BAUD_PERIOD = 1_000_000_000 / BAUD_RATE  // 8680.55 ns
) (
  output logic io_out,
  input  logic io_in
);

  // Default logging mode
  int logging_mode = SimUart::LOGGING_INFO;

  /**
   * Send a byte to the UART
   * 
   * @param tx_char The byte to send.
   */
  task automatic write(
    input byte tx_char,
    input int logging_mode = 0
  );
    int i;
    begin
      io_out = 0;
      #(BAUD_PERIOD);
      // Data bits (payload)
      for (i = 0; i < 8; i = i + 1) begin
        io_out = tx_char[i];
        #(BAUD_PERIOD);
      end
      // Stop bit
      io_out = 1;
      #(BAUD_PERIOD);

      if (logging_mode == SimUart::LOGGING_DEBUG) begin
        $display("[time %t] <SimUART> TX --> 0x%h", $time, tx_char);
      end
    end
  endtask

  /**
   * Continuously receive bytes from the UART
   */
  task automatic listen(
    input int logging_mode = 0
  );
    int j;
    begin
      automatic logic [9:0] rx_char;

      // Display message at the start of task execution
      if (logging_mode == SimUart::LOGGING_INFO) begin
        $display("[UART] UART0 is here (stdin/stdout):");
      end

      forever begin
        // Wait until serial_out is LOW (start of transaction)
        wait (io_in === 1'b0);

        for (j = 0; j < 10; j = j + 1) begin
          // sample output half-way through the baud period to avoid tricky edge cases
          #(BAUD_PERIOD / 2);
          rx_char[j] = io_in;
          #(BAUD_PERIOD / 2);
        end

        if (logging_mode == SimUart::LOGGING_INFO) begin
          $write("%c", rx_char[8:1]);
          $fflush();
        end
        if (logging_mode == SimUart::LOGGING_DEBUG) begin
          $display("[time %t] <SimUART> RX <-- 0x%h, start_bit=%b, stop_bit=%b", $time, rx_char[8:1], rx_char[0], rx_char[9]);
        end
      end
    end
  endtask
endmodule
