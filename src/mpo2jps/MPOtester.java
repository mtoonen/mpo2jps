/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpo2jps;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author gertjan.al
 */
public class MPOtester {

    private ByteArrayOutputStream byteOutputStream;
    private Set<Integer> noProblem = new HashSet<Integer>();
    private Set<Integer> problem = new HashSet<Integer>();

    public static void main(String... args) throws Exception {
        MPOtester test = new MPOtester();
        File file = new File("IMAG0960.mpo");

        int failcount = 0;
        int succescount = 0;
        
        for (int i = 5; i <= 257; i++) {
            try {
                test.test(file, i);
                System.out.println("Test passed for buffersize " + i);
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

    public void test(File file, int buffersize) throws Exception {
        InputStream stream = new FileInputStream(file);

        byte[] bytes = new byte[buffersize];

        int length = buffersize;
        int last = 0;
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

                                byteOutputStream = new ByteArrayOutputStream();

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
                byteOutputStream.write(bytes, last, Math.max(0, Math.min(index - last + 4, length)));
                noProblem.add(buffersize);

            } catch (IndexOutOfBoundsException e) {
                System.err.println("");
                System.err.println(" bytes.length=" + bytes.length + " last=" + last + " index=" + index + " length=" + length);
                System.err.println("Math.min(index - last + 4, length) = Math.min(" + (index - last + 4) + ", " + length + ")");
                System.err.println("Problem? " + (bytes.length - index));
                problem.add(buffersize);
                throw e;
            }
            last = 0;
        }
        addBitmap(byteOutputStream);
        stream.close();

    }

    private static int fixNegative(byte bytesMeine) {
        return ((int) bytesMeine) < 0 ? ((int) bytesMeine) + 256 : (int) bytesMeine;
    }

    private void addBitmap(ByteArrayOutputStream byteOutputStream) throws IOException {
        byteOutputStream.close();
    }

    public void close() {
        byteOutputStream.reset();
        byteOutputStream = null;
    }

    private void printresults() {
        System.out.println("No problem");
        for(Integer value : noProblem)
        {
            System.out.print(value + ", ");
        }
        System.out.println();
        
        System.out.println("Problem");
        for(Integer value : problem)
        {
            System.out.print(value + ", ");
        }
    }
}
