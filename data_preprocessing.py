import pandas as pd
from pathlib import Path
import numpy as np

testset_ratio = 1/8

data = []
data_label = []
for i in range(len(sorted(Path('./data/capture_data/data/Black').glob('*.csv')))):
    history_path = sorted(Path('./data/capture_data/data/Black').glob('*.csv'))[-1*(i+1)]
    data.append(pd.read_csv(history_path).to_numpy().reshape(32, 32, 3))
    data_label.append(0)
for i in range(len(sorted(Path('./data/capture_data/data/White').glob('*.csv')))):
    history_path = sorted(Path('./data/capture_data/data/White').glob('*.csv'))[-1*(i+1)]
    data.append(pd.read_csv(history_path).to_numpy().reshape(32, 32, 3))
    data_label.append(1)
for i in range(len(sorted(Path('./data/capture_data/data/None').glob('*.csv')))):
    history_path = sorted(Path('./data/capture_data/data/None').glob('*.csv'))[-1*(i+1)]
    data.append(pd.read_csv(history_path).to_numpy().reshape(32, 32, 3))
    data_label.append(2)

data = np.array(data)
data_label = np.array(data_label).reshape(data.shape[0], 1)

s = np.arange(data.shape[0])
np.random.shuffle(s)
data = data[s]
data_label = data_label[s]

split_point = int(data.shape[0]*(1-testset_ratio))
train_data = data[0:split_point]
test_data = data[split_point:]
train_labels = data_label[0:split_point]
test_labels = data_label[split_point:]

np.save('./data/train_data', train_data)
np.save('./data/test_data', test_data)
np.save('./data/train_labels', train_labels)
np.save('./data/test_labels', test_labels)