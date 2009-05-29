package org.jboss.webbeans.examples.wicket;


import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.AnnotationLiteral;
import javax.enterprise.inject.Current;
import javax.enterprise.inject.Named;
import javax.enterprise.inject.spi.BeanManager;
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
   
   @Current BeanManager manager;
   
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

   public boolean check() 
   {
      if (guess>number)
      {
         biggest = guess - 1;
      }
      if (guess<number)
      {
         smallest = guess + 1;
      }
      remainingGuesses--;
      return (guess == number);
   }

   
   @PostConstruct
   public void reset()
   {
      this.smallest = 0;
      this.guess = 0;
      this.remainingGuesses = 10;
      this.biggest = maxNumber;
      this.number = manager.getInstanceByType(Integer.class, new AnnotationLiteral<Random>(){});
   }
   

}
