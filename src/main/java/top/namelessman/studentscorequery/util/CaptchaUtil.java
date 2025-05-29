// src/main/java/com/example/score/util/CaptchaUtil.java
package top.namelessman.studentscorequery.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

public class CaptchaUtil {
    private static final int WIDTH = 100;
    private static final int HEIGHT = 40;
    private static final Random RANDOM = new Random();
    private static final char[] CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
    private static final int CODE_LENGTH = 4;

    public static Captcha generateCaptcha() throws IOException {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();

        // 设置背景色
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, WIDTH, HEIGHT);

        // 设置字体
        graphics.setFont(new Font("Arial", Font.BOLD, 24));

        // 生成随机验证码
        String captchaCode = generateCode();


        // 绘制验证码
        for (int i = 0; i < CODE_LENGTH; i++) {
            graphics.setColor(getRandomColor());
            graphics.drawString(String.valueOf(captchaCode.charAt(i)), 15 + i * 20, 30);
        }

        // 添加干扰线
        for (int i = 0; i < 3; i++) {
            graphics.setColor(getRandomColor());
            graphics.drawLine(RANDOM.nextInt(WIDTH), RANDOM.nextInt(HEIGHT), RANDOM.nextInt(WIDTH), RANDOM.nextInt(HEIGHT));
        }


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        String base64Image =  Base64.getEncoder().encodeToString(outputStream.toByteArray());
         graphics.dispose();
        return new Captcha(captchaCode, base64Image);

    }


    private static String generateCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARS[RANDOM.nextInt(CHARS.length)]);
        }
        return sb.toString();
    }

    private static Color getRandomColor() {
        return new Color(RANDOM.nextInt(200), RANDOM.nextInt(200), RANDOM.nextInt(200));
    }


     public static class Captcha{
        private String code;
        private String base64Image;

         public Captcha(String code, String base64Image) {
             this.code = code;
             this.base64Image = base64Image;
         }

         public String getCode() {
             return code;
         }

         public String getBase64Image() {
             return base64Image;
         }
     }
}