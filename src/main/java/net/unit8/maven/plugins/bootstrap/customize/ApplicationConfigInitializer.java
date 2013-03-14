package net.unit8.maven.plugins.bootstrap.customize;

import org.apache.maven.plugin.MojoExecutionException;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ApplicationConfigInitializer {
	private Object applicationConfig;
	private final Map<String, Method> setterMethods = new HashMap<String, Method>();

	public ApplicationConfigInitializer(Class<?> applicationConfigClass) throws MojoExecutionException {
		Method instanceMethod = null;
		for (Method method : applicationConfigClass.getMethods()) {
			if (method.getName().equals("instance")) {
				instanceMethod = method;
			} else if (method.getName().startsWith("set") && method.getParameterTypes().length == 1) {
				String propertyName = method.getName().substring(3);
				propertyName = propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1);
				setterMethods.put(propertyName, method);
			}
		}

		if (instanceMethod == null) {
			throw new MojoExecutionException("Type mismatch: ApplicationConfig class");
		}
		try {
			applicationConfig = instanceMethod.invoke(null, (Object[])null);
		} catch (Exception e) {
			throw new MojoExecutionException("Instantiation failure: ApplicationConfig");
		}
	}
	public void set(String propertyName, Object value) throws MojoExecutionException {
		Method setter = setterMethods.get(propertyName);
		if (setter == null)
			throw new MojoExecutionException("property " + propertyName + " not found.");
		try {
			setter.invoke(applicationConfig, value);
		} catch (Exception e) {
			throw new MojoExecutionException("Property set failure: " + propertyName);
		}
	}

	public void setIfNotNull(String propertyName, Object value) throws MojoExecutionException {
		if (value != null)
			set(propertyName, value);
	}
}
