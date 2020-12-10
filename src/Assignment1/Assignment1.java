import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Assignment1 {

    // Method to find the Public Key B
    public static BigInteger pubKeyB(BigInteger g, BigInteger b, BigInteger p) {

        return (modExp(g, b, p));
    }

    // Method to find the SharedSecretS
    public static BigInteger findSharedSecrets(BigInteger A, BigInteger b, BigInteger p) {

        return (modExp(A, b, p));
    }

    // Method to get g^b (mod p) etc
    public static BigInteger ls
    mod(BigInteger powers, BigInteger p) {

        return (powers.remainder(p));
    }

    // Modular Exponentiation Method using the right to left variant of the algorithm
    public static BigInteger modExp(BigInteger g, BigInteger b, BigInteger p) {
        BigInteger y = BigInteger.ONE;
        int k = b.bitLength();
        String x = b.toString(2);
        char bitValue;

        for (int i = 0; i < k; i++) {
            bitValue = x.charAt(i);

            if (bitValue == '1') {
                y = y.multiply(g).mod(p);
            }
            g = mod((g.multiply(g)), p);
        }
        return (y);
    }
    // Method to get the hashed Byte Array of AES key k
    public static byte[] findAesKeyk(String sharedkey) {
        try {
            MessageDigest dig = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = dig.digest(sharedkey.getBytes(StandardCharsets.UTF_8));
            return (encodedhash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return (null);

    }
// Encryption method
    public static byte[] encrypt(byte[] key, BigInteger fileBytesBig) {
        try {
            // Pad the file
            BigInteger plaintextPadded = padFile(fileBytesBig);
            // Constant value for IV in hex
            String IVValue = "a92a106fdf4936f8d2d32e9a30f4c23a";
            // Change it to a BigInteger
            BigInteger IV = new BigInteger(IVValue, 16);
            // Write it out to IV.txt
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("IV.txt")));
            writer.write(IVValue);
            writer.close();
            // Create the SecretKeySpec
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            //  Create the IV ParameterSpec
            IvParameterSpec ivParameterSpec = new IvParameterSpec(bigIntegerToByteArray(IV));
            // Ensure its encrypted using AES in CBC mode with no padding as we already implemented our own
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            return cipher.doFinal(bigIntegerToByteArray(plaintextPadded));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    // Method to pad the file
    public static BigInteger padFile(BigInteger fileBytesBig) {
        // convert to binary string to make sure they arent taken as charactes and are taken as bit 1 or 0
        String fileBytesString = fileBytesBig.toString(2);
        int extra = 128 - fileBytesString.length() % 128;
        if (extra == 0) {
            extra = 128;
        }
        fileBytesString += "1";
        for (int i = 1; i < extra; i++) {
            fileBytesString += "0";
        }
        return new BigInteger(fileBytesString, 2);
    }

    // method to convert BigInteger to ByteArray to prevent adding leading zeros
    public static byte[] bigIntegerToByteArray(BigInteger big) {
        byte[] byteArray = big.toByteArray();
        if (byteArray[0] == 0) {
            byteArray = Arrays.copyOfRange(byteArray, 1, byteArray.length);
        }
        return byteArray;
    }

    public static void main(String[] args) {
        try {

            // Hex Strings of all keys and variables needed
            String secretKeybHex = "1800da4a9b35fb6aeb11bbf0dbba9da4ea81f1c65441d2712bfbd177d06286ad2916ab16060c71c7184a14b160c37e7ee7e3c32014cdf30cf0b5d004f6b1b657855596a3c79799482555f2689929b10b5f63e11408af3e09ad6722e227431dbfeb1c00b036c807d9bf7edd7046bbf8c3fdebd009aff7f9655969a5fc040b8a85";
            String primeModpHex = "b59dd79568817b4b9f6789822d22594f376e6a9abc0241846de426e5dd8f6eddef00b465f38f509b2b18351064704fe75f012fa346c5e2c442d7c99eac79b2bc8a202c98327b96816cb8042698ed3734643c4c05164e739cb72fba24f6156b6f47a7300ef778c378ea301e1141a6b25d48f1924268c62ee8dd3134745cdf7323";
            String gengHex = "44ec9d52c8f9189e49cd7c70253c2eb3154dd4f08467a64a0267c9defe4119f2e373388cfa350a4e66e432d638ccdc58eb703e31d4c84e50398f9f91677e88641a2d2f6157e2f4ec538088dcf5940b053c622e53bab0b4e84b1465f5738f549664bd7430961d3e5a2e7bceb62418db747386a58ff267a9939833beefb7a6fd68";
            String publicKeyAhex = "5af3e806e0fa466dc75de60186760516792b70fdcd72a5b6238e6f6b76ece1f1b38ba4e210f61a2b84ef1b5dc4151e799485b2171fcf318f86d42616b8fd8111d59552e4b5f228ee838d535b4b987f1eaf3e5de3ea0c403a6c38002b49eade15171cb861b367732460e3a9842b532761c16218c4fea51be8ea0248385f6bac0d";

            // Big Integer Versions of above
            BigInteger secretKeybBig = new BigInteger(secretKeybHex, 16);
            BigInteger primeModpBig = new BigInteger(primeModpHex, 16);
            BigInteger gengBig = new BigInteger(gengHex, 16);
            BigInteger publicKeyABig = new BigInteger(publicKeyAhex, 16);
            BigInteger publicKeyBBig = pubKeyB(gengBig, secretKeybBig, primeModpBig);

            // Writing to "DH.txt" and overwriting if it contains somethiing to ensure b is always correct
            String bString = publicKeyBBig.toString(16);
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("DH.txt")));
            writer.write(bString);
            writer.close();

            BigInteger sharedSecrets = findSharedSecrets(publicKeyABig, secretKeybBig, primeModpBig);
            String s = sharedSecrets.toString(10);
            byte[] aesKeyk = findAesKeyk(s);

            // Write to file and if no filename given use Assignment1.class
            String fileName = null;
            if (args.length < 1) {
                fileName = "Assignment1.class";
            } else {
                fileName = args[0];
            }

            // Encrypt the file
            // And ensure not negative
            BigInteger fileAsBigInt = new BigInteger(Files.readAllBytes(Paths.get(fileName)));
            byte [] fileAsBigIntArray = bigIntegerToByteArray(fileAsBigInt);
            BigInteger fileAsBigIntNotNeg = new BigInteger(1, fileAsBigIntArray);
            BigInteger encrypted = new BigInteger(encrypt(aesKeyk, fileAsBigIntNotNeg));
            byte [] encryptedArray = bigIntegerToByteArray(encrypted);
            BigInteger encryptedNotNeg = new BigInteger(1, encryptedArray);

            // print to the file
            System.out.println(encryptedNotNeg.toString(16));
            } 
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }