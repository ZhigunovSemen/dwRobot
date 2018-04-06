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
    static Random rand = new Random();
    private static ArrayList<Timer> timesLists = new ArrayList<>();
    private static Timer timerQ;
    private static Timer timer123456;
    private static Timer timerAnalyzerHPAndMana;
    private static Timer timerColorTester;
    private static GUI instance;
    JTextArea displayArea;
    JScrollPane displayAreaPane;
    JTextField typingArea;
    JButton buttonStart;
    JButton buttonStop;


    public GUI(String name) throws Exception {
        super(name);
        instance = this;
        createAndShowGUI();
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        UIManager.put("swing.boldMetal", Boolean.FALSE);

        addKeyListener(this);
        timerQ = new Timer(100, new TaskQ());

        timerAnalyzerHPAndMana = new Timer(500, new TaskAnalyzerHPAndMana());

        timerColorTester = new Timer(2000, new TaskColorTester());

        timesLists.add(timerQ);
        timesLists.add(timerAnalyzerHPAndMana);
        timesLists.add(timerColorTester);
        startAllTimers();

    }

    public static void imitateKeyPress(Robot robot, int keycode) {
        robot.keyPress(keycode);
        robot.delay(rand.nextInt(100));
        robot.keyRelease(keycode);
        robot.delay(rand.nextInt(100));
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private void createAndShowGUI() {
        instance.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        instance.setLayout(new FlowLayout());
        instance.setFont(new Font("Arial", Font.PLAIN, 16));
        //Display the window.
        instance.setSize(240, 220);
        instance.setVisible(true);
        instance.setAlwaysOnTop(true);
        instance.setLocation(0, 488);

        //Set up the content pane.34
        instance.setContentPane(CreateComponentsToPane());
    }

    private JPanel CreateComponentsToPane() {
        JPanel panel = new JPanel();
        buttonStart = new JButton("Start");
        buttonStart.setFont(instance.getFont());
        buttonStart.setLocation(10,20);
        buttonStart.setPreferredSize(new Dimension(200, 50));
        buttonStart.addActionListener((e) -> {
            startAllTimers();
        });
        panel.add(buttonStart);

        buttonStop = new JButton("Stop");
        buttonStop.setFont(instance.getFont());
        buttonStop.setLocation(10,70);
        buttonStop.setPreferredSize(new Dimension(200, 50));
        buttonStop.addActionListener((e) -> {
            stopAllTimers();
        });
        panel.add(buttonStop);

        displayArea = new JTextArea(3,7);
        displayArea.setFont(instance.getFont());
//        displayArea.setLineWrap(true);
//        displayArea = new JScrollPane(displayArea);
        displayArea.setLocation(10,120);
        displayArea.setSize(new Dimension(150, 40));
        displayArea.setVisible(true);
        panel.add(displayArea);

        return panel;
    }



    public void startAllTimers() {
        timesLists.forEach(Timer::start);
        displayArea.setText("Идет бой");
    }

    public void stopAllTimers() {
        timesLists.stream()
                .filter(timer -> timer != timerColorTester)
                .forEach(Timer::stop);
        displayArea.setText("Выключено");
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
        displayArea.setText(text);
        instance.toFront();
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
                Color healthColor = robot.getPixelColor(300, 233);
                if (healthColor.equals(new Color(156, 0, 0))) {
                    robot.keyPress(KeyEvent.VK_Q);
                    robot.delay(rand.nextInt(250));
                    robot.keyRelease(KeyEvent.VK_Q);
                    displayArea.append("\nИдет бой");
                } else if (healthColor.getRed() > 60
                        && healthColor.getRed() < 75
                        && healthColor.getBlue() < 30
                        && healthColor.getGreen() < 30) {
                    displayArea.append("\nПьем банку");
                    imitateKeyPress(robot, KeyEvent.VK_2);
                    imitateKeyPress(robot, KeyEvent.VK_5);
                    imitateKeyPress(robot, KeyEvent.VK_6);
                    imitateKeyPress(robot, KeyEvent.VK_G);
                }

                /* Анализируем ману
                x = 305, y = 247
                java.awt.Color[r=0,g=79,b=156]
                */
                Color manaColor = robot.getPixelColor(300, 247);
                System.out.println(manaColor.equals(new Color(0, 79, 156)));
                if (!manaColor.equals(new Color(0, 79, 156))) {
                    imitateKeyPress(robot, KeyEvent.VK_3);
                    imitateKeyPress(robot, KeyEvent.VK_4);
                    displayArea.append("\nПьем ману");
                }

                /* Анализируем завершенность боя
                x = 431, y = 378
                в состоянии боя r=132,g=0,b=0
                после завпршения боя r=54,g=7,b=8
                */
                Color finishColor = robot.getPixelColor(431, 378);
                System.out.println(finishColor.equals(new Color(125, 189, 0)));
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
                System.out.println("x = " + x);
                System.out.println("y = " + y);
                System.out.println(robot.getPixelColor(x, y));

                displayArea.setText(displayArea.getText().split("\n")[0]
                        + "\n"
                        + robot.getPixelColor(431, 378).toString());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


}
