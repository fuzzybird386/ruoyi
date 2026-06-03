# RK3588 SENSOR_T (GPIO3_A3) — libgpio_signal.so
# 板级：/dev/gpiochip3 line 3，输入，上拉，触发为低
#
#   make
#   sudo cp libgpio_signal.so /usr/local/lib/
#   sudo ldconfig
#
# 测试读电平（需 root 或 gpio 组）:
#   gpioget gpiochip3 3

CC ?= gcc
CFLAGS ?= -Wall -Wextra -fPIC -O2
LDFLAGS ?= -shared

TARGET = libgpio_signal.so
SRC = gpio_signal.c
TEST_BIN = gpio_signal_test

.PHONY: all clean install test

all: $(TARGET)

$(TARGET): $(SRC) gpio_signal.h
	$(CC) $(CFLAGS) $(LDFLAGS) -o $@ $(SRC)

$(TEST_BIN): $(SRC) gpio_signal.h
	$(CC) $(CFLAGS) -DGPIO_SIGNAL_TEST_MAIN -o $@ $(SRC)

test: $(TEST_BIN)
	./$(TEST_BIN)

install: $(TARGET)
	install -m 755 $(TARGET) /usr/local/lib/
	ldconfig

clean:
	rm -f $(TARGET) $(TEST_BIN)
