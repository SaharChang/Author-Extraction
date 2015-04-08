package authorExtract;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class test {

	public static void main(String[] args) {
		String conceptFile = "";
		String textFile = " test";

		treeTagg(textFile);

		//loadConcepts("C:\\Users\\changuel\\Desktop\\physics\\KineticEnergy.txt"
		// );
	}

	static ArrayList<ArrayList> treeTagg(String fileName) {
		ArrayList<ArrayList> sentenceList = new ArrayList<ArrayList>();
		String lang = null;
		String cmdLang = "tag-english";
		String cmdline = "cmd.exe /c " + cmdLang + fileName;
		int nbWords = 0;
		try {
			Process p = Runtime.getRuntime().exec(cmdline);
			// get its tree tagger output (gate input)
			BufferedReader input = new BufferedReader(new InputStreamReader(p
					.getInputStream()));
			/*
			 * Chaque ligne taggée va etre inseree dans wordsList est une list
			 * de mots avec leurs tags
			 */
			ArrayList<String[]> wordsList = new ArrayList<String[]>();
			System.out.println("here");
			String line;
			// Chaque ligne caracterise un mot
			while ((line = input.readLine()) != null) {
				
				if (line.split("\t").length > 1) {
					String[] elems = line.split("\t");

					// les noeuds texte sont séparerf par -- dans le fichier
					if (!elems[0].equals("---")) {
						System.out.println(elems[0] + "[" + elems[1] + "] ");
						wordsList.add(elems);
						nbWords++;
					}
					if (elems[1].equals("SENT")) {
						System.out.println("------------------");
						sentenceList.add(wordsList);
						wordsList = new ArrayList<String[]>();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sentenceList;
	}

	static ArrayList<String[]> loadConcepts(String fichier) {
		String chaine = "";
		ArrayList<String[]> conceptList = new ArrayList<String[]>();
		try {
			InputStream ips = new FileInputStream(fichier);
			InputStreamReader ipsr = new InputStreamReader(ips);
			BufferedReader br = new BufferedReader(ipsr);
			String ligne;
			while ((ligne = br.readLine()) != null) {
				String[] elems = ligne.split("\\s");
				System.out.println(elems.length + " " + elems[0] + "-"
						+ elems[elems.length - 1]);
				conceptList.add(elems);
			}
			br.close();
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		return conceptList;
	}

	void findConcepts(ArrayList<ArrayList> textList,
			ArrayList<String[]> conceptList) {
		for (int i = 0; i < textList.size(); i++) {
			ArrayList<String[]> wordsList = textList.get(i);
			for (int j = 0; j < wordsList.size(); j++) {
				String[] word = wordsList.get(j);
				for (int k = 0; k < word.length; k++) {
					String[] concept = conceptList.get(k);
					if (concept[0].equals(word[0])) {
						String test ="";
					}

				}

			}
		}
	}
}
