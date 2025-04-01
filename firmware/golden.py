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
# x:  tensor([[0.1100, 0.2200, 0.3300]])

print("w: ", w)
# w:  tensor([[ 0.1200,  0.5600,  0.9000],
#             [ 0.3400, -0.7800,  1.1200],
#             [ 0.0700,  0.0800,  0.0900],
#             [-0.1100,  0.2200, -0.3300]])

print("b: ", b)
# b:  tensor([-0.5500, -0.6600, -0.7700, -0.8800])

print("y: ", y)
# y:  tensor([[-0.1166, -0.4246, -0.7150, -0.9526]])
