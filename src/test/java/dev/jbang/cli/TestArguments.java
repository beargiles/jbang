package dev.jbang.cli;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dev.jbang.BaseTest;
import dev.jbang.Settings;

import picocli.CommandLine;

class TestArguments extends BaseTest {

	private CommandLine cli;

	@BeforeEach
	void setup() {
		cli = JBang.getCommandLine();
	}

	@Test
	public void testHelpSections() {
		JBang.getCommandRenderer().validate(JBang.getCommandLine().getHelp());
	}

	@Test
	public void testBasicArguments() {
		CommandLine.ParseResult pr = cli.parseArgs("run", "-h", "--debug", "myfile.java");
		Run run = (Run) pr.subcommand().commandSpec().userObject();

		assert run.helpRequested;
		assertThat(run.getRunContext().isDebugEnabled(), is(true));
		assertThat(run.scriptMixin.scriptOrFile, is("myfile.java"));
		assertThat(run.userParams.size(), is(0));

	}

	@Test
	public void testDoubleDebug() {
		CommandLine.ParseResult pr = cli.parseArgs("run", "--debug", "test.java", "--debug", "wonka");
		Run run = (Run) pr.subcommand().commandSpec().userObject();

		assertThat(run.getRunContext().isDebugEnabled(), is(true));
		assertThat(run.debugString, is("4004"));

		assertThat(run.scriptMixin.scriptOrFile, is("test.java"));
		assertThat(run.userParams, is(Arrays.asList("--debug", "wonka")));
	}

	/**
	 * @Test public void testInit() { cli.parseArgs("--init", "x.java", "y.java");
	 *       assertThat(main.script, is("x.java")); assertThat(main.params,
	 *       is(Arrays.asList("x.java", "y.java"))); }
	 **/

	@Test
	public void testStdInWithHelpParam() {
		CommandLine.ParseResult pr = cli.parseArgs("run", "-", "--help");
		Run run = (Run) pr.subcommand().commandSpec().userObject();

		assertThat(run.scriptMixin.scriptOrFile, is("-"));
		assertThat(run.helpRequested, is(false));
		assertThat(run.userParams, is(Collections.singletonList("--help")));
	}

	@Test
	public void testScriptWithHelpParam() {
		CommandLine.ParseResult pr = cli.parseArgs("run", "test.java", "-h");
		Run run = (Run) pr.subcommand().commandSpec().userObject();

		assertThat(run.scriptMixin.scriptOrFile, is("test.java"));
		assertThat(run.helpRequested, is(false));
		assertThat(run.userParams, is(Collections.singletonList("-h")));
	}

	@Test
	public void testDebugWithScript() {
		CommandLine.ParseResult pr = cli.parseArgs("run", "--debug", "test.java");
		Run run = (Run) pr.subcommand().commandSpec().userObject();

		assertThat(run.scriptMixin.scriptOrFile, is("test.java"));
		assertThat(run.getRunContext().isDebugEnabled(), is(true));
	}

	@Test
	public void testDebugPort() {
		CommandLine.ParseResult pr = cli.parseArgs("run", "--debug=*:5000", "test.java");
		Run run = (Run) pr.subcommand().commandSpec().userObject();

		assertThat(run.scriptMixin.scriptOrFile, is("test.java"));
		assertThat(run.getRunContext().isDebugEnabled(), is(true));
		assertThat(run.debugString, is("*:5000"));
	}

	@Test
	public void testDebugPortSeperateValue() {
		CommandLine.ParseResult pr = cli.parseArgs("run", "--debug", "xyz.dk:5005", "test.java");
		Run run = (Run) pr.subcommand().commandSpec().userObject();

		assertThat(run.scriptMixin.scriptOrFile, is("test.java"));
		assertThat(run.getRunContext().isDebugEnabled(), is(true));
		assertThat(run.debugString, is("xyz.dk:5005"));
	}

	@Test
	public void testSimpleScript() {
		CommandLine.ParseResult pr = cli.parseArgs("run", "test.java");
		Run run = (Run) pr.subcommand().commandSpec().userObject();

		assertThat(run.scriptMixin.scriptOrFile, is("test.java"));
	}

	@Test
	public void testClearCache() {
		Path dir = jbangTempDir;
		environmentVariables.set(Settings.JBANG_CACHE_DIR, dir.toString());
		assertThat(Files.isDirectory(dir), is(true));

		cli.execute("cache", "clear", "--all");

		assertThat(Files.isDirectory(dir.resolve("urls")), is(false));
		assertThat(Files.isDirectory(dir.resolve("jars")), is(false));
		assertThat(Files.isDirectory(dir.resolve("jdks")), is(false));
		assertThat(Files.notExists(Settings.getCacheDependencyFile()), is(true));
	}

}
