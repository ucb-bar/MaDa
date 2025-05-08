--SimSpiFlash
-- file: SimSpiFlash.vhd
--
-- (c) Copyright 2008 - 2023 Advanced Micro Devices, Inc. All rights reserved.
--
-- This file contains confidential and proprietary information
-- of Advanced Micro Devices, Inc. and is protected under U.S. and
-- international copyright and other intellectual property
-- laws.
--
-- DISCLAIMER
-- This disclaimer is not a license and does not grant any
-- rights to the materials distributed herewith. Except as
-- otherwise provided in a valid license issued to you by
-- AMD, and to the maximum extent permitted by applicable
-- law: (1) THESE MATERIALS ARE MADE AVAILABLE "AS IS" AND
-- WITH ALL FAULTS, AND AMD HEREBY DISCLAIMS ALL WARRANTIES
-- AND CONDITIONS, EXPRESS, IMPLIED, OR STATUTORY, INCLUDING
-- BUT NOT LIMITED TO WARRANTIES OF MERCHANTABILITY, NON-
-- INFRINGEMENT, OR FITNESS FOR ANY PARTICULAR PURPOSE; and
-- (2) AMD shall not be liable (whether in contract or tort,
-- including negligence, or under any other theory of
-- liability) for any loss or damage of any kind or nature
-- related to, arising under or in connection with these
-- materials, including for any direct, or any indirect,
-- special, incidental, or consequential loss or damage
-- (including loss of data, profits, goodwill, or any type of
-- loss or damage suffered as a result of any action brought
-- by a third party) even if such damage or loss was
-- reasonably foreseeable or AMD had been advised of the
-- possibility of the same.
--
-- CRITICAL APPLICATIONS
-- AMD products are not designed or intended to be fail-
-- safe, or for use in any application requiring fail-safe
-- performance, such as life-support or safety devices or
-- systems, Class III medical devices, nuclear facilities,
-- applications related to the deployment of airbags, or any
-- other applications that could lead to death, personal
-- injury, or severe property or environmental damage
-- (individually and collectively, "Critical
-- Applications"). Customer assumes the sole risk and
-- liability of any use of AMD products in Critical
-- Applications, subject only to applicable laws and
-- regulations governing limitations on product liability.
--
-- THIS COPYRIGHT NOTICE AND DISCLAIMER MUST BE RETAINED AS
-- PART OF THIS FILE AT ALL TIMES.
--
------------------------------------------------------------------------------
-- User entered comments
------------------------------------------------------------------------------
-- This is a self-desigined Memory model for XIP mode support memories to act as slave
-- for AXI QSPI in Example design
--
------------------------------------------------------------------------------
library ieee;
    use ieee.std_logic_1164.all;
    use ieee.std_logic_arith.all;
    use ieee.std_logic_unsigned.all;
    use ieee.numeric_std.all;
    use ieee.std_logic_misc.all;


entity SimSpiFlash is generic (
  C_FIFO_DEPTH          : integer              := 256;-- allowed 0,16,256.
  C_ADDR_WIDTH          : integer              := 256;-- allowed 0,16,256.

	C_SPI_MODE            : integer range 0 to 2 := 0; -- used for differentiating
                                                     -- Standard, Dual or Quad mode
                                                     -- in Ports as well as internal
                                                     -- functionality
	C_DATA_WIDTH          : integer               := 8       -- allowed 8,32.
);
  port (
    MODEL_CLK            : in  std_logic;
    MODEL_RESET          : in  std_logic;
    Core_Clk             : in std_logic;
    Chip_Selectn         : in std_logic;

    -------------------------------
    --*SPI port interface      * --
    -------------------------------
    io0_i          : in std_logic;  -- MOSI signal in standard SPI
    io0_o          : out std_logic;
    io0_t          : out std_logic;
    -------------------------------
    io1_i          : in std_logic;  -- MISO signal in standard SPI
    io1_o          : out std_logic;
    io1_t          : out std_logic;
    -----------------
    -- quad mode pins
    -----------------
    io2_i          : in std_logic;
    io2_o          : out std_logic;
    io2_t          : out std_logic;
    ---------------
    io3_i          : in std_logic;
    io3_o          : out std_logic;
    io3_t          : out std_logic
    ---------------------------------
);
end SimSpiFlash;

architecture imp of SimSpiFlash is

  --------------------------------------------------------------------------------------
  -- below attributes are added to reduce the synth warnings in Vivado tool
    attribute DowngradeIPIdentifiedWarnings: string;
    attribute DowngradeIPIdentifiedWarnings of imp : architecture is "yes";
  --------------------------------------------------------------------------------------

  function log2(x : natural) return integer is
    variable i  : integer := 0;
    variable val: integer := 1;
  begin
    if x = 0 then return 0;
    else
      for j in 0 to 29 loop -- for loop for XST
        if val >= x then null;
        else
          i := i+1;
          val := val*2;
        end if;
      end loop;
    -- Fix per CR520627  XST was ignoring this anyway and printing a
    -- Warning in SRP file. This will get rid of the warning and not
    -- impact simulation.
    -- synthesis translate_off
      assert val >= x
        report "Function log2 received argument larger" &
              " than its capability of 2^30. "
        severity failure;
    -- synthesis translate_on
      return i;
    end if;
  end function log2;


  constant ADDR_WIDTH     : INTEGER   := log2(C_FIFO_DEPTH);
  constant RESET_ACTIVE   : std_logic := '0';
  constant CMD_FAST_READ  : std_logic_vector(7 downto 0):= X"0B";
  constant CMD_DUAL_READ  : std_logic_vector(7 downto 0):= X"BB";
  constant CMD_QUAD_READ  : std_logic_vector(7 downto 0):= X"EB";
  constant Wait_std       : INTEGER   := (C_ADDR_WIDTH + 8);
  constant Wait_dual      : INTEGER   := (C_ADDR_WIDTH/2 + 4);
  constant Wait_quad      : INTEGER   := (C_ADDR_WIDTH/4 + 8);


  signal Serial_Dout_0    : std_logic := '0';
  signal Serial_Dout_1    : std_logic := '0';
  signal Serial_Dout_2    : std_logic := '0';
  signal Serial_Dout_3    : std_logic := '0';

  signal FIFO_RD_EN       : std_logic := '0';
  signal rising           : std_logic := '0';
  signal falling          : std_logic := '0';
  signal SCK_D            : std_logic := '0';

  signal Count_Pulse      : std_logic := '0';
  signal Count_Pulse_d    : std_logic := '0';


  signal Wait_clk         : INTEGER := 0;
  signal Cnt_8_Clk        : std_logic_vector(2 downto 0):=(others => '1');

  signal Counter          : std_logic_vector(5 downto 0):=(others => '0');
  signal Addr_Cnt         : std_logic_vector(4 downto 0):=(others => '0');
  signal Read_Addr        : std_logic_vector(4 downto 0):=(others => '1');


  signal Data_From_Rx_FIFO  : std_logic_vector(0 to C_DATA_WIDTH-1):=(others => '0');
  signal Transmit_Data      : std_logic_vector(0 to C_DATA_WIDTH-1):=(others => '0');

  signal Read_Addr_Int,Count_Int  : INTEGER := 0;


  type STATE_TYPE is (
    IDLE,       -- decode command can be combined here later
    WAIT_STATE,
    READ
  );
  TYPE mem_array IS ARRAY (31 DOWNTO 0) OF STD_LOGIC_VECTOR(7 DOWNTO 0);
  SIGNAL ram : mem_array:= (
    X"01",X"02",X"03",X"04",
    X"05",X"06",X"07",X"08",
    X"09",X"0A",X"0B",X"0C",
    X"0D",X"0E",X"0F",X"10",
    X"11",X"12",X"13",X"14",
    X"15",X"16",X"17",X"18",
    X"03",X"02",X"01",X"00",
    X"FF",X"FF",X"FF",X"FF"
  );



  signal spi_cntrl_ns: STATE_TYPE;

  attribute dont_touch : string;
  attribute dont_touch of Serial_Dout_0   : signal is "TRUE";
  attribute dont_touch of Serial_Dout_1   : signal is "TRUE";
  attribute dont_touch of Serial_Dout_2   : signal is "TRUE";
  attribute dont_touch of Serial_Dout_3   : signal is "TRUE";
  attribute dont_touch of Count_Pulse     : signal is "TRUE";
  attribute dont_touch of FIFO_RD_EN      : signal is "TRUE";
  attribute dont_touch of ram             : signal is "TRUE";

  attribute dont_touch of Count_Pulse_d   : signal is "TRUE";
  attribute dont_touch of Wait_clk        : signal is "TRUE";
  attribute dont_touch of Counter         : signal is "TRUE";
  attribute dont_touch of Addr_Cnt        : signal is "TRUE";
  attribute dont_touch of Read_Addr       : signal is "TRUE";
  attribute dont_touch of Data_From_Rx_FIFO : signal is "TRUE";
  attribute dont_touch of Transmit_Data   : signal is "TRUE";
  attribute dont_touch of spi_cntrl_ns    : signal is "TRUE";

  begin
  Rising_falling_process: process(MODEL_CLK)is
  -----
  begin
  -----
    if(MODEL_CLK'event and MODEL_CLK = '1') then
      if(MODEL_RESET = RESET_ACTIVE) then
        SCK_D  <= '0';
      else
        SCK_D <= Core_Clk;
      end if;
    end if;
  end process Rising_falling_process;

  rising <= Core_Clk and (not(SCK_D));
  falling <= SCK_D and (not(Core_Clk));



  COUNTER_8_Cycles_Pos_PROCESS : process(MODEL_CLK)is
  begin
  -----
    if(MODEL_CLK'event and MODEL_CLK = '1') then --1
      if(MODEL_RESET = RESET_ACTIVE) then
        Cnt_8_Clk <= (others => '1');
      elsif (Chip_Selectn = '1') then
        Cnt_8_Clk <= (others => '1');
        elsif(rising = '1') then
        Cnt_8_Clk <= Cnt_8_Clk + 1;
        end if;
      end if;
  end process COUNTER_8_Cycles_Pos_PROCESS;

  COUNTER_PROCESS : process(MODEL_CLK)is
  begin
  -----
    if(MODEL_CLK'event and MODEL_CLK = '1') then --1
      if(MODEL_RESET = RESET_ACTIVE) then
        Counter <= (others => '1');
      elsif (Chip_Selectn = '1') then
        Counter <= (others => '1');
        elsif (falling = '1') then
        Counter <= Counter + 1;
        end if;
      end if;
  end process COUNTER_PROCESS;

  Count_Int <= CONV_INTEGER(Counter);

  Wait_clk  <= Wait_std  WHEN (C_SPI_MODE = 0) ELSE
            Wait_dual WHEN (C_SPI_MODE = 1) ELSE
            Wait_quad ;

  SPI_STATE_MACHINE_P: process(MODEL_CLK)
  begin
    if(MODEL_CLK'event and MODEL_CLK = '0') then   --1

      --------------------------
      case spi_cntrl_ns is
        --------------------------
        when IDLE =>
          if(Chip_Selectn = '0') then
            spi_cntrl_ns <= WAIT_STATE;
          else
            spi_cntrl_ns <= IDLE;
          end if;
          io0_t <= '1';
          io1_t <= '0';
          io2_t <= '0';
          io3_t <= '0';
        -------------------------------------
        when WAIT_STATE =>
          if(Chip_Selectn = '0') then
            if (Count_Int = Wait_clk) then
              spi_cntrl_ns <= READ;
            else
              spi_cntrl_ns <= WAIT_STATE;
            end if;
          else
            spi_cntrl_ns <= IDLE;
          end if;
          io0_t <= '1';
          io1_t <= '0';
          io2_t <= '0';
          io3_t <= '0';
        -------------------------------------
        when READ =>
          if(Chip_Selectn = '0') then
            spi_cntrl_ns <= READ;
          else
            spi_cntrl_ns <= IDLE;
          end if;
          io0_t <= '0';
          io1_t <= '0';
          io2_t <= '0';
          io3_t <= '0';
        when others =>
          spi_cntrl_ns <= IDLE;
          io0_t <= '1';
          io1_t <= '0';
          io2_t <= '0';
          io3_t <= '0';
        -------------------------------------
      end case;
    end if;
  end process SPI_STATE_MACHINE_P;


  Quad_Read_GENERATE : if (C_SPI_MODE = 2) generate
    FIFO_READ_EN_PROCESS : process(MODEL_CLK)is
    begin
      if(MODEL_CLK'event and MODEL_CLK = '1') then --SPIXfer_done_int_pulse_d2
        if ((MODEL_RESET = RESET_ACTIVE) or (Chip_Selectn = '1'))then
          FIFO_RD_EN       <= '0';
        elsif((spi_cntrl_ns = READ) and (falling = '1'))then
          FIFO_RD_EN <= Cnt_8_Clk(0) ;
        end if;
      end if;
    end process FIFO_READ_EN_PROCESS;

    READ_PROCESS : process(MODEL_CLK)is
    begin
      if(MODEL_CLK'event and MODEL_CLK = '1') then --SPIXfer_done_int_pulse_d2
        if ((MODEL_RESET = RESET_ACTIVE) or (Chip_Selectn = '1'))then
          Read_Addr       <= (others => '1');
          Data_From_Rx_FIFO       <= (others => '0');
        elsif ((FIFO_RD_EN = '1') and (falling = '1')) then
          Read_Addr       <= Read_Addr + 1;
          Data_From_Rx_FIFO  <= ram(Read_Addr_Int) ;
        end if;
      end if;
    end process READ_PROCESS;

    Read_Addr_Int <= CONV_INTEGER(Read_Addr);

    REGISTER_SHIFT_PROCESS : process(MODEL_CLK)is
    begin
      if(MODEL_CLK'event and MODEL_CLK = '1') then
        if (MODEL_RESET = RESET_ACTIVE) or (Chip_Selectn = '1')then
          Transmit_Data <= (others => '0');
        elsif ((FIFO_RD_EN = '1') and ( falling = '1'))then
          Transmit_Data       <= Data_From_Rx_FIFO;
        elsif (falling = '1') then
          Transmit_Data <= Transmit_Data(4 to (C_DATA_WIDTH-1)) & "0000";
        end if;
      end if;
    end process REGISTER_SHIFT_PROCESS;
  end generate Quad_Read_GENERATE ;


  Dual_Read_GENERATE : if (C_SPI_MODE = 1) generate
    FIFO_READ_EN_PROCESS : process(MODEL_CLK)is
    begin
      if(MODEL_CLK'event and MODEL_CLK = '1') then --SPIXfer_done_int_pulse_d2
        if ((MODEL_RESET = RESET_ACTIVE) or (Chip_Selectn = '1'))then
          FIFO_RD_EN       <= '0';
        elsif((spi_cntrl_ns = READ) and (falling = '1'))then
          FIFO_RD_EN <= Cnt_8_Clk(0) and (not(Cnt_8_Clk(1)));--not(Cnt_8_Clk(0) or Cnt_8_Clk(1));
        end if;
      end if;
    end process FIFO_READ_EN_PROCESS;


    READ_PROCESS : process(MODEL_CLK)is
      begin
        if(MODEL_CLK'event and MODEL_CLK = '1') then --SPIXfer_done_int_pulse_d2
          if ((MODEL_RESET = RESET_ACTIVE) or (Chip_Selectn = '1'))then
            Read_Addr       <= (others => '1');
            Data_From_Rx_FIFO       <= (others => '0');
          elsif ((FIFO_RD_EN = '1') and (falling = '1')) then
            Read_Addr       <= Read_Addr + 1;
            Data_From_Rx_FIFO  <= ram(Read_Addr_Int) ;
          end if;
        end if;
      end process READ_PROCESS;

    Read_Addr_Int <= CONV_INTEGER(Read_Addr);

    REGISTER_SHIFT_PROCESS : process(MODEL_CLK)is
    begin
      if(MODEL_CLK'event and MODEL_CLK = '1') then
        if (MODEL_RESET = RESET_ACTIVE) or (Chip_Selectn = '1')then
          Transmit_Data <= (others => '0');
        elsif ((FIFO_RD_EN = '1') and (falling = '1'))then
          Transmit_Data       <= Data_From_Rx_FIFO;
        elsif (falling = '1') then
          Transmit_Data <= Transmit_Data(2 to (C_DATA_WIDTH-1)) & "00";
        end if;
      end if;
    end process REGISTER_SHIFT_PROCESS;
  end generate Dual_Read_GENERATE ;


  STD_Read_Process : if (C_SPI_MODE = 0) generate
    FIFO_READ_EN_PROCESS : process(MODEL_CLK)is
    begin
      if(MODEL_CLK'event and MODEL_CLK = '1') then --SPIXfer_done_int_pulse_d2
        if ((MODEL_RESET = RESET_ACTIVE) or (Chip_Selectn = '1'))then
          FIFO_RD_EN       <= '0';
        elsif((spi_cntrl_ns = READ) and (falling = '1'))then
          FIFO_RD_EN <= Cnt_8_Clk(2) and Cnt_8_Clk(1) and Cnt_8_Clk(0) ;
        end if;
      end if;
    end process FIFO_READ_EN_PROCESS;


    READ_PROCESS : process(MODEL_CLK)is
    begin
      if(MODEL_CLK'event and MODEL_CLK = '1') then
        if ((MODEL_RESET = RESET_ACTIVE) or (Chip_Selectn = '1'))then
          Read_Addr       <= (others => '1');
          Data_From_Rx_FIFO       <= (others => '0');
        elsif ((FIFO_RD_EN = '1') and (falling = '1')) then
          Read_Addr       <= Read_Addr + 1;
          Data_From_Rx_FIFO  <= ram(Read_Addr_Int) ;
        end if;
      end if;
    end process READ_PROCESS;

    Read_Addr_Int <= CONV_INTEGER(Read_Addr);

    REGISTER_SHIFT_PROCESS : process(MODEL_CLK)is
    begin
      if(MODEL_CLK'event and MODEL_CLK = '1') then
        if (MODEL_RESET = RESET_ACTIVE) or (Chip_Selectn = '1')then
          Transmit_Data <= (others => '0');
        elsif ((FIFO_RD_EN = '1') and (falling = '1'))then
          Transmit_Data       <= Data_From_Rx_FIFO;
        elsif (falling = '1') then
          Transmit_Data <= Transmit_Data(1 to (C_DATA_WIDTH-1)) & '0';
        end if;
      end if;
    end process REGISTER_SHIFT_PROCESS;
  end generate STD_Read_Process ;


  FIFO_READ_PROCESS: process(MODEL_CLK) is
  -----
  begin
  -----
    if(MODEL_CLK'event and MODEL_CLK = '1') then
      if((MODEL_RESET = RESET_ACTIVE)or (Chip_Selectn = '1')) then
        Serial_Dout_0 <= '0';-- default values of the IO0_O
        Serial_Dout_1 <= '0';
        Serial_Dout_2 <= '0';
        Serial_Dout_3 <= '0';
      elsif (falling = '1') then
        --if(spi_cntrl_ns = READ_DATA)then
          --Shift_Reg   <= Transmit_Data;-- loading trasmit data in SR
          if(C_SPI_MODE = 0) then    -- standard mode
            Serial_Dout_1 <= Transmit_Data(0);
          elsif(C_SPI_MODE = 1) then -- dual mode
            Serial_Dout_1 <= Transmit_Data(0); -- msb to IO1_O
            Serial_Dout_0 <= Transmit_Data(1);
          elsif(C_SPI_MODE = 2) then -- quad mode
            Serial_Dout_3 <= Transmit_Data(0); -- msb to IO3_O
            Serial_Dout_2 <= Transmit_Data(1);
            Serial_Dout_1 <= Transmit_Data(2);
            Serial_Dout_0 <= Transmit_Data(3);
          end if;
        -- end if;
      end if;
    end if;
  end process FIFO_READ_PROCESS;

	io0_o <= Serial_Dout_0;
	io1_o <= Serial_Dout_1;
	io2_o <= Serial_Dout_2;
	io3_o <= Serial_Dout_3;
end imp;
