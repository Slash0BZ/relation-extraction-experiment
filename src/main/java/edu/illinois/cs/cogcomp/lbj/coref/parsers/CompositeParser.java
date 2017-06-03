package edu.illinois.cs.cogcomp.lbj.coref.parsers;

import java.util.Random;

import edu.illinois.cs.cogcomp.lbj.coref.Parameters;


import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;

/*
 * Eric stores all the documents in the memory, which is kind of ok in ACE (requires 4g)
 * but in CoNLL we have 10 times more data - hence we cannot load all of it into the memory (40G)
 * The quick and dirty solution is to break the CoNLL training set to 10 parts, and to implement
 * a parser which takes the 10 parsers over the parts and provides an abstraction over them
 */
public class CompositeParser implements edu.illinois.cs.cogcomp.lbjava.parse.Parser {
	public Random rand = new Random(31);
	public static enum TaskName {CoNLL, ACE};
	//public static String ACE_TrainAll = "./conll/ACE_TRAINANDDEV_NONTRANSCRIPTS"; // "conll/ACE2004_TRAINANDDEV_ALL";  
	//public static String ACE_TestAll = "./conll/ACE_TEST_NONTRANSCRIPTS"; //"conll/ACE_TEST_ALL";  
	//public static String ACE_TrainForParts =  "./conll/ACE_TRAINANDDEV_NONTRANSCRIPTS"; // "conll/ACE2004_TRAINANDDEV_part0";
	//change to train on all data
	public static String ACE_TrainForParts =  "./conll/ACE_TRAINANDDEV_NONTRANSCRIPTS"; // "conll/ACE2004_TRAINANDDEV_part0";
	public static String ACE_Train  = "./data/ace/HLT2007.TRAINANDDEV";
	public static String ACE_Test  = "./data/ace/HLT2007.TEST";
	public static String ACE_TuneTrain = "./data/ace/HLT2007.TRAIN";
	public static String ACE_TuneTest = "./data/ace/HLT2007.DEV";
	public static String ACE_TestForParts = "./conll/ACE_TEST_NONTRANSCRIPTS"; // "conll/ACE2004_TEST_part0";
	public static String ACE2_Train  = "./data/ace2/ACE2_TRAINANDDEV";
	public static String ACE2_Test  = "./data/ace2/ACE2_TEST";
	public static String ACE2_TuneTrain = "./data/ace2/ACE2_TRAIN";
	public static String ACE2_TuneTest = "./data/ace2/ACE2_DEV";
	public static String ACE2_TestForParts = "./conll/ACE_TEST_NONTRANSCRIPTS"; // "conll/ACE2004_TEST_part0";
	
	public static String NWIRE_Train  = "./data/ace/HLT2007.TRAINANDDEV_NWIRE";
	public static String NWIRE_Test  = "./data/ace/HLT2007.TEST_NWIRE";
	public static String NWIRE_TuneTrain = "./data/ace/HLT2007.TRAIN_NWIRE";
	public static String NWIRE_TuneTest = "./data/ace/HLT2007.DEV_NWIRE";	

	//public static String CoNLL_TrainAll = "conll/CoNLL_TRAINANDDEV_AUTO_ALL";  
	//public static String CoNLL_TestAll = "conll/CoNLL_TEST_AUTO_ALL";// "conll/CoNLL_TEST_AUTO_ALL.toy"; 
	//public static String CoNLL_DevAll = "conll/CoNLL_DEV_AUTO_ALL"; 
	public static String CoNLL_TrainForParts =  "v2/CoNLL_TRAINANDDEV_AUTO_ALL"; 
	public static String CoNLL_TestForParts = "v2/CoNLL_TEST_AUTO_ALL"; // "conll/CoNLL_TEST_AUTO_toypart_"; 
	public static String CoNLL_DevForParts = "conll/CoNLL_DEV_AUTO_part0";
	public static String[] parts = {""};
	//public static String[] parts= {"0","1","2","3","4","5","6","7","8","9"};// {"00","01","02","03","04","05","06","07","08","09","10","11","12","13","14","15","16","17","18","19"};
		
	private int parserPos = 0;
	private int samplesGenerated=0;
	private ParserPromise[] promises = null;
	private edu.illinois.cs.cogcomp.lbjava.parse.Parser currentParser = null;
	
	

	/*
	 * This constructor is used when I'm using two types of data: 
	 * CONLL+ACE
	 * NOTE: expects that the number of shards for both domains is identical!!!!
	 */
	public CompositeParser(String comment, 
			Classifier relevantParserConctructorCoNLL, String corepathCoNLL, String[] shardsCoNLL,
			Classifier relevantParserConctructorACE, String corepathACE, String[] shardsACE) {
		if(shardsCoNLL.length!=shardsACE.length) {
			System.out.println("Not an equal number fo shards between CoNLL and ACE!!!");
			System.exit(0);
		}
		ParserPromise[] promisesCoNLL = new ParserPromise[shardsCoNLL.length];
		for(int i=0;i<shardsCoNLL.length;i++) 
			promisesCoNLL[i] =  new  ParserPromise(corepathCoNLL+shardsCoNLL[i], relevantParserConctructorCoNLL, comment);
		ParserPromise[] promisesACE = new ParserPromise[shardsACE.length];
		for(int i=0;i<shardsACE.length;i++) 
			promisesACE[i] =  new  ParserPromise(corepathACE+shardsACE[i], relevantParserConctructorACE, comment);
		
		sufflePromises(promisesCoNLL, rand);
		sufflePromises(promisesACE, rand);
		this.promises = new ParserPromise[shardsCoNLL.length+shardsACE.length];
		int pos =0;
		for(int i=0;i<promisesCoNLL.length;i++) {
			this.promises[pos]= promisesCoNLL[i];
			pos++;
			this.promises[pos]= promisesACE[i];			
			pos++;
		}			
		this.parserPos = 0;
		this.currentParser = this.promises[this.parserPos].getParser();
		System.out.println("The random sorted order is:");
		for(int i=0;i<promises.length;i++) 
			System.out.println("\t"+promises[i].path);
	}

	/* 
	 * This constructor is used if I'm using only one type of data (ACE/CONLL)
	 */
	public CompositeParser(String comment, Classifier relevantParserConctructor, String corepath, String[] shards) {
		this.promises = new ParserPromise[shards.length];
		for(int i=0;i<shards.length;i++)
			this.promises[i] =  new  ParserPromise(corepath+shards[i], relevantParserConctructor, comment);
		sufflePromises(this.promises, this.rand);
		this.parserPos = 0;
		this.currentParser = this.promises[this.parserPos].getParser();
		System.out.println("The random sorted order is:");
		for(int i=0;i<promises.length;i++) 
			System.out.println("\t"+promises[i].path);
	}

	/*
	 * arrange the promises in a random order
	 */
	private static void sufflePromises(ParserPromise[] promises, Random rand) {
		for(int i=0;i<promises.length;i++) {
			int randomPos = rand.nextInt(promises.length-i); // the numbers are generated from 0 to (this.promises.length-i-1) 
			ParserPromise temp = promises[i];
			promises[i] = promises[i+randomPos];
			promises[i+randomPos] = temp;
		}
	}

	
	//@Override
	public void close() {
		if(this.currentParser!=null)
			this.currentParser.close();
	}

	//@Override
	public Object next() {
		Object res = null;
		if(currentParser!=null) {
			try {
				res = currentParser.next();
				if(samplesGenerated++%100000==0)
					System.out.println(samplesGenerated + " samples generated by the composite parser");
			} catch (Exception e) {
				System.out.println("Some of the subparsers of the composite parser are acting up");
				e.printStackTrace();
				next();
			}
		}
		if(res!=null)
			return res;
		else {
			currentParser.close();
			parserPos++;
			if(parserPos>=promises.length)
				return null;
			else
				currentParser = promises[parserPos].getParser();			
			return next();
		}
	}

	//@Override
	public void reset() {
		if(currentParser!=null)
			currentParser.close();
		parserPos = 0;
		samplesGenerated=0;
		currentParser = promises[parserPos].getParser();
	}

	
	
	public static class ParserStoringClass {
		public String comment = null;
		public String pathToParserData = null;
		public edu.illinois.cs.cogcomp.lbjava.parse.Parser parser=null;
		public String wordCountFile = null;
	}
	
	
	public static class ParserPromise{
		String comment = null;
		String path = null;
		Classifier relevantParserConctructor = null;
		
		public ParserPromise(String _path, Classifier _relevantParserConctructor, String _comment) {
			this.comment = _comment;
			this.path = _path;
			this.relevantParserConctructor = _relevantParserConctructor;
		}
		
		private edu.illinois.cs.cogcomp.lbjava.parse.Parser getParser() {
			ParserStoringClass store = new ParserStoringClass();
			store.pathToParserData = path;
			store.wordCountFile = Parameters.PreWordCount;
			relevantParserConctructor.discreteValue(store); // this returns bullshit. all it really does is fills up the parser field in the store
			System.out.println("Using the parser:"+this.comment+" : "+store.comment);
			return store.parser;		
		}		
	}
	
	public static void main(String[] args) throws Exception {		
		/*
		 * Test the composite parser
		 *
		CompositeParser compose = new CompositeParser(new edu.illinois.cs.cogcomp.lbjava.parse.Parser[]{
				new CoParser(new DocAPFLoader("HLT2007.TRAIN.part1"),
						new CExExClosestPosAllNeg(false,false,false, false, 1, true, false)),
				new CoParser(new DocAPFLoader("HLT2007.TRAIN.part2"),
						new CExExClosestPosAllNeg(false,false,false, false, 1, true, false)),
				new CoParser(new DocAPFLoader("HLT2007.TRAIN.part3"),
						new CExExClosestPosAllNeg(false,false,false, false, 1, true, false))
				});
		CoParser original = new CoParser(new DocAPFLoader("HLT2007.TRAIN"),
				new CExExClosestPosAllNeg(false,false,false, false, 1, true, false));
		CExample next1 = (CExample)compose.next();
		CExample next2 = (CExample)original.next();
		while(next1!=null&&next2!=null) {
			if(!next1.getM1().getID().equals(next2.getM1().getID())) {
				throw new Exception("Shoot1 - the composite parse doesn't work!");
			}
			next1 = (CExample)compose.next();
			next2 = (CExample)original.next();
		}
		if(next1!=next2)
			throw new Exception("Shoot2 - the composite parse doesn't work!");
			*/		
	}
}
