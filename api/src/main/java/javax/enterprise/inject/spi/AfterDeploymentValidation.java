package javax.enterprise.inject.spi;

public interface AfterDeploymentValidation 
{
   
   public void addDeploymentProblem(Throwable t);

}
