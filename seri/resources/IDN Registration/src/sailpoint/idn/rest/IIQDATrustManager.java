package sailpoint.idn.rest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import javax.net.ssl.X509TrustManager;

public class IIQDATrustManager implements X509TrustManager {

  private KeyStore keystore;
  
	public IIQDATrustManager(KeyStore keystore) {
		this.keystore=keystore;
	}

	@Override
	public void checkClientTrusted(X509Certificate[] paramArrayOfX509Certificate,
			String paramString) throws CertificateException {
	}

	@Override
	public void checkServerTrusted(X509Certificate[] certChain,
			String paramString) throws CertificateException {
		X509Certificate cert=certChain[certChain.length-1];
		boolean hasCert=false;
		hasCert=hasCert(cert, keystore);
	}

	private boolean hasCert(X509Certificate cert, KeyStore keystore) {
		boolean hasCert=false;
		try {
			Enumeration<String> aliases=keystore.aliases();
			while (aliases.hasMoreElements() && !hasCert) {
				String alias=aliases.nextElement();
				try {
					cert.verify(keystore.getCertificate(alias).getPublicKey());
					hasCert=true;
					break;
				} catch (Exception e) {}
			}
    } catch (KeyStoreException e) {
//	    IIQDeploymentAccelerator.logException("Keystore exception", e);
    }
		return hasCert;
	}
	
	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return null;
	}

}
