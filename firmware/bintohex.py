#!/usr/bin/env python3
import argparse
import sys
import struct
from typing import BinaryIO, TextIO

def bin_to_hex(input_file: str, output_file: str, data_width: int = 32, endianness: str = "little", padding_lines: int = 32) -> None:
    """
    Convert a binary file to hexadecimal format.
    
    Args:
        input_file: Path to the input binary file
        output_file: Path to the output hex file
        data_width: Width of data bus in bits (default: 32)
        endianness: Byte order ('little' or 'big', default: 'little')
        padding_lines: Number of padding lines to add at the end (default: 32)
    """
    try:
        with open(input_file, "rb") as bin_file, open(output_file, "w") as hex_file:
            process_binary_file(bin_file, hex_file, data_width, endianness, padding_lines)
    except IOError as e:
        print(f"Error: {e}", file=sys.stderr)
        sys.exit(1)

def process_binary_file(bin_file: BinaryIO, hex_file: TextIO, data_width: int, endianness: str, padding_lines: int) -> None:
    """Process the binary file and write hex output."""
    word_size = data_width // 8  # Convert bits to bytes
    words_per_line = word_size // 4  # Number of 32-bit words per line
    
    # Read the entire file
    buffer = []

    if data_width == 8:
        while True:
            word = bin_file.read(1)
            if not word:
                break
            byte = int.from_bytes(word, "little")
            hex_line = f"{byte:02x}"
            hex_file.write(hex_line + "\n")
        return
    
    while True:
        # Read 4 bytes (32 bits) at a time
        word = bin_file.read(4)
        if not word:
            break
        
        # Pad with zeros if less than 4 bytes
        if len(word) < 4:
            word = word + b'\x00' * (4 - len(word))
        
        # Convert to 32-bit integer and format as hex
        format_char = "<I" if endianness == "little" else ">I"
        value = struct.unpack(format_char, word)[0]
        hex_line = f"{value:08x}"  # 8 characters, no '0x' prefix
        buffer.append(hex_line)
    
    # Process the buffer in chunks of words_per_line
    for i in range(0, len(buffer), words_per_line):
        chunk = buffer[i:i+words_per_line]
        if endianness == "little":
            chunk.reverse()  # Reverse only if little-endian
        hex_file.write("".join(chunk) + "\n")
    
    # Add padding lines at the end
    for _ in range(padding_lines):
        hex_file.write("00\n")  # Use consistent 8-digit format

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Convert binary file to hexadecimal format")
    parser.add_argument("input_file", type=str, help="Input binary file")
    parser.add_argument("output_file", type=str, help="Output hex file")
    parser.add_argument("--data_width", type=int, default=32, help="Data width in bits (default: 32)")
    parser.add_argument("--endianness", type=str, default="little", choices=["little", "big"], 
                        help="Byte order (little or big, default: little)")
    parser.add_argument("--padding_lines", type=int, default=32, 
                        help="Number of extra zero paddings to add at the end (default: 32)")
    
    args = parser.parse_args()
    bin_to_hex(args.input_file, args.output_file, args.data_width, args.endianness, args.padding_lines)
