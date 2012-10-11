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
public class MPOtesterGertjan {

    private OutputStream byteOutputStream;
    private SortedSet<Integer> noProblem = new TreeSet<Integer>();
    private SortedSet<Integer> problem = new TreeSet<Integer>();
    private File root = new File("C:/mpooutput");

    public MPOtesterGertjan() {
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
        MPOtesterGertjan test = new MPOtesterGertjan();
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
        int last = 0;
        int bitmapcount = 1;
        while ((length = stream.read(bytes, 0, length)) != -1) {

            int index = 0;
            do {
                int indexOffset = 0;
                if (fixNegative(bytes[index]) == 0xff) {
                    if (fixNegative(bytes[index + 1]) == 0xd8) {
                        if (fixNegative(bytes[index + 2]) == 0xff) {
                            if (fixNegative(bytes[index + 3]) == 0xe1) {
                                if (byteOutputStream != null) {
                                    byteOutputStream.write(bytes, 0, index);//, Math.max(0, Math.min(index -last, length)));
                                    addBitmap(byteOutputStream);
                                }

                                byteOutputStream = new FileOutputStream(new File(outputDir, "mpo_bitmap" + bitmapcount++ + ".jpg"));

                                last = index;
                                indexOffset += 4;
                            } else {
                                indexOffset += 2;
                            }
                        } else {
                            indexOffset += 3;
                        }
                    } else {
                        indexOffset++;
                    }
                } else {
                    indexOffset++;
                }

                index += indexOffset;
            } while (index < (bytes.length - 4));

            try {
                byteOutputStream.write(bytes, last, Math.min(Math.min(index - last + 4, length), length - last));

            } catch (IndexOutOfBoundsException e) {
                System.err.println("");
                System.err.println(" bytes.length=" + bytes.length + " last=" + last + " index=" + index + " length=" + length);
                System.err.println("Math.min(index - last + 4, length) = Math.min(" + (index - last + 4) + ", " + length + ")");
                System.err.println("Problem? " + (bytes.length - index));
                throw e;
            }
            last = 0;
        }
        addBitmap(byteOutputStream);
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

    private void addBitmap(OutputStream byteOutputStream) throws IOException {
        byteOutputStream.flush();
        byteOutputStream.close();
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
