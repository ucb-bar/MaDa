import torch


torch.manual_seed(42)

torch.set_printoptions(precision=3)

in_features = 83
out_features = 8

class ToyMlpPolicy(torch.nn.Module):
     def __init__(self, in_features, out_features):
         super().__init__()
         self.lin1 = torch.nn.Linear(in_features, 32)
         self.lin2 = torch.nn.Linear(32, 16)
         self.lin3 = torch.nn.Linear(16, out_features)
 
     def forward(self, x):
         x = self.lin1(x)
         x = torch.nn.functional.relu(x)
         x = self.lin2(x)
         x = torch.nn.functional.relu(x)
         x = self.lin3(x)
         return x

class MlpPolicy(torch.nn.Module):
    def __init__(self, in_features, out_features):
        super().__init__()
        self.lin1 = torch.nn.Linear(in_features, 512)
        self.lin2 = torch.nn.Linear(512, 256)
        self.lin3 = torch.nn.Linear(256, out_features)

    def forward(self, x):
        x = self.lin1(x)
        x = torch.nn.functional.relu(x)
        print("v: ", x)
        x = self.lin2(x)
        x = torch.nn.functional.relu(x)
        print("u: ", x)
        x = self.lin3(x)
        return x


def generate_weight_binary(tensors: list[torch.Tensor]) -> str:
    flat_tensors = []
    for tensor in tensors:
        if tensor.dim() == 2:
            # transpose weight matrix
            tensor = tensor.T
        tensor = tensor.detach().cpu().flatten()

        assert tensor.shape[0] % 8 == 0, tensor.shape[0]

        flat_tensors.append(tensor)

    offset = 0
    for tensor in flat_tensors:
        print("offset: ", offset, "shape: ", tensor.shape)
        offset += tensor.shape[0]

    weight_arr = torch.cat(flat_tensors, dim=-1)

    bin_weight = weight_arr.numpy().tobytes()

    with open("weights.bin", "wb") as f:
        f.write(bin_weight)

    return bin_weight




x = torch.rand((1, in_features)) - 0.5


model = MlpPolicy(in_features, out_features)

print("x: ", x)
print("w: ", model.lin1.weight.T)
print("b: ", model.lin1.bias)

y = model.forward(x)

print("================")
print("Expected golden output y:")
print(y.detach())
print("================")

generate_weight_binary([
    model.lin1.weight,
    model.lin1.bias,
    model.lin2.weight,
    model.lin2.bias,
    model.lin3.weight,
    model.lin3.bias,
])


# for i, elem in enumerate(x.flatten()):
#     print(f"write_f32(x_tensor + {i}, {elem.item():.8f});")

# print([hex(x) for x in data])
