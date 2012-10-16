/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpo2jps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author gertjan.al
 */
public class ByteCheckerStream extends OutputStream {

    private InputStream inputStream;
    private int position = 0;
    private int fileNumber = 1;
    private boolean hasProblem = false;

    public ByteCheckerStream(File original) throws FileNotFoundException, IOException {
        this.inputStream = new FileInputStream(original);

        byte[] bytes = new byte[(int) original.length()];
        inputStream.read(bytes, 0, bytes.length);
        print(bytes);
        inputStream.close();

        this.inputStream = new FileInputStream(original);
    }

    public void reset() throws FileNotFoundException, IOException {
        this.position = 0;
        this.fileNumber++;
        this.hasProblem = false;
        // write((byte)0); // resetting, show if header is found in sout
    }

    @Override
    public void write(int b) throws IOException {

        byte read = (byte) inputStream.read();
        if (read != b) {
//            System.err.println("wrong byte at position " + position + " in file " + fileNumber + " " + getHex((byte)b) + " != " + getHex(read));
            System.out.print("[" + String.format("%3s", getHex((byte) b)) + "]");
            hasProblem = true;
        } else {
            System.out.print(" " + String.format("%3s", getHex((byte) b)) + " ");
        }

        position++;
    }

    private void print(byte[] bytes) {
        for (byte value : bytes) {
            System.out.print(String.format(" %3s ", getHex(value)));
        }
        System.out.println("");
    }

    private static int fixNegative(byte bytesMeine) {
        return ((int) bytesMeine) < 0 ? ((int) bytesMeine) + 256 : (int) bytesMeine;
    }

    public static String getHex(byte raw) {
        final String HEXES = "0123456789ABCDEF";
        return String.valueOf(HEXES.charAt((raw & 0xF0) >> 4)) + String.valueOf(HEXES.charAt((raw & 0x0F)));
    }

    public boolean hasProblem() {
        return hasProblem;
    }
}
