import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;


class Assignment2 {
//    Declaring some variables
    static BigInteger x,y,d_tmp;
    static BigInteger one = BigInteger.ONE;

// Method to Generate random 512 bit Probable Prime values
    public static BigInteger probablePrimeValue()
    {
        Random rnd = new Random();
        BigInteger val;

        val = BigInteger.probablePrime(512, rnd);

        return(val);
    }
//    Method to determine if two numbers are coPrime (Returns 1 if they are)
    static BigInteger isCoPrime(BigInteger n, BigInteger m)
    {
        BigInteger zero = new BigInteger("0");
        if (m.equals(zero))
            return n;
        else
            return isCoPrime(m,n.mod(m));
    }
//Method for getting the modular inverse
    static BigInteger modInverse(BigInteger e,BigInteger phiN)
    {
        BigInteger xVal=ExtendedEuclidGCD(e,phiN);
        return((xVal.mod(phiN)).add(phiN)).mod(phiN);

    }
//Method for Extended Euclidean GCD
    static  BigInteger ExtendedEuclidGCD(BigInteger e,BigInteger phiN)
    {
        BigInteger zero =new BigInteger("0");

        if(phiN.equals(zero))
        {
            d_tmp=e;
            x=BigInteger.ONE;
            y=BigInteger.ZERO;
        }
        else
        {
            ExtendedEuclidGCD(phiN,e.mod(phiN));
            BigInteger tmp=x;
            x=y;
            y=tmp.subtract((e.divide(phiN)).multiply(y));
        }
        return x;
    }

// Method to calculate Phi(n) which, when p and q are prime: Phi(n) = (p-1)(q-1)
    static BigInteger phi(BigInteger p, BigInteger q) {

        p = p.subtract(one);
        q = q.subtract(one);

        return(p.multiply(q));

    }

//    Method to get the Message Digest
    static MessageDigest getMessageDigest() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md;
        } catch (NoSuchAlgorithmException nsae){
            nsae.printStackTrace();
        }
        return null;
    }

//    Method to get the modular exponentiation for the chinese remainder theorem
    public static BigInteger modExp(BigInteger g, BigInteger b, BigInteger p) {
        BigInteger var = BigInteger.ONE;
        int k = b.bitLength();
        String x = b.toString(2);
        char bitValue;

        for (int i = 0; i < k; i++) {
            bitValue = x.charAt(i);

            if (bitValue == '1') {
                var = var.multiply(g).mod(p);
            }
            g = g.multiply(g).mod(p);
        }
        return (var);
    }

    public static BigInteger chineseRemainderTheorem(BigInteger cModExpP,BigInteger inverse, BigInteger cModExpQ, BigInteger p , BigInteger q){
        return cModExpQ.add(q.multiply((inverse.multiply(cModExpP.subtract(cModExpQ))).mod(p)));
    }

    public static BigInteger decrypt(BigInteger classFileDigest, BigInteger d, BigInteger p, BigInteger q){
        BigInteger cModExpP = modExp(classFileDigest, d.mod(p.subtract(one)), p);
        BigInteger cModExpQ = modExp(classFileDigest, d.mod(q.subtract(one)), q);

        BigInteger iQModP = modInverse(q, p);

        return chineseRemainderTheorem(cModExpP, iQModP, cModExpQ, p, q);

    }

//    Method to write out content to a file
    public static void writeToFile(String filename, String content ){
        try {
            Files.write(Paths.get(filename), content.getBytes(StandardCharsets.UTF_8));
        }catch (IOException ioException){
            ioException.printStackTrace();
        }
    }

//    Main method
    public static void main(String[] args) {
//        Declare some variables
        BigInteger p, q, d, classFile;
        String fileName;
        MessageDigest digest;
        BigInteger one = new BigInteger("1");
        BigInteger e = new BigInteger("65537");
//        Generate 512 bit Values for p and q
        p = probablePrimeValue();
        q = probablePrimeValue();
//        Phi(n)
        BigInteger phiN = phi(p,q);
//        Check Phi(n) is co prime with e
        while (!isCoPrime(phiN, e).equals(one)) {
//            if not generate new values and check again
            p = probablePrimeValue();
            q = probablePrimeValue();
            isCoPrime(phiN, e);
        }
//        Get d Inverse e(mod(phi(n)))
        d = modInverse(e,phiN);

//        Decrypt Assignment2.class if no args
        if(args.length < 1){
            fileName = "Assignment2.class";
        } else {
            fileName = args[0];
        }

        digest = getMessageDigest();

        try {
            classFile = new BigInteger(1, digest.digest(Files.readAllBytes(Paths.get(fileName))));
        }
        catch(IOException ioException) {
            classFile = null;
            ioException.printStackTrace();
        }

//        calculate n
        BigInteger n = p.multiply(q);

//        decrypt the file
        BigInteger digestSigned = decrypt(classFile, d, p, q );
        String signedDigest = digestSigned.toString(16);
        String nHex = n.toString(16);

//        Write n to Modulus.txt
        writeToFile("Modulus.txt", nHex);

//        Digital Signature of class in hex output to file
        System.out.println(signedDigest);


    }

}
