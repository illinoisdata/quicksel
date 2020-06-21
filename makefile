DIR := ${CURDIR}

all:
	mvn compile assembly:single
	mvn test-compile

exp_speed:
	java -Dproject_home=${DIR} -Dplatform=linux \
        -classpath target/test-classes:target/quickSel-0.0.1-SNAPSHOT-jar-with-dependencies.jar \
	-Xmx8g -Xms1g experiments.SpeedComparison

exp_dmv:
	java -Dproject_home=${DIR} -Dplatform=linux \
        -classpath target/test-classes:target/quickSel-0.0.1-SNAPSHOT-jar-with-dependencies.jar \
	-Xmx32g -Xms1g experiments.DMVSpeedComparison

exp_instacart:
	java -Dproject_home=${DIR} -Dplatform=linux \
        -classpath target/test-classes:target/quickSel-0.0.1-SNAPSHOT-jar-with-dependencies.jar \
	-Xmx32g -Xms1g experiments.InstacartSpeedComparison

exp_scan:
	java -Dproject_home=${DIR} -Dplatform=linux \
				-classpath target/test-classes:target/quickSel-0.0.1-SNAPSHOT-jar-with-dependencies.jar \
  -Xmx32g -Xms1g experiments.PerAttAndSamplingTest
