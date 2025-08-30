package com.circulosiete.curso.minibank.bdd;

import org.junit.platform.suite.api.*;
import io.cucumber.junit.platform.engine.Constants;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features") // looks under src/test/resources/features
@ConfigurationParameter(
    key = Constants.GLUE_PROPERTY_NAME,
    value = "com.circulosiete.curso.minibank.bdd"
)
@ConfigurationParameter(
    key = Constants.PLUGIN_PROPERTY_NAME,
    value = "pretty, html:target/RunCucumberTests.html"
)
public class RunCucumberTests {
}
