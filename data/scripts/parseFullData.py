from shutil import copyfile
def moveData():
	train_file_name = "./split/train"
	test_file_name = "./split/test"
	train_list = list()
	test_list = list()
	with open(train_file_name) as f:
		train_list = f.readlines()
	with open(test_file_name) as f:
		test_list = f.readlines()
	train_list = [x.strip() for x in train_list]
	test_list = [x.strip() for x in test_list]

	bn_list = list()
	bc_list = list()
	cts_list = list()
	nw_list = list()
	un_list = list()
	wl_list = list()
	with open("../full/bn/FileList") as f:
		bn_list = f.readlines()
	bn_list = [x.strip().split("\t")[0] for x in bn_list]
	with open("../full/bc/FileList") as f:
		bc_list = f.readlines()
	bc_list = [x.strip().split("\t")[0] for x in bc_list]
	with open("../full/cts/FileList") as f:
		cts_list = f.readlines()
	cts_list = [x.strip().split("\t")[0] for x in cts_list]
	with open("../full/nw/FileList") as f:
		nw_list = f.readlines()
	nw_list = [x.strip().split("\t")[0] for x in nw_list]
	with open("../full/un/FileList") as f:
		un_list = f.readlines()
	un_list = [x.strip().split("\t")[0] for x in un_list]
	with open("../full/wl/FileList") as f:
		wl_list = f.readlines()
	wl_list = [x.strip().split("\t")[0] for x in wl_list]
	
	extensions = [".ag.xml", ".apf.xml", ".sgm", ".tab"]
	
	for tf in test_list:
		if tf in bn_list:
			for e in extensions:
				copyfile("../full/bn/timex2norm/" + tf + e, "../full/test/bn/" + tf + e)
		elif tf in nw_list:
			for e in extensions:
				copyfile("../full/nw/timex2norm/" + tf + e, "../full/test/nw/" + tf + e)
		elif tf in bc_list:
			for e in extensions:
				copyfile("../full/bc/timex2norm/" + tf + e, "../full/test/bc/" + tf + e)
		elif tf in wl_list:
			for e in extensions:
				copyfile("../full/wl/timex2norm/" + tf + e, "../full/test/wl/" + tf + e)
		else:
			print "[ERROR]: " + tf  + "not found"
			

		

moveData()
