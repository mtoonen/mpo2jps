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
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author HJ200, Meine Toonen
 */
public class MPO2JPG {

    private OutputStream byteOutputStream;
    private SortedSet<Integer> noProblem = new TreeSet<Integer>();
    private SortedSet<Integer> problem = new TreeSet<Integer>();
    private File root = new File("C:/mpooutput");

    public MPO2JPG() {
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
        MPO2JPG test = new MPO2JPG();
        File file = new File("leftright.mpo");

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
        byteOutputStream = null;
        InputStream stream = new FileInputStream(file);
        File outputDir = new File(root, "size_" + buffersize);
        outputDir.mkdirs();

        byte[] bytes = new byte[buffersize];
        byte[] headerBridge = new byte[6];
        int length = buffersize;
        int last = 0;
        int bitmapcount = 1;
        while ((length = stream.read(bytes, 0, length)) != -1) {
            headerBridge[3] = bytes[0];
            headerBridge[4] = bytes[1];
            headerBridge[5] = bytes[2];
            int offset;
            int index = 0;
            if ((offset = isHeader(headerBridge)) != -1) {
                byteOutputStream.write(headerBridge, 0, offset);
                addBitmap(byteOutputStream);
                byteOutputStream = new FileOutputStream(new File(outputDir, "mpo_bitmap" + bitmapcount++ + ".jpg"));
                byteOutputStream.write(headerBridge, offset, 4);
                index = offset + 1;
                last = index;
            } else {
                if (byteOutputStream != null) {
                    byteOutputStream.write(headerBridge, 0, 3);
                }
            }
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

            headerBridge[0] = bytes[bytes.length - 3];
            headerBridge[1] = bytes[bytes.length - 2];
            headerBridge[2] = bytes[bytes.length - 1];
            try {
                byteOutputStream.write(bytes, last, Math.min(Math.min(index - last + 4 /*offset for number of headerbytes*/ /* offset for overbrugging*/,
                        length), length - last));

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

    private int isHeader(byte[] bytes) {
        for (int index = 0; index < 3; index++) {
            if (fixNegative(bytes[index]) == 0xff) {
                if (fixNegative(bytes[index + 1]) == 0xd8) {
                    if (fixNegative(bytes[index + 2]) == 0xff) {
                        if (fixNegative(bytes[index + 3]) == 0xe1) {
                            return index;
                        }
                    }
                }
            }
        }
        return -1;
    }

    private static int fixNegative(byte bytesMeine) {
        return ((int) bytesMeine) < 0 ? ((int) bytesMeine) + 256 : (int) bytesMeine;
    }

    private void addBitmap(OutputStream byteOutputStream) throws IOException {
        // Method overridden in Android lib
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
