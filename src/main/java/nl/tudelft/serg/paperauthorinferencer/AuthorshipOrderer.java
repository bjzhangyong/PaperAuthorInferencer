package nl.tudelft.serg.paperauthorinferencer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;

public class AuthorshipOrderer {
	Map<String, Integer> authorsToFrequency = new HashMap<String, Integer>();
	ArrayList<Author> authors = new ArrayList<Author>();
	PDFPaper paper;

	public AuthorshipOrderer(PDFPaper paper) {
		this.paper = paper;
	}

	public void buildAuthorList(Set<Reference> references) {
		references.forEach(new Consumer<Reference>() {

			@Override
			public void accept(Reference r) {
				r.authors.forEach(t -> {
					Author temporaryAuthor = new Author(t);
					Author author;
					if (authors.contains(temporaryAuthor)) {
						author = authors.get(authors.indexOf(temporaryAuthor));
					} else {
						author = new Author(t);
						authors.add(author);
					}
					updateAuthorEntry(r, t, author);
				});
			}

			private void updateAuthorEntry(Reference r, String t, Author author) {
				author.occurenceRatio += r.occurenceRatio;
				author.referenceEntriesRatio += r.referenceEntriesRatio;
				int yearDiff = paper.year - r.year;
				if (yearDiff >= 0) {
					if (yearDiff < author.newestRefDelta) {
						author.newestRefDelta = yearDiff;
					}
					if (yearDiff > author.eldestRefDelta) {
						author.eldestRefDelta = yearDiff;
					}
				}
			}
		});
	}

	public void printAuthors() {
		try {
			paper.authors.stream().forEach(a -> System.out.print(a + ", "));
			System.out.println();

			authors.stream().sorted(Collections.reverseOrder()).forEach(a -> {
				System.out.println(a.getCanonicalName() + ", " + a.occurenceRatio + ", " + a.referenceEntriesRatio
						+ ", " + a.eldestRefDelta + ", " + a.newestRefDelta + ", " + isRealAuthor(a));
			});
		} catch (NoSuchElementException e) {
			System.out.print("PDF not analyzable.");
		}
	}

	private boolean isRealAuthor(Author author) {
		return paper.authors.stream().filter(a -> a.equals(author)).count() > 0;
	}
}
