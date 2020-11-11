package edu.illinois.quicksel.experiments;

import edu.illinois.quicksel.basic.AssertionReader;
import edu.illinois.quicksel.Assertion;
import edu.illinois.quicksel.Hyperrectangle;
import edu.illinois.quicksel.quicksel.QuickSel;
import edu.illinois.quicksel.isomer.Isomer;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.HashMap;

public class Test{
    public static void main(String[] args) throws IOException {
        String dataset = args[0];
        int train_num = Integer.parseInt(args[1]);
        long rows = Long.parseLong(args[2]);
        int var_num = Integer.parseInt(args[3]);
        System.out.println("Project Directory : "+ System.getProperty("user.dir"));
        System.out.println(String.format("dataset: %s, train_num: %d, row_num: %d, var_num: %d", dataset, train_num, rows, var_num));

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
        System.out.println("Dataset and query set generations done.\n");

        String result_file = String.format("%s-var=%d-train=%d.csv", dataset, var_num, train_num);
        System.out.println("QuickSel test");
        quickSelTest(permanent_assertions, train_assertions, test_assertions, columns, rows, var_num, result_file);
        System.out.println("");
    }

    private static void quickSelTest(
        Vector<Assertion> permanent_assertions,
        Vector<Assertion> train_assertions,
        List<Assertion> test_assertions,
        int columns, long rows, int var_num,
        String result_file) throws IOException{

        Pair<Hyperrectangle, Double> range_freq = computeMinMaxRange(columns);
        QuickSel quickSel = new QuickSel(range_freq.getLeft(), range_freq.getRight());
        quickSel.setEnforcedVarCount(var_num);
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

        boolean debug_output = false;
        quickSel.assignOptimalWeights(debug_output);
        long time3 = System.nanoTime();

        System.out.println(String.format("Insertion time: %.3f, Optimization time: %.3f", (time2 - time1) / 1e9, (time3 - time2) / 1e9));
        System.out.println(String.format("Total construction time: %.4f mins", (time3 - time1) / 1e9 / 60));
        double per_sel = Math.max(0, quickSel.answer(permanent_assertions.get(0).query));
        System.out.println(String.format("Predict %.1f for permanent assertion", per_sel));

        FileWriter csvWriter = new FileWriter(result_file);
        csvWriter.append("id,error,predict,label,dur_ms\n");
        csvWriter.flush();

        double squared_err_sum = 0.0;
        double max_qerror = 0.0;
        double qerror_sum = 0.0;
        double latency_sum = 0.0;
        for (int i = 0; i < test_assertions.size(); ++i) {
            Assertion q = test_assertions.get(i);
            long start_time = System.nanoTime();
            double sel = Math.max(0, quickSel.answer(q.query));
            long end_time = System.nanoTime();
            squared_err_sum += Math.pow(sel - q.freq, 2);

            // Q-Error is computed on cardinality instead of selectivity
            double card = Math.round(q.freq * rows);
            double est_card = Math.round(sel * rows);
            double qerror = computeQError(card, est_card);
            if (max_qerror < qerror) {
                max_qerror = qerror;
            }
            qerror_sum += qerror;
            latency_sum += (end_time-start_time)/1e6;

            csvWriter.append(String.format("%d,%.6f,%.1f,%.1f,%.6f\n", i, qerror, est_card, card, (end_time-start_time)/1e6));
            csvWriter.flush();

            if (++i % 1000 == 0) {
                System.out.println(String.format("%d queries done", i));
            }
        }
        csvWriter.close();
        double rms_err = Math.sqrt(squared_err_sum / test_assertions.size());
        double qerror_mean = qerror_sum / test_assertions.size(); 
        double latency_mean = latency_sum / test_assertions.size();

        System.out.println(String.format("Latency mean: %.5f", latency_mean));
        System.out.println(String.format("Q-Error: max=%.5f, mean=%.5f", max_qerror, qerror_mean));
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