import io
import re
import subprocess
import os
import json

dir_path = os.path.dirname(os.path.realpath(__file__))


class Param(object):

	def __init__(self, num_var, kernel_scale_factor, constraint_weight, added_noise):
		self._num_var = num_var
		self._kernel_scale_factor = kernel_scale_factor
		self._constraint_weight = constraint_weight
		self._added_noise = added_noise

	def __eq__(self, another):
		return (self._num_var == another._num_var
			and self._kernel_scale_factor == another._kernel_scale_factor
			and self._constraint_weight == another._constraint_weight
			and self._added_noise == another._added_noise)

	def __hash__(self):
		return hash(self._num_var) ^ hash(self._kernel_scale_factor) ^ hash(self._constraint_weight) ^ hash(self._added_noise)

	def __repr__(self):
		return 'Param(num_var={}, kernel_scale_factor={}, constraint_weight={}, added_noise={})'.format(
			self._num_var, self._kernel_scale_factor, self._constraint_weight, self._added_noise)


def run_with_params(num_var, kernel_scale_factor, constraint_weight, added_noise, results):
	training_set_size = 10000
	proc = subprocess.Popen(
		[
			'java',
			'-Dproject_home=%s' % dir_path,
			'-classpath',
			'target/test-classes:target/quickSel-0.1-jar-with-dependencies.jar',
			'-Xmx16g', 
			'-Xms16g',
			'edu.illinois.quicksel.experiments.Test',
			'census',
			str(training_set_size),
			'48842',
			str(num_var),
			str(kernel_scale_factor),
			str(constraint_weight),
			str(added_noise)],
		stdout=subprocess.PIPE)

	lines = ''
	for line in io.TextIOWrapper(proc.stdout, encoding="utf-8"):
		lines += line
		print(line.rstrip())

	weighted_sum = re.search('weights sum: (.*)\n', lines).group(1)
	avg_l1_gap = re.search('avg l1 gap: (.*)\n', lines).group(1)
	qerror_match = re.search('Q-Error: max=(.*), mean=(.*), q99=(.*), q90=(.*), q50=(.*)\n', lines)
	q_error_max = qerror_match.group(1)
	q_error_mean = qerror_match.group(2)
	q_error_99 = qerror_match.group(3)
	q_error_90 = qerror_match.group(4)
	q_error_50 = qerror_match.group(5)
	rms_error = re.search('RMS error: (.*)\n', lines).group(1)

	results[Param(num_var, kernel_scale_factor, constraint_weight, added_noise)] = {
		"weighted_sum": weighted_sum,
		"avg_l1_gap": avg_l1_gap,
		"q_error_max": q_error_max,
		"q_error_mean": q_error_mean,
		"q_error_99": q_error_99,
		"q_error_90": q_error_90,
		"q_error_50": q_error_50,
		"rms_error": rms_error
	}
	print(json.dumps(results))


if __name__ == "__main__":
	results = {}
	num_var = 10000
	for eps in [0, 1e-9, 1e-8]:
		for kernel_scale_factor in [0.5, 1.0, 1.5, 2.0]:
			for constraint_weight in [1e5, 5e5, 1e6, 5e6, 1e7]:
				run_with_params(num_var, kernel_scale_factor, constraint_weight, eps, results)
	print('\n\n\n\n')
	print(json.dumps(results))
	print('\n\n')

