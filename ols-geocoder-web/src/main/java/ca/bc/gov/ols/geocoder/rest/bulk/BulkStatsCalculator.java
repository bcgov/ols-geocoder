/**
 * Copyright © 2008-2019, Province of British Columbia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.bc.gov.ols.geocoder.rest.bulk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.bc.gov.ols.util.StopWatch;

public class BulkStatsCalculator {

		List<Double> times = new ArrayList<Double>();
		List<Integer> scores = new ArrayList<Integer>();
		double maxTime = -1;
		double minTime = -1;
		double totalTime = 0;
		double meanTime = -1;
		double medianTime = -1;
		double stdDevTime = -1;
		int maxScore = -1;
		int minScore = -1;
		int totalScore = 0;
		int meanScore = -1;
		int medianScore = -1;
		double stdDevScore = -1;
		StopWatch clock = new StopWatch();
		
		public void start() {
			clock.start();
		}
		
		public void record(double time, int score) {
			times.add(time);
			scores.add(score);
		}
		
		public void stop() {
			clock.stop();
			Collections.sort(times);
			Collections.sort(scores);
			minTime = times.get(0);
			maxTime = times.get(times.size()-1);
			medianTime = times.get(times.size()/2);
			minScore = scores.get(0);
			maxScore = scores.get(scores.size()-1);
			medianScore = scores.get(scores.size()/2);
			for(int i = 0; i < times.size(); i++) {
				totalTime += times.get(i);
				totalScore += scores.get(i);
			}
			meanTime = totalTime / times.size();
			meanScore = totalScore / scores.size();
			Double varianceTime = 0d, varianceScore = 0d;
			for(int i = 0; i < times.size(); i++) {
				varianceTime += Math.pow(times.get(i) - meanTime, 2);
				varianceScore += Math.pow(scores.get(i) - meanScore, 2);
			}
			varianceTime /= times.size();
			varianceScore /= scores.size();
			stdDevTime = Math.sqrt(varianceTime);
			stdDevScore = Math.sqrt(varianceScore);
			
		}
		
		public int getProcessedCount() {
			return times.size();
		}
		
		public long getElapsedTime() {
			return clock.getElapsedTime();
		}

		public double getMaxTime() {
			return maxTime;
		}

		public double getMinTime() {
			return minTime;
		}

		public double getMeanTime() {
			return meanTime;
		}

		public double getMedianTime() {
			return medianTime;
		}

		public int getMaxScore() {
			return maxScore;
		}

		public int getMinScore() {
			return minScore;
		}

		public int getMeanScore() {
			return meanScore;
		}

		public int getMedianScore() {
			return medianScore;
		}

		public double getStdDevTime() {
			return stdDevTime;
		}

		public double getStdDevScore() {
			return stdDevScore;
		}
		
		public String toString() {
			return minTime + "\t"
					+ maxTime + "\t"
					+ meanTime + "\t"
					+ medianTime + "\t"
					+ stdDevTime + "\t"
					+ totalTime + "\t"
					+ minScore + "\t"
					+ maxScore + "\t"
					+ meanScore + "\t"
					+ medianScore + "\t"
					+ stdDevScore;
		}
}
