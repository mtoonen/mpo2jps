package mpo2jps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Gertjan
 */
public class OldButWorking {

    private static int views = 0;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        File input = new File("leftright.mpo");
        InputStream stream = new FileInputStream(input);

        byte[] bytes = new byte[(int) input.length()];

        stream.read(bytes);

        int index = 0;
        int last = -1;
        while (index < (bytes.length - 4)) {
            if (fixNegative(bytes[index]) == 0xff) {
                if (fixNegative(bytes[index + 1]) == 0xd8) {
                    if (fixNegative(bytes[index + 2]) == 0xff) {
                        if (fixNegative(bytes[index + 3]) == 0xe1) {
                            if (last != -1) {
                                writeView(bytes, last, index - last);
                            }
                            last = index;
                            index += 4;
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
        }
        
        writeView(bytes, last, bytes.length - last);

        stream.close();
    }

    private static void writeView(byte[] bytes, int offset, int length) throws IOException {
        File output = new File("C:/meine/dinges_" + ++views + ".jpg");

        FileOutputStream fos = new FileOutputStream(output);
        fos.write(bytes, offset, length);

        fos.close();
    }

    private static int fixNegative(byte bytesMeine) {
        return ((int) bytesMeine) < 0 ? ((int) bytesMeine) + 256 : (int) bytesMeine;
    }
}
