package javax.event;

/**
 * The contract between the manager and an observer object.
 * This interface should not be called directly by the application.
 * 
 * @author Gavin King
 * 
 */
public interface Observer<T>
{
   public boolean notify(T event);
}