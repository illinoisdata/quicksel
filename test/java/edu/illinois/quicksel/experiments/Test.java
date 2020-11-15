package edu.illinois.quicksel.experiments;

import edu.illinois.quicksel.basic.AssertionReader;
import edu.illinois.quicksel.Assertion;
import edu.illinois.quicksel.Hyperrectangle;
import edu.illinois.quicksel.quicksel.QuickSel;
import edu.illinois.quicksel.isomer.Isomer;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.HashMap;

import static com.google.common.math.Quantiles.percentiles;

public class Test{
    public static void main(String[] args) throws IOException {
        String dataset = args[0];
        int train_num = Integer.parseInt(args[1]);
        long rows = Long.parseLong(args[2]);
        int var_num = Integer.parseInt(args[3]);
        double kernel_scale_factor = Double.parseDouble(args[4]);
        double constraint_weight = Double.parseDouble(args[5]);
        double eps = Double.parseDouble(args[6]);
        System.out.println("Project Directory : "+ System.getProperty("user.dir"));
        System.out.println(String.format("dataset: %s, train_num: %d, row_num: %d, var_num: %d", dataset, train_num, rows, var_num));
        System.out.println(String.format("kernel_scale_factor: %f, constraint_weight: %f", kernel_scale_factor, constraint_weight));

        Pair<Vector<Assertion>, Vector<Assertion>> assertionPair = AssertionReader.readAssertion(
            String.format("%s/train_assertion.csv", dataset),
            String.format("%s/permanent_assertion.csv", dataset)
        );
        Vector<Assertion> all_train_assertions = assertionPair.getLeft();
        Vector<Assertion> permanent_assertions = assertionPair.getRight();

        int columns = all_train_assertions.get(0).query.getConstraints().size();
        System.out.println(String.format("Dataset get %d columns", columns));
        Vector<Assertion> test_assertions = AssertionReader.readAssertion(String.format("%s/test_assertion.csv", dataset)).getLeft();

        System.out.println("# training set: " + all_train_assertions.size());
        System.out.println("# test set: " + test_assertions.size());
        System.out.println("# extra permanent query:" + permanent_assertions.size());

        // use first train_num of queries to train
        Vector<Assertion> train_assertions = new Vector<>(all_train_assertions.subList(0, train_num));
        System.out.println("# actual training set: " + train_assertions.size());
        System.out.println("Dataset and query set generations done.\n");

        String result_file = String.format("%s-var=%d-train=%d-kernel=%.1f-weight=%.0f.csv", dataset, var_num, train_num, kernel_scale_factor, constraint_weight);
        System.out.println("QuickSel test");
        quickSelTest(permanent_assertions, train_assertions, test_assertions, columns, rows,
            var_num, kernel_scale_factor, constraint_weight, eps, result_file);
        System.out.println("");
    }

    private static void quickSelTest(
        Vector<Assertion> permanent_assertions,
        Vector<Assertion> train_assertions,
        List<Assertion> test_assertions,
        int columns, long rows, int var_num,
        double kernel_scale_factor, double constraint_weight, double eps,
        String result_file) throws IOException{

        Pair<Hyperrectangle, Double> range_freq = computeMinMaxRange(columns);
        QuickSel quickSel = new QuickSel(range_freq.getLeft(), range_freq.getRight());
        quickSel.setEnforcedVarCount(var_num);
        quickSel.setKernelScaleFactor(kernel_scale_factor);
        quickSel.setConstraintWeight(constraint_weight);
        // quickSel.setSubpopulationModel("kmeans");

        for (Assertion a: permanent_assertions) {
            quickSel.addPermanentAssertion(a);
        }

        long time1 = System.nanoTime();
        for (Assertion a: train_assertions) {
            quickSel.addAssertion(a);
        }
        quickSel.prepareOptimization();

        long time2 = System.nanoTime();

        boolean debug_output = true;
        quickSel.assignOptimalWeights(debug_output);
        long time3 = System.nanoTime();

        System.out.println(String.format("Insertion time: %.3f, Optimization time: %.3f", (time2 - time1) / 1e9, (time3 - time2) / 1e9));
        System.out.println(String.format("Total construction time: %.4f mins", (time3 - time1) / 1e9 / 60));
        double per_sel = Math.max(0, quickSel.answer(permanent_assertions.get(0).query));
        System.out.println(String.format("Predict %.5f for permanent assertion", per_sel));

        FileWriter csvWriter = new FileWriter(result_file);
        csvWriter.append("id,error,predict,label,dur_ms\n");
        csvWriter.flush();

        double squared_err_sum = 0.0;
        double max_qerror = 0.0;
        double qerror_sum = 0.0;
        double latency_sum = 0.0;
        List<Double> qerrors = new ArrayList<>();
        for (int i = 0; i < test_assertions.size(); ++i) {
            Assertion q = test_assertions.get(i);
            long start_time = System.nanoTime();
            double sel = Math.max(0, quickSel.answer(q.query));
            sel += eps;
            long end_time = System.nanoTime();
            squared_err_sum += Math.pow(sel - q.freq, 2);

            // Q-Error is computed on cardinality instead of selectivity
            double card = Math.round(q.freq * rows);
            double est_card = Math.round(sel * rows);
            double qerror = computeQError(card, est_card);
            qerrors.add(qerror);
            if (max_qerror < qerror) {
                max_qerror = qerror;
            }
            qerror_sum += qerror;
            latency_sum += (end_time-start_time)/1e6;

            csvWriter.append(String.format("%d,%.6f,%.1f,%.1f,%.6f\n", i, qerror, est_card, card, (end_time-start_time)/1e6));
            csvWriter.flush();
        }
        csvWriter.close();
        double rms_err = Math.sqrt(squared_err_sum / test_assertions.size());
        double qerror_mean = qerror_sum / test_assertions.size(); 
        double latency_mean = latency_sum / test_assertions.size();

        double qerror99 = percentiles().index(99).compute(qerrors);
        double qerror90 = percentiles().index(90).compute(qerrors);
        double qerror50 = percentiles().index(50).compute(qerrors);

        System.out.println(String.format("Latency mean: %.5f", latency_mean));
        System.out.println(
            String.format("Q-Error: max=%.5f, mean=%.5f, q99=%.5f, q90=%.5f, q50=%.5f",
            max_qerror, qerror_mean, qerror99, qerror90, qerror50));
        System.out.println(String.format("RMS error: %.5f\n", rms_err));
    }

    private static double computeQError(double card, double est_card) {
        if (card == 0 && est_card == 0) {
            return 1.0;
        }
        if (card == 0) {
            return est_card;
        }
        if (est_card == 0) {
            return card;
        }
        if (est_card > card) {
            return est_card / card;
        }
        else {
            return card / est_card;
        }
    }

    private static Pair<Hyperrectangle, Double> computeMinMaxRange(int columns) {
        Vector<Pair<Double, Double>> min_max = new Vector<Pair<Double, Double>>();
        for (int i = 0; i < columns; ++i) {
            min_max.add(Pair.of(0.0, 1.0));
        }
        Hyperrectangle min_max_rec = new Hyperrectangle(min_max);
        double total_freq = 1.0;
        return Pair.of(min_max_rec, total_freq);
    }
}