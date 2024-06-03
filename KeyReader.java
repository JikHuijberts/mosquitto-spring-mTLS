import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class KeyReader {
    static ResourceLoader resourceLoader = new DefaultResourceLoader();
    public KeyReader() {
    }
    public static RSAPublicKey readX509PublicKey(File file) throws Exception {
        KeyFactory factory = KeyFactory.getInstance("RSA");

        try (FileReader keyReader = new FileReader(file);
             PemReader pemReader = new PemReader(keyReader)) {

            PemObject pemObject = pemReader.readPemObject();
            byte[] content = pemObject.getContent();
            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(content);
            return (RSAPublicKey) factory.generatePublic(pubKeySpec);
        }
    }
    public static X509Certificate readCert(File file) throws Exception {
        X509Certificate caCert = null;

        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        while (bis.available() > 0) {
            caCert = (X509Certificate) cf.generateCertificate(bis);
            // System.out.println(caCert.toString());
        }
        return caCert;
    }
    public static RSAPrivateKey readPKCS8PrivateKey(File file) throws Exception {
        KeyFactory factory = KeyFactory.getInstance("RSA");

        try (FileReader keyReader = new FileReader(file);
             PemReader pemReader = new PemReader(keyReader)) {

            PemObject pemObject = pemReader.readPemObject();
            byte[] content = pemObject.getContent();
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(content);
            return (RSAPrivateKey) factory.generatePrivate(privateKeySpec);
        }
    }
    public static SSLSocketFactory createSSLSocket(String certPath, String pkPath, String caPath) throws Exception {
        X509Certificate uCert = readCert(resourceLoader.getResource(certPath).getFile());
        X509Certificate caCert = readCert(resourceLoader.getResource(caPath).getFile());
        RSAPrivateKey pk = readPKCS8PrivateKey(resourceLoader.getResource(pkPath).getFile());

        KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
        caKs.load(null, null);
        caKs.setCertificateEntry("ca-certificate", caCert);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(caKs);

        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null,null);
        ks.setCertificateEntry("certificate", uCert);
        ks.setKeyEntry("private-key",pk, "".toCharArray(),new java.security.cert.Certificate[] {uCert});
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, "".toCharArray());
        SSLContext context = SSLContext.getInstance("TLSv1.3");
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(),null);
        return context.getSocketFactory();
    }
}

