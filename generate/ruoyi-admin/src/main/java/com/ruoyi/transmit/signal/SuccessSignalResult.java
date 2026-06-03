package com.ruoyi.transmit.signal;

/**
 * 扫描端成功信号检测结果。
 */
public enum SuccessSignalResult
{
    /** 无信号 / 空闲 */
    NONE(0),

    /** 扫描成功 */
    SUCCESS(1),

    /** 硬件或驱动错误 */
    ERROR(-1);

    private final int nativeCode;

    SuccessSignalResult(int nativeCode)
    {
        this.nativeCode = nativeCode;
    }

    public int getNativeCode()
    {
        return nativeCode;
    }

    public static SuccessSignalResult fromNativeCode(int code)
    {
        for (SuccessSignalResult result : values())
        {
            if (result.nativeCode == code)
            {
                return result;
            }
        }
        return ERROR;
    }
}
