import torch


torch.manual_seed(42.0)

# x = torch.randn(1)
# w = torch.randn(1)
# b = torch.randn(1)

x = torch.tensor([0.1, 0.2])
w = torch.tensor([[0.3, 0.4]])
b = torch.tensor([0.5])

y = w @ x + b

print("x: ", x)
print("w: ", w)
print("b: ", b)
print("y: ", y)