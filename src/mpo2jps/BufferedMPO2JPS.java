package mpo2jps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 *
 * @author Gertjan Al
 * @author Meine Toonen
 */
public class BufferedMPO2JPS {

    private static int views = 0;
    private static final int BUFFER_SIZE = 256;
    /**
     * @param args the command line arguments
     */
    private static File current = null;
    private static FileOutputStream fos = null;

    public static void main(String[] args) throws Exception {
        File input = new File("leftright.mpo");
        InputStream stream = new FileInputStream(input);


        byte[] bytes = new byte[BUFFER_SIZE];

        int off = 0;
        int length = BUFFER_SIZE;
        int last = -1;
        while ((length = stream.read(bytes, off, length)) != -1) {

            int index = 0;
            do {
                int indexOffset = 0;
                if (fixNegative(bytes[index]) == 0xff) {
                    if (fixNegative(bytes[index + 1]) == 0xd8) {
                        if (fixNegative(bytes[index + 2]) == 0xff) {
                            if (fixNegative(bytes[index + 3]) == 0xe1) {
                                if (fos != null) {
                                    fos.write(bytes, 0, index);//, Math.max(0, Math.min(index -last, length)));
                                    System.out.println("Write from " + 0 + " with length " + index);
                                    fos.close();
                                }
                                current = new File("C:/meine/buffereddinges_" + ++views + ".jpg");
                                fos = new FileOutputStream(current);
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
            } while (index < (bytes.length));

            System.out.println("Write from" + last + " with length " + Math.max(0, Math.min(index - last, length)));
            fos.write(bytes, last, Math.max(0, Math.min(index - last, length)));
            last = 0;
        }
        fos.close();
        stream.close();
    }

    private static int fixNegative(byte bytesMeine) {
        return ((int) bytesMeine) < 0 ? ((int) bytesMeine) + 256 : (int) bytesMeine;
    }
}
