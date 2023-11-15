import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import javax.imageio.ImageIO;
import javax.swing.*;

public class PartyTasksClient {

    private JFrame mainFrame = new JFrame("PartyTasks");
    private JPanel mainPanel = new JPanel();
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private JTextField nickname;
    private String task;
    private BufferedImage photo;
    private byte port;
    private byte taskCounter = 0;

    public static void main(String[] args) {
        new PartyTasksClient().makeGUI();
    }

    public void makeGUI() {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainPanel.removeAll();
        mainPanel.repaint();
        mainPanel.revalidate();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.GREEN);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        JLabel welcomeText = new JLabel("Choose mode", SwingConstants.CENTER);
        mainPanel.add(welcomeText);

        JLabel soloText = new JLabel("Solo modes:", SwingConstants.CENTER);
        mainPanel.add(soloText);

        JButton oneHit = new JButton("1-hit");
        oneHit.addActionListener(new OneHitListener());
        mainPanel.add(oneHit);

        JButton twenty = new JButton("20");
        twenty.addActionListener(new TwentyListener());
        mainPanel.add(twenty);

        JLabel multiText = new JLabel("Multi modes:", SwingConstants.CENTER);
        mainPanel.add(multiText);

        JButton top20 = new JButton("TOP20");
        top20.addActionListener(new Top20Listener());
        mainPanel.add(top20);

        JButton mirror = new JButton("Mirror");
        mirror.addActionListener(new MirrorListener());
        mainPanel.add(mirror);

        JButton unlimited = new JButton("Unlimited");
        unlimited.addActionListener(new UnlimitedListener());
        mainPanel.add(unlimited);

        mainFrame.getContentPane().add(BorderLayout.CENTER, mainPanel);

        mainFrame.setSize(500, 500);
        mainFrame.setVisible(true);
    }

    public class OneHitListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            port = (byte) 4441;
            setNameAndStart();
        }
    }

    public class TwentyListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            port = (byte) 4442;
            setNameAndStart();
        }
    }

    public class Top20Listener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            port = (byte) 4443;
            setNameAndStart();
        }
    }

    public class MirrorListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            port = (byte) 4444;
            setNameAndStart();
        }
    }

    public class UnlimitedListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            port = (byte) 4445;
            setNameAndStart();
        }
    }

    public void setNameAndStart() {
        Component[] componentList = mainPanel.getComponents();
        for (Component c : componentList) {
            mainPanel.remove(c);
        }
        mainPanel.revalidate();
        mainPanel.repaint();
        mainPanel.setLayout(new BorderLayout());

        nickname = new JTextField("Enter your name");
        mainPanel.add(BorderLayout.CENTER, nickname);

        JButton start = new JButton("Start");
        start.addActionListener(new StartListener());
        mainPanel.add(BorderLayout.SOUTH, start);

        mainFrame.getContentPane().add(mainPanel);

        mainFrame.setSize(500, 500);
        mainFrame.setVisible(true);
    }

    public class StartListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            waitForPlayers();
        }
    }

    public void waitForPlayers() {
        mainPanel.removeAll();

        JLabel welcomeText;
        if (port == (byte) 4441 || port == (byte) 4442) {
            welcomeText = new JLabel("Waiting for server", SwingConstants.CENTER);
        } else {
            welcomeText = new JLabel("Waiting for players", SwingConstants.CENTER);
        }
        mainPanel.add(BorderLayout.NORTH, welcomeText);

        mainPanel.revalidate();
        mainPanel.repaint();

        mainFrame.revalidate();
        mainFrame.repaint();

        new Thread(new StopWaiting()).start();
    }

    public class StopWaiting implements Runnable {

        @Override
        public void run() {
            appConfigure();
            while (task == null) {
                try {
                    Thread.sleep(1);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            showTask();
        }
    }

    public void appConfigure() {
        try {
            if (socket == null) {
                socket = new Socket("localhost", port);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                out.writeObject(nickname.getText());
            }
            Thread gettingTasks = new Thread(new GettingTasks());
            gettingTasks.start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void showTask() {
        Component[] componentList = mainPanel.getComponents();
        for (Component c : componentList) {
            mainPanel.remove(c);
        }
        mainPanel.revalidate();
        mainPanel.repaint();

        JLabel taskLabel = new JLabel(task);
        mainPanel.add(BorderLayout.NORTH, taskLabel);

        JButton send = new JButton("Send photo");
        send.addActionListener(new SendListener());
        mainPanel.add(BorderLayout.CENTER, send);

        mainPanel.revalidate();
        mainPanel.repaint();

        System.out.println("pokazuje zadanie hej");

    }

    public synchronized void sendPhoto() {
        try {
            photo = ImageIO.read(new File("lena.bmp"));
            ImageIO.write(photo, "bmp", out);
            System.out.println("chyba wysłane zdj");
            photoSent();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public void photoSent() {
        mainPanel.removeAll();

        if (port != (byte) 4444) {
            JLabel photoSentLabel = new JLabel("Photo has been sent", SwingConstants.CENTER);
            mainPanel.add(BorderLayout.CENTER, photoSentLabel);

            JButton okButton = new JButton("OK");
            okButton.addActionListener(new OkButtonListener());
            mainPanel.add(BorderLayout.SOUTH, okButton);
        } else {
            if (taskCounter < (byte) 20) {
                taskCounter++;
                task = null;
                new Thread(new WaitingForPlayers()).start();
            } else {
                try {
                    socket.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                makeGUI();
            }
        }

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public class OkButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            switch (port) {
                case (byte) 4441:
                    try {
                        socket.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    makeGUI();
                    break;
                case (byte) 4442, (byte) 4443:
                    if (taskCounter < (byte) 20) {
                        taskCounter++;
                        showTask();
                    } else {
                        try {
                            socket.close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        makeGUI();
                    }
                    break;
                case (byte) 4444:
                    if (taskCounter < (byte) 20) {
                        taskCounter++;
                        task = null;
                        new Thread(new WaitingForPlayers()).start();
                    } else {
                        try {
                            socket.close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        makeGUI();
                    }
                    break;
                case (byte) 4445:
                    // unlimited
                    break;
            }
        }
    }

    public class WaitingForPlayers implements Runnable {
        @Override
        public void run() {
            mainPanel.removeAll();

            JLabel photoSentLabel = new JLabel("Photo has been sent", SwingConstants.CENTER);
            mainPanel.add(BorderLayout.NORTH, photoSentLabel);

            JLabel welcomeText = new JLabel("Waiting for other players", SwingConstants.CENTER);
            mainPanel.add(BorderLayout.CENTER, welcomeText);

            mainPanel.revalidate();
            mainPanel.repaint();

            mainFrame.revalidate();
            mainFrame.repaint();
            new Thread(new GettingTasks()).start();
            while (task == null) {
                try {
                    Thread.sleep(1);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            System.out.println("wyszło");
            showTask();
        }
    }

    public class SendListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            sendPhoto();
        }
    }

    public class GettingTasks implements Runnable {

        Object obj;

        @Override
        public synchronized void run() {
            try {
                System.out.println("przed while");
                while ((obj = in.readObject()) != null) {
                    System.out.println("chodzi");
                    task = (String) obj;
                    System.out.println(task);
                }
                System.out.println("po while");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
