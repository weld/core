package org.jboss.webbeans.examples.numberguess;


import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.webbeans.Initializer;
import javax.webbeans.Named;
import javax.webbeans.SessionScoped;

@Named
@SessionScoped
public class Game
{
   private int number;
   
   private int guess;
   private int smallest;
   private int biggest;
   private int remainingGuesses;
   
   public Game()
   {
   }
   
   @Initializer
   Game(@Random int number, @MaxNumber int maxNumber)
   {
      this.number = number;
      this.smallest = 1;
      this.biggest = maxNumber;
      this.remainingGuesses = 10;
   }

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
   
   public String check()
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
   
}
