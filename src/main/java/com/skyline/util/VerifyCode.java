package com.skyline.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import javax.imageio.ImageIO;

public class VerifyCode {

    private int w = 50;
    private int h = 25;
    private Random r = new Random();
    private String[] fontNames = { "Monospaced"};
    private String codes = "23456789abcdefghjkmnpqrstuvwxyzABCEFGHIJKLMNPQRSTUVWXYZ";
    private Color bgColor = new Color(255, 255, 255);
    private String text;

    private Color randomColor() {
        int red = r.nextInt(200);
        int green = r.nextInt(200);
        int blue = r.nextInt(200);
        return new Color(red, green, blue);

    }

    private Font randomFont() {
        int index = r.nextInt(fontNames.length);
        String fontName = fontNames[index];
        int style = r.nextInt(4);
        int size = r.nextInt(6) + 15;
        return new Font(fontName, style, size);
    }

    private void drawLine(BufferedImage image) {
        int num = 3;
        Graphics2D g2 = (Graphics2D) image.getGraphics();
        for (int i = 0; i < num; i++) {
            int y1 = r.nextInt(h);
            int y2 = r.nextInt(h);
            g2.setStroke(new BasicStroke(1.0F));
            g2.setColor(Color.BLUE);
            g2.drawLine(0, y1, 50, y2);
        }
    }
    private char randomChar() {
        int index = r.nextInt(codes.length());
        return codes.charAt(index);
    }

    private BufferedImage createImage() {
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = (Graphics2D) image.getGraphics();
        g2.setColor(this.bgColor);
        g2.fillRect(0, 0, w, h);
        return image;
    }

    /***
     * 获取图片
     * @return 图片数据
     */
    public BufferedImage getImage() {
        BufferedImage image = createImage();
        Graphics2D g2 = (Graphics2D) image.getGraphics();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            String s = randomChar() + "";
            sb.append(s);
            float x = i * 1.0F * w / 4;
            g2.setFont(randomFont());
            g2.setColor(randomColor());
            g2.drawString(s, x, h - 5);
        }
        this.text = sb.toString();
        drawLine(image);
        return image;
    }

    /***
     * 获取图片的文本
     * @return
     */
    public String getText() {
        return text;
    }

    public static void output(BufferedImage image, OutputStream out) throws IOException {
        ImageIO.write(image, "JPEG", out);
    }
}