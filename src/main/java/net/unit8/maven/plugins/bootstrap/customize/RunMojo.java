package net.unit8.maven.plugins.bootstrap.customize;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import winstone.Launcher;

public class RunMojo extends AbstractMojo{

	/** @parameter default-value="${localRepository}" */
	private ArtifactRepository localRepository;

	/** @component */
	private ArtifactFactory artifactFactory;

	/** @component */
	private ArtifactResolver resolver;

	public void execute() throws MojoExecutionException, MojoFailureException {
		Map<String, String> args = new HashMap<String, String>();
		Artifact artifact = artifactFactory
				.createArtifact("net.unit8.bootstrap", "bootstrap-customize-war", "0.1.0-SNAPSHOT", "runtime", "war");
		try {
			resolver.resolve(artifact, null, localRepository);
		} catch (Exception ex) {
			throw new MojoExecutionException("bootstrap-customize-war not found.", ex);
		}
		File warFile = artifact.getFile();

		args.put("warfile", warFile.getAbsolutePath());
		try {
			Launcher.initLogger(args);
			Launcher winstone = new Launcher(args);
			winstone.run();
		} catch (IOException e) {
			throw new MojoExecutionException("IOException", e);
		}
	}

}
