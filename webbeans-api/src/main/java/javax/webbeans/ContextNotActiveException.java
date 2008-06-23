package javax.webbeans;

public class ContextNotActiveException extends RuntimeException
{

   public ContextNotActiveException()
   {
   }

   public ContextNotActiveException(String message)
   {
      super(message);
   }

   public ContextNotActiveException(Throwable cause)
   {
      super(cause);
   }

   public ContextNotActiveException(String message, Throwable cause)
   {
      super(message, cause);
   }

}
