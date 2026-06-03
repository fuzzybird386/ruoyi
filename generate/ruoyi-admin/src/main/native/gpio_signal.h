/**
 * 扫描成功 GPIO 信号 C 接口（由 Java JNA 调用）。
 *
 * 返回值约定：
 *   0  = 空闲 / 无成功信号
 *   1  = 扫描成功
 *  -1  = 错误
 */
#ifndef GPIO_SIGNAL_H
#define GPIO_SIGNAL_H

#ifdef __cplusplus
extern "C" {
#endif

/**
 * 初始化 GPIO。
 * @param pin 引脚编号（平台相关，如 RK3588 GPIO 编号）
 * @return 0 成功，非 0 失败
 */
int gpio_signal_init(int pin);

/**
 * 读取当前扫描成功信号。
 * @return 0=空闲，1=成功，-1=错误
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
