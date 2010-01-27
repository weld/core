package org.jboss.weld.tests.producer.field.named;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

@Named("save")
@SessionScoped
public class SaveAction implements Serializable
{

   @Produces
   @Named
   private Employee employeeField = new Employee();

   private Employee employeeMethod = new Employee();
   
   private boolean executeCalled;

   @Produces
   @Named
   public Employee getEmployeeMethod()
   {
      return employeeMethod;
   }

   public SaveAction()
   {
   }

   public String execute()
   {
      assert employeeMethod.getName().equals("Gavin");
      assert employeeField.getName().equals("Pete");
      this.executeCalled = true;
      return "/home?faces-redirect=true";
   }
   
   public boolean isExecuteCalled()
   {
      return executeCalled;
   }
   
}
