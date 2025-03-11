#!/usr/bin/env python3
import argparse
import sys
import struct

def bin_to_hex(input_file, output_file, data_width=32):
    try:
        with open(input_file, "rb") as bin_file, open(output_file, "w") as hex_file:
            word_size = data_width // 32
            counter = 0
            words = []

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
                hex_line = f"{value:08x}"  # 8 characters, no '0x' prefix
                words.append(hex_line)
                counter += 1

                if counter == word_size:
                    words.reverse()
                    hex_file.write("".join(words) + "\n")
                    counter = 0
                    words = []
                
            # add several 0x00 to the end of the file
            for _ in range(32):
                hex_file.write("00\n")
                
    except IOError as e:
        print(f"Error: {e}", file=sys.stderr)
        sys.exit(1)

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("input_file", type=str)
    parser.add_argument("output_file", type=str)
    parser.add_argument("--data_width", type=int, default=32)
    args = parser.parse_args()

    bin_to_hex(args.input_file, args.output_file, args.data_width)
