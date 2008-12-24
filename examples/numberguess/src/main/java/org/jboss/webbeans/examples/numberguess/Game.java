package org.jboss.webbeans.examples.numberguess;


import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.webbeans.AnnotationLiteral;
import javax.webbeans.Current;
import javax.webbeans.Initializer;
import javax.webbeans.Named;
import javax.webbeans.SessionScoped;
import javax.webbeans.manager.Manager;

@Named
@SessionScoped
public class Game
{
   private int number;
   
   private int guess;
   private int smallest;
   private int biggest;
   private int remainingGuesses;
   
   @Current Manager manager;
   
   public Game()
   {
   }
   
   @Initializer
   Game(@MaxNumber int maxNumber)
   {      
      this.biggest = maxNumber;
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
   
   @PostConstruct
   public void reset()
   {
      this.smallest = 0;
      this.guess = 0;
      this.remainingGuesses = 10;
      this.number = manager.getInstanceByType(Integer.class, new AnnotationLiteral<Random>(){});
   }
   
}
