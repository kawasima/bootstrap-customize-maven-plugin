package net.unit8.maven.plugins.bootstrap.customize;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.webapp.WebAppContext;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;

/**
 * The maven plugin for customize Twitter bootstrap.
 *
 * @author kawasima
 */
@Mojo(name = "run")
public class RunMojo extends AbstractMojo{
    @Parameter
	private int port;

    @Parameter(defaultValue = "8080")
    private int startPort;

    @Parameter(defaultValue = "9000")
    private int endPort;

	@Parameter
	private File lessDirectory;

	@Parameter
	private File baseLessFile;

	@Parameter
	private File cssOutputFile;

    @Parameter
	private File templatePath;

    @Parameter(defaultValue = "${localRepository}")
	private ArtifactRepository localRepository;

	@Component
	private ArtifactFactory artifactFactory;

	@Component
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
        if (port == 0)
            scanPort();
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
            Desktop.getDesktop().browse(URI.create("http://localhost:" + port + "/"));
			server.join();
		} catch (Exception e) {
			throw new MojoExecutionException("Jetty Server Error", e);
		}
	}
    protected void scanPort() {
        for (int p = startPort; p <= endPort; p++) {
            try {
                Socket sock = new Socket("localhost", p);
                sock.close();
            } catch (IOException e) {
                port = p;
                return;
            }
        }
        throw new RuntimeException("Can't find available port from " + startPort + " to " + endPort);
    }


}
