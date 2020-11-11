DIR := ${CURDIR}

all:
	mvn compile assembly:single
	mvn test-compile

exp_speed:
	java -Dproject_home=${DIR} \
	-classpath target/test-classes:target/quickSel-0.1-jar-with-dependencies.jar \
	-Xmx8g -Xms1g edu.illinois.quicksel.experiments.SpeedComparison

# exp_dmv:
# 	java -Dproject_home=${DIR} \
# 	-classpath target/test-classes:target/quickSel-0.1-jar-with-dependencies.jar \
# 	-Xmx32g -Xms1g edu.illinois.quicksel.experiments.DMVSpeedComparison

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
    -Xmx32g -Xms16g edu.illinois.quicksel.experiments.Test census 1000 48842 1000 1.0 1e6

exp_forest:
	java -Dproject_home=${DIR} \
	-classpath target/test-classes:target/quickSel-0.1-jar-with-dependencies.jar \
    -Xmx32g -Xms16g edu.illinois.quicksel.experiments.Test forest 1000 581012 1000 1.0 1e6

exp_power:
	java -Dproject_home=${DIR} \
	-classpath target/test-classes:target/quickSel-0.1-jar-with-dependencies.jar \
    -Xmx32g -Xms16g edu.illinois.quicksel.experiments.Test power 1000 2075259 1000 1.0 1e6

exp_dmv:
	java -Dproject_home=${DIR} \
	-classpath target/test-classes:target/quickSel-0.1-jar-with-dependencies.jar \
    -Xmx32g -Xms16g edu.illinois.quicksel.experiments.Test dmv 1000 11591877 1000 1.0 1e6

exp_synth:
	java -Dproject_home=${DIR} \
	-classpath target/test-classes:target/quickSel-0.1-jar-with-dependencies.jar \
    -Xmx32g -Xms16g edu.illinois.quicksel.experiments.Test synth 1000 1000000 1000 1.0 1e6

exp_synth1:
	java -Dproject_home=${DIR} \
	-classpath target/test-classes:target/quickSel-0.1-jar-with-dependencies.jar \
    -Xmx32g -Xms16g edu.illinois.quicksel.experiments.Test synth1 1000 1000000 1000 1.0 1e6


