package org.example;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

public class ScreenServer {

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println("Server started. Waiting for client...");

            while (true) {
                try (Socket socket = serverSocket.accept()) {
                    System.out.println("Client connected.");

                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    Robot robot = new Robot();
                    Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());

                    while (true) {
                        BufferedImage screenShot = robot.createScreenCapture(screenRect);
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                        ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
                        ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
                        jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                        jpgWriteParam.setCompressionQuality(1f); // качество

                        ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(byteArrayOutputStream);
                        jpgWriter.setOutput(imageOutputStream);
                        IIOImage outputImage = new IIOImage(screenShot, null, null);
                        jpgWriter.write(null, outputImage, jpgWriteParam);
                        jpgWriter.dispose();

                        out.writeObject(byteArrayOutputStream.toByteArray());
                        out.reset();
                        Thread.sleep(50);
                    }
                } catch (IOException | AWTException | InterruptedException e) {
                    System.out.println("Connection lost. Waiting for new client...");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
