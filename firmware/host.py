import struct

import serial

ser = serial.Serial("/dev/ttyUSB1", 115200)

try:
    while True:
        command = ser.read(1)

        if command == b"\x01":
            print("DUT request exit()")
        elif command == b"\x03":
            print("DUT request putchar(): ", end="")
            data = ser.read(1)
            print(data)
        elif command == b"\x04":
            print("DUT request putfloat(): ", end="")
            data = ser.read(4)
            data_f32 = struct.unpack(">f", data)[0]
            data_u32 = struct.unpack(">I", data)[0]
            print(f"{data_f32:.2f} (0x{data_u32:08X})")
            
except KeyboardInterrupt:
    pass


ser.close()
