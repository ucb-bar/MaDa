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

def hex_to_coe(input_file: str):
    output_file = input_file.replace(".hex", ".coe")
    with open(input_file, "r") as hex_file, open(output_file, "w") as coe_file:
        coe_file.write("memory_initialization_radix=16;\n")
        coe_file.write("memory_initialization_vector=\n")
        for line in hex_file:
            coe_file.write(line.strip() + ",\n")
        coe_file.write(";\n")

def process_binary_file(bin_file: BinaryIO, hex_file: TextIO, data_width: int, endianness: str, padding_lines: int) -> None:
    """Process the binary file and write hex output.
    
    Args:
        bin_file: Binary file object to read from
        hex_file: Text file object to write hex output to
        data_width: Width of data bus in bits
        endianness: Byte order ('little' or 'big')
        padding_lines: Number of padding lines to add at the end
    """
    bytes_per_word = data_width // 8
    format_char = "<" if endianness == "little" else ">"
    
    # Calculate hex format string based on data width
    hex_format = f"{{:0{data_width // 4}x}}"

    def read_large_word(num_bytes: int) -> int:
        """Helper function to read and combine large words."""
        if num_bytes <= 8:  # Up to 64 bits
            if num_bytes <= 1:
                struct_format = f"{format_char}B"
            elif num_bytes <= 2:
                struct_format = f"{format_char}H"
            elif num_bytes <= 4:
                struct_format = f"{format_char}I"
            else:
                struct_format = f"{format_char}Q"
            
            word_bytes = bin_file.read(num_bytes)
            if not word_bytes:
                return None
            if len(word_bytes) < num_bytes:
                word_bytes = word_bytes + b'\x00' * (num_bytes - len(word_bytes))
            return struct.unpack(struct_format, word_bytes)[0]
        
        # For larger widths, read in 64-bit chunks and combine
        result = 0
        bytes_read = 0
        while bytes_read < num_bytes:
            chunk_size = min(8, num_bytes - bytes_read)
            chunk = read_large_word(chunk_size)
            if chunk is None:
                return None
            
            if endianness == "little":
                result |= chunk << (bytes_read * 8)
            else:
                result = (result << (chunk_size * 8)) | chunk
            
            bytes_read += chunk_size
        
        return result

    while True:
        value = read_large_word(bytes_per_word)
        if value is None:
            break
            
        # Write the hex value
        hex_file.write(hex_format.format(value) + "\n")
    
    # Add padding lines at the end
    for _ in range(padding_lines):
        hex_file.write("0" * (data_width // 4) + "\n")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Convert binary file to hexadecimal format")
    parser.add_argument("input_file", type=str, help="Input binary file")
    parser.add_argument("output_file", type=str, help="Output hex file")
    parser.add_argument("--data_width", type=int, default=32, help="Data width in bits (default: 32)")
    parser.add_argument("--endianness", type=str, default="little", choices=["little", "big"], 
                        help="Byte order (little or big, default: little)")
    parser.add_argument("--padding_lines", type=int, default=10,
                        help="Number of extra zero paddings to add at the end (default: 10)")
    
    args = parser.parse_args()
    bin_to_hex(args.input_file, args.output_file, args.data_width, args.endianness, args.padding_lines)
    hex_to_coe(args.output_file)
