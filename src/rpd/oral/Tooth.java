package rpd.oral;

import rpd.conceptions.*;

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
	private boolean buccal_surface_slope = false;
	private boolean lingual_surface_slope = false;
	private boolean cingulum = false;

	private ToothMobility mobility = ToothMobility.No;
	private GingivalRecession gingival_recession = GingivalRecession.No;
	private AlveolarAbsorption alveolar_absorption = AlveolarAbsorption.No;
	private CrownRootRatio crown_root_ratio = CrownRootRatio.NO;
	private FurcationInvolvement furcation_involvement = FurcationInvolvement.NO;
	private TeethRelatedImaging teeth_related_imaging = TeethRelatedImaging.NO;
	private ClassificationOfSurveyLineOnBuccalSurface classification_of_survey_line_on_buccal_surface
			= ClassificationOfSurveyLineOnBuccalSurface.NO;

	//	private float gingival_recession = 0;
	private float space_below_gingival_margins = 0;

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


	public FurcationInvolvement getFurcationInvolvement() {
		return furcation_involvement;
	}

	public void setFurcationInvolvement(FurcationInvolvement furcation_involvement) {
		this.furcation_involvement = furcation_involvement;
	}

	public TeethRelatedImaging getTeethRelatedImaging() {
		return teeth_related_imaging;
	}

	public void setTeethRelatedImaging(TeethRelatedImaging teeth_related_imaging) {
		this.teeth_related_imaging = teeth_related_imaging;
	}

	public ClassificationOfSurveyLineOnBuccalSurface getClassificationOfSurveyLineOnBuccalSurface() {
		return classification_of_survey_line_on_buccal_surface;
	}

	public void setClassificationOfSurveyLineOnBuccalSurface(ClassificationOfSurveyLineOnBuccalSurface classification_of_survey_line_on_buccal_surface) {
		this.classification_of_survey_line_on_buccal_surface = classification_of_survey_line_on_buccal_surface;
	}



	public float getSpaceBelowGingivalMargins() {
		return space_below_gingival_margins;
	}

	public void setSpaceBelowGingivalMargins(float space_below_gingival_margins) {
		this.space_below_gingival_margins = space_below_gingival_margins;
	}

	
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

	public CrownRootRatio getCrownRootRatio() {
		return crown_root_ratio;
	}

	public void setCrownRootRatio(CrownRootRatio crown_root_ratio) {
		this.crown_root_ratio = crown_root_ratio;
	}

	@Override
	public int compareTo(Tooth arg0) {
		return this.toString().compareTo(arg0.toString());
	}
}
