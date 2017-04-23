package misc;

import exceptions.ToothMapException;

public class ToothMap {

	public static String tooth_map_regex = "(([0-9]| |\\-|[BCDILMOP]|[a]|;|,)*/){3}([0-9]|\\-|[BCDILMOP]|[a]|;|,)*( )*";

	private String tooth_map_str = null;
	private int start_index = -1;
	private int end_index = -1;

	//牙位图
	public ToothMap(String tooth_map_str, int start_index, int end_index) {

		this.tooth_map_str = tooth_map_str;
		this.start_index = start_index;
		this.end_index = end_index;
	}

	//取出下颌部分
	public String getMandibular() throws ToothMapException {

		String tooth_map = this.tooth_map_str + " ";
		String[] fs = tooth_map.split("/");
		if (fs.length != 4)
			throw new ToothMapException("illegal tooth map: " + this.tooth_map_str);

		String[] parts = new String[7];
		for (int i = 0; i < parts.length; i++)
			parts[i] = "";
		parts[1] = "/";
		parts[3] = "/";
		parts[5] = "/";

		parts[2] = fs[1];
		parts[6] = fs[3];

		StringBuilder s = new StringBuilder();
		for (int i = 0; i < parts.length; i++)
			s.append(parts[i]);
		return s.toString();
	}

	//取出上颌部分
	public String getMaxillary() throws ToothMapException {

		String tooth_map = this.tooth_map_str + " ";
		String[] fs = tooth_map.split("/");
		if (fs.length != 4)
			throw new ToothMapException("illegal tooth map: " + this.tooth_map_str);

		String[] parts = new String[7];
		for (int i = 0; i < parts.length; i++)
			parts[i] = "";
		parts[1] = "/";
		parts[3] = "/";
		parts[5] = "/";

		parts[0] = fs[0];
		parts[4] = fs[2];

		StringBuilder s = new StringBuilder();
		for (int i = 0; i < parts.length; i++)
			s.append(parts[i]);
		return s.toString();
	}

	public String getToothMapString() {
		return this.tooth_map_str;
	}

	public int getStartIndex() {
		return this.start_index;
	}

	public int getEndIndex() {
		return this.end_index;
	}

	public String toString() {
		return this.tooth_map_str.trim();
	}

	public boolean equals(Object o) {

		if (o == null)
			return false;

		if (o.getClass() != ToothMap.class)
			return false;
		else {

			ToothMap m1 = (ToothMap) o;
			if (this.start_index == m1.start_index && this.end_index == m1.end_index)
				return true;
			else
				return false;
		}
	}

}
