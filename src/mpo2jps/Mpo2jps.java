/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mpo2jps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Gertjan
 */
public class Mpo2jps {

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
        while ((length = stream.read(bytes, off, length)) !=-1) {

            int index = 0;
            if(length < 0 ){
                int a = 0;
            }
            while (index < (bytes.length - 4)) {
                if (fixNegative(bytes[index]) == 0xff) {
                    if (fixNegative(bytes[index + 1]) == 0xd8) {
                        if (fixNegative(bytes[index + 2]) == 0xff) {
                            if (fixNegative(bytes[index + 3]) == 0xe1) {
                                if (last != -1) {
                                    writeView(bytes, last, Math.min(index - last,length), true);
                                }
                                last = index;
                                index += 4;
                            } else {
                                writeView(bytes, last, Math.min(index - last,length), false);
                                index += 2;
                            }
                        } else {
                            writeView(bytes, last,Math.min(index - last,length), false);
                            index += 3;
                        }
                    } else {
                        writeView(bytes, last, Math.min(index - last,length), false);
                        index++;
                    }
                } else {
                    writeView(bytes, last, Math.min(index - last,length), false);
                    index++;
                }
            }
        }
       // writeView(bytes, last, bytes.length - last, true);

        fos.close();
        stream.close();
    }

    private static void writeView(byte[] bytes, int offset, int length, boolean isNew) throws IOException {
        if (isNew) {
            if (fos != null) {
                fos.close();
            }
            current = new File("C:/meine/buffereddinges_" + ++views + ".jpg");
            fos = new FileOutputStream(current);
        }
        
        System.out.println("Byteslength"+bytes.length);
        System.out.println("length"+length);
        System.out.println("Offset"+offset);
        if(length < 0){
            System.out.println("AAAP");
        }
        if (fos != null) {
            fos.write(bytes, offset, length);
        }
    }

    private static int fixNegative(byte bytesMeine) {
        return ((int) bytesMeine) < 0 ? ((int) bytesMeine) + 256 : (int) bytesMeine;
    }
}
