package edu.illinois.cs.cogcomp.lbj.coref.main;

import edu.illinois.cs.cogcomp.core.utilities.commands.InteractiveShell;


public class MainClass {
	public static void main(String[] args) throws Exception{
//
//		PropertyConfigurator.configure("log4j.properties");
//		
//
//		Properties.readProperties("re.properties");

		InteractiveShell<AllTest> tester = new InteractiveShell<AllTest>(
				AllTest.class);

		if (args.length == 0)
			//tester.ShowDocumentation();
			tester.showDocumentation();
		else
		{
			long start_time = System.currentTimeMillis();
			//tester.RunCommand(args);
			tester.runCommand(args);
					
			System.out.println("This experiment took "
					+ (System.currentTimeMillis() - start_time) / 1000.0
					+ " secs");
		}
	}
}
