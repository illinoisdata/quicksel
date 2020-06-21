# QuickSel: Quick Selectivity Learning with Mixture Models

This repository contains the code for the QuickSel paper published in SIGMOD 2020. The manuscript is also available from [this arxiv link](https://arxiv.org/abs/1812.10568).



# Download and install

Implemented in Java (and Maven). After cloning this repository, running `make all` will build the project. To build the project, of course, you will need JDK and Apache Maven installed on your machine.



# Test

We provides some tests to show the performance of QuickSel. In the project directory, after project is built successfully, you can issue:
1. `make exp_speed` to run the speed test on synthetic generated data.
1. `make exp_dmv` to run the test on DMV dataset. 
1. `make exp_instacart` to run the test on Instacart dataset. 
1. `make exp_scan` to run the test to compare with scan-based methods such as sampling and histogram. 



# Misc.

The repository includes some other test code we wrote in the process of working on QuickSel. For example, `src/cpp/qpsolver` includes JNI-based code for calling `cvxopt`'s Quadratic Programming (QP) solver. Since QuickSel analytically computes the solution, the JNI-based code is not needed. However, if you want to re-enable the QP feature, extra steps are required (which is not currently documented here).

