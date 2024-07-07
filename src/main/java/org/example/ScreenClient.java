package org.example;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class ScreenClient extends JFrame {

    private BufferedImage image;
    private double scale = 1.0;
    private double minScale = 1.0;
    private int imageX = 0, imageY = 0;
    private Point dragStart;

    public ScreenClient() {
        setTitle("Screen Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setVisible(true);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (image != null) {
                    int newWidth = (int) (image.getWidth() * scale);
                    int newHeight = (int) (image.getHeight() * scale);
                    g.drawImage(image, imageX, imageY, newWidth, newHeight, this);
                }
            }
        };
        add(panel);

        panel.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getWheelRotation() < 0) {
                    scale *= 1.1;
                } else {
                    scale = Math.max(minScale, scale * 0.9);
                }
                panel.repaint();
            }
        });

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragStart = e.getPoint();
                setCursor(new Cursor(Cursor.MOVE_CURSOR));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point dragEnd = e.getPoint();
                imageX += dragEnd.x - dragStart.x;
                imageY += dragEnd.y - dragStart.y;
                dragStart = dragEnd;
                panel.repaint();
            }
        });

        new Thread(() -> {
            while (true) {
                try (Socket socket = new Socket("localhost", 5000);
                     ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                    while (true) {
                        byte[] imageBytes = (byte[]) in.readObject();
                        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageBytes);
                        image = ImageIO.read(byteArrayInputStream);
                        minScale = Math.min((double) getWidth() / image.getWidth(), (double) getHeight() / image.getHeight());
                        scale = Math.max(scale, minScale);
                        panel.repaint();
                    }
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("Connection lost. Reconnecting...");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public static void main(String[] args) {
        new ScreenClient();
    }
}
