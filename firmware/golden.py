import torch


torch.manual_seed(42.0)

# x = torch.randn(1)
# w = torch.randn(1)
# b = torch.randn(1)

x = torch.tensor([
    [0.11, 0.22, 0.33],
])
w = torch.tensor([
    [0.12,  0.34, 0.07, -0.11],
    [0.56, -0.78, 0.08,  0.22],
    [0.90,  1.12, 0.09, -0.33],
]).T

b = torch.tensor([-0.55, -0.66, -0.77, -0.88])

in_features = 3
out_features = 4

# x = x[:, :in_features]
# w = w[:, :in_features]



y = x @ w.T + b

print("x: ", x)
print("w: ", w)
print("b: ", b)
print("y: ", y)
