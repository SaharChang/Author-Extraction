/*
 * Balie - BAseLine Information Extraction
 * Copyright (C) 2004-2007  David Nadeau
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 
/*
 * Created on Apr 1, 2004
 */
package ca.uottawa.balie;

import java.io.Serializable;
import java.util.Hashtable;

/**
 * Wraps around a hashtable that contains a list of punctuation.
 * Also offers service functions to resolve punctuation equivalence.
 * 
 * @author David Nadeau (pythonner@gmail.com)
 */
public class PunctLookup implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
	 * Initializes the hashtable with the punctuation lookup and equivalences.
	 */
	public PunctLookup() {
		if(Balie.DEBUG_PUNCT_LOOKUP) DebugInfo.Out("Initializing Punctuations");
		m_Punctuations = new Hashtable<String,Integer>();
		m_Punctuations.put(".",  new Integer(TokenConsts.PUNCT_PERIOD));
		m_Punctuations.put(",",  new Integer(TokenConsts.PUNCT_COMMA));
		m_Punctuations.put(":",  new Integer(TokenConsts.PUNCT_COLON));
		m_Punctuations.put(";",  new Integer(TokenConsts.PUNCT_SEMI_COLON));
		m_Punctuations.put("!",  new Integer(TokenConsts.PUNCT_EXCLAMATION));
		m_Punctuations.put("¡",  new Integer(TokenConsts.PUNCT_INV_EXCLAMATION));
		m_Punctuations.put("?",  new Integer(TokenConsts.PUNCT_INTERROGATION));
		m_Punctuations.put("¿",  new Integer(TokenConsts.PUNCT_INV_INTERROGATION));
		m_Punctuations.put("@",  new Integer(TokenConsts.PUNCT_COMMERCIAL_AT));
		m_Punctuations.put("\"", new Integer(TokenConsts.PUNCT_QUOTE));
		m_Punctuations.put("'",  new Integer(TokenConsts.PUNCT_APOSTROPHE));
		m_Punctuations.put("(",  new Integer(TokenConsts.PUNCT_OPEN_PARENTHESIS));
		m_Punctuations.put(")",  new Integer(TokenConsts.PUNCT_CLOSE_PARENTHESIS));
		m_Punctuations.put("-",  new Integer(TokenConsts.PUNCT_DASH));
		m_Punctuations.put("_",  new Integer(TokenConsts.PUNCT_UNDERSCORE));
		m_Punctuations.put("/",  new Integer(TokenConsts.PUNCT_SLASH));
		m_Punctuations.put("\\", new Integer(TokenConsts.PUNCT_BACK_SLASH));
		m_Punctuations.put("+",  new Integer(TokenConsts.PUNCT_MISC_ARITHMETIC));
		m_Punctuations.put("*",  new Integer(TokenConsts.PUNCT_MISC_ARITHMETIC));
		m_Punctuations.put("·",  new Integer(TokenConsts.PUNCT_MISC_ARITHMETIC));
		m_Punctuations.put("=",  new Integer(TokenConsts.PUNCT_MISC_ARITHMETIC));
		m_Punctuations.put("÷",  new Integer(TokenConsts.PUNCT_MISC_ARITHMETIC));		
		m_Punctuations.put("°",  new Integer(TokenConsts.PUNCT_MISC_ARITHMETIC));	
		m_Punctuations.put("ª",  new Integer(TokenConsts.PUNCT_MISC_ARITHMETIC));
		m_Punctuations.put("¼",  new Integer(TokenConsts.PUNCT_MISC_ARITHMETIC));		
		m_Punctuations.put("½",  new Integer(TokenConsts.PUNCT_MISC_ARITHMETIC));		
		m_Punctuations.put("¾",  new Integer(TokenConsts.PUNCT_MISC_ARITHMETIC));		
		m_Punctuations.put("²",  new Integer(TokenConsts.PUNCT_MISC_ARITHMETIC));		
		m_Punctuations.put("³",  new Integer(TokenConsts.PUNCT_MISC_ARITHMETIC));		
		m_Punctuations.put("#",  new Integer(TokenConsts.PUNCT_MISC_ARITHMETIC));		
		m_Punctuations.put("^",  new Integer(TokenConsts.PUNCT_MISC_ARITHMETIC));		
		m_Punctuations.put("±",  new Integer(TokenConsts.PUNCT_MISC_ARITHMETIC));		
		m_Punctuations.put("%",  new Integer(TokenConsts.PUNCT_PERCENT));
		m_Punctuations.put("[",  new Integer(TokenConsts.PUNCT_OPEN_BRACKET));
		m_Punctuations.put("]",  new Integer(TokenConsts.PUNCT_CLOSE_BRACKET));
		m_Punctuations.put("{",  new Integer(TokenConsts.PUNCT_OPEN_BRACKET));
		m_Punctuations.put("}",  new Integer(TokenConsts.PUNCT_CLOSE_BRACKET));
		m_Punctuations.put("<",  new Integer(TokenConsts.PUNCT_OPEN_BRACKET));
		m_Punctuations.put(">",  new Integer(TokenConsts.PUNCT_CLOSE_BRACKET));
		m_Punctuations.put("&",  new Integer(TokenConsts.PUNCT_AMPERSAND));
		m_Punctuations.put("~",  new Integer(TokenConsts.PUNCT_TILDE));
		m_Punctuations.put("|",  new Integer(TokenConsts.PUNCT_PIPE));
		m_Punctuations.put("$",  new Integer(TokenConsts.PUNCT_CURRENCY));
		m_Punctuations.put("€",  new Integer(TokenConsts.PUNCT_CURRENCY));
		m_Punctuations.put("¢",  new Integer(TokenConsts.PUNCT_CURRENCY));
		m_Punctuations.put("£",  new Integer(TokenConsts.PUNCT_CURRENCY));
		m_Punctuations.put("¥",  new Integer(TokenConsts.PUNCT_CURRENCY));
		m_Punctuations.put("§",  new Integer(TokenConsts.PUNCT_CURRENCY));
		m_Punctuations.put("©",  new Integer(TokenConsts.PUNCT_COPYRIGHT));
		m_Punctuations.put("®",  new Integer(TokenConsts.PUNCT_COPYRIGHT));
		
		m_Punctuations.put("\t", new Integer(TokenConsts.PUNCT_TABULATION));
		m_Punctuations.put("\r", new Integer(TokenConsts.PUNCT_LINEFEED));
		m_Punctuations.put("\n", new Integer(TokenConsts.PUNCT_NEWLINE));

		if(Balie.DEBUG_PUNCT_LOOKUP) DebugInfo.Out("Initializing Equivalent Punctuations");
		m_EquivalentPunctuations = new Hashtable<String,String>();
		m_EquivalentPunctuations.put("„", "\"");
		m_EquivalentPunctuations.put("‹", "\"");
		m_EquivalentPunctuations.put("›", "\"");
		m_EquivalentPunctuations.put("«", "\"");
		m_EquivalentPunctuations.put("»", "\"");
		m_EquivalentPunctuations.put("�?", "\"");
		m_EquivalentPunctuations.put("“", "\"");
		m_EquivalentPunctuations.put("’", "'");
		m_EquivalentPunctuations.put("‘", "'");
		m_EquivalentPunctuations.put("`", "'");
		m_EquivalentPunctuations.put("´", "'");
		m_EquivalentPunctuations.put("�?�", "/");
		m_EquivalentPunctuations.put("…", "-");
		m_EquivalentPunctuations.put("•", "-");
		m_EquivalentPunctuations.put("—", "-");
		m_EquivalentPunctuations.put("­", "-");
		m_EquivalentPunctuations.put("™", "'");  
		m_EquivalentPunctuations.put("º", "-");
		
	}

	/**
	 * Gets the punctuation type (e.g.: '.' is period) of a char
	 * @param pi_Char String of lenght 1
	 * @return	The punctuation type (see {@link TokenConsts} for enumeration)
	 * @see TokenConsts
	 */
	public int GetPunctType(String pi_Char) {
		int nCharType = TokenConsts.PUNCT_UNKNOWN;	
		
		if (m_Punctuations.containsKey(pi_Char)) {
			nCharType = ((Integer)m_Punctuations.get(pi_Char)).intValue();
		}
		
		if (TokenConsts.Is(nCharType, TokenConsts.PUNCT_UNKNOWN)) {
			if(Balie.DEBUG_PUNCT_LOOKUP) DebugInfo.Out("Unknown Punctuation: " + pi_Char);
		}

		return nCharType;
	}
	
	/**
	 * Determines wether or not a given char is a known punctuation in Balie.
	 * 
	 * @param pi_Char The char to test
	 * @return True if the char is a punctuation in Balie.
	 */
	public boolean IsPunctuation(char pi_Char) {
	    return m_Punctuations.containsKey(String.valueOf(pi_Char)) || m_EquivalentPunctuations.containsKey(String.valueOf(pi_Char));
	}
	
	/**
	 * Gets the equivalent char to a given punctuation (e.g.: every quote characters are equivalent with the char '"')
	 * 
	 * @param pi_Char String (typically of lenght 1) for which we want the equivalent representation.
	 * @return The equivalent string
	 */
	public String GetPunctEquivalence(String pi_Char) {
		String strEquivalent = null;
		if (m_EquivalentPunctuations.containsKey(pi_Char)) {
			strEquivalent = (String)m_EquivalentPunctuations.get(pi_Char);
		}
		return strEquivalent; 
	}
	
	/**
	 * Checks if a char is an internal punctuation that is not welcome inside a word (ex.: '.' or ',' or ':')
	 * 
	 * @param pi_Char The char to test
	 * @return True if the char is an internal punctuation that is not welcome inside a word.
	 */
	public boolean IsInternalPunct(char pi_Char) {
		boolean bIsInternalPunct = false;
		if (!Character.isLetterOrDigit(pi_Char)) {
			int nCharType = GetPunctType(String.valueOf(pi_Char));
			bIsInternalPunct = 	TokenConsts.Is(nCharType, TokenConsts.PUNCT_PERIOD) ||
								TokenConsts.Is(nCharType, TokenConsts.PUNCT_COMMA) ||
								TokenConsts.Is(nCharType, TokenConsts.PUNCT_COLON);	
		}
		return bIsInternalPunct;
	}
	
	private Hashtable<String,Integer> m_Punctuations;
	private Hashtable<String,String> m_EquivalentPunctuations;

}
