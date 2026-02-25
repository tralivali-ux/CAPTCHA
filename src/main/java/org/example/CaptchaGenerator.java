package org.example;

import java.awt.Color; //Класс для цветов
import java.awt.Font; //Класс для текста
import java.awt.GradientPaint; //Класс для градиента
import java.awt.Graphics2D; //Класс для рисования
import java.awt.geom.AffineTransform; //Класс для трансформаций (масштаб, поворот, сдвиг).
import java.awt.image.BufferedImage; //Класс для изображения в памяти (буфер).
import java.awt.image.ConvolveOp; //Операция свёртки для фильтров (например, размытие).
import java.awt.image.Kernel; //Ядро для ConvolveOp (матрица значений для фильтра).
import java.io.File; //Класс для работы с файлами.
import java.io.IOException; //Исключение для ошибок ввода/вывода.
import java.util.Random; //Генератор случайных чисел.
import javax.imageio.ImageIO; //Класс для чтения/записи изображений (PNG, JPG).

public class CaptchaGenerator {
    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Usage: java CaptchaGenerator <width> <height> <text> <filename>");
            return;
        }


        int width = Integer.parseInt(args[0]);
        int height = Integer.parseInt(args[1]);
        String text = args[2];
        String filename = args[3];
        int length = text.length();
        int tallness = width / length;
        Color[] rainbow = new Color[]{
                Color.RED,
                Color.ORANGE,
                Color.YELLOW,
                Color.GREEN,
                Color.BLUE,
                Color.MAGENTA
        };

        // Создаем изображение
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Фон (градиент для эффекта)
        GradientPaint gp = new GradientPaint(0, 0, Color.gray, width, height, Color.white);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, width, height);

        // Настройки для текста
        Font font = new Font("Serif", Font.BOLD, tallness);
        Random random = new Random();

        // Рисуем каждую букву с эффектами
        int x = 5; // Начальная позиция
        for (int i = 0; i < text.length(); i++) {
            AffineTransform at = new AffineTransform();
            double t = (double) x / width;                        // 0.0 → 1.0 по ширине
            double wave = Math.sin(2 * Math.PI * 2.5 * t + 1.2);  // 2.5 волны на ширину

            // Эффект 1: Поворот (rotate)
            double rotate = random.nextDouble() * 8 - 4; // От -4 до 4 градусов
            at.rotate(Math.toRadians(rotate));

            // Эффект 2: Сдвиг (shear)
            double shear = random.nextDouble() * 0.2 - 0.1; // Легкий сдвиг
            at.shear(shear, 0);

            // небольшое изменение масштаба по вертикали
            double scaleY = 1.0 + 0.12 * wave;
            at.scale(1.0, scaleY);

            // небольшое дополнительное сжатие/растяжение по горизонтали
            double scaleX = 1.0 + 0.08 * Math.cos(2 * Math.PI * 3.2 * t);
            at.scale(scaleX, 1.0);

            // лёгкий дополнительный сдвиг по y от волны
            at.translate(0, 4 * wave);

            // Применяем трансформации
            g2d.setTransform(at);

            //Выбираем цвет
            g2d.setColor(rainbow[random.nextInt(6)]);

            // Рисуем букву
            g2d.setFont(font);
            g2d.drawString(String.valueOf(text.charAt(i)), x, height / 2);

            // Сдвигаем позицию для следующей буквы
            x += width / length;

            // Сбрасываем трансформацию
            g2d.setTransform(new AffineTransform());

            System.out.printf("Буква '%s' рисуется в x=%d\n",
                    text.charAt(i), x);
        }

        // Эффект 3: Наложение линий (для помех)
        g2d.setColor(Color.gray);
        for (int i = 0; i < 5; i++) {
            int x1 = random.nextInt(width);
            int y1 = random.nextInt(height);
            int x2 = random.nextInt(width);
            int y2 = random.nextInt(height);
            g2d.drawLine(x1, y1, x2, y2);
        }

        // Эффект 4: Наложение шума (случайные точки)
        for (int i = 0; i < width * height / 50; i++) { // Примерно 2% пикселей
            int px = random.nextInt(width);
            int py = random.nextInt(height);
            image.setRGB(px, py, new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)).getRGB());
        }

        // Эффект 5: Размытие (blur)
        float[] blurKernel = {
                1/9f, 1/9f, 1/9f,
                1/9f, 1/9f, 1/9f,
                1/9f, 1/9f, 1/9f
        };
        Kernel kernel = new Kernel(3, 3, blurKernel);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        image = op.filter(image, null);

        // Сохраняем файл
        try {
            ImageIO.write(image, "png", new File(filename));
            System.out.println("CAPTCHA generated: " + filename);
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
        }

        g2d.dispose();
    }
}