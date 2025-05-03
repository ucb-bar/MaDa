import struct

# Hex values to convert
hex_values = [
    0.12, 0.34, 0.07,
    -0.11, 0.56, -0.78,
    0.08, 0.22, 0.90,
    1.12, 0.09, -0.33]
# hex_values = [
#     0.1, 0.2, 0.3,
#     0.4, 0.5, 0.6,
#     0.7, 0.8, 0.9,
#     1.0, 1.1, 1.2]

# Open file in binary write mode
with open("weights.bin", "wb") as f:
    # Pack each value as a 32-bit float (assuming these are float values based on their format)
    for value in hex_values:
        # Convert hex to float bytes and write
        f.write(struct.pack("<f", value))