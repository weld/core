package javax.webbeans;

public class ExecutionException extends RuntimeException
{

   public ExecutionException()
   {
      super();
   }

   public ExecutionException(String message, Throwable throwable)
   {
      super(message, throwable);
   }

   public ExecutionException(String message)
   {
      super(message);
   }

   public ExecutionException(Throwable throwable)
   {
      super(throwable);
   }
   
}
