/**
 * Copyright © 2014 Leo Xiong <hello@leoxiong.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Steganography - to conceal data within other data
 *
 * This implementation supports concealing any form of data within
 * images by utilizing the two least significant bits of each
 * RGB (no alpha) channel of each pixel in the carrier image.
 *
 * Notes:
 *  TODO: encode length of payload by using a header or TLV
 *  TODO: pre-calculate maximum data capacity of carrier image
 *  TODO: support carrier images with a bit depth of less than 24 bits
 *  TODO: variable amount of least significant bits to use for encoding
 *  TODO: better cmd line arg parser
 */

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class Steganography {

    public static void main(String[] args) {

        if (args.length < 2 || args.length > 3) {
            System.out.println("steganography <input image> <output image> [payload file]");
            return;
        }

        Stopwatch stopwatch = new Stopwatch();

        try {
            if (args.length == 3) {
                try {
                    ImageIO.write(encode(ImageIO.read(new File(args[0])), new BitInputStream(new File(args[2]))), "PNG", new File(args[1]));
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            } else if (args.length == 2) {
                decode(ImageIO.read(new File(args[0])), new BitOutputStream(new FileOutputStream(args[1])));
            }

            System.out.println(String.format("done %sms", stopwatch.getTime() / 1000000));
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public static BufferedImage encode(BufferedImage carrier, BitInputStream payload) throws Exception {
        for (int y = 0; y < carrier.getHeight(); y++) {
            for (int x = 0; x < carrier.getWidth(); x++) {
                int pixel = carrier.getRGB(x, y) & 0xFFFCFCFC;
                for (int offset = 16; offset >= 0; offset -= 8) {
                    int bits = payload.readBits(2);
                    if (bits == -1)
                        return carrier;
                    pixel |= bits << offset;
                }
                carrier.setRGB(x, y, pixel);
            }
        }
        throw new Exception("not enough space");
    }

    public static void decode(BufferedImage carrier, BitOutputStream payload) throws IOException {
        for (int y = 0; y < carrier.getHeight(); y++) {
            for (int x = 0; x < carrier.getWidth(); x++) {
                for (int offset = 16; offset >= 0; offset -= 8) {
                    payload.write(2, (carrier.getRGB(x, y) >> offset) & 0x3);
                }
            }
        }
        payload.close();
    }
}
