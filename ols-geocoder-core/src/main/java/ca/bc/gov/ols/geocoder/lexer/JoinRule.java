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
package ca.bc.gov.ols.geocoder.lexer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A rule which matches a sequence of one or more atoms 
 * and replaces them with a single atom consisting
 * of portions of the original atoms as well
 * as optional new text.
 * For a pure join, the retained portion of an input atom consists
 * of the concatenation of all the matched groups in the atom.
 * If there are no groups defined in a pattern, the atom matched by
 * that pattern is skipped.
 * For a replacement, the output consists
 * of the supplied output pattern,
 * with references to input groups replaced by the values of those groups.
 * 
 * Examples of the kinds of transformations supported are:
 * <pre>
 *	JoinRule.createJoin("(S)TATE", "(R)T", "(\\d+)"),
 *	State Rt 602 : SR602
 *	
 *	JoinRule.createJoin("SR", "\\d+"),
 *	SR 602 : SR602
 *			
 *	JoinRule.createJoin("I", "\\d+"),
 *	I 5 : I5
 *		
 * </pre>
 * 
 * @author mbdavis
 *
 */
public class JoinRule 
{
	public static JoinRule createJoin(String re1)
	{
		return new JoinRule(new String[] { re1 } );
	}
	public static JoinRule createJoin(String re1, String re2)
	{
		return new JoinRule(new String[] { re1, re2 });
	}
	public static JoinRule createJoin(String re1, String re2, String re3)
	{
		return new JoinRule(new String[] { re1, re2, re3 });
	}
	/*
	public static JoinRule createReplace(String re1, String re2, String re3, String replaceVal )
	{
		JoinRule rule = new JoinRule(new String[] { re1, re2, re3 });
		rule.setReplaceValue(replaceVal);
		return rule;
	}
	*/
	public static JoinRule createReplace(String re1, String replaceVal )
	{
		JoinRule rule = new JoinRule(new String[] { re1 } );
		rule.setReplaceTemplate(replaceVal);
		return rule;
	}
	
	private String[] regex;
	private Pattern[] pat;
	/**
	 * Holds the matchers which matched a sequence of atoms.
	 * These are later used to extract the strings to join
	 */
	//private Matcher[] matcher;  
	private int patternCount = 0;
	//private String replaceVal = null;
	private AtomBuilder atomBuilder = new JoinAtomBuilder();
	
	public JoinRule(String[] regex)
	{
		this.regex = regex.clone();
		init();
	}
	
	public void setReplaceTemplate(String template)
	{
		atomBuilder = new ReplaceAtomBuilder(template);
	}
	public int getWidth()
	{
		return patternCount;
	}
	
	private void init()
	{
		patternCount = regex.length;
		pat = new Pattern[patternCount];
		for (int i = 0; i < regex.length; i++) {
			pat[i] = Pattern.compile(regex[i]);
		}
	}
	
	public String[] process(String[] atoms)
	{
		/**
		 * Iterate along atoms, searching for a match.
		 * If a match is found, replace those atoms
		 * with the joined string.
		 */
		Matcher[] matcher = new Matcher[patternCount];

		int lastStartIndex = atoms.length - patternCount;
		for (int i = 0; i <= lastStartIndex; i++) {
			if (isMatch(matcher, atoms, i)) {
				String newAtom = atomBuilder.buildAtom(matcher);
				return createNewAtoms(atoms, i, newAtom);
			}
		}
		// no match found - just return original input
		return atoms;
	}
	
	private boolean isMatch(Matcher[] matcher, String[] atoms, int startIndex)
	{
		for (int i = 0; i < patternCount; i++) {
			String atom = atoms[startIndex + i];
			matcher[i] = pat[i].matcher(atom);
			boolean isMatch = matcher[i].matches();
			if (! isMatch) { return false; }
		}
		// all patterns match, so build result
		return true;
	}
	
	
	private String[] createNewAtoms(String[] atoms, int joinIndex, String joinStr)
	{
		boolean insertJoin = joinStr.length() > 0;
		int newLen = atoms.length - patternCount + 1;
		if (! insertJoin) {
			newLen = atoms.length - patternCount;
		}
		
		String[] newAtoms = new String[newLen];
		int i = 0;
		int ni = 0;
		while (i < atoms.length) {
			if (i == joinIndex) {
				if (insertJoin) {
					newAtoms[ni++] = joinStr;
				}
				i = joinIndex + patternCount;
			}
			else {
				newAtoms[ni++] = atoms[i];
				i++;
			}
		}
		return newAtoms;
	}
	
	interface AtomBuilder {
		String buildAtom(Matcher[] matcher);
	}
	
	static class JoinAtomBuilder
	implements AtomBuilder
	{
		public String buildAtom(Matcher[] matcher)
		{
				StringBuilder buf = new StringBuilder();
				for (int i = 0; i < matcher.length; i++) {
					for (int j = 1; j <= matcher[i].groupCount(); j++) {
						buf.append(matcher[i].group(j));
					}
				}
				return buf.toString();
		}

	}
	
	/**
	 * Replaces a match pattern which contains one or more
	 * match groups with an output pattern which 
	 * may contain group variables to be substituted.
	 * 
	 * @author mbdavis
	 *
	 */
	static class ReplaceAtomBuilder
	implements AtomBuilder
	{
		private String outputTemplate;		
		
		public ReplaceAtomBuilder(String outputTemplate)
		{
			this.outputTemplate = outputTemplate;
		}
		
		public String buildAtom(Matcher[] matcher)
		{
			if (matcher.length > 1) {
				throw new IllegalStateException("Replace Rule: Multiple match patterns are not supported");
			}
			
			return evalTemplate(matcher[0]);
		}
		
		private String evalTemplate(Matcher matcher)
		{			
			String atom = outputTemplate;
			for (int i = 1; i <= matcher.groupCount(); i++) {
				String groupRE = "{" + i + "}";
				String grp = matcher.group(i);
				atom = atom.replace(groupRE, grp);
			}
			return atom;
		}

	}
	
	
	/**
	 * Only patterns of the form "ssss{1}ssss" are supported for now.
	 * 
	 * @author mbdavis
	 *
	 */
	static class OLDReplaceAtomBuilder
	implements AtomBuilder
	{
		private String outputTemplate;
		private String[] constData;
		
		
		public OLDReplaceAtomBuilder(String outputTemplate)
		{
			this.outputTemplate = outputTemplate;
			parseTemplate(outputTemplate);
		}
		
		private void parseTemplate(String outputTemplate)
		{
			int index = outputTemplate.indexOf("{1}");
			if (index < 0) {
				constData = new String[] { outputTemplate };
				return;
			}
			String pref = outputTemplate.substring(0, index);
			String suff = outputTemplate.substring(index + 4, outputTemplate.length());
			constData = new String[] { pref, suff };

		}
		
		public String buildAtom(Matcher[] matcher)
		{
			if (matcher.length > 1) throw new IllegalStateException("Replace Rule: Multiple match atoms are not yet supported");
			if (matcher[0].groupCount() != constData.length - 1) {
				throw new IllegalStateException("Replace Rule: Too few groups for output template - " + outputTemplate);
			}
			if (constData.length == 1)
				return constData[0];
			
			return evalTemplate(matcher[0]);
		}
		
		private String evalTemplate(Matcher matcher)
		{			
			StringBuilder buf = new StringBuilder();
			for (int i = 0; i < constData.length; i++) {
				buf.append(constData[i]);
				if (i < constData.length-1)
					buf.append(matcher.group(i));
			}
			return buf.toString();
		}

	}
}
