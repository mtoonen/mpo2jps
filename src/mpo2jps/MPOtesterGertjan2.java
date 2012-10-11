/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpo2jps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author gertjan.al
 */
public class MPOtesterGertjan2 {

    private OutputStream byteOutputStream;
    private SortedSet<Integer> noProblem = new TreeSet<Integer>();
    private SortedSet<Integer> problem = new TreeSet<Integer>();
    private File root = new File("C:/mpooutput");

    private enum State {

        NONE, BIT1, BIT2, BIT3, BIT4;
    }

    public MPOtesterGertjan2() {
        if (root.isDirectory() && root.exists()) {
            deleteFolder(root, false);
        }
    }

    private void deleteFolder(File directory, boolean deleteRoot) {
        File[] files = directory.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f, true);
                } else {
                    f.delete();
                }
            }
        }
        if (deleteRoot) {
            directory.delete();
        }
    }

    public static void main(String... args) throws Exception {
        MPOtesterGertjan2 test = new MPOtesterGertjan2();
        File file = new File("IMAG0960.mpo");

        int failcount = 0;
        int succescount = 0;

        for (int i = 5; i <= 257; i++) {
            try {
                int bitmaps = test.test(file, i);
                System.out.println("Test passed for buffersize " + i + "; " + bitmaps);
                succescount++;

            } catch (IndexOutOfBoundsException e) {
                System.err.println("Test failed for buffersize " + i);
                failcount++;
            }
        }

        test.printresults();

        System.out.println("");
        System.out.println(succescount + " tests passed, " + failcount + " tests failed");
    }

    public int test(File file, int buffersize) throws Exception {
        InputStream stream = new FileInputStream(file);
        File outputDir = new File(root, "size_" + buffersize);
        outputDir.mkdirs();

        byte[] bytes = new byte[buffersize];

        int length = buffersize;
        int bitmapcount = 1;
        State state = State.NONE;
        int[] headerInt = {0xff, 0xd8, 0xff, 0xe1};
        byte[] headerBytes = new byte[4];

        while ((length = stream.read(bytes, 0, length)) != -1) {

            for (int i = 0; i < length; i++) {
                byte code = bytes[i];

                if (fixNegative(code) == headerInt[0] && state == State.NONE) {
                    state = State.BIT1;
                    headerBytes[0] = code;

                } else if (fixNegative(code) == headerInt[1] && state == State.BIT1) {
                    state = State.BIT2;
                    headerBytes[1] = code;

                } else if (fixNegative(code) == headerInt[2] && state == State.BIT2) {
                    state = State.BIT3;
                    headerBytes[2] = code;

                } else if (fixNegative(code) == headerInt[3] && state == State.BIT3) {
                    state = State.BIT4;
                    headerBytes[3] = code;

                    if (byteOutputStream != null) {
                        byteOutputStream.flush();
                        byteOutputStream.close();
                    }

                    byteOutputStream = new FileOutputStream(new File(outputDir, "mpo_bitmap" + bitmapcount++ + ".jpg"));
                    byteOutputStream.write(headerBytes);
                    
                    state = State.NONE;

                } else {
                    switch (state) {
                        case BIT1:
                            byteOutputStream.write(headerBytes[0]);
                            state = State.NONE;
                            break;
                            
                        case BIT2:
                            byteOutputStream.write(headerBytes, 0, 2);
                            state = State.NONE;
                            break;
                            
                        case BIT3:
                            byteOutputStream.write(headerBytes, 0, 3);
                            state = State.NONE;
                            break;
                    }
                    byteOutputStream.write(code);
                }
            }
        }
        if (byteOutputStream != null) {
            byteOutputStream.flush();
            byteOutputStream.close();
        }

        stream.close();

        if (--bitmapcount < 2) {
            problem.add(buffersize);
        } else {
            noProblem.add(buffersize);
        }

        return bitmapcount;
    }

    private static int fixNegative(byte bytesMeine) {
        return ((int) bytesMeine) < 0 ? ((int) bytesMeine) + 256 : (int) bytesMeine;
    }

    public void printresults() {
        System.out.println("No problem");
        for (Integer value : noProblem) {
            System.out.print(value + ", ");
        }
        System.out.println();

        System.out.println("Problem");
        for (Integer value : problem) {
            System.out.print(value + ", ");
        }
    }
}
