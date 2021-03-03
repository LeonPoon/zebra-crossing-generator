package com.codepowered.zebra_crossing_generator;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.apache.commons.cli.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class ZebraCrossingGenerator {

    private static final int WHITE = new Color(255, 255, 255).getRGB();
    private static final int BLACK = new Color(0, 0, 0).getRGB();
    private final String[] args;

    public ZebraCrossingGenerator(String[] args) {
        this.args = args;
    }

    public static void main(String[] args) throws WriterException, ParseExceptionEx, ParseException, IOException {
        new ZebraCrossingGenerator(args).main();
    }

    public void main() throws WriterException, ParseExceptionEx, ParseException, IOException {
        CommandLine cmd = parseCommandLine();
        if (cmd == null) {
            System.exit(1);
            return;
        }

        boolean hasInputFile = cmd.hasOption("input");
        File intputFilePath = parseFileCheckUseStandardInOut(cmd, "input");
        boolean hasOutputFile = cmd.hasOption("output");
        File outputFilePath = parseFileCheckUseStandardInOut(cmd, "output");
        int width = parseIntOptionOrDefault(cmd, "width", 600);
        int height = parseIntOptionOrDefault(cmd, "height", 500);

        String data = cmd.getArgs().length == 0 ? null : cmd.getArgs()[0];
        if (data != null && hasInputFile)
            throw new IllegalArgumentException("Expecting only none of or either of input file or input DATA string, but received: both");
        if (data == null) {
            final InputStream in = intputFilePath == null ? System.in : new FileInputStream(intputFilePath);
            try {
                data = IOUtils.toString(in, Charset.defaultCharset());
            } finally {
                if (intputFilePath != null)
                    in.close();
            }
        }

        BitMatrix matrix = new QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, width, height);

        if (hasOutputFile) {
            saveFile(matrix, outputFilePath);
        } else {
            try {
                showWindow(matrix);
            } catch (HeadlessException e) {
                saveFile(matrix, null);
            }
        }
    }

    private File parseFileCheckUseStandardInOut(CommandLine cmd, String optName) throws ParseException {
        final File f = cmd.hasOption(optName) ? (File) cmd.getParsedOptionValue(optName) : null;
        return "-".equals(f == null ? null : f.toString()) ? null : f;
    }

    private int parseIntOptionOrDefault(CommandLine cmd, String optName, int defaultValue) throws ParseExceptionEx {
        if (!cmd.hasOption("width"))
            return defaultValue;
        try {
            return ((Number) cmd.getParsedOptionValue("width")).intValue();
        } catch (ParseException e) {
            throw new ParseExceptionEx("For argument value of: " + optName, e);
        }
    }

    protected void showWindow(BitMatrix matrix) {
        JFrame frame = new JFrame();
        frame.setSize(matrix.getWidth(), matrix.getHeight());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new BufferedImagePane(getBufferedImage(matrix)));
        frame.pack();
        frame.setVisible(true);
    }

    protected void saveFile(BitMatrix matrix, File outputFilePath) throws IOException {
        BufferedImage bi = getBufferedImage(matrix);
        if (outputFilePath == null)
            ImageIO.write(bi, "png", System.out);
        else
            ImageIO.write(bi, FilenameUtils.getExtension(outputFilePath.getName()), outputFilePath);
    }

    private BufferedImage getBufferedImage(BitMatrix matrix) {
        BufferedImage bi = new BufferedImage(matrix.getWidth(), matrix.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        for (int y = 0; y < matrix.getHeight(); y++)
            for (int x = 0; x < matrix.getWidth(); x++)
                bi.setRGB(x, y, matrix.get(x, y) ? BLACK : WHITE);
        return bi;
    }

    protected CommandLine parseCommandLine() {
        Options options = new Options();

        Option width = new Option("w", "width", true, "width");
        width.setType(Number.class);
        width.setRequired(false);
        options.addOption(width);

        Option height = new Option("h", "height", true, "height");
        height.setType(Number.class);
        height.setRequired(false);
        options.addOption(height);

        Option input = new Option("i", "input", true, "input file");
        input.setType(File.class);
        input.setRequired(false);
        options.addOption(input);

        Option output = new Option("o", "output", true, "output file");
        output.setType(File.class);
        output.setRequired(false);
        options.addOption(output);

        CommandLineParser parser = new BasicParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);

            int argsCount = cmd.getArgs().length;
            if (argsCount != 0 && argsCount != 1) {
                throw new ParseException("Expecting 0 or 1 DATA element, received: " + argsCount);
            }
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp(getClass().getSimpleName() + " DATA", options);

            return null;
        }
        return cmd;
    }
}
