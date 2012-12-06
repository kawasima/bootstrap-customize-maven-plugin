package net.unit8.maven.plugins.bootstrap.customize;

import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * The maven plugin for customize Twitter bootstrap.
 *
 * @author kawasima
 * @goal run
 */
public class RunMojo extends AbstractMojo{
	/** @parameter */
	private int port = 8090;

	/** @parameter */
	private File lessDirectory;

	/** @parameter */
	private File baseLessFile;

	/** @parameter */
	private File cssOutputFile;

	/** @parameter */
	private File templatePath;

	/** @parameter default-value="${localRepository}" */
	private ArtifactRepository localRepository;

	/** @component */
	private ArtifactFactory artifactFactory;

	/** @component */
	private ArtifactResolver resolver;

	public void execute() throws MojoExecutionException, MojoFailureException {
		Artifact artifact = artifactFactory
				.createArtifact("net.unit8.bootstrap", "bootstrap-customize-war", "0.1.0-SNAPSHOT", "runtime", "war");
		try {
			resolver.resolve(artifact, null, localRepository);
		} catch (Exception ex) {
			throw new MojoExecutionException("bootstrap-customize-war not found.", ex);
		}
		File warFile = artifact.getFile();

		final Server server = new Server();
		SocketConnector socketConnector = new SocketConnector();
		socketConnector.setPort(port);
		Connector[] connectors = new Connector[]{ socketConnector };
		server.setConnectors(connectors);
		final WebAppContext context = new WebAppContext();
		context.setContextPath("/");
		context.setWar(warFile.getAbsolutePath());
		server.setHandler(context);
		context.addLifeCycleListener(new AbstractLifeCycle.AbstractLifeCycleListener() {
			public void lifeCycleStarting(LifeCycle event) {
				if (templatePath != null)
					context.setInitParameter("templatePath", "file://" + templatePath.getAbsolutePath());
			}
			public void lifeCycleStarted(LifeCycle event) {
				try {
					Class<?> applicationConfigClass = context
							.loadClass("net.unit8.bootstrap.customize.config.ApplicationConfig");
					ApplicationConfigInitializer initializer = new ApplicationConfigInitializer(applicationConfigClass);
					initializer.setIfNotNull("baseLessFile", baseLessFile);
					initializer.setIfNotNull("lessDirectory", lessDirectory);
					initializer.setIfNotNull("cssOutputFile", cssOutputFile);
					initializer.setIfNotNull("templatePath",  templatePath);
				} catch (Exception e) {
					getLog().error(e);
					if (server.isRunning()) {
						try {
							server.stop();
						} catch (Exception ignore) {}

					}
				}
			}
		});
		try {
			server.setStopAtShutdown(true);
			server.start();
			server.join();
		} catch (Exception e) {
			throw new MojoExecutionException("Jetty Server Error", e);
		}
	}


}
