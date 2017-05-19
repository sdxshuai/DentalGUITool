package misc;

import exceptions.ToothIndexException;
import exceptions.ToothMapException;
import exceptions.ToothModifierException;
import ontologies.LabelModifier;

import java.util.*;

public class ToothPosition {

	public static int[] zone_mapping = {1, 4, 2, 3};

	public static void main(String[] args) throws ToothMapException {

		//String tooth_map = "1;D;//3;PB;7;P;/6;B;";
		String tooth_map = "1678/68 /678 /678";
		List<String> tooth_list = getRest(tooth_map);
		System.out.println(tooth_list);
	}

	public static Map<String, Integer> getToothIndexes() {

		Map<String, Integer> tooth_indexes = new HashMap<String, Integer>();
		int tooth_index = 1;
		for (int tooth_zone = 1; tooth_zone <= 4; tooth_zone++)
			for (int tooth_num = 1; tooth_num <= 8; tooth_num++) {

				String tooth_index_str = String.valueOf(tooth_zone) + String.valueOf(tooth_num);
				tooth_indexes.put(tooth_index_str, tooth_index);
				tooth_index++;
			}

		tooth_indexes.put("None", tooth_index);
		return tooth_indexes;
	}

	//取出对合牙index
	public static String getOcclusalOne(String tooth_index) throws ToothIndexException {

		if (tooth_index.length() != 2)
			throw new ToothIndexException("illegal tooth index: " + tooth_index);

		int tooth_zone = Integer.valueOf(tooth_index.substring(0, 1));
		int tooth_num = Integer.valueOf(tooth_index.substring(1, 2));
		if (tooth_zone < 1 || tooth_zone > 4)
			throw new ToothIndexException("illegal tooth index: " + tooth_index);

		int occlusal_tooth_zone = -1;
		if (tooth_zone == 1)
			occlusal_tooth_zone = 4;
		else if (tooth_zone == 2)
			occlusal_tooth_zone = 3;
		else if (tooth_zone == 3)
			occlusal_tooth_zone = 2;
		else if (tooth_zone == 4)
			occlusal_tooth_zone = 1;
		else
			throw new ToothIndexException("illegal tooth index: " + tooth_index);

		return String.valueOf(occlusal_tooth_zone) + String.valueOf(tooth_num);
	}

	//取出modified（NTXX）牙index
	public static List<String> getModifiedToothList(String tooth_map, String modifider) throws ToothMapException, ToothModifierException {

		if (modifider.equals("pos:$nt"))
			return getToothList(tooth_map);
		else if (modifider.equals("pos:$nt1"))
			return getOcclusal(tooth_map);
		else if (modifider.equals("pos:$nt2"))
			return getNeighboring(tooth_map);
		else if (modifider.equals("pos:$nt3"))
			return getRest(tooth_map);
		else if (modifider.equals("pos:$nt4"))
			return getNT4();
		else if (modifider.equals("pos:$nt5"))
			return getNT5();
		else if (modifider.equals("pos:$nt6"))
			return getNT6();
		else if (modifider.equals("pos:$nt7"))
			return getNT7();
		else if (modifider.equals("pos:$nt8"))
			return getNT8();
		else
			throw new ToothModifierException("illegal tooth modifier: " + modifider);
	}

	public static List<String> getModifiedToothList(String tooth_map, LabelModifier modifier) throws ToothMapException, ToothModifierException {

		if (modifier.equals(LabelModifier.NT))
			return getToothList(tooth_map);
		else if (modifier.equals(LabelModifier.NT1))
			return getOcclusal(tooth_map);
		else if (modifier.equals(LabelModifier.NT2))
			return getNeighboring(tooth_map);
		else if (modifier.equals(LabelModifier.NT3))
			return getRest(tooth_map);
		else if (modifier.equals(LabelModifier.NT4))
			return getNT4();
		else if (modifier.equals(LabelModifier.NT5))
			return getNT5();
		else if (modifier.equals(LabelModifier.NT6))
			return getNT6();
		else if (modifier.equals(LabelModifier.NT7))
			return getNT7();
		else if (modifier.equals(LabelModifier.NT8))
			return getNT8();
		else if (modifier.equals(LabelModifier.NT9))
			return null;
		else
			throw new ToothModifierException("illegal tooth modifier: " + modifier);
	}

	public static List<String> getNT4() {

		List<String> tooth_list = new ArrayList<String>();
		tooth_list.add("36");
		tooth_list.add("37");
		tooth_list.add("38");
		return tooth_list;
	}

	public static List<String> getNT5() {

		List<String> tooth_list = new ArrayList<String>();
		tooth_list.add("48");
		return tooth_list;
	}

	public static List<String> getNT6() {

		List<String> tooth_list = new ArrayList<String>();
		tooth_list.add("11");
		tooth_list.add("12");
		tooth_list.add("13");
		tooth_list.add("21");
		tooth_list.add("22");
		tooth_list.add("23");
		tooth_list.add("31");
		tooth_list.add("32");
		tooth_list.add("33");
		tooth_list.add("41");
		tooth_list.add("42");
		tooth_list.add("43");
		return tooth_list;
	}

	public static List<String> getNT7() {

		List<String> tooth_list = new ArrayList<String>();
		tooth_list.add("11");
		tooth_list.add("12");
		tooth_list.add("13");
		tooth_list.add("14");
		tooth_list.add("15");
		tooth_list.add("16");
		tooth_list.add("17");
		tooth_list.add("18");
		tooth_list.add("21");
		tooth_list.add("22");
		tooth_list.add("23");
		tooth_list.add("24");
		tooth_list.add("25");
		tooth_list.add("26");
		tooth_list.add("27");
		tooth_list.add("28");
		return tooth_list;
	}

	public static List<String> getNT8() {

		List<String> tooth_list = new ArrayList<String>();
		tooth_list.add("31");
		tooth_list.add("32");
		tooth_list.add("33");
		tooth_list.add("34");
		tooth_list.add("35");
		tooth_list.add("36");
		tooth_list.add("37");
		tooth_list.add("38");
		tooth_list.add("41");
		tooth_list.add("42");
		tooth_list.add("43");
		tooth_list.add("44");
		tooth_list.add("45");
		tooth_list.add("46");
		tooth_list.add("47");
		tooth_list.add("48");
		return tooth_list;
	}

	//取出对合牙index
	public static List<String> getOcclusal(String tooth_map) throws ToothMapException {

		List<String> tooth_list = new ArrayList<String>();
		tooth_map = " " + tooth_map + " ";
		String[] tooth_zones = tooth_map.split("/");
		if (tooth_zones.length != 4)
			throw new ToothMapException("illegal tooth map: " + tooth_map);

		for (int i = 0; i < tooth_zones.length; i++) {

			String tooth_numbers = tooth_zones[i];
			tooth_numbers = tooth_numbers.trim();
			int tooth_zone = i + 1;
			int occlusal_tooth_zone = -1;
			if (tooth_zone == 1)
				occlusal_tooth_zone = 4;
			else if (tooth_zone == 2)
				occlusal_tooth_zone = 3;
			else if (tooth_zone == 3)
				occlusal_tooth_zone = 2;
			else if (tooth_zone == 4)
				occlusal_tooth_zone = 1;
			else
				throw new ToothMapException("illegal tooth map: " + tooth_map);

			for (int j = 0; j < tooth_numbers.length(); j++) {

				String tooth_num_str = tooth_numbers.substring(j, j + 1);
				if (!tooth_num_str.matches("[1-8]"))
					continue;
				tooth_list.add(String.valueOf(occlusal_tooth_zone) + tooth_num_str);
			}
		}

		return tooth_list;
	}

	//余牙index
	public static List<String> getRest(String tooth_map) throws ToothMapException {

		List<String> tooth_list = new ArrayList<String>();

		tooth_map = " " + tooth_map + " ";
		String[] tooth_zones = tooth_map.split("/");
		if (tooth_zones.length != 4)
			throw new ToothMapException("illegal tooth map: " + tooth_map);

		for (int i = 0; i < tooth_zones.length; i++) {

			String tooth_numbers = tooth_zones[i];
			tooth_numbers = tooth_numbers.trim();
			String tooth_zone = String.valueOf(zone_mapping[i]);
			boolean[] tooth_flag = new boolean[9];

			for (int j = 0; j < tooth_numbers.length(); j++) {

				String tooth_num_str = tooth_numbers.substring(j, j + 1);
				if (!tooth_num_str.matches("[1-8]"))
					continue;
				int tooth_num = Integer.valueOf(tooth_num_str);
				tooth_flag[tooth_num] = true;
			}

			for (int k = 1; k <= 8; k++) {

				if (!tooth_flag[k])
					tooth_list.add(tooth_zone + String.valueOf(k));
			}
		}

		return tooth_list;
	}

	//取出邻牙index
	public static List<String> getNeighboring(String tooth_map) throws ToothMapException {

		List<String> tooth_list = new ArrayList<String>();

		boolean[] tooth_flag_up = new boolean[17];
		boolean[] tooth_flag_down = new boolean[17];

		tooth_map = " " + tooth_map + " ";
		String[] tooth_zones = tooth_map.split("/");
		if (tooth_zones.length != 4)
			throw new ToothMapException("illegal tooth map: " + tooth_map);

		for (int i = 0; i < tooth_zones.length; i++) {

			String tooth_numbers = tooth_zones[i];
			tooth_numbers = tooth_numbers.trim();
			if (tooth_numbers.length() == 0)
				continue;

			int tooth_zone_num = zone_mapping[i];
			boolean[] tooth_flag = null;
			if (tooth_zone_num == 1 || tooth_zone_num == 2)
				tooth_flag = tooth_flag_up;
			else
				tooth_flag = tooth_flag_down;

			for (int j = 0; j < tooth_numbers.length(); j++) {

				String tooth_num_str = tooth_numbers.substring(j, j + 1);
				if (!tooth_num_str.matches("[1-8]"))
					continue;
				int tooth_num = Integer.valueOf(tooth_num_str);
				if (tooth_zone_num == 1 || tooth_zone_num == 4)
					tooth_flag[9 - tooth_num] = true;
				else
					tooth_flag[8 + tooth_num] = true;
			}
		}

		Set<String> up_neighbors = getNeighboring(tooth_flag_up, true);
		Set<String> down_neighbors = getNeighboring(tooth_flag_down, false);
		tooth_list.addAll(up_neighbors);
		tooth_list.addAll(down_neighbors);

		return tooth_list;
	}

	private static Set<String> getNeighboring(boolean[] tooth_missing_flag, boolean is_up) {

		Set<String> tooth_list = new HashSet<String>();
		int k = 1;
		int area_start = -1;
		int area_end = -1;

		String left_zone = null;
		String right_zone = null;

		if (is_up) {

			left_zone = "1";
			right_zone = "2";
		} else {

			left_zone = "4";
			right_zone = "3";
		}

		while (k <= 16) {

			if (tooth_missing_flag[k]) {

				area_start = k;
				int p = k + 1;
				while (p <= 16 && tooth_missing_flag[p])
					p++;
				area_end = p - 1;

				int front_neighbor = area_start - 1;
				int end_neighbor = area_end + 1;
				if (front_neighbor >= 1) {

					if (front_neighbor <= 8)
						tooth_list.add(left_zone + String.valueOf(9 - front_neighbor));
					else
						tooth_list.add(right_zone + String.valueOf(front_neighbor - 8));
				}
				if (end_neighbor <= 16) {

					if (end_neighbor <= 8)
						tooth_list.add(left_zone + String.valueOf(9 - end_neighbor));
					else
						tooth_list.add(right_zone + String.valueOf(end_neighbor - 8));
				}

				k = area_end + 1;
			} else
				k++;
		}

		return tooth_list;
	}
	
	/*public static List<String> getNeighboring(String tooth_map) throws ToothMapException {
		
		List<String> tooth_list = new ArrayList<String>();
		
		tooth_map = " " + tooth_map + " ";
		String[] tooth_zones = tooth_map.split("/");
		if(tooth_zones.length != 4)
			throw new ToothMapException("illegal tooth map: " + tooth_map);
		
		for(int i = 0; i < tooth_zones.length; i++) {
			
			String tooth_numbers = tooth_zones[i];
			tooth_numbers = tooth_numbers.trim();
			if(tooth_numbers.length() == 0)
				continue;
			
			String tooth_zone = String.valueOf(zone_mapping[i + 1]);
			boolean[] tooth_flag = new boolean[9];
			
			for(int j = 0; j < tooth_numbers.length(); j++) {
				
				String tooth_num_str = tooth_numbers.substring(j, j + 1);
				if(!tooth_num_str.matches("[1-8]"))
					continue;
				int tooth_num = Integer.valueOf(tooth_num_str);
				tooth_flag[tooth_num] = true;
			}
			
			int k = 1;
			int area_start = -1;
			int area_end = -1;
			while(k <= 8) {
				
				if(tooth_flag[k]) {
					
					area_start = k;
					int p = k + 1;
					while(p <= 8 && tooth_flag[p])
						p++;
					area_end = p - 1;
					if(area_start - 1 >= 1)
						tooth_list.add(tooth_zone + String.valueOf(area_start - 1));
					if(area_end + 1 <= 8)
						tooth_list.add(tooth_zone + String.valueOf(area_end + 1));
					
					k = area_end + 1;
				}
				
				else
					k++;
			}
		}
		
		return tooth_list;
	}*/

	//取出牙位图index
	public static List<String> getToothList(String tooth_map) throws ToothMapException {

		List<String> tooth_list = new ArrayList<String>();

		tooth_map = " " + tooth_map + " ";
		String[] tooth_zones = tooth_map.split("/");
		if (tooth_zones.length != 4)
			throw new ToothMapException("illegal tooth map: " + tooth_map);

		for (int i = 0; i < tooth_zones.length; i++) {

			String tooth_numbers = tooth_zones[i];
			tooth_numbers = tooth_numbers.trim();
			if (tooth_numbers.length() == 0)
				continue;

			String tooth_zone = String.valueOf(zone_mapping[i]);
			for (int j = 0; j < tooth_numbers.length(); j++) {

				String tooth_num = tooth_numbers.substring(j, j + 1);
				if (!tooth_num.matches("[1-8]"))
					continue;
				String tooth_index = tooth_zone + tooth_num;
				tooth_list.add(tooth_index);
			}
		}
		return tooth_list;
	}
}

