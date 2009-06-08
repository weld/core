package org.jboss.webbeans.examples.numberguess;


import java.io.Serializable;

import javax.annotation.Named;
import javax.annotation.PostConstruct;
import javax.context.SessionScoped;
import javax.enterprise.inject.Instance;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.inject.Obtains;
import javax.naming.NamingException;

@Named
@SessionScoped
public class Game implements Serializable
{
   private int number;
   
   private int guess;
   private int smallest;
   
   @MaxNumber
   private int maxNumber;
   
   private int biggest;
   private int remainingGuesses;
   
   @Obtains @Random Instance<Integer> randomNumber;
   
   public Game() throws NamingException {}

   public int getNumber()
   {
      return number;
   }
   
   public int getGuess()
   {
      return guess;
   }
   
   public void setGuess(int guess)
   {
      this.guess = guess;
   }
   
   public int getSmallest()
   {
      return smallest;
   }
   
   public int getBiggest()
   {
      return biggest;
   }
   
   public int getRemainingGuesses()
   {
      return remainingGuesses;
   }
   
   public String check() throws InterruptedException
   {
      if (guess>number)
      {
         biggest = guess - 1;
      }
      if (guess<number)
      {
         smallest = guess + 1;
      }
      if (guess == number)
      {
         FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Correct!"));
      }
      remainingGuesses--;
      return null;
   }
   
   @PostConstruct
   public void reset()
   {
      this.smallest = 0;
      this.guess = 0;
      this.remainingGuesses = 10;
      this.biggest = maxNumber;
      this.number = randomNumber.get();;
   }
   
   public void validateNumberRange(FacesContext context,  UIComponent toValidate, Object value)
   {
      int input = (Integer) value;

      if (input < smallest || input > biggest) 
	   {
         ((UIInput)toValidate).setValid(false);

         FacesMessage message = new FacesMessage("Invalid guess");
         context.addMessage(toValidate.getClientId(context), message);
      }
   }
}
