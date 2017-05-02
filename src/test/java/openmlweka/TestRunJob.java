package openmlweka;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;
import org.openml.weka.experiment.RunOpenmlJob;

public class TestRunJob {
	
	
	@Test
	public void testApiRunUpload() throws Exception {
		String[] algorithms = {"weka.classifiers.trees.REPTree", "weka.classifiers.meta.Bagging -- -P 50 -S 4385 -num-slots 4 -I 10 -W weka.classifiers.trees.J48 -- -R -N 3", "weka.classifiers.meta.FilteredClassifier -- -F \"weka.filters.supervised.attribute.Discretize -R first-last -precision 6\" -W weka.classifiers.trees.RandomForest -- -I 100 -K 0 -S 1 -num-slots 1"};
		String[] args = {"-task_id", "1", "-config", "server=https://test.openml.org/; avoid_duplicate_runs=false; skip_jvm_benchmark=true; api_key=2845678bcfe6f0f489535fb8bdfd7ae8", "-C"};
		
		for (String algorithm : algorithms) 
		{
			RunOpenmlJob.main(ArrayUtils.add(args, algorithm));
		}
	}
}
