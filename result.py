import sys
import csv
import numpy as np

if __name__ == '__main__':
    file_name = sys.argv[1]
    with open(file_name, 'r') as ofile:
        reader = csv.reader(ofile)
        next(reader, None)  # skip the headers
        errors = []
        for line in reader:
            errors.append(float(line[1]))
    
    metrics = {
        'max': np.max(errors),
        '99th': np.percentile(errors, 99),
        '95th': np.percentile(errors, 95),
        '90th': np.percentile(errors, 90),
        'median': np.median(errors),
        'mean': np.mean(errors)
    }

    print(metrics)