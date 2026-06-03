/**
 * 扫描成功 GPIO 信号 C 接口（由 Java JNA 调用）。
 *
 * RK3588 CN2 SENSOR_T = GPIO3_A3 = /dev/gpiochip3 line 3
 * 输入、板上拉，触发时为低电平。
 *
 * 返回值约定：
 *   0  = 空闲（引脚为高）
 *   1  = 扫描成功（引脚为低，触发态）
 *  -1  = 错误
 */
#ifndef GPIO_SIGNAL_H
#define GPIO_SIGNAL_H

#ifdef __cplusplus
extern "C" {
#endif

/** SENSOR_T 默认 line（GPIO3_A3 @ gpiochip3） */
#define GPIO_SIGNAL_SENSOR_T_LINE 3

/**
 * 初始化 GPIO。
 * @param pin gpiochip3 上的 line 编号；<=0 时使用 GPIO_SIGNAL_SENSOR_T_LINE(3)
 * @return 0 成功，非 0 失败
 */
int gpio_signal_init(int pin);

/**
 * 读取当前扫描成功信号。
 * @return 0=空闲，1=成功(低电平)，-1=错误
 */
int gpio_signal_read(void);

/**
 * 释放 GPIO 资源。
 */
void gpio_signal_shutdown(void);

#ifdef __cplusplus
}
#endif

#endif /* GPIO_SIGNAL_H */
