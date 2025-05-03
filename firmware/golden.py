import torch


torch.manual_seed(42)

torch.set_printoptions(precision=3)

in_features = 3
out_features = 4

x = torch.rand((1, in_features)) - 0.5
# w = torch.rand((out_features, in_features)) - 0.5
# b = torch.rand((1, out_features)) - 0.5

# x = torch.tensor([
#     [0.11, 0.22, 0.33],
# ])
# w = torch.tensor([
#     [0.12,  0.34, 0.07, -0.11],
#     [0.56, -0.78, 0.08,  0.22],
#     [0.90,  1.12, 0.09, -0.33],
# ])
# b = torch.tensor([-0.55, -0.66, -0.77, -0.88])

lin = torch.nn.Linear(in_features, out_features)

w = lin.weight
b = lin.bias


def generate_weight_binary(tensors: list[torch.Tensor]) -> str:
    flat_tensors = []
    for tensor in tensors:
        if tensor.dim() == 2:
            # transpose weight matrix
            flat_tensors.append(tensor.T.cpu().flatten())
        else:
            flat_tensors.append(tensor.cpu().flatten())

    offset = 0
    for tensor in flat_tensors:
        print("offset: ", offset, "shape: ", tensor.shape)
        offset += tensor.shape[0]

    weight_arr = torch.cat(flat_tensors, dim=-1)

    bin_weight = weight_arr.numpy().tobytes()

    with open("weights.bin", "wb") as f:
        f.write(bin_weight)

    return bin_weight




# x = x[:, :in_features]
# w = w[:, :in_features]

print("x: ", x)
print("w: ", w.T)
print("b: ", b)

y = torch.nn.functional.linear(x, w, b)

print("_y: ", y)

y = torch.nn.functional.relu(y)

print("y: ", y)

generate_weight_binary([w, b])


# print([hex(x) for x in data])
