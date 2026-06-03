#include "gpio_signal.h"

#include <stdio.h>

static int g_pin = -1;
static int g_initialized = 0;

int gpio_signal_init(int pin)
{
    /*
     * TODO: 在此实现 RK3588 GPIO 导出、方向设置、中断/轮询准备。
     * 例如：open /sys/class/gpio/gpioN/value 或 devmem/ioctl。
     */
    g_pin = pin;
    g_initialized = 1;
    fprintf(stderr, "[gpio_signal] stub init pin=%d\n", pin);
    return 0;
}

int gpio_signal_read(void)
{
    if (!g_initialized)
    {
        return -1;
    }

    /*
     * TODO: 读取引脚电平并判断是否为“扫描成功”。
     * 当前桩实现始终返回空闲(0)，便于 Java 侧联调框架。
     */
    (void)g_pin;
    return 0;
}

void gpio_signal_shutdown(void)
{
    /*
     * TODO: 关闭文件描述符、取消导出 GPIO 等。
     */
    g_initialized = 0;
    g_pin = -1;
    fprintf(stderr, "[gpio_signal] stub shutdown\n");
}
