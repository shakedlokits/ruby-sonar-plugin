SonarQube Ruby Plugin [![Build Status](https://travis-ci.org/shakedlokits/ruby-sonar-plugin.svg?branch=master)](https://travis-ci.org/shakedlokits/ruby-sonar-plugin)
=================

##Description
This plugin enables publishing static and dynamic analysis reports of Ruby projects onto SonarQube 5.6.3+  

The plugin currently supports the following services:
* Lines of code, Number of classes, Comment precentage
* Code Complexity (file complexity)
* Code Smells
* Code Coverage (line coverage)
* Coding Style Violations

It relies on standardized external tools: [SimpleCov](https://github.com/colszowka/simplecov), [SimpleCov-RCov](https://github.com/fguillen/simplecov-rcov) and [Metric_Fu](https://github.com/metricfu/metric_fu/) in order to make the analysis and report the metrics which are then in turn published to SonarQube.

##Install
1. Run `mvn install` to produce the `target/sonar-ruby-plugin-VERSION.jar` plugin file
2. Place the plugin file into the `SONARQUBE_HOME/extensions/plugins` directory

##Usage
Make sure the property sonar.language is set to ruby: `sonar.language=ruby` in the sonar-project.properties file as well as the `sonar.exclusions` and `sonar.inclusions` patterns


#####Code Coverage
In order for the plugin to report on code coverage, the ruby project needs to be using [simplecov-rcov](https://github.com/fguillen/simplecov-rcov)
to generate a coverage report when you run your tests/specs, please see the gem's homepage [here](https://github.com/fguillen/simplecov-rcov) for installation
and usage instructions.  
Generally, you will need to add to your test suite a snippet similiar to:

    # Run simplecov code coverage for sonar integration  
    if( ENV['COVERAGE'] == 'on' )  

      # Include requirements  
      require 'simplecov'  
      require 'simplecov-rcov'  

      # Set formatter as rcov to support third-party plugin  
      SimpleCov.formatters = SimpleCov::Formatter::RcovFormatter  

      SimpleCov.start 'rails' do  
        add_group 'API', 'app/controllers/api'  
      end  
    end
**Important:** Do not change the output directory for the simplecov-rcov report, leave it as default, or code coverage will not be reported.

#####Code Complexity
In order for the plugin to report on code complexity, [metric_fu](https://github.com/metricfu/metric_fu/) needs to be ran against the ruby project,
which will generate a metric report. Please see the [gem's homepage](https://github.com/metricfu/metric_fu/) for installation and usage instructions.  

**Important:**  We recommend using metric_fu by running `metric_fu -r --no-flog --no-flay --no-roodi --no-open` which would analyze and report all of the metrics supported by the plugin. Such as Saikuro/Cane coverage, Cane issues, Hostpots, Code smells and more..

##Future Plans
* Code Duplication
* Structural Analysis
* Code Debt

##Giving Credit
The github project [pica/ruby-sonar-plugin](https://github.com/pica/ruby-sonar-plugin), is where the ruby-sonar-plugin started, bringing basic project statistics.  
It was then carried forward by [GoDaddy-Hosting/ruby-sonar-plugin](https://github.com/GoDaddy-Hosting/ruby-sonar-plugin), bringing code coverage and code complexity as well as updating it to SonarQube 4.5.5 LTS.  
We took this project as a reference point, updated to the latest SonarQube LTS v5.6.3, fixed broken sensor bugs, added code smells, added unit tests and refactored a large portion of the code base. *Basically adding new functionality and cleaning the house.*

We referenced the [Java Sonar Plugin](https://github.com/SonarSource/sonar-java) and the [Python Sonar Plugin](https://github.com/SonarSource/sonar-python) for the code coverage sensor and the static analysis reporting methodology.

##Tool Versions
This plugin has been tested with the following dependency versions:
* [SonarQube](http://www.sonarqube.org/downloads/) 5.6.3 LTS
* SonarQube Runner 2.7 (or newer)
* metric_fu gem version 4.12.0 (latest at time of edit)
* simplecov 0.12.0
* simplecov-rcov 0.2.3
