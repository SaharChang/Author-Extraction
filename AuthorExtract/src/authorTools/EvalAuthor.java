package authorTools;
import java.awt.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//dans exel : =SI(LC(-4)<0;LC(-4);SI(LC(-3)<0;LC(-3);SI(LC(-2)<0;LC(-2);SI(LC(-1)<0;LC(-1);0))))
public class EvalAuthor {

	public static void main(String[] args) {
		// evalClassifier();
		randomAttrib((2 * 175), 2425);
	}

	static void evalClassifier() {
		String file = "eval";
		InputStream ips;
		String actual;
		String predicted;
		int pageId;
		int nbCorrectAuthors = 0;
		float percentCorrectAuthors = 0;
		Map<Integer, Boolean> pageMap = new HashMap<Integer, Boolean>();

		try {
			ips = new FileInputStream(file);
			InputStreamReader ipsr = new InputStreamReader(ips);
			BufferedReader br = new BufferedReader(ipsr);
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line != null) {
					String[] tmp = line.split("	");
					System.out.println(tmp[0] + "-" + tmp[1]);
					actual = tmp[0].trim();
					predicted = tmp[1].trim();
					pageId = Integer.valueOf(tmp[2].trim());
					// Contient l'auteur
					boolean containAuthor = false;
					if (actual.equals("02:01") && predicted.equals("01:00"))
						containAuthor = false;
					else if (actual.equals("02:01")
							&& predicted.equals("02:01")) {
						containAuthor = true;
						System.out.println("true");
					}

					if (!pageMap.containsKey(pageId)) {
						pageMap.put(pageId, containAuthor);
					} else if (containAuthor == true)
						pageMap.put(pageId, containAuthor);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		int count = 0;
		System.out.println(pageMap.size());
		for (int i = 1; i < pageMap.size() + 1; i++) {
			if (pageMap.get(i) != null) {
				count++;
				boolean contain = pageMap.get(i);
				System.out.println(i + " - " + contain);
				if (contain == true)
					nbCorrectAuthors++;
			}
		}
		percentCorrectAuthors = (float) nbCorrectAuthors / count;

		System.out.println("nbCorrectAuthors : " + nbCorrectAuthors
				+ " percentCorrectAuthors : " + percentCorrectAuthors
				+ " nbPages :  " + pageMap.size());
	}

	static ArrayList<Integer> randomNumbers(int count, int number) {
		ArrayList<Integer> listRand = new ArrayList<Integer>();
		for (int i = 0; i < count; i++) {
			int rand = (int) (Math.random() * number + 1);
			if (!listRand.contains(rand)) {
				listRand.add(rand);
				System.out.println("nb = " + rand);
			} else {
				count++;
			}
		}
		return listRand;

	}

	static void randomAttrib(int numberAttrib, int nbLignes) {
		String file = "nonAuthorAttrib";
		String randomFile = "randomAttrib";
		InputStream ips;
		int count = 0;
		ArrayList<Integer> listRand = randomNumbers(numberAttrib, nbLignes);
		try {
			BufferedWriter sortieAuteur = new BufferedWriter(new FileWriter(
					randomFile, true));
			ips = new FileInputStream(file);
			InputStreamReader ipsr = new InputStreamReader(ips);
			BufferedReader br = new BufferedReader(ipsr);
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line != null) {
					count++;
					if (listRand.contains(count)) {
						sortieAuteur.write(line + "\n");
					}
				}
			}
			sortieAuteur.flush();
			sortieAuteur.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
