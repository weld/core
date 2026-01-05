package org.jboss.weld.environment.se.test.discovery.inheritedScope;

// this class inherits the scope and declares no BDA itself
// it should therefore NOT be discovered with `annotated` discovery mode
public class OnlyInheritedScopeBean extends ProperBean {
}
