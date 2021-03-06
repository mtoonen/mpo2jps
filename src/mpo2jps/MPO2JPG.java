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

    private ByteCheckerStream outputStream;
    private SortedSet<Integer> noProblem = new TreeSet<Integer>();
    private SortedSet<Integer> problem = new TreeSet<Integer>();

    public static void main(String... args) throws Exception {
        MPO2JPG test = new MPO2JPG();
        File file = new File("onetwothree.mpo");

        int failcount = 0;
        int succescount = 0;

        for (int i = 7; i <= 40; i++) {
            try {
                int bitmaps = test.test(file, i);
                System.out.println();
                if (bitmaps == 2) {
                    System.out.println("Test passed for buffersize " + i + "; " + bitmaps);
                    succescount++;

                } else {
                    System.out.println("Test failed for buffersize " + i);
                    failcount++;
                }


            } catch (IndexOutOfBoundsException e) {
                System.out.println("Test failed for buffersize " + i);
                failcount++;
            }
            System.out.println();
        }

        test.printresults();

        System.out.println("");
        System.out.println(succescount + " tests passed, " + failcount + " tests failed");
    }

    public int test(File file, int buffersize) throws IndexOutOfBoundsException, IOException {
        outputStream = null;
        InputStream stream = new FileInputStream(file);

        boolean hasLastHeaderBytes = false;
        byte[] bytes = new byte[buffersize];
        byte[] headerBridge = new byte[6];
        int length = buffersize;
        int bitmapcount = 1;
        int index = 0;
        int outerBufferIndex = 0;

        while ((length = stream.read(bytes, 0, length)) != -1) {
            headerBridge[3] = bytes[0];
            headerBridge[4] = bytes[1];
            headerBridge[5] = bytes[2];

            int headerOffset;
            index = 0;
            outerBufferIndex = 0;

            if ((headerOffset = isHeader(headerBridge)) != -1) {
                // Header found in bridge buffer
                outputStream.write(headerBridge, 0, headerOffset);
                addBitmap(outputStream);
                outputStream.reset();
                bitmapcount++;

                outputStream.write(headerBridge, headerOffset, 4);
                index = outerBufferIndex = headerOffset + 1;

            } else if (outputStream != null) {
                // No header in bridge buffer, write bridge buffer
                // (last 3 bytes of data from previous buffer run)
                outputStream.write(headerBridge, 0, 3);
            }

            int innerOffset = 0;
            do {
                if (fixNegative(bytes[index]) == 0xff) {
                    if (fixNegative(bytes[index + 1]) == 0xd8) {
                        if (fixNegative(bytes[index + 2]) == 0xff) {
                            if (fixNegative(bytes[index + 3]) == 0xe1) {
                                if (outputStream != null) {
                                    // Header found in current buffer
                                    outputStream.write(bytes, innerOffset, index - innerOffset);
                                    outputStream.reset();

                                } else {
                                    outputStream = new ByteCheckerStream(file); // First time
                                }

                                innerOffset = Math.min(length - index - 3, 4);
                                outputStream.write(bytes, index, innerOffset);
                                bitmapcount++;

                                index += innerOffset;
                                outerBufferIndex = index;
                            } else {
                                index += 2;
                            }
                        } else {
                            index += 3;
                        }
                    } else {
                        index++;
                    }
                } else {
                    index++;
                }
            } while (index < length - 3);

            if (length >= 3) {
                headerBridge[0] = bytes[length - 3];
                headerBridge[1] = bytes[length - 2];
                headerBridge[2] = bytes[length - 1];

                try {

                    if (length - outerBufferIndex - 3 > 0) {
                        outputStream.write(bytes, outerBufferIndex, length - outerBufferIndex - 3);
                    }

                } catch (IndexOutOfBoundsException e) {
                    System.out.println();
                    throw e;
                }
                outerBufferIndex = 0;
                hasLastHeaderBytes = true;

            } else {
                outputStream.write(bytes, 0, length);
                hasLastHeaderBytes = false;
            }
        }

        // TODO check if this is always true
        if (hasLastHeaderBytes) {
            outputStream.write(headerBridge, 0, 3);
        }

        addBitmap(outputStream);
        stream.close();

        if (--bitmapcount < 2) {
            problem.add(buffersize);
        } else {
            noProblem.add(buffersize);
        }

        if (outputStream.hasProblem()) {
            return -1;
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

    private static int fixNegative(byte byteValue) {
        return ((int) byteValue) < 0 ? ((int) byteValue) + 256 : (int) byteValue;
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
