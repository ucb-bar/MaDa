# MaDa

## Getting Started

This project uses Mill as the Scala build tool. A ready-to-run script is provided as `/toolchains/mill`. To invoke mill directly, do

```bash
./toolchains/mill
```

The directory structure is organized in standalone packages. When running build flow, both the package name and the config name need to be provided:

```bash
make verilog PACKAGE=delta-soc MODULE=delta.MlpPolicyRunner
```

## Build Bitstream

```bash
make bitstream CONFIG=MinimalArty100T
```

## TODOs

- [ ] Better way of doing clock domain
- [ ] Fix Chipyard IP

