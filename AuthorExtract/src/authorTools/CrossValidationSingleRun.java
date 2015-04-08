package authorTools;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Random;

import weka.core.Instances;
import weka.core.Utils;

/**
 * Classe utilisée pour le test mais peut être utile.
 * 
 * Performs a single run of cross-validation.
 * 
 * Command-line parameters:
 * <ul>
 * <li>-t filename - the dataset to use</li>
 * <li>-x int - the number of folds to use</li>
 * <li>-s int - the seed for the random number generator</li>
 * <li>-c int - the class index, "first" and "last" are accepted as well; "last"
 * is used by default</li>
 * <li>-W classifier - classname and options, enclosed by double quotes; the
 * classifier to cross-validate</li>
 * </ul>
 * 
 * Example command-line:
 * 
 * <pre>
 * java CrossValidationSingleRun -t anneal.arff -c last -x 10 -s 1 -W &quot;weka.classifiers.trees.J48 -C 0.25&quot;
 * </pre>
 * 
 * @author FracPete (fracpete at waikato dot ac dot nz)
 */
public class CrossValidationSingleRun {

	/**
	 * Performs the cross-validation. See Javadoc of class for information on
	 * command-line parameters.
	 * 
	 * @param args
	 *            the command-line parameters
	 * @throws Excecption
	 *             if something goes wrong
	 */
	public static void main(String[] args) throws Exception {

		// Le fichier a diviser
		BufferedReader br = new BufferedReader(new FileReader(
				"CrossValidation\\author13.arff"));
		Instances data = new Instances(br);

		String clsIndex = Utils.getOption("c", args);
		if (clsIndex.length() == 0)
			clsIndex = "last";
		if (clsIndex.equals("first"))
			data.setClassIndex(0);
		else if (clsIndex.equals("last"))
			data.setClassIndex(data.numAttributes() - 1);
		else
			data.setClassIndex(Integer.parseInt(clsIndex) - 1);

		// other options
		int seed = 1;
		int folds = 10;

		// randomize data
		Random rand = new Random(seed);
		Instances randData = new Instances(data);
		randData.randomize(rand);
		if (randData.classAttribute().isNominal())
			randData.stratify(folds);

		// perform cross-validation
		for (int n = 0; n < folds; n++) {
			Instances train = randData.trainCV(folds, n);
			Instances test = randData.testCV(folds, n);

			// weka.core.converters.ConverterUtils.DataSink.write(
			// "CrossValidation\\train" + n + ".dat", train);
			//			 
			// weka.core.converters.ConverterUtils.DataSink.write(
			// "CrossValidation\\test" + n + ".dat", test);

			BufferedWriter writer = new BufferedWriter(new FileWriter(
					"CrossValidation\\app" + (n + 1) + ".dat"));
			writer.write(train.toString());
			writer.flush();
			writer.close();

			BufferedWriter writer2 = new BufferedWriter(new FileWriter(
					"CrossValidation\\test" + (n + 1) + ".dat"));
			writer2.write(test.toString());
			writer2.flush();
			writer2.close();

		}

	}

}
