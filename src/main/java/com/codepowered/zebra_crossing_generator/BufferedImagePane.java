package com.codepowered.zebra_crossing_generator;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class BufferedImagePane extends JPanel {

    private final BufferedImage img;

    public BufferedImagePane(BufferedImage img) {
        this.img = img;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(img.getWidth(), img.getHeight());
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        if (img != null) {
            g2d.drawImage(img, 0, 0, this);
        }
        g2d.dispose();
    }

}
