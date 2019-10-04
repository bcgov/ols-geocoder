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
package ca.bc.gov.ols.geocoder.filters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultiFilter<T> implements Filter<T> {
	
	@SuppressWarnings("rawtypes")
	private List<Filter> filters;
	
	@SuppressWarnings("rawtypes")
	@SafeVarargs
	public MultiFilter(Filter<? super T>... filters) {
		this.filters = new ArrayList<Filter>(Arrays.asList(filters));
	}

	@SuppressWarnings("rawtypes")
	public MultiFilter() {
		this.filters = new ArrayList<Filter>();
	}

	@SuppressWarnings("rawtypes")
	public MultiFilter(int numFilters) {
		this.filters = new ArrayList<Filter>(numFilters);
	}

	public void add(Filter<? super T> filter) {
		filters.add(filter);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean pass(T item) {
		for(Filter<T> filter : filters) {
			if(!filter.pass(item)) {
				return false;
			}
		}
		return true;
	}
	
}
