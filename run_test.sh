#!/bin/sh
# run sonar scanner aginst the provisioned sonarqube server
docker-compose run foreman /sonar-runner/sonar-scanner-2.6-SNAPSHOT/bin/sonar-scanner -X -e\
	-Dsonar.host.url=http://sonarqube:9000\
	-Dsonar.projectKey=some-test_ruby_plugin\
	"-Dsonar.projectName=Test Ruby Plugin"\
	-Dsonar.projectVersion=1.0\
	-Dsonar.sources=/usr/src/app\
	-Dsonar.projectBaseDir=/usr/src/app\
	-Dsonar.python.coverage.reportPath=coverage.xml\
	-Dsonar.language=ruby\
	"-Dsonar.inclusions=**/*.rb"\
	"-Dsonar.exclusions=test/**/*.rb,db/**/*.rb"\
	-Dsonar.ws.timeout=180\
