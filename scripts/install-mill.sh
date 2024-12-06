source ./env.sh

# Install mill
echo "Installing mill..."
echo "Toolchain directory: $TOOLCHAIN_DIR"

mkdir -p $TOOLCHAIN_DIR

curl -L https://github.com/com-lihaoyi/mill/releases/download/0.12.3/0.12.3 > $TOOLCHAIN_DIR/mill && chmod +x $TOOLCHAIN_DIR/mill
export PATH="$TOOLCHAIN_DIR:$PATH"
