package rpd.oral;

import rpd.conceptions.AlveolarAbsorption;
import rpd.conceptions.GingivalRecession;
import rpd.conceptions.ToothMobility;
import rpd.conceptions.ToothType;

public class Tooth implements Comparable<Tooth> {

	private int zone;
	private int num;
	
	private ToothType tooth_type = null;
	
	private boolean missing = false;
	
	private boolean mesial_tight_bite = false;
	private boolean distal_tight_bite = false;
	
	private boolean mesial_tiling = false;
	private boolean distal_tiling = false;
	
	private boolean distal_buccal_undercut = false;
	private boolean distal_lingual_undercut = false;
	private boolean mesial_buccal_undercut = false;
	private boolean mesial_lingual_undercut = false;
	
	private boolean bone_undercut = false;
	
	private ToothMobility mobility = ToothMobility.No;
	private GingivalRecession gingival_recession = GingivalRecession.No;
	private AlveolarAbsorption alveolar_absorption = AlveolarAbsorption.No;
	
	public Tooth(int zone, int num) {
		
		this.zone = zone;
		this.num = num;
		
		if(num == 1 || num == 2)
			this.tooth_type = ToothType.Incisal;
		else if(num == 3)
			this.tooth_type = ToothType.Canine;
		else if(num == 4 || num == 5)
			this.tooth_type = ToothType.Premolar;
		else if(num == 6 || num == 7 || num == 8)
			this.tooth_type = ToothType.Molar;
		else {}
	}
	
	public String toString() {
		return "tooth" + zone + num;
	}
	
	public int getZone() {
		return this.zone;
	}
	
	public int getNum() {
		return this.num;
	}
	
	public ToothType getToothType() {
		return this.tooth_type;
	}
	
	public boolean isMissing() {
		return this.missing;
	}
	
	public void setMissing(boolean missing) {
		this.missing = missing;
	}
	
	public boolean isMesialTightBite() {
		return this.mesial_tight_bite;
	}
	
	public void setMesialTightBite(boolean mesial_tight_bite) {
		this.mesial_tight_bite = mesial_tight_bite;
	}
	
	public boolean isDistalTightBite() {
		return this.distal_tight_bite;
	}
	
	public void setDistalTightBite(boolean distal_tight_bite) {
		this.distal_tight_bite = distal_tight_bite;
	}
	
	public boolean isMesialTiling() {
		return this.mesial_tiling;
	}
	
	public void setMesialTiling(boolean mesial_tiling) {
		this.mesial_tiling = mesial_tiling;
	}
	
	public boolean isDistalTiling() {
		return this.distal_tiling;
	}
	
	public void setDistalTiling(boolean distal_tiling) {
		this.distal_tiling = distal_tiling;
	}
	
	public boolean isBoneUndercut() {
		return this.bone_undercut;
	}
	
	public void setBoneUndercut(boolean bone_undercut) {
		this.bone_undercut = bone_undercut;
	}
	
	public boolean isDistalBuccalUndercut() {
		return this.distal_buccal_undercut;
	}
	
	public void setDistalBuccalUndercut(boolean distal_buccal_undercut) {
		this.distal_buccal_undercut = distal_buccal_undercut;
	}
	
	public boolean isDistalLingualUndercut() {
		return this.distal_lingual_undercut;
	}
	
	public void setDistalLingualUndercut(boolean distal_lingual_undercut) {
		this.distal_lingual_undercut = distal_lingual_undercut;
	}
	
	public boolean isMesialBuccalUndercut() {
		return this.mesial_buccal_undercut;
	}
	
	public void setMesialBuccalUndercut(boolean mesial_buccal_undercut) {
		this.mesial_buccal_undercut = mesial_buccal_undercut;
	}
	
	public boolean isMesialLingualUndercut() {
		return this.mesial_lingual_undercut;
	}
	
	public void setMesialLingualUndercut(boolean mesial_lingual_undercut) {
		this.mesial_lingual_undercut = mesial_lingual_undercut;
	}
	
	public GingivalRecession getGingivalRecession() {
		return this.gingival_recession;
	}
	
	public void setGingivalRecession(GingivalRecession gingival_recession) {
		this.gingival_recession = gingival_recession;
	}
	
	public ToothMobility getMobility() {
		return this.mobility;
	}
	
	public void setMobility(ToothMobility mobility) {
		this.mobility = mobility;
	}
	
	public AlveolarAbsorption getAlveolarAbsorption() {
		return this.alveolar_absorption;
	}
	
	public void setAlveolarAbsorption(AlveolarAbsorption alveolar_absorption) {
		this.alveolar_absorption = alveolar_absorption;
	}

	@Override
	public int compareTo(Tooth arg0) {
		return this.toString().compareTo(arg0.toString());
	}
}
