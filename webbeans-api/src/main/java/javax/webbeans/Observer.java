package javax.webbeans;

/**
 * The contract between the Web Bean manager and a Web Beans observer object.
 * This interface should not be called directly by the application.
 * 
 * @author Gavin King
 * 
 */
public interface Observer<T>
{
   public void notify(T event);
}