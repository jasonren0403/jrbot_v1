package me.cqp.JRbot.Utils.misc;


import sun.font.FontDesignMetrics;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;


public class picutils {

    public static int getWordWidth(Font font, short content){
        return getWordWidth(font,String.valueOf(content));
    }

    public static int getWordWidth(Font font, long content){
        return getWordWidth(font,String.valueOf(content));
    }

    public static int getWordWidth(Font font, String content) {
        FontDesignMetrics metrics = FontDesignMetrics.getMetrics(font);

        int width = 0;
        for (int i = 0; i < content.length(); i++) {
            width += metrics.charWidth(content.charAt(i));
        }
        return width;
    }

    public static int getWordHeight(Font font,short content){
        return getWordHeight(font,String.valueOf(content));
    }

    public static int getWordHeight(Font font,long content){
        return getWordHeight(font,String.valueOf(content));
    }

    public static int getWordHeight(Font font, String content){
        FontDesignMetrics metrics = FontDesignMetrics.getMetrics(font);
        return metrics.getHeight();
    }

    public static BufferedImage coverImage(String baseFilePath, String coverFilePath, int x, int y, int width, int height) throws Exception{

        File baseFile = new File(baseFilePath);//底图
        BufferedImage buffImg = ImageIO.read(baseFile);

        File coverFile = new File(coverFilePath); //覆盖层
        BufferedImage coverImg = ImageIO.read(coverFile);

        return coverImage(buffImg, coverImg, x, y, width, height);
    }

    /**
     * 图片覆盖（覆盖图压缩到width*height大小，覆盖到底图上）
     *
     * @param baseBufferedImage  底图
     * @param coverBufferedImage 覆盖图
     * @param x                  起始x轴
     * @param y                  起始y轴
     * @param width              覆盖宽度
     * @param height             覆盖长度
     * @return
     * @throws Exception
     */
    public static BufferedImage coverImage(BufferedImage baseBufferedImage, BufferedImage coverBufferedImage, int x, int y, int width, int height) throws Exception {

        // 创建Graphics2D对象，用在底图对象上绘图
        Graphics2D g2d = baseBufferedImage.createGraphics();

        // 绘制
        g2d.drawImage(coverBufferedImage, x, y, width, height, null);
        g2d.dispose();// 释放图形上下文使用的系统资源

        return baseBufferedImage;
    }

    public static BufferedImage zoomImageTo(BufferedImage src,int height,int width){
        BufferedImage result = null;
        try {
            /* 调整后的图片的宽度和高度 */
            int toWidth = width;
            int toHeight = height;

            /* 新生成结果图片 */
            result = new BufferedImage(toWidth, toHeight, BufferedImage.TYPE_INT_ARGB);

            result.getGraphics().drawImage(src.getScaledInstance(toWidth, toHeight, java.awt.Image.SCALE_SMOOTH), 0, 0,
                    null);

        } catch (Exception e) {
            System.out.println("创建缩略图发生异常" + e.getMessage());
        }

        return result;
    }

    public static BufferedImage zoomImage(BufferedImage src,float resizeTimes) {
        BufferedImage result = null;
        try {
            /* 原始图像的宽度和高度 */
            int width = src.getWidth();
            int height = src.getHeight();

            /* 调整后的图片的宽度和高度 */
            int imgWidth = (int) (width * resizeTimes);
            int imgHeight = (int) (height * resizeTimes);

            /* 新生成结果图片 */
            //构建新的图片
            BufferedImage resizedImg = new BufferedImage(imgWidth,imgHeight,BufferedImage.TYPE_INT_RGB);
            //将原图放大或缩小后画下来:并且保持png图片放大或缩小后背景色是透明的而不是黑色
            Graphics2D resizedG = resizedImg.createGraphics();
            resizedImg = resizedG.getDeviceConfiguration().createCompatibleImage(imgWidth,imgHeight,Transparency.TRANSLUCENT);
            resizedG.dispose();
            resizedG = resizedImg.createGraphics();
            Image from = src.getScaledInstance(imgWidth, imgHeight, Image.SCALE_AREA_AVERAGING);
            resizedG.drawImage(from, 0, 0, null);
            resizedG.dispose();

            return resizedImg;

        } catch (Exception e) {
            System.out.println("创建缩略图发生异常" + e.getMessage());
        }

        return result;
    }


    public static void drawCenteredString(Graphics g, String text, Rectangle rect, Font font) {
        // Get the FontMetrics
        FontMetrics metrics = g.getFontMetrics(font);
        // Determine the X coordinate for the text
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        // Set the font
        g.setFont(font);
        // Draw the String
        g.drawString(text, x, y);
    }

}

