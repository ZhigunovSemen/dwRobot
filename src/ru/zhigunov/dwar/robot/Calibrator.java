package ru.zhigunov.dwar.robot;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Calibrator {

    public static int screenWidth;
    public static int screenHeight;

    public static BufferedImage calibrateImage;
    public static int calibrateImageWidth;
    public static int calibrateImageHeight;
    public static Color samplePoint1;
    public static Color samplePoint2;
    public static Color samplePoint3;
    public static Color samplePoint4;
    public static int deviationPercent = 10;

    public static void loadSample() {
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        screenWidth = gd.getDisplayMode().getWidth();
        screenHeight = gd.getDisplayMode().getHeight();

        if (null == calibrateImage) {
            try {
                calibrateImage = ImageIO.read(new File("sample.bmp"));
                calibrateImageWidth = calibrateImage.getWidth();
                calibrateImageHeight = calibrateImage.getHeight();

                samplePoint1 = new Color(calibrateImage.getRGB(0, 0));
                samplePoint2 = new Color(calibrateImage.getRGB(calibrateImageWidth-1, 0));
                samplePoint3 = new Color(calibrateImage.getRGB(0, calibrateImageHeight-1));
                samplePoint4 = new Color(calibrateImage.getRGB(calibrateImageWidth-1, calibrateImageHeight-1));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Point Calibrate() throws Exception {
        loadSample();
        if (null == calibrateImage) throw new Exception("В директории отсутствует файл sample.bmp!");
        Robot robot = new Robot();
        BufferedImage screenRectangle = robot.createScreenCapture(
                new Rectangle(0,0,
                screenWidth - calibrateImageWidth,
                screenHeight - calibrateImageHeight)
        );
        for (int x = 0; x < (screenRectangle.getWidth() - calibrateImageWidth); x++) {
            for (int y = 0; y < (screenRectangle.getHeight() - calibrateImageHeight); y++) {
                try {
                    Color testPoint1 = new Color(screenRectangle.getRGB(x, y));
                    Color testPoint2 = new Color(screenRectangle.getRGB(x + calibrateImageWidth-1, y));
                    Color testPoint3 = new Color(screenRectangle.getRGB(x, y + calibrateImageHeight-1));
                    Color testPoint4 = new Color(screenRectangle.getRGB(x + calibrateImageWidth-1, y + calibrateImageHeight-1));

                    if (   pointRGBEqualsWithDeviation(testPoint1, samplePoint1, deviationPercent)
                        && pointRGBEqualsWithDeviation(testPoint2, samplePoint2, deviationPercent)
                        && pointRGBEqualsWithDeviation(testPoint3, samplePoint3, deviationPercent)
                        && pointRGBEqualsWithDeviation(testPoint4, samplePoint4, deviationPercent)) {

                        BufferedImage testImage2 = robot.createScreenCapture(new Rectangle(x,y,
                                calibrateImageWidth,
                                calibrateImageHeight)
                        );
                        if (bufferedImagesEqual(testImage2, calibrateImage)) {
                            return new Point(x,y);
                        }
                    }
                } catch(Exception ex) {
                    ex.printStackTrace();
                }

            }
        }
        BufferedImage test2 = robot.createScreenCapture(new Rectangle(0,0,
                screenWidth - calibrateImageWidth,
                screenHeight - calibrateImageHeight)
        );
        ImageIO.write(test2, "bmp", new File("test2.bmp"));
        throw new Exception("Не найдено подходящее изображение. Калибровка невозможна.");
    }

    /**
     * Сравнивает два цвета по величине отклонения каждого потока R/G/B
     * @param pointColor1
     * @param pointColor2
     * @param percent максимальная погрешность в процентах по каждому потоку R/G/B
     * @return
     */
    public static boolean pointRGBEqualsWithDeviation(Color pointColor1, Color pointColor2, int percent) {
        int deviationRed = Math.abs(pointColor1.getRed() - pointColor2.getRed()) * 100 / 255;
        if (deviationRed > percent) {
            return false;
        }
        int deviationBlue = Math.abs(pointColor1.getBlue() - pointColor2.getBlue()) * 100 / 255;
        if (deviationBlue > percent) {
            return false;
        }
        int deviationGreen = Math.abs(pointColor1.getGreen() - pointColor2.getGreen()) * 100 / 255;
        if (deviationGreen > percent) {
            return false;
        }
        return true;
    }


    public static boolean bufferedImagesEqual(BufferedImage img1, BufferedImage img2) {
        if (img1.getWidth() == img2.getWidth() && img1.getHeight() == img2.getHeight()) {
            for (int x = 0; x < img1.getWidth(); x++) {
                for (int y = 0; y < img1.getHeight(); y++) {
                    if (!pointRGBEqualsWithDeviation(new Color(img1.getRGB(x, y)), new Color(img2.getRGB(x, y)), deviationPercent))
                        return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }



}
