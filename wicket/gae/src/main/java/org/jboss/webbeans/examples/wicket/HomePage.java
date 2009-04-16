package org.jboss.webbeans.examples.wicket;

import javax.inject.Current;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.target.component.listener.BehaviorRequestTarget;

public class HomePage extends WebPage {

	private static final long serialVersionUID = 1L;

	@Current Game game;

    public HomePage() {

    	
        Form form = new Form("NumberGuessMain");
        add(form);
        form.add(new FeedbackPanel("messages").setOutputMarkupId(true));
        
        final Component prompt = new Label("prompt", new Model() { 
        	@Override
        	public Object getObject() {
        		return "I'm thinking of a number between " + game.getSmallest() + " and " + game.getBiggest() + 
        		".  You have " + game.getRemainingGuesses() + " guesses.";
        	}
        });
        form.add(prompt);
        
        final Component guessLabel = new Label("guessLabel","Your Guess:");
        form.add(guessLabel);
        final Component inputGuess = new TextField("inputGuess",new Model() { 
        	public Object getObject() {
        		return game.getGuess();
        	}
        	public void setObject(Object object) {
        		game.setGuess(Integer.parseInt((String)object));
        	}
        });
        form.add(inputGuess);
        
        final Component guessButton = new AjaxButton("GuessButton") { 
        	protected void onSubmit(AjaxRequestTarget target, Form form) {
        		if (game.check()) {
        			info("Correct!");
        			setVisible(false);
        			prompt.setVisible(false);
        			guessLabel.setVisible(false);
        			inputGuess.setVisible(false);
        		}
        		else if (game.getNumber() > game.getGuess())
        			info("Higher!");
        		else if (game.getNumber() < game.getGuess())
        			info("Lower");
        		target.addComponent(form);
        	}	
        };
        form.add(guessButton);
        
        form.add(new AjaxButton("RestartButton") { 
        	protected void onSubmit(AjaxRequestTarget target, Form form) {
        		game.reset();
        		guessButton.setVisible(true);
        		prompt.setVisible(true);
        		guessLabel.setVisible(true);
        		inputGuess.setVisible(true);
        		target.addComponent(form);
        	}
        });
        
    }
}
