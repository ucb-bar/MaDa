# add to mem
li x1, 0x08000000 # dmem address
li x2, 0x10000000 # gpio address
li x3, 0
li x4, 0
li x5, 2000000    # loop counter
sw x0, 0(x1)

loop:
wait:
# increment from dmem
lw x3, 0(x1)
addi x3, x3, 1
sw x3, 0(x1)
bne x3, x5, wait
sw x0, 0(x1)

# toggle GPIO
xori x4, x4, 1
sw x4, 0(x2)
j loop

# safety zeros
nop
nop
nop
nop
nop
nop
nop
nop
nop
nop
nop
nop
nop
nop
nop
nop
nop
nop
nop
nop
