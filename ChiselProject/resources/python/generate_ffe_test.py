import numpy as np
from scipy import signal


np.set_printoptions(precision=3)


weights = np.array([ 0.00518536,  0.02622284,  0.113615,    0.58174452, -0.58174452, -0.113615, -0.02622284, -0.00518536])

s = 2**7


time_series = np.array(range(0, 10))

tb_in = np.zeros_like(time_series, dtype=np.float32)
tb_in[0] = 0.99
tb_in[1] = 0.5

print(tb_in)


# simulate FIR filter
tb_out = signal.lfilter(weights, 1, tb_in)

print(tb_out)



q_weights = np.round(weights * s).astype(np.int32)
q_tb_in = np.round(tb_in * s).astype(np.int32)
q_tb_out = signal.lfilter(q_weights.astype(np.float32), 1, q_tb_in.astype(np.float32)).astype(np.int32)
# qq_tb_out = np.round(tb_out * s**2).astype(np.int32)

assert np.all(q_weights < 2**8), "weights overflow"
assert np.all(q_weights > -2**8), "weights underflow"
assert np.all(q_tb_in < 2**8), "input overflow"
assert np.all(q_tb_in > -2**8), "input underflow"
assert np.all(q_tb_out < 2**18), "output overflow"
assert np.all(q_tb_out > -2**18), "output underflow"

# print("quantized weights:", q_weights, "\n\t(", weights*s, ")")
# # [   5   27  116  596 -596 -116  -27   -5]

# print("quantized input:", q_tb_in, "\n\t(", tb_in*s, ")")
# # [1024    0    0    0    0    0    0    0    0    0]

# print("expected output:", q_tb_out, "\n\t(", tb_out*s**2, ")")
# # [   5   27  116  596 -596 -116  -27   -5    0    0]

print("  logic [7:0] input_sequence[10] = {", end="")
print(", ".join([f"{'+' if q_tb_in[i] > 0 else '-'}8'd{abs(q_tb_in[i])}" for i in range(len(q_tb_in))]), end="")
print("};")

print("  logic [17:0] expected_output[10] = {", end="")
print(", ".join([f"{'+' if q_tb_out[i] > 0 else '-'}18'd{abs(q_tb_out[i])}" for i in range(len(q_tb_out))]), end="")
print("};")

