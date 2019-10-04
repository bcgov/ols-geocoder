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
/**
 * The parser generator package is focused around using an {@link ca.bc.gov.ols.geocoder.parser.generator.AddressParserGenerator}
 * to allow a BNF-style grammar to be converted into an {@link ca.bc.gov.ols.geocoder.parser.AddressParser}. The 
 * Generator uses (@link ca.bc.gov.ols.data.indexing.WordClass}es and {@link ca.bc.gov.ols.geocoder.parser.generator.Rule}s to build a {@link ca.bc.gov.ols.geocoder.lexer.Lexer} and 
 * {@link ca.bc.gov.ols.geocoder.parser.State} machine to do the parsing.
 * 
 * {@link ca.bc.gov.ols.geocoder.data.indexing.WordClass}es use Regexes or other string comparisons to identify words of
 * specific types. {@link ca.bc.gov.ols.geocoder.parser.generator.Rule}s are used to define the ways in which 
 * {@link ca.bc.gov.ols.geocoder.data.indexing.WordClass}es and other {@link ca.bc.gov.ols.geocoder.parser.generator.Rule}s 
 * can be combined to build valid parses ({@link ca.bc.gov.ols.geocoder.parser.ParseDerivation}s).
 * 
 * The {@link ca.bc.gov.ols.geocoder.parser.generator.Rule} ({@link ca.bc.gov.ols.geocoder.parser.generator.RuleChoice}, {@link ca.bc.gov.ols.geocoder.parser.generator.RuleSequence}, {@link ca.bc.gov.ols.geocoder.parser.generator.RuleTerm}) 
 * is responsible for building the section of the {@link ca.bc.gov.ols.geocoder.parser.State} machine which matches 
 * the {@link ca.bc.gov.ols.geocoder.parser.generator.Rule}s specified.
 * 
 * A {@link ca.bc.gov.ols.geocoder.parser.generator.RuleTerm} references either another {@link ca.bc.gov.ols.geocoder.parser.generator.Rule}, or a {@link ca.bc.gov.ols.geocoder.data.indexing.WordClass}, 
 * using the symbol field. Additionally the {@link ca.bc.gov.ols.geocoder.parser.generator.RuleTerm} can have a unary 
 * {@link ca.bc.gov.ols.geocoder.parser.generator.RuleOperator}, such as a "*" or "?". 
 * 
 * {@link ca.bc.gov.ols.geocoder.parser.generator.RuleChoice} represents the "|" (or) operator in the grammar, while 
 * {@link ca.bc.gov.ols.geocoder.parser.generator.RuleSequence} represents a sequence of symbols in the grammar.
 * 
 * The first {@link ca.bc.gov.ols.geocoder.parser.generator.Rule} added to the {@link ca.bc.gov.ols.geocoder.parser.generator.AddressParserGenerator} is considered to be 
 * the final target, or overall match rule. Strings going into the parser must match 
 * the overall rule to parse successfully.
 * 
 * @author chodgson
 * 
 */
package ca.bc.gov.ols.geocoder.parser.generator;

