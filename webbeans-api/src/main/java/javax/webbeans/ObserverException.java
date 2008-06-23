package javax.webbeans;

public class ObserverException extends RuntimeException
{

   public ObserverException()
   {
      
   }

   public ObserverException(String message)
   {
      super(message);
   }

   public ObserverException(Throwable cause)
   {
      super(cause);
   }

   public ObserverException(String message, Throwable cause)
   {
      super(message, cause);
   }

}
