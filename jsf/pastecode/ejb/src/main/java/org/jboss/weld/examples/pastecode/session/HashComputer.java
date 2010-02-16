package org.jboss.weld.examples.pastecode.session;

import org.jboss.weld.examples.pastecode.model.Code;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;

public class HashComputer
{
   public HashComputer()
   {
   }

   public String getHashValue(Code code) throws NoSuchAlgorithmException
   {
      String hashValue;
      MessageDigest md = MessageDigest.getInstance("SHA-1");
      String combinedValue = code.getText() + code.getDatetime();
      md.update(combinedValue.getBytes());
      hashValue = asHex(md.digest());
      return hashValue;
   }

   private String asHex(byte buf[])
   {
      StringBuffer strBuf = new StringBuffer(buf.length * 2);

      for (int i = 0; i < buf.length; i++)
      {
         if (((int) buf[i] & 0xff) < 0x10)
            strBuf.append("0");
         strBuf.append(Long.toString((int) buf[i] & 0xff, 16));
      }

      return strBuf.toString();
   }
}
