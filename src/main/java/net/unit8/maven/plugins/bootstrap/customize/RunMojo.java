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

		Server server = new Server();
		SocketConnector socketConnector = new SocketConnector();
		socketConnector.setPort(port);
		Connector[] connectors = new Connector[]{ socketConnector };
		server.setConnectors(connectors);
		WebAppContext context = new WebAppContext();
		context.setContextPath("/");
		context.setWar(warFile.getAbsolutePath());
		server.setHandler(context);
		try {
			server.start();
			server.join();
		} catch (Exception e) {
			throw new MojoExecutionException("Jetty Server Error", e);
		}
	}

}
