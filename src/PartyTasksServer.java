import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.*;


public class PartyTasksServer {

    private JFrame mainFrame = new JFrame("PartyTasks Server");
    private JPanel mainPanel = new JPanel();
    private byte numberOfUsers = 0;
    private ArrayList<ObjectOutputStream> outputStream = new ArrayList<>();
    private ArrayList<ObjectInputStream> inputStream = new ArrayList<>();
    private ArrayList<String> names = new ArrayList<>();
    private ServerSocket serverSocket;
    private Vector<String> receivedPhotosInfo = new Vector<>();
    private JList listOfPhotos = new JList();
    private byte port;
    private boolean[] isPhotoSent = new boolean[63];
    private byte taskCounter = 0;


    public static void main(String[] args) {
        new PartyTasksServer().makeGUI();
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
            waitForPlayers();
        }
    }

    public class TwentyListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            port = (byte) 4442;
            waitForPlayers();
        }
    }

    public class Top20Listener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            port = (byte) 4443;
            waitForPlayers();
        }
    }

    public class MirrorListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            port = (byte) 4444;
            waitForPlayers();
        }
    }

    public class UnlimitedListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            port = (byte) 4445;
            waitForPlayers();
        }
    }

    public void waitForPlayers() {
        new Thread(new Connect()).start();

        mainPanel.removeAll();
        mainPanel.setLayout(new BorderLayout());

        JLabel waitingText = new JLabel("Waiting for players", SwingConstants.CENTER);
        mainPanel.add(BorderLayout.NORTH, waitingText);

        mainPanel.revalidate();
        mainFrame.revalidate();

        JButton startTheGame = new JButton("Start the game");
        startTheGame.addActionListener(new StartTheGameListener());
        mainPanel.add(BorderLayout.CENTER, startTheGame);

        mainPanel.revalidate();
        mainFrame.revalidate();
    }

    public class StartTheGameListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            stopWaiting();
        }
    }

    public class Connect implements Runnable {
        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(port);

                while (true) {
                    System.out.println("czeka");
                    Socket clientSocket = serverSocket.accept();
                    numberOfUsers++;

                    ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                    outputStream.add(out);

                    ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                    inputStream.add(in);

                    System.out.println("Connected");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void stopWaiting() {
        System.out.println("koniec czekania");
        for (byte i = 0; i < numberOfUsers; i++) {
            new Thread(new ClientService(i)).start();
        }

        mainPanel.removeAll();

        JLabel welcomeText = new JLabel("In game...", SwingConstants.CENTER);
        mainPanel.add(BorderLayout.NORTH, welcomeText);

        JScrollPane gameStatus = new JScrollPane(listOfPhotos);
        mainPanel.add(BorderLayout.CENTER, gameStatus);

        mainPanel.revalidate();
        mainFrame.revalidate();

        new Thread(new RoundEnded()).start();
    }

    public class ClientService implements Runnable {
        private ObjectInputStream in;
        private byte userId;
        private String name;

        public ClientService(byte id) {
            userId = id;
            in = inputStream.get(userId);
            try {
                name = (String) in.readObject();
            } catch (Exception ex) {
                name = "User" + userId;
            }
            names.add(name);
        }

        @Override
        public void run() {
            sendTask();

            BufferedImage img;
            try {
                while ((img = ImageIO.read(in)) != null) {
                    File outputPhoto = new File("photos/photo" + name + userId + "task1.bmp");
                    receivedPhotosInfo.add(name + " sent photo");
                    listOfPhotos.setListData(receivedPhotosInfo);
                    System.out.println("Got image");
                    //Thread.sleep(5000);
                    isPhotoSent[userId] = true;
                    ImageIO.write(img, "bmp", outputPhoto);
                    return;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public synchronized void sendTask() {
            String task = Tasks.getTask();
            System.out.println(task);
            try {
                ObjectOutputStream out = outputStream.get(userId);
                out.writeObject(task);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public class TwentyService implements Runnable {
        private ObjectInputStream in;
        private byte userId;
        private byte numberOfTask;
        private String name;

        public TwentyService(byte id, byte counter) {
            numberOfTask = (byte) (counter + 1);
            userId = id;
            in = inputStream.get(userId);
            name = names.get(userId);
        }

        @Override
        public void run() {
            sendTask();

            BufferedImage img;
            try {
                while ((img = ImageIO.read(in)) != null) {
                    File outputPhoto = new File("photos/photo" + name + userId +
                            "task" + numberOfTask + ".bmp");
                    String info = switch (numberOfTask) {
                        case 1 -> name + " sent " + numberOfTask + "st photo";
                        case 2 -> name + " sent " + numberOfTask + "nd photo";
                        case 3 -> name + " sent " + numberOfTask + "rd photo";
                        default -> name + " sent " + numberOfTask + "th photo";
                    };
                    receivedPhotosInfo.add(info);
                    listOfPhotos.setListData(receivedPhotosInfo);
                    System.out.println("Got image");
                    isPhotoSent[userId] = true;
                    ImageIO.write(img, "bmp", outputPhoto);
                    return;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public synchronized void sendTask() {
            String task = Tasks.getTask();
            System.out.println(task);
            try {
                ObjectOutputStream out = outputStream.get(userId);
                out.writeObject(task);
                System.out.println("Wysłano zadanie");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public class TopTwentyService implements Runnable {
        private ObjectInputStream in;
        private byte userId;
        private byte numberOfTask;
        private String name;

        public TopTwentyService(byte id, byte counter) {
            numberOfTask = (byte) (counter + 1);
            userId = id;
            in = inputStream.get(userId);
            name = names.get(userId);
        }

        @Override
        public void run() {
            sendTask();

            BufferedImage img;
            try {
                while ((img = ImageIO.read(in)) != null) {
                    File outputPhoto = new File("photos/photo" + name + userId +
                            "task" + numberOfTask + ".bmp");
                    String info = switch (numberOfTask) {
                        case 1 -> name + " sent " + numberOfTask + "st photo";
                        case 2 -> name + " sent " + numberOfTask + "nd photo";
                        case 3 -> name + " sent " + numberOfTask + "rd photo";
                        default -> name + " sent " + numberOfTask + "th photo";
                    };
                    receivedPhotosInfo.add(info);
                    listOfPhotos.setListData(receivedPhotosInfo);
                    System.out.println("Got image");
                    isPhotoSent[userId] = true;
                    ImageIO.write(img, "bmp", outputPhoto);
                    return;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public synchronized void sendTask() {
            String task = Tasks.getTask();
            System.out.println(task);
            try {
                ObjectOutputStream out = outputStream.get(userId);
                out.writeObject(task);
                System.out.println("Wysłano zadanie");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public class MirrorService implements Runnable {
        private ObjectInputStream in;
        private byte userId;
        private byte numberOfTask;
        private String name;

        public MirrorService(byte id, byte counter) {
            numberOfTask = (byte) (counter + 1);
            userId = id;
            in = inputStream.get(userId);
            name = names.get(userId);
        }

        @Override
        public void run() {
            sendTask();

            BufferedImage img;
            try {
                while ((img = ImageIO.read(in)) != null) {
                    File outputPhoto = new File("photos/photo" + name + userId +
                            "task" + numberOfTask + ".bmp");
                    String info = switch (numberOfTask) {
                        case 1 -> name + " sent " + numberOfTask + "st photo";
                        case 2 -> name + " sent " + numberOfTask + "nd photo";
                        case 3 -> name + " sent " + numberOfTask + "rd photo";
                        default -> name + " sent " + numberOfTask + "th photo";
                    };
                    receivedPhotosInfo.add(info);
                    listOfPhotos.setListData(receivedPhotosInfo);
                    System.out.println("Got image");
                    isPhotoSent[userId] = true;
                    ImageIO.write(img, "bmp", outputPhoto);
                    return;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public synchronized void sendTask() {
            String task = Tasks.getTask();
            System.out.println(task);
            try {
                ObjectOutputStream out = outputStream.get(userId);
                out.writeObject(task);
                System.out.println("Wysłano zadanie");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public class RoundEnded implements Runnable {
        @Override
        public void run() {
            System.out.println("czeka na koniec");
            while (!isRoundEnded()) {
                try {
                    Thread.sleep(1);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            System.out.println("prawie koniec");
            switch (port) {
                case (byte) 4441:
                    endScreen();
                    break;
                case (byte) 4442:
                    if (taskCounter < (byte) 20) {
                        isPhotoSent = new boolean[63];
                        taskCounter++;
                        for (byte i = 0; i < numberOfUsers; i++) {
                            System.out.println("nowe rozpoczecie");
                            new Thread(new TwentyService(i, taskCounter)).start();
                        }
                        new Thread(new RoundEnded()).start();
                    } else {
                        endScreen();
                    }
                    break;
                case (byte) 4443:
                    if (taskCounter < (byte) 20) {
                        isPhotoSent = new boolean[63];
                        taskCounter++;
                        for (byte i = 0; i < numberOfUsers; i++) {
                            System.out.println("nowe rozpoczecie");
                            new Thread(new TopTwentyService(i, taskCounter)).start();
                        }
                        new Thread(new RoundEnded()).start();
                    } else {
                        endScreen();
                    }
                    break;

                case (byte) 4444:
                    if (taskCounter < (byte) 20) {
                        isPhotoSent = new boolean[63];
                        taskCounter++;
                        for (byte i = 0; i < numberOfUsers; i++) {
                            System.out.println("nowe rozpoczecie");
                            new Thread(new MirrorService(i, taskCounter)).start();
                        }
                        new Thread(new RoundEnded()).start();
                    } else {
                        endScreen();
                    }
                    break;
                    /*
                case (byte) 4445:
                    // unlimited
                    break; */
                default:
                    for (byte i = 0; i < numberOfUsers; i++) {
                        System.out.println("nowe rozpoczecie");
                        new Thread(new ClientService(i)).start();
                    }
                    new Thread(new RoundEnded()).start();
            }
        }
    }

    public boolean isRoundEnded() {
        for (byte i = 0; i < numberOfUsers; i++) {
            if (!isPhotoSent[i]) {
                return false;
            }
        }
        return true;
    }

    public void endScreen() {
        for (Component c : mainPanel.getComponents()) {
            if (c instanceof JLabel) {
                mainPanel.remove(c);
            }
        }

        JLabel infoText = new JLabel("Game ended", SwingConstants.CENTER);
        mainPanel.add(BorderLayout.NORTH, infoText);

        JButton quitButton = new JButton("Quit");
        quitButton.addActionListener(new QuitListener());
        mainPanel.add(BorderLayout.SOUTH, quitButton);

        mainPanel.revalidate();
        mainFrame.revalidate();
    }

    public class QuitListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            try {
                serverSocket.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            receivedPhotosInfo = new Vector<>();
            makeGUI();
        }
    }

    public static class Tasks {
        private static ArrayList<String> tasks;

        static {
            tasks = new ArrayList<>();
            try {
                File tasksFile = new File("tasks.txt");
                FileReader tasksFileReader = new FileReader(tasksFile);
                BufferedReader tasksFileBufferedReader = new BufferedReader(tasksFileReader);

                String task;
                while ((task = tasksFileBufferedReader.readLine()) != null) {
                    tasks.add(task);
                }
                tasksFileBufferedReader.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public static String getTask() {
            try {
                int idx = (int) (Math.random() * tasks.size());
                return tasks.remove(idx);
            } catch (Exception ex) {
                return "Koniec zadań";
            }
        }
    }
}
