package ontologies;

import java.util.Comparator;

public class LabelPosComparator implements Comparator<PropertyLabel> {

	@Override
	public int compare(PropertyLabel p1, PropertyLabel p2) {

		int p1_start = p1.getStartOffset();
		int p2_start = p2.getStartOffset();

		if (p1_start < p2_start)
			return -1;
		else if (p1_start > p2_start)
			return 1;
		else
			return 0;
	}
}
