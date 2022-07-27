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
package ca.bc.gov.ols.rest.controllers;

import static ca.bc.gov.ols.rest.test.GeocodeResultChecker.check;
import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ca.bc.gov.ols.junitFlags.DevTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import ca.bc.gov.ols.geocoder.rest.WebConfig;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = WebConfig.class)

@Category(DevTest.class)
public class OccupantControllerTest {
	
	@Autowired
	private WebApplicationContext wac;
	
	MockMvc mockMvc;

	@Before
    public void init(){
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }
	
	@Test
	public void testGetOccupantById() throws Exception {
		ResultActions ra = mockMvc.perform(get("/occupants/00000000-0000-0000-0000-000000006001.json"));
		MvcResult result = ra.andExpect(status().isOk()).andReturn();
		check(result).property("occupantID", "00000000-0000-0000-0000-000000006001");
	}

	@Test
	public void testGetOccupantByName() throws Exception {
		ResultActions ra = mockMvc.perform(get("/occupants/addresses.json?addressString=refractions research --"));
		ra.andExpect(status().isOk());
	}

	@Test
	public void testGetOccupantByPartialName() throws Exception {
		ResultActions ra = mockMvc.perform(get("/occupants/addresses.json?addressString=refractions --"));
		MvcResult result = ra.andExpect(status().isOk()).andReturn();
		check(result).getFirst().property("occupantName", "Refractions Research");
	}

	@Test
	public void testGetOccupantByNameAndAddress() throws Exception {
		ResultActions ra = mockMvc.perform(get("/occupants/addresses.json?addressString=refractions -- 1207 Douglas st victoria BC"));
		MvcResult result = ra.andExpect(status().isOk()).andReturn();
		check(result).getFirst().property("occupantName", "Refractions Research");
	}

	@Test
	public void testGetNearestOccupant() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetOccupantsNear() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetOccupantsWithin() {
		fail("Not yet implemented");
	}

}
