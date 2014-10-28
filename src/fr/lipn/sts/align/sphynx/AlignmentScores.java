package fr.lipn.sts.align.sphynx;

public class AlignmentScores {
	/** Number of correct words in the aligned hypothesis with respect to the reference */
	private int numCorrect;

	/** Number of word substitutions made in the hypothesis with respect to the reference */
	private int numSubstitutions;
	
	/** Number of word insertions (unnecessary words present) in the hypothesis with respect to the reference */
	private int numInsertions;
	
	/** Number of word deletions (necessary words missing) in the hypothesis with respect to the reference */
	private int numDeletions;

	/** Total number of words in the reference sequences */
	private int numReferenceWords;
	
	/** Total number of words in the hypothesis sequences */
	private int numHypothesisWords;
	
	public AlignmentScores(Alignment a) {
		set(a);
	}
	
	/**
	 * Add a new alignment result
	 * @param alignment result to add
	 */
	public void set(Alignment alignment) {
		numCorrect += alignment.getNumCorrect();
		numSubstitutions += alignment.numSubstitutions;
		numInsertions += alignment.numInsertions;
		numDeletions += alignment.numDeletions;
		numReferenceWords += alignment.getReferenceLength();
		numHypothesisWords += alignment.getHypothesisLength();
	}

	public int getNumReferenceWords() {
		return numReferenceWords;
	}
	
	public int getNumHypothesisWords() {
		return numHypothesisWords;
	}
	
	public float getCorrectRate() {
		return numCorrect / (float) numReferenceWords;
	}
	
	public float getSubstitutionRate() {
		return numSubstitutions / (float) numReferenceWords;
	}

	public float getDeletionRate() {
		return numDeletions / (float) numReferenceWords;
	}

	public float getInsertionRate() {
		return numInsertions / (float) numReferenceWords;
	}
	
	/** @return the word error rate */
	public float getWordErrorRate() {
		return (numSubstitutions + numDeletions + numInsertions) / (float) numReferenceWords;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("# ref").append("\t");
		sb.append("# hyp").append("\t");
		sb.append("cor").append("\t");
		sb.append("sub").append("\t");
		sb.append("ins").append("\t");
		sb.append("del").append("\t");
		sb.append("WER").append("\t");
		sb.append("\n");

		sb.append(numReferenceWords).append("\t");
		sb.append(numHypothesisWords).append("\t");
		sb.append(getCorrectRate()).append("\t");
		sb.append(getSubstitutionRate()).append("\t");
		sb.append(getInsertionRate()).append("\t");
		sb.append(getDeletionRate()).append("\t");
		sb.append(getWordErrorRate()).append("\t");
		return sb.toString();
	}
}
