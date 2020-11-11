DIR := ${CURDIR}

all:
	mvn compile assembly:single
	mvn test-compile

exp_speed:
	java -Dproject_home=${DIR} \
	-classpath target/test-classes:target/quickSel-0.1-jar-with-dependencies.jar \
	-Xmx8g -Xms1g edu.illinois.quicksel.experiments.SpeedComparison

exp_dmv:
	java -Dproject_home=${DIR} \
	-classpath target/test-classes:target/quickSel-0.1-jar-with-dependencies.jar \
	-Xmx32g -Xms1g edu.illinois.quicksel.experiments.DMVSpeedComparison

exp_instacart:
	java -Dproject_home=${DIR} \
	-classpath target/test-classes:target/quickSel-0.1-jar-with-dependencies.jar \
	-Xmx32g -Xms1g edu.illinois.quicksel.experiments.InstacartSpeedComparison

exp_scan:
	java -Dproject_home=${DIR} \
	-classpath target/test-classes:target/quickSel-0.1-jar-with-dependencies.jar \
    -Xmx32g -Xms1g edu.illinois.quicksel.experiments.PerAttAndSamplingTest

exp_census:
	java -Dproject_home=${DIR} \
	-classpath target/test-classes:target/quickSel-0.1-jar-with-dependencies.jar \
    -Xmx32g -Xms16g edu.illinois.quicksel.experiments.Test census 10000 48842 10000

exp_forest:
	java -Dproject_home=${DIR} \
	-classpath target/test-classes:target/quickSel-0.1-jar-with-dependencies.jar \
    -Xmx32g -Xms16g edu.illinois.quicksel.experiments.Test forest 10000 581012 10000

exp_power:
	java -Dproject_home=${DIR} \
	-classpath target/test-classes:target/quickSel-0.1-jar-with-dependencies.jar \
    -Xmx32g -Xms16g edu.illinois.quicksel.experiments.Test power 10000 2075259 10000

exp_dmv:
	java -Dproject_home=${DIR} \
	-classpath target/test-classes:target/quickSel-0.1-jar-with-dependencies.jar \
    -Xmx32g -Xms16g edu.illinois.quicksel.experiments.Test dmv 10000 11591877 10000
