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
package ca.bc.gov.ols.geocoder.parser;

import ca.bc.gov.ols.geocoder.data.indexing.TrieWordMap;
import ca.bc.gov.ols.geocoder.data.indexing.WordClass;
import ca.bc.gov.ols.geocoder.data.indexing.WordMap;
import ca.bc.gov.ols.geocoder.data.indexing.WordMapBuilder;
import ca.bc.gov.ols.geocoder.dra.DraLexicalRules;
import ca.bc.gov.ols.geocoder.lexer.Lexer;
import ca.bc.gov.ols.geocoder.parser.generator.AddressParserGenerator;
import ca.bc.gov.ols.geocoder.parser.generator.Rule;
import ca.bc.gov.ols.geocoder.parser.generator.RuleChoice;
import ca.bc.gov.ols.geocoder.parser.generator.RuleOperator;
import ca.bc.gov.ols.geocoder.parser.generator.RuleSequence;
import ca.bc.gov.ols.geocoder.parser.generator.RuleTerm;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import org.bytedeco.javacpp.*;
import org.bytedeco.libpostal.*;

import java.io.UnsupportedEncodingException;

import static org.bytedeco.libpostal.global.postal.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AddressParseTest
{
	@Test
	@Tag("Prod")
	public void testTest()
	{
		AddressParser parser = createParser();
		run(parser, "123 1/2");
	}

	@Tag("Prod")
	@Test
	public void testAddress()
	{
		AddressParser parser = createParser();

		run(parser, "123 main");
		run(parser, "123 n main");
		run(parser, "123 n san pedro");
		run(parser, "123 n 13th");
	}

	void run(AddressParser parser, String sentence)
	{
		run(parser, sentence, true);
	}

	void run(AddressParser parser, String sentence, boolean expected)
	{
		parser.setTrace(true);
		BasicParseDerivationHandler handler = new BasicParseDerivationHandler();
		parser.parse(sentence, false, handler);
		boolean isValid = handler.getDerivations().size() > 0;
		assertTrue(isValid == expected);
	}

	AddressParser createParser()
	{
		String dataDir = "ols-geocoder-core/src/main/resources/libpostal_data/";
		String libpostal_data = Loader.load(org.bytedeco.libpostal.libpostal_data.class);
		ProcessBuilder pb = new ProcessBuilder("bash", libpostal_data, "download", "all", dataDir);
		try {
			pb.inheritIO().start().waitFor();
		} catch (Exception e) {
			System.out.println("libpostal data download failed.");
		}

		boolean setup1 = libpostal_setup_datadir(dataDir);
		boolean setup2 = libpostal_setup_parser_datadir(dataDir);
		boolean setup3 = libpostal_setup_language_classifier_datadir(dataDir);
		if (setup1 && setup2 && setup3) {
			libpostal_address_parser_options_t options = libpostal_get_address_parser_default_options();
			BytePointer address = null;
			try {
				address = new BytePointer("781 Franklin Ave Crown Heights Brooklyn NYC NY 11216 USA", "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			libpostal_address_parser_response_t response = libpostal_parse_address(address, options);
			long count = response.num_components();
			for (int i = 0; i < count; i++) {
				System.out.println(response.labels(i).getString() + " " + response.components(i).getString());
			}
			libpostal_teardown();
			libpostal_teardown_parser();
			libpostal_teardown_language_classifier();
		} else {
			System.out.println("Cannot setup libpostal, check if the training data is available at the specified path!");
		}

		AddressParserGenerator parserGen = new AddressParserGenerator();

		Rule ruleAddr = new RuleSequence("addr", true, new RuleTerm[] {
				new RuleTerm("number"),
				new RuleTerm("directional", RuleOperator.OPTION),
				new RuleTerm("name"),
		});
		parserGen.addRule(ruleAddr);

		parserGen.addRule(new RuleTerm("number", "NUMBER"));

		parserGen.addRule(new RuleTerm("directional", "STREET_DIRECTIONAL"));

		parserGen.addRule(new RuleChoice("name", true, new RuleTerm[] {
				new RuleTerm("STREET_NAME_BODY"),
				new RuleTerm("NAME", RuleOperator.STAR)
		}));

		WordMapBuilder wordMapBuilder = new WordMapBuilder();
		wordMapBuilder.addWord("N", WordClass.STREET_DIRECTIONAL);
		WordMap wordMap = new TrieWordMap(wordMapBuilder.getWordMap());
		Lexer lexer = new Lexer(new DraLexicalRules(), wordMap);
		parserGen.setLexer(lexer);

		return parserGen.getParser();
	}

}
