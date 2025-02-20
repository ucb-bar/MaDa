#!/usr/bin/env python3

import sys
import struct

def bin_to_hex(input_file, output_file):
    try:
        with open(input_file, "rb") as bin_file, open(output_file, "w") as hex_file:
            while True:
                # Read 4 bytes (32 bits) at a time
                word = bin_file.read(4)
                if not word:
                    break
                
                # Pad with zeros if less than 4 bytes
                if len(word) < 4:
                    word = word + b'\x00' * (4 - len(word))
                
                # Convert to 32-bit integer and format as hex
                value = struct.unpack("<I", word)[0]  # Little-endian
                hex_line = f"{value:08x}\n"  # 8 characters, no '0x' prefix
                hex_file.write(hex_line)
                
    except IOError as e:
        print(f"Error: {e}", file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python bintohex.py input.bin output.hex", file=sys.stderr)
        sys.exit(1)
    
    input_file = sys.argv[1]
    output_file = sys.argv[2]
    bin_to_hex(input_file, output_file)
