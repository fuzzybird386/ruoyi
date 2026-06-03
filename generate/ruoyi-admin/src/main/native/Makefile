# RK3588 / Linux 本地库构建示例
# 产物: libgpio_signal.so
# 用法:
#   make
#   sudo cp libgpio_signal.so /usr/local/lib/
#   sudo ldconfig

CC ?= gcc
CFLAGS ?= -Wall -Wextra -fPIC -O2
LDFLAGS ?= -shared

TARGET = libgpio_signal.so
SRC = gpio_signal.c

.PHONY: all clean install

all: $(TARGET)

$(TARGET): $(SRC) gpio_signal.h
	$(CC) $(CFLAGS) $(LDFLAGS) -o $@ $(SRC)

install: $(TARGET)
	install -m 755 $(TARGET) /usr/local/lib/
	ldconfig

clean:
	rm -f $(TARGET)
