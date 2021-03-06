/*
BSD 3-Clause License

Copyright (c) 2017, Jan N. van Rijn <j.n.van.rijn@liacs.leidenuniv.nl>
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

* Neither the name of the copyright holder nor the names of its
  contributors may be used to endorse or promote products derived from
  this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.*/

package org.openml.weka.algorithm;

import java.io.BufferedReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.openml.apiconnector.algorithms.Input;
import org.openml.apiconnector.algorithms.TaskInformation;
import org.openml.apiconnector.io.OpenmlConnector;
import org.openml.apiconnector.xml.DataSetDescription;
import org.openml.apiconnector.xml.Task;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class DataSplits {

	private final Instances[][][][] subsamples;
	private final ArrayList<Integer>[][][] rowids;

	public final int REPEATS;
	public final int FOLDS;
	public final int SAMPLES;
	public final int DATASET_ID;
	
	// TODO: should overload?? 
	public static DataSplits get(OpenmlConnector openml, int taskId) throws Exception {
		Task task = openml.taskGet(taskId);
		String targetAttribute = TaskInformation.getSourceData(task).getTarget_feature();
		int dataId = TaskInformation.getSourceData(task).getData_set_id();
		DataSetDescription dsd = openml.dataGet(dataId);
		URL splitsUrl = new URL(TaskInformation.getEstimationProcedure(task).getData_splits_url());
		URL datasetUrl = openml.getOpenmlFileUrl(dsd.getFile_id(), dsd.getName() + ".arff");
		
		Instances dataset = new Instances(new BufferedReader(Input.getURL(datasetUrl)));
		
		dataset.setClass(dataset.attribute(targetAttribute));
		Instances splits = new Instances(new BufferedReader(Input.getURL(splitsUrl)));
		
		return new DataSplits(task, dataset, splits);
	}
	
	@SuppressWarnings("unchecked")
	public DataSplits(Task task, Instances dataset, Instances datasplits) throws Exception {
		int numRepeats = TaskInformation.getNumberOfRepeats(task);
		int numFolds = TaskInformation.getNumberOfFolds(task);
		int numSamples = 1;
		try {numSamples = TaskInformation.getNumberOfSamples(task);} catch (Exception e) {}

		DATASET_ID = TaskInformation.getSourceData(task).getData_set_id();
		REPEATS = numRepeats;
		FOLDS = numFolds;
		SAMPLES = numSamples;
		
		subsamples = new Instances[REPEATS][FOLDS][SAMPLES][2];
		rowids = new ArrayList[REPEATS][FOLDS][SAMPLES];
		for (int repeats = 0; repeats < REPEATS; ++repeats) {
			for (int folds = 0; folds < FOLDS; ++folds) {
				for (int samples = 0; samples < SAMPLES; ++samples) {
					for (int i = 0; i < 2; ++i) {
						subsamples[repeats][folds][samples][i] = new Instances(dataset, 0);
						rowids[repeats][folds][samples] = new ArrayList<Integer>();
					}
				}
			}
		}

		Attribute attRowid = datasplits.attribute("rowid");
		Attribute attRepeat = datasplits.attribute("repeat");
		Attribute attFold = datasplits.attribute("fold");
		Attribute attSample = datasplits.attribute("sample");
		Attribute attType = datasplits.attribute("type");
		for (int i = 0; i < datasplits.numInstances(); ++i) {
			Instance instanceMeta = datasplits.get(i);
			int rowid = (int) instanceMeta.value(attRowid);
			int repeat = attRepeat == null ? 0 : (int) instanceMeta.value(attRepeat);
			int fold = attFold == null ? 0 : (int) instanceMeta.value(attFold);
			int sample = attSample == null ? 0 : (int) instanceMeta.value(attSample);
			boolean train = attType.value((int) instanceMeta.value(attType)).equals("TRAIN");

			Instance instanceBase = dataset.get(rowid);
			subsamples[repeat][fold][sample][train == true ? 0 : 1].add(instanceBase);
			if (train == false) {
				rowids[repeat][fold][sample].add(rowid);
			}
		}
	}


	public Instances getTrainingSet(int repeat, int fold) {
		return subsamples[repeat][fold][0][0];
	}
	
	public Instances getTrainingSet(int repeat, int fold, int sample) {
		return subsamples[repeat][fold][sample][0];
	}

	public Instances getTestSet(int repeat, int fold) {
		return subsamples[repeat][fold][0][1];
	}

	public Instances getTestSet(int repeat, int fold, int sample) {
		return subsamples[repeat][fold][sample][1];
	}

	public List<Integer> getTestSetRowIds(int repeat, int fold, int sample) {
		return rowids[repeat][fold][sample];
	}
}
