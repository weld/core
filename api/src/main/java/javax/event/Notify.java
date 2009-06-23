package javax.event;

/**
 * An enumeration that is used to declare the condition under which an
 * observer method should be called. The default behavior is to
 * create the bean and invoke the observer method synchronously.
 * 
 * @author Gavin King
 * @author Dan Allen
 */
public enum Notify {
	/**
	 * Specifies that an observer method is only called if the current instance of
	 * the bean declaring the observer method already exists.
	 */
	IF_EXISTS,
	
	/**
	 * Specifies that an observer method is called synchronously.
	 */
	SYNCHRONOUSLY,
	
	/**
	 * Specifies that an observer method receives the event notifications
	 * asynchronously.
	 */
	ASYNCHRONOUSLY
}