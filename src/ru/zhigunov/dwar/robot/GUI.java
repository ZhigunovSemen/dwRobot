package ru.zhigunov.dwar.robot;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class GUI extends JFrame implements KeyListener {
    static final String newline = System.getProperty("line.separator");
    static final Logger log = new Logger();
    static Random rand = new Random();
    private static ArrayList<Timer> timersLists = new ArrayList<>();
    private static Timer timerQ;
    private static Timer timer123456;
    private static Timer timerAnalyzerHPAndMana;
    private static Timer timerColorTester;
    private static GUI instance;
    private static boolean needPressKey2 = false;

    private JButton startButton;
    private JButton stopButton;
    private JTextArea logTextArea;
    private JPanel mainPanel;
    private JButton calibrateButton;
    private JCheckBox press2CheckBox;

    /* положения пикселей, на которые мы будем опираться при анализе боя */
    public static int xBase = 0; // положение X шкалы ХП
    public static int yBase = 0; // положение Y шкалы ХП
    public static int xHP = 370;
    public static int yHP = 291;
    public static int xMana = xHP;
    public static int yMana = yHP + 17;
    public static int xFinishBattle = 538;
    public static int yFinishBattle = 472;



    public GUI(String name) throws Exception {
        super(name);
        instance = this;
        createAndShowGUI();
        try {
//            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        UIManager.put("swing.boldMetal", Boolean.FALSE);

        addKeyListener(this);
        timerQ = new Timer(100, new TaskQ());
        timerAnalyzerHPAndMana = new Timer(500, new TaskAnalyzerHPAndMana());
        timerColorTester = new Timer(3000, new TaskColorTester());

//        timersLists.add(timerQ);
        timersLists.add(timerAnalyzerHPAndMana);
//        timesLists.add(timerColorTester);
//        startAllTimers();

    }


    public static void imitateKeyPress(Robot robot, int keycode) {
        robot.keyPress(keycode);
        robot.delay(rand.nextInt(100));
        robot.keyRelease(keycode);
        robot.delay(rand.nextInt(100));
    }

    private void createAndShowGUI() {
        instance.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        instance.setLayout(new FlowLayout());
        instance.setFont(new Font("Arial", Font.PLAIN, 16));
        //Display the window.
        instance.setSize(240, 220);
        instance.setVisible(true);
        instance.setAlwaysOnTop(true);
        instance.setLocation(0, 488);

        //Set up the content pane.
        createUIComponents();
        instance.setContentPane(mainPanel);
    }

    private void createUIComponents() {
        startButton.setFont(instance.getFont());
        startButton.setLocation(10, 20);
        startButton.setPreferredSize(new Dimension(200, 50));
        startButton.addActionListener((e) -> startAllTimers());

        stopButton.setFont(instance.getFont());
        stopButton.setLocation(10, 70);
        stopButton.setPreferredSize(new Dimension(200, 50));
        stopButton.addActionListener((e) -> stopAllTimers());

        calibrateButton.setFont(instance.getFont());
        calibrateButton.addActionListener((e) -> calibrate());

        press2CheckBox.addChangeListener(e -> {
            needPressKey2 = press2CheckBox.isSelected();
        });

        logTextArea.setFont(instance.getFont());

    }


    public void startAllTimers() {
        timersLists.forEach(Timer::start);
        logTextArea.setText("Идет бой");
    }

    public void stopAllTimers() {
        timersLists.stream()
                .filter(timer -> timer != timerColorTester)
                .forEach(Timer::stop);
        logTextArea.setText("Выключено");
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            stopAllTimers();
        }
    }

    public void finishBattle() {
        finishBattle("Бой завершен.");
    }
    public void finishBattle(String text) {
        stopAllTimers();
        logTextArea.setText(text);
        instance.toFront();
    }

    /**
     * Калибровка положения экрана. При режиме "на весь экран" положение квадрата sample.bmp Должно совпадать с квадратом 273, 244
     */
    public void calibrate() {
        try {
            stopAllTimers();
            Point point = Calibrator.Calibrate();
            xBase = point.x;
            yBase = point.y;
            log.info(logTextArea, "Успешно откалибровано: x=" + xBase + " y=" + yBase);
            xHP = xBase + 97;
            yHP = yBase + 47;
            xMana = xHP;
            yMana = yHP + 17;
            xFinishBattle = xBase + 265;
            yFinishBattle = yBase + 228;
        } catch (Exception ex) {
            ex.printStackTrace();
            log.info(logTextArea, ex.getMessage());
        }
    }

    /**
     * нажатие на кнопку Q
     */
    private class TaskQ implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                Robot robot = new Robot();
                timerQ.setDelay(200 + rand.nextInt(250));
                robot.keyPress(KeyEvent.VK_Q);
                robot.delay(rand.nextInt(250));
                robot.keyRelease(KeyEvent.VK_Q);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    /**
     * проверка нужно ли нам пить хп или ману
     */
    private class TaskAnalyzerHPAndMana implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                Robot robot = new Robot();
                /* Анализируем ХП
                активное ХП       r=156,g=0,b=0
                потраченное хп    r=54,g=7,b=8
                */
//                timerQ.stop();
                Color healthColor = robot.getPixelColor(xHP, yHP);
                if (healthColor.getRed() > 150 && healthColor.getRed() < 160
                        && healthColor.getBlue() == 0
                        && healthColor.getGreen() == 0) {

                    if (needPressKey2) imitateKeyPress(robot, KeyEvent.VK_2);
                    imitateKeyPress(robot, KeyEvent.VK_Q);
                    log.info(logTextArea, "\nИдет бой");

                } else if (healthColor.getRed() > 60 && healthColor.getRed() < 75
                        && healthColor.getBlue() < 30
                        && healthColor.getGreen() < 30) {
                    log.info(logTextArea, "\nПьем банку");
                    imitateKeyPress(robot, KeyEvent.VK_2);
                    imitateKeyPress(robot, KeyEvent.VK_5);
                    imitateKeyPress(robot, KeyEvent.VK_6);
                    imitateKeyPress(robot, KeyEvent.VK_G);
                }

                /* Анализируем ману
                мана есть java.awt.Color[r=0,g=79,b=156]
                маны нет java.awt.Color[r=1,g=28,b=71]
                */
                Color manaColor = robot.getPixelColor(xMana, yMana);
                testPixelColor(xMana, yMana, robot, "Анализируем состояние (завершенность) боя: ");
                if (manaColor.getRed() < 10 &&
                        manaColor.getGreen() > 20 && manaColor.getGreen() < 30 &&
                        manaColor.getBlue() > 65 && manaColor.getBlue() < 75) {

                    imitateKeyPress(robot, KeyEvent.VK_3);
                    imitateKeyPress(robot, KeyEvent.VK_4);
                    log.info(logTextArea, "\nПьем ману");

                } else if (!manaColor.equals(new Color(0, 79, 156))) {

                }

                /* Анализируем завершенность боя
                x = 431, y = 378
                в состоянии боя ?
                после завпршения боя r=125,g=189,b=0
                */
                Color finishColor = robot.getPixelColor(xFinishBattle, yFinishBattle);
                testPixelColor(xFinishBattle, yFinishBattle, robot, "Анализируем состояние (завершенность) боя: ");
                if (finishColor.getRed() > 120 && finishColor.getRed() < 130 &&
                        finishColor.getGreen() > 185 && finishColor.getGreen() < 195 &&
                        finishColor.getBlue() == 0) {
                    finishBattle();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private class TaskColorTester implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                Robot robot = new Robot();
                int x = MouseInfo.getPointerInfo().getLocation().x;
                int y = MouseInfo.getPointerInfo().getLocation().y;
                testPixelColor(x, y, robot, "Цвет указанной точки мыши ");

//                testPixelColor(xHP, yHP, robot, "Цвет банок ХП");
                testPixelColor(xMana, yMana, robot, "Цвет маны");
//                testPixelColor(xFinishBattle, yFinishBattle, robot, "Цвет завершенности боя");

                logTextArea.setText(logTextArea.getText().split("\n")[0]
                        + "\n"
                        + robot.getPixelColor(431, 378).toString());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Тестирует цвет указанной точки и выводит его на экран
     * @param x
     * @param y
     * @param robot
     * @return
     */
    private Color testPixelColor(int x, int y, Robot robot, String prefix) {
        Color color = robot.getPixelColor(x, y);
        log.info(logTextArea, prefix + "x = " + x + ", y = " + y);
        log.info(logTextArea, "Цвет указанной точки " + robot.getPixelColor(x, y));

        return color;
    }


    /**
     * логирование в консоль и в textarea
     */
    public static class Logger {
        public Logger() {
        }

        void info(Object logTextArea, String message) {
            System.out.println(message);
            try {
                ((JTextArea) logTextArea).append(message + System.lineSeparator());
            } catch (Exception ex) {
            }
        }

        void error(Object logTextArea, String message) {
            System.err.println(message);
            try {
                ((JTextArea) logTextArea).append(message);
            } catch (Exception ex) {
            }
        }

        void error(Object logTextArea, String message, Throwable e) {
            System.err.println(message);
            System.err.println(e.getMessage());
            try {
                ((JTextArea) logTextArea).append(message);
                ((JTextArea) logTextArea).append(e.getMessage());
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
        }
    }
}
