'''
This program is a partitioner for ACE dataset
It randomly shuffles the file order
Then splits all the documents into 5 chunks by category
Finally it dumps a five-fold CV set
e.g.
/PATH_TO_DATA/parition/eval/0 is the test set for fold 0
/PATH_TO_DATA/partition/train/0 is the train set for fold 0
Usage:
Modify data path accordingly, then run
python partitioner.py 0.2
'''
import sys
import os
import math
import random
import shutil

'''
The variables below marks the default value for data paths
original_path: path to originaldata
train_path: path to store future train sets
			The program generates five path under this path
eval_path: path to store future test sets
			The program generates five path under this path
anchor_file: the name of the anchor file for the ACE set
'''
original_path = "./data/original"
train_path = "./data/partition/train"
eval_path = "./data/partition/eval"
anchor_file = "apf.v5.1.1.dtd"
eval_size = 0
extensions = [".ag.xml", ".apf.xml", ".apf.xml.score", ".sgm", ".tab"]


def main():
	if (len(sys.argv) < 2):
		print "Usage: python partitioner.py [eval_portion]"
		return
	global eval_size
	eval_size = (float)(sys.argv[1])
	if (eval_size <= 0.0 or eval_size >= 1.0):
		print "[ERROR]: eval_portion should be within 0 to 1"
		return
	if (not os.path.isdir(original_path)):
		print "[ERROR]: original_path does not exist"
		return
	clear_path(train_path)
	clear_path(eval_path)
	init_partition()

'''
Delete all the contents under the to-store paths
'''
def clear_path(dir_name):
	for root, dirs, files in os.walk(dir_name):
		for f in files:
			path = root + "/" + f
			os.remove(path)
	
'''
Actual parition handler
All splits are random
'''
def init_partition():
	targets = list()
	for root, dirs, files in os.walk(original_path):
		if (root == original_path):
			for d in dirs:
				targets.append(d)
	for target in targets:
		target_files = list()
		for root, dirs, files in os.walk(original_path + "/" + target):
			target_files = files
		target_files.remove(anchor_file)
		if (len(target_files) % 5 != 0):
			print "[ERROR]: Invalid dataset " + target
			return
		file_groups = set()
		for tf in target_files:
			file_info = tf.split(".")
			file_name = file_info[0] + "." + file_info[1]
			file_groups.add(file_name)
				
		eval_file_num = (int)(math.ceil((float)(len(file_groups)) * eval_size)) 
		file_groups = list(file_groups)
		random.shuffle(file_groups)
		for fold in range (0, 5): 
			eval_start = fold * eval_file_num
			eval_end = (fold + 1) * eval_file_num
			if (eval_end > len(file_groups)):
				eval_end = len(file_groups)
			eval_files = file_groups[eval_start : eval_end]
			train_files = file_groups[0:eval_start] + file_groups[eval_end:]
			print "Fold: " + str(fold)
			print "Total: " + str(len(file_groups))
			print "eval_size: " + str(len(eval_files))
			print "train_size: " + str(len(train_files))

			if not os.path.exists(eval_path + "/" + str(fold) + "/" + target):
				os.makedirs(eval_path + "/" + str(fold) + "/" + target)
			if not os.path.exists(train_path + "/" + str(fold) + "/" + target):
				os.makedirs(train_path + "/" + str(fold) + "/" + target)

			for ef in eval_files:
				for e in extensions:
					from_path = original_path + "/" + target + "/" + ef + e
					to_path = eval_path + "/" + str(fold) + "/" + target + "/" + ef + e
					shutil.copy(from_path, to_path)
			for tf in train_files:
				for e in extensions:
					from_path = original_path + "/" + target + "/" + tf + e
					to_path = train_path + "/" + str(fold) + "/" + target + "/" + tf + e
					shutil.copy(from_path, to_path)
			anchor_path = original_path + "/" + target + "/" + anchor_file
			shutil.copy(anchor_path, eval_path + "/" + str(fold) + "/" + target + "/" + anchor_file)
			shutil.copy(anchor_path, train_path + "/" + str(fold) + "/" + target + "/" + anchor_file)

if __name__ == "__main__":
	main()
