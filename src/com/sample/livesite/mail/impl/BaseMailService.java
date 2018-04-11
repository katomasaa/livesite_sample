package com.sample.livesite.mail.impl;

import com.interwoven.livesite.common.service.AbstractService;
import com.interwoven.livesite.mail.MailService;
import com.interwoven.livesite.mail.MailSigningInformation;
import com.interwoven.livesite.system.Version;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import javax.mail.BodyPart;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.SMIMEException;
import org.bouncycastle.mail.smime.SMIMESignedGenerator;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

public class BaseMailService
  extends AbstractService
  implements MailService
{
  protected final Log mLogger = LogFactory.getLog(getClass());
  private static Certificate certificate;
  private static KeyPair keyPair;
  private static final String HASHING_ALGO_FOR_SIGNING = "SHA1with";
  private Collection mSmtpHosts;
  private Properties mConfiguration;
  private MailSigningInformation mailSigningInfo;

  static
  {
    Security.addProvider(new BouncyCastleProvider());
  }

  public BaseMailService()
  {
    this.mSmtpHosts = new ArrayList();
  }

  public void initService()
  {
    if (this.mLogger.isDebugEnabled())
    {
      this.mLogger.debug("initService: using SMTP host(s): " + getSmtpHosts().toString());
      this.mLogger.debug("initService: successfully initialized mail service.");
    }
  }

  public String getServiceDescription()
  {
    return "Interwoven LiveSite: Java Mail Service v" + Version.getProductVersion();
  }

  public void sendMail(Collection recipients, String sender, String subject, String body)
  {
    Collection parts = new ArrayList();
    BodyPart p = new MimeBodyPart();
    try
    {
      p.setText(body);
    }
    catch (MessagingException e)
    {
      throw new RuntimeException("Invalid body text, unable to set email body, message: " + e.getMessage(), e);
    }
    parts.add(p);
    sendMultiPartMailInternal(recipients, sender, subject, parts);
  }

  public void sendHtmlMail(Collection recipients, String sender, String subject, String body)
  {
    Collection parts = new ArrayList();
    BodyPart p = new MimeBodyPart();
    try
    {
      p.setContent(body, "text/html");
      p.setHeader("Content-Type", "text/html");
      p.setHeader("Content-Type", "text/html; charset=\"utf-8\"");
      p.setHeader("Content-Transfer-Encoding","8bit");
    }
    catch (MessagingException e)
    {
      throw new RuntimeException("Invalid body HTML, unable to set email body, message: " + e.getMessage(), e);
    }
    parts.add(p);
    sendMultiPartMailInternal(recipients, sender, subject, parts);
  }

  protected Collection mapToBodyParts(Map partMap)
  {
    Collection parts = new ArrayList();
    for (Iterator i = partMap.entrySet().iterator(); i.hasNext();)
    {
      Map.Entry entry = (Map.Entry)i.next();
      BodyPart p = new MimeBodyPart();
      try
      {
        p.setContent(entry.getValue().toString(), entry.getKey().toString());
      }
      catch (MessagingException e)
      {
        e.printStackTrace();
      }
    }
    return parts;
  }

  public void sendMultiPartMail(Collection recipients, String sender, String subject, Map parts)
  {
    sendMultiPartMailInternal(recipients, sender, subject, mapToBodyParts(parts));
  }

  protected void sendMultiPartMailInternal(Collection recipients, String sender, String subject, Collection parts)
  {
    Session session = getMailSession();
    if (null != session)
    {
      MimeMessage message = new MimeMessage(session);
      try
      {
        for (Iterator i = recipients.iterator(); i.hasNext();)
        {
          String recipient = (String)i.next();
          message.addRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(recipient));
        }
        if ((null != sender) && (!"".equals(sender.trim()))) {
          message.setFrom(new InternetAddress(sender));
        }
        message.setSubject(subject == null ? "" : subject);
        if (parts.size() > 1)
        {
          Multipart multiPart = new MimeMultipart();
          for (Iterator i = parts.iterator(); i.hasNext();) {
            multiPart.addBodyPart((BodyPart)i.next());
          }
          message.setContent(multiPart);
        }
        else
        {
          BodyPart part = (BodyPart)parts.iterator().next();
          message.setContent(part.getContent(), part.getContentType());
        }
        if (this.mailSigningInfo != null && this.mailSigningInfo.getSslEnabled() != null && this.mailSigningInfo.isSigningEnabled().booleanValue())
        {
          loadKeyStore(this.mailSigningInfo);
          MimeMultipart signedMM = signEmail(message);
          MimeMessage signedMessage = new MimeMessage(session);
          Enumeration<?> headers = message.getAllHeaderLines();
          while (headers.hasMoreElements()) {
            signedMessage.addHeaderLine((String)headers.nextElement());
          }
          signedMessage.setContent(signedMM);
          signedMessage.saveChanges();
          message = signedMessage;
        }
        Transport.send(message);
      }
      catch (Exception ex)
      {
        throw new RuntimeException("Error while sending mail: " + ex.getMessage(), ex);
      }
    }
  }

  protected Session getMailSession()
  {
        this.mLogger.debug("getMailSession Start.");
    Properties props = getConfiguration();

    if(props == null) {
        this.mLogger.debug("props is null.");
    }

        this.mLogger.debug("1");
    if (this.mailSigningInfo != null && this.mailSigningInfo.getSslEnabled() != null && this.mailSigningInfo.getSslEnabled().booleanValue())
    {
        this.mLogger.debug("2");
      if (props.getProperty("mail.smtp.socketFactory.class") == null) {
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
      }
      if (props.getProperty("mail.smtp.port") == null) {
        props.put("mail.smtp.port", "465");
      }
        this.mLogger.debug("3");
    }
    Session session = null;
        this.mLogger.debug("4");

    for (Iterator i = this.mSmtpHosts.iterator(); i.hasNext();)
    {
        this.mLogger.debug("5");
      String smtp = (String)i.next();
      this.mLogger.debug("SMTP : " + smtp);
      if (smtp.equalsIgnoreCase("dev.nul")) {
        return null;
      }
      props.put("mail.smtp.host", smtp);
      try
      {
        session = Session.getDefaultInstance(props);
        if (session != null)
        {
          session.setDebug(this.mLogger.isDebugEnabled());
          break;
        }
        this.mLogger.debug("6");
      }
      catch (RuntimeException rre)
      {
        if (this.mLogger.isErrorEnabled()) {
          this.mLogger.error("getMailSession: unable to connect to smtp server: " + smtp);
        }
      }
    }
        this.mLogger.debug("7");
    return session;
  }

  public Collection getSmtpHosts()
  {
    return this.mSmtpHosts;
  }

  public void setSmtpHosts(Collection hostnamesOrIps)
  {
    this.mSmtpHosts = hostnamesOrIps;
  }

  public Properties getConfiguration()
  {
    return this.mConfiguration;
  }

  public void setConfiguration(Properties config)
  {
    this.mConfiguration = config;
  }

  public MailSigningInformation getMailSigningInfo()
  {
    return this.mailSigningInfo;
  }

  public void setMailSigningInfo(MailSigningInformation mailSigningInfo)
  {
    this.mailSigningInfo = mailSigningInfo;
  }

  private static void loadKeyStore(MailSigningInformation config)
    throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, UnrecoverableKeyException
  {
    if ((certificate == null) || (keyPair == null))
    {
      FileInputStream fis = new FileInputStream(config.getKeyStoreLocation());
      KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      keyStore.load(fis, config.getKeyStorePassword().toCharArray());
      Key key = keyStore.getKey(config.getKeyAlias(), config.getKeyPassword().toCharArray());
      PublicKey pubKey = null;
      if ((key instanceof PrivateKey))
      {
        certificate = keyStore.getCertificate(config.getKeyAlias());
        pubKey = certificate.getPublicKey();
      }
      else
      {
        throw new UnrecoverableKeyException("Given key details does not contain private key information,  please check your keystore configuration.");
      }
      keyPair = new KeyPair(pubKey, (PrivateKey)key);
    }
  }

  private MimeMultipart signEmail(MimeMessage msg)
    throws NoSuchAlgorithmException, NoSuchProviderException, SMIMEException, OperatorCreationException, CertificateEncodingException
  {
    SMIMESignedGenerator signer = new SMIMESignedGenerator();

    ContentSigner sha1Signer = new JcaContentSignerBuilder("SHA1with" + keyPair.getPrivate().getAlgorithm()).build(keyPair.getPrivate());
    signer.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder()

      .build())
      .setDirectSignature(true)
      .build(sha1Signer, (X509Certificate)certificate));
    return signer.generate(msg);
  }
}
