package com.circulosiete.curso.testing.clase04.runners;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

@Suite
@SelectClasspathResource("features")
@ConfigurationParameter(
  key=GLUE_PROPERTY_NAME,
  value="com.circulosiete.curso.testing.clase04.steps" // Specify the package where step definitions are located
)
@ConfigurationParameter(
  key=PLUGIN_PROPERTY_NAME,
  value="pretty,html:target/cucumber-reports/clase04.html,summary" // Specify the output format and location for the report
)
public class Clase04CucumberRunner {
}
