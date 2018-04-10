package ru.zhigunov.dwar.robot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Random;

public class GUI extends JFrame implements KeyListener {
    static final String newline = System.getProperty("line.separator");
    static final Logger log = new Logger();
    static Random rand = new Random();
    private static ArrayList<Timer> timesLists = new ArrayList<>();
    private static Timer timerQ;
    private static Timer timer123456;
    private static Timer timerAnalyzerHPAndMana;
    private static Timer timerColorTester;
    private static GUI instance;
    JScrollPane logTextAreaPane;
    JTextField typingArea;

    private JButton startButton;
    private JButton stopButton;
    private JTextArea logTextArea;
    private JPanel mainPanel;

    /* переменные положения пикселей, которые мы будем анализировать во время боя */
    int xHP = 370;
    int yHP = 291;
    int xMana = 370;
    int yMana = 308;
    int xFinishBattle = 538;
    int yFinishBattle = 472;



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

        timesLists.add(timerQ);
        timesLists.add(timerAnalyzerHPAndMana);
//        timesLists.add(timerColorTester);
        startAllTimers();
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

        //Set up the content pane.34
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

        logTextArea.setFont(instance.getFont());
//        logTextArea.setLocation(10,120);
//        logTextArea.setSize(new Dimension(150, 40));
//        logTextArea.setVisible(true);

    }


    public void startAllTimers() {
        timesLists.forEach(Timer::start);
        logTextArea.setText("Идет бой");
    }

    public void stopAllTimers() {
        timesLists.stream()
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
     * Калибровка положения экрана
     */
    public void calibrate() {

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
                x = 300, y = 233
                активное ХП       r=156,g=0,b=0
                потраченное хп    r=54,g=7,b=8
                */
                timerQ.stop();
                Color healthColor = robot.getPixelColor(xHP, yHP);
                if (healthColor.equals(new Color(156, 0, 0))) {
                    robot.keyPress(KeyEvent.VK_Q);
                    robot.delay(rand.nextInt(250));
                    robot.keyRelease(KeyEvent.VK_Q);
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
                x = 305, y = 247
                java.awt.Color[r=0,g=79,b=156]
                */
                Color manaColor = robot.getPixelColor(xMana, yMana);
                testPixelColor(xMana, yMana, robot, "Анализируем состояние (завершенность) боя: ");
                if (!manaColor.equals(new Color(0, 79, 156))) {
                    imitateKeyPress(robot, KeyEvent.VK_3);
                    imitateKeyPress(robot, KeyEvent.VK_4);
                    log.info(logTextArea, "\nПьем ману");
                }

                /* Анализируем завершенность боя
                x = 431, y = 378
                в состоянии боя r=132,g=0,b=0
                после завпршения боя r=54,g=7,b=8
                */
                Color finishColor = robot.getPixelColor(xFinishBattle, yFinishBattle);
                testPixelColor(xFinishBattle, yFinishBattle, robot, "Анализируем состояние (завершенность) боя: ");
                if (finishColor.equals(new Color(125, 189, 0))) {
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


                testPixelColor(xHP, yHP, robot, "Цвет банок ХП");
                testPixelColor(xMana, yMana, robot, "Цвет маны");
                testPixelColor(xFinishBattle, yFinishBattle, robot, "Цвет завершенности боя");

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
     *
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
