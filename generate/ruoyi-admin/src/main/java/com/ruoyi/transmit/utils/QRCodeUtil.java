package com.ruoyi.transmit.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 二维码生成工具类
 */
public class QRCodeUtil {
    /**
     * 生成二维码图片字节数组
     * @param content 二维码内容
     * @param width 宽度
     * @param height 高度
     * @return 二维码图片字节数组
     * @throws Exception
     */


    // 默认颜色：黑色
    private static final int BLACK = 0xFF000000;
    // 默认颜色：白色
    private static final int WHITE = 0xFFFFFFFF;

    /**
     * 生成二维码图片
     */
    public static void createCodeToStream(String content, int width, int height,
                                          String format, OutputStream stream) throws Exception {
        BitMatrix bitMatrix = createBitMatrix(content, width, height);
        writeToStream(bitMatrix, format, stream);
    }



    /**
     * 创建BitMatrix
     */
    private static BitMatrix createBitMatrix(String content, int width, int height) throws Exception {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        // 设置字符编码
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        // 设置容错等级
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        // 设置二维码边距
        hints.put(EncodeHintType.MARGIN, 1);

        return new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
    }

    /**
     * 将BitMatrix写入输出流
     */
    private static void writeToStream(BitMatrix matrix, String format, OutputStream stream) throws Exception {
        BufferedImage image = toBufferedImage(matrix);
        if (!ImageIO.write(image, format, stream)) {
            throw new IOException("Could not write an image of format " + format);
        }
    }

    /**
     * 将BitMatrix转换为BufferedImage
     */
    private static BufferedImage toBufferedImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? BLACK : WHITE);
            }
        }
        return image;
    }

    /**
     * 将BitMatrix转换为Base64字符串
     */
    private static String writeToBase64(BitMatrix matrix, String format) throws Exception {
        BufferedImage image = toBufferedImage(matrix);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        byte[] bytes = baos.toByteArray();
        return "data:image/" + format + ";base64," + Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * 生成二维码Base64字符串
     */
    public static String createCodeToBase64(String content, int width, int height,
                                            String format) throws Exception {
        BitMatrix bitMatrix = createBitMatrix(content, width, height);
        return writeToBase64(bitMatrix, format);
    }
}

