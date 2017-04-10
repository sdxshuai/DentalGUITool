package rpd.oral;

import rpd.conceptions.*;

public class Tooth implements Comparable<Tooth> {

	private int zone;
	private int num;
	
	private ToothType tooth_type = null;
	
	private boolean missing = false;
	private boolean bone_undercut = false;
	private boolean buccal_surface_slope = false;
	private boolean lingual_surface_slope = false;
	private boolean cingulum = false;
	private boolean torus = false;
	private boolean space_below_gingival_margins = false;

	private ToothMobility mobility = ToothMobility.No;
	private CrownRootRatio crown_root_ratio = CrownRootRatio.NO;
	private FurcationInvolvement furcation_involvement = FurcationInvolvement.NO;
	private AlveolarAbsorption alveolar_absorption = AlveolarAbsorption.No;
	private ClassificationOfSurveyLineOnBuccalSurface classification_of_survey_line_on_buccal_surface
			= ClassificationOfSurveyLineOnBuccalSurface.UnknownDegree;
	private rpd.conceptions.ToothPosition tooth_position = rpd.conceptions.ToothPosition.No;

	public ToothPosition getToothPosition() {
		return tooth_position;
	}

	public void setToothPosition(ToothPosition tooth_position) {
		this.tooth_position = tooth_position;
	}

	public AlveolarAbsorption getAlveolarAbsorption() {
		return alveolar_absorption;
	}

	public void setAlveolarAbsorption(AlveolarAbsorption alveolar_absorption) {
		this.alveolar_absorption = alveolar_absorption;
	}

	public boolean isSpaceBelowGingivalMargins() {
		return space_below_gingival_margins;
	}

	public void setSpaceBelowGingivalMargins(boolean space_below_gingival_margins) {
		this.space_below_gingival_margins = space_below_gingival_margins;
	}

	public boolean getBuccalSurfaceSlope() {
		return buccal_surface_slope;
	}

	public void setBuccalSurfaceSlope(boolean buccal_surface_slope) {
		this.buccal_surface_slope = buccal_surface_slope;
	}

	public boolean getLingualSurfaceSlope() {
		return lingual_surface_slope;
	}

	public void setLingualSurfaceSlope(boolean lingual_surface_slope) {
		this.lingual_surface_slope = lingual_surface_slope;
	}

	public boolean isCingulum() {
		return cingulum;
	}

	public void setCingulum(boolean cingulum) {
		this.cingulum = cingulum;
	}

	public boolean isTorus() {
		return torus;
	}

	public void setTorus(boolean torus) {
		this.torus = torus;
	}

	public FurcationInvolvement getFurcationInvolvement() {
		return furcation_involvement;
	}

	public void setFurcationInvolvement(FurcationInvolvement furcation_involvement) {
		this.furcation_involvement = furcation_involvement;
	}

	public ClassificationOfSurveyLineOnBuccalSurface getClassificationOfSurveyLineOnBuccalSurface() {
		return classification_of_survey_line_on_buccal_surface;
	}

	public void setClassificationOfSurveyLineOnBuccalSurface(ClassificationOfSurveyLineOnBuccalSurface classification_of_survey_line_on_buccal_surface) {
		this.classification_of_survey_line_on_buccal_surface = classification_of_survey_line_on_buccal_surface;
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

	public boolean isBoneUndercut() {
		return this.bone_undercut;
	}

	public void setBoneUndercut(boolean bone_undercut) {
		this.bone_undercut = bone_undercut;
	}

	public ToothMobility getMobility() {
		return this.mobility;
	}

	public void setMobility(ToothMobility mobility) {
		this.mobility = mobility;
	}

	public CrownRootRatio getCrownRootRatio() {
		return crown_root_ratio;
	}

	public void setCrownRootRatio(CrownRootRatio crown_root_ratio) {
		this.crown_root_ratio = crown_root_ratio;
	}
	
	public Tooth(int zone, int num) {
		
		this.zone = zone;
		this.num = num;
		
		if(num == 1 || num == 2)
			this.tooth_type = ToothType.Incisor;
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

	public int getToothDistance(Tooth tooth) {
		//返回值为牙位数字做差，即间隔牙数+1
		int res = -1;
		if (this.zone == 1 || this.zone == 2) {
			if (tooth.getZone() != 1 && tooth.getZone() != 2) {
				return -1;
			}
			else {
				if (this.zone == tooth.getZone()) {
					res = Math.abs(this.num - tooth.getNum());
				}
				else {
					res = this.num + tooth.getNum() - 1;
				}
			}
		}
		else if (this.zone == 3 || this.zone == 4) {
			if (tooth.getZone() != 3 && tooth.getZone() != 4) {
				return -1;
			}
			else {
				if (this.zone == tooth.getZone()) {
					res = Math.abs(this.num - tooth.getNum());
				}
				else {
					res = this.num + tooth.getNum() - 1;
				}
			}
		}
		else {
			return -2;
		}
		return res;
	}
	
	@Override
	public int compareTo(Tooth arg0) {
		return this.toString().compareTo(arg0.toString());
	}
}
