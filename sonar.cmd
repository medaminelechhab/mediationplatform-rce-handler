git checkout develop
rem set BRANCH=`git branch | grep '* ' | cut -d" " -f2`
set SPRING_PROFILES_ACTIVE=test
set PROJECT_KEY=rce
set BRANCH=develop
mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent verify org.jacoco:jacoco-maven-plugin:report org.owasp:dependency-check-maven:check  ^
 -DretireJsAnalyzerEnabled=false -DassemblyAnalyzerEnabled=false -Dformats=html,json,xml ^
   sonar:sonar org.owasp:dependency-check-maven:check -DretireJsAnalyzerEnabled=false -DassemblyAnalyzerEnabled=false ^
       -Dformats=html,json,xml sonar:sonar -Dsonar.login=squ_33a66eb5c9927891f82413a9e8afaf7f02b214d8 ^
        -Dsonar.coverage.jacoco.xmlReportPaths=./target/*/TEST*.xml -Dsonar.host.url=https://sqaas.dos.tech.MyProject/ ^
        -Dsonar.dependencyCheck.htmlReportPath=./target/dependency-check-report.html  ^
        -Dsonar.dependencyCheck.jsonReportPath=./target/dependency-check-report.json ^
        -Dsonar.coverage.jacoco.xmlReportPaths=./target/site/jacoco/jacoco.xml ^
        -Dsonar.coverage.exclusions=**/model/* ^
 -Dsonar.projectKey=%PROJECT_KEY% -Dsonar.branch.name=%BRANCH% 