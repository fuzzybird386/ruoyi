#include "gpio_signal.h"

#include <errno.h>
#include <fcntl.h>
#include <linux/gpio.h>
#include <stdio.h>
#include <string.h>
#include <sys/ioctl.h>
#include <unistd.h>

#ifndef GPIO_SIGNAL_CHIP_PATH
#define GPIO_SIGNAL_CHIP_PATH "/dev/gpiochip3"
#endif

static int g_pin = -1;
static int g_line_fd = -1;
static int g_initialized = 0;

static void close_line_fd(void)
{
    if (g_line_fd >= 0)
    {
        close(g_line_fd);
        g_line_fd = -1;
    }
}

int gpio_signal_init(int pin)
{
    int line = (pin > 0) ? pin : GPIO_SIGNAL_SENSOR_T_LINE;
    int chip_fd;
    struct gpiohandle_request req;

    if (g_initialized)
    {
        gpio_signal_shutdown();
    }

    chip_fd = open(GPIO_SIGNAL_CHIP_PATH, O_RDWR);
    if (chip_fd < 0)
    {
        fprintf(stderr, "[gpio_signal] open %s failed: %s\n",
                GPIO_SIGNAL_CHIP_PATH, strerror(errno));
        return -1;
    }

    memset(&req, 0, sizeof(req));
    req.lineoffsets[0] = line;
    req.lines = 1;
    req.flags = GPIOHANDLE_REQUEST_INPUT;
    strncpy(req.consumer_label, "gpio_signal", sizeof(req.consumer_label) - 1);

    if (ioctl(chip_fd, GPIO_GET_LINEHANDLE_IOCTL, &req) < 0)
    {
        fprintf(stderr, "[gpio_signal] GPIO_GET_LINEHANDLE_IOCTL chip=%s line=%d: %s\n",
                GPIO_SIGNAL_CHIP_PATH, line, strerror(errno));
        close(chip_fd);
        return -1;
    }

    close(chip_fd);

    g_line_fd = req.fd;
    g_pin = line;
    g_initialized = 1;

    fprintf(stderr, "[gpio_signal] init ok: %s line=%d (SENSOR_T active-low)\n",
            GPIO_SIGNAL_CHIP_PATH, line);
    return 0;
}

int gpio_signal_read(void)
{
    struct gpiohandle_data data;

    if (!g_initialized || g_line_fd < 0)
    {
        return -1;
    }

    memset(&data, 0, sizeof(data));
    if (ioctl(g_line_fd, GPIOHANDLE_GET_LINE_VALUES_IOCTL, &data) < 0)
    {
        fprintf(stderr, "[gpio_signal] read line=%d failed: %s\n",
                g_pin, strerror(errno));
        return -1;
    }

    /*
     * SENSOR_T：板上拉，常态高；扫描/触发时拉低。
     * 低电平 -> 返回 1（成功）；高电平 -> 返回 0（空闲）。
     */
    if (data.values[0] == 0)
    {
        return 1;
    }

    return 0;
}

void gpio_signal_shutdown(void)
{
    close_line_fd();
    g_initialized = 0;
    g_pin = -1;
    fprintf(stderr, "[gpio_signal] shutdown\n");
}

#ifdef GPIO_SIGNAL_TEST_MAIN
#include <stdlib.h>

int main(int argc, char *argv[])
{
    int pin = (argc > 1) ? atoi(argv[1]) : 0;
    int i;

    if (gpio_signal_init(pin) != 0)
    {
        return 1;
    }

    for (i = 0; i < 10; i++)
    {
        int v = gpio_signal_read();
        printf("read[%d] = %d (%s)\n", i, v,
               v == 1 ? "SUCCESS/low" : (v == 0 ? "IDLE/high" : "ERROR"));
        sleep(1);
    }

    gpio_signal_shutdown();
    return 0;
}
#endif
