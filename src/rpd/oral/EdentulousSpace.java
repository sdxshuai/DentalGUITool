package rpd.oral;

import exceptions.rpd.RuleException;
import rpd.conceptions.EdentulousType;
import rpd.conceptions.Position;
import rpd.rules.EdentulousTypeRule;

import java.util.ArrayList;
import java.util.List;

//缺牙区域
public class EdentulousSpace {

	private EdentulousType edentulous_type = EdentulousType.Undefined;

	private Tooth left_neighbor = null;
	private Tooth right_neighbor = null;
	private Tooth left_most = null;
	private Tooth right_most = null;

	private boolean is_mandibular = false;
	private boolean is_maxillary = false;

	public EdentulousSpace(Tooth left_neighbor, Tooth right_neighbor, Position mandibular_or_maxillary) throws RuleException {

		this.left_neighbor = left_neighbor;
		this.right_neighbor = right_neighbor;


		if (this.left_neighbor == null) {
			if (mandibular_or_maxillary == Position.Mandibular) {
				this.left_most = new Tooth(4, 7);
			} else {
				this.left_most = new Tooth(1, 7);
			}
		} else {
			int numLeftNeighbor = this.left_neighbor.getNum();
			int zoneLeftNeighbor = this.left_neighbor.getZone();
			if (numLeftNeighbor == 1 && (zoneLeftNeighbor == 1 || zoneLeftNeighbor == 4)) {
				if (mandibular_or_maxillary == Position.Mandibular) {
					this.left_most = new Tooth(3, 1);
				} else {
					this.left_most = new Tooth(2, 1);
				}
			} else {
				if (zoneLeftNeighbor == 1 || zoneLeftNeighbor == 4) {
					this.left_most = new Tooth(zoneLeftNeighbor, numLeftNeighbor - 1);
				} else {
					this.left_most = new Tooth(zoneLeftNeighbor, numLeftNeighbor + 1);
				}
			}
		}

		if (this.right_neighbor == null) {
			if (mandibular_or_maxillary == Position.Mandibular) {
				this.right_most = new Tooth(3, 7);
			} else {
				this.right_most = new Tooth(2, 7);
			}
		} else {
			int numRightNeighbor = this.right_neighbor.getNum();
			int zoneRightNeighbor = this.right_neighbor.getZone();
			if (numRightNeighbor == 1 && (zoneRightNeighbor == 2 || zoneRightNeighbor == 3)) {
				if (mandibular_or_maxillary == Position.Mandibular) {
					this.right_most = new Tooth(4, 1);
				} else {
					this.right_most = new Tooth(1, 1);
				}
			} else {
				if (zoneRightNeighbor == 2 || zoneRightNeighbor == 3) {
					this.right_most = new Tooth(zoneRightNeighbor, numRightNeighbor - 1);
				} else {
					this.right_most = new Tooth(zoneRightNeighbor, numRightNeighbor + 1);
				}
			}
		}

		if (mandibular_or_maxillary.equals(Position.Mandibular))
			this.is_mandibular = true;
		if (mandibular_or_maxillary.equals(Position.Maxillary))
			this.is_maxillary = true;
		computeType();
	}

	public boolean isMandibular() {
		return this.is_mandibular;
	}

	public boolean isMaxillary() {
		return this.is_maxillary;
	}

	public EdentulousType getEdentulousType() {
		return this.edentulous_type;
	}

	public Tooth getLeftNeighbor() {
		return this.left_neighbor;
	}

	public Tooth getRightNeighbor() {
		return this.right_neighbor;
	}

	public Tooth getLeftMost() {
		return this.left_most;
	}

	public Tooth getRightMost() {
		return this.right_most;
	}

	public int getNumMissingTeeth() {
		if (this.left_most.getZone() == this.right_most.getZone()){
			return Math.abs(this.left_most.getNum() - this.right_most.getNum()) + 1;
		} else {
			return this.left_most.getNum() + this.right_most.getNum();
		}
	}

	private void computeType() throws RuleException {

		List<EdentulousType> types = new ArrayList<EdentulousType>();
		if (EdentulousTypeRule.edentulous_type_rules == null)
			EdentulousTypeRule.initRules();
		for (EdentulousTypeRule edentulous_type_rule : EdentulousTypeRule.edentulous_type_rules) {
			EdentulousType res = edentulous_type_rule.apply(this);
			if (res != null)
				types.add(res);
		}

		if (types.size() > 1)
			throw new RuleException("more than 1 rules applied");
		else if (types.size() == 1)
			this.edentulous_type = types.get(0);
		else {
		}
	}
	
	/*private void computeTypeOld() {
		
		if(left_neighbor == null && right_neignbor == null)
			edentulous_type = EdentulousType.AllMissing;
		
		else if(left_neighbor != null && right_neignbor != null) {
			
			int left_neighbor_zone = left_neighbor.getZone();
			int right_neighbor_zone = right_neignbor.getZone();
			int left_neighbor_num = left_neighbor.getNum();
			int right_neignbor_num = right_neignbor.getNum();
			
			if(left_neighbor_zone == right_neighbor_zone)
				edentulous_type = EdentulousType.ToothSupport;
			else
				edentulous_type = EdentulousType.AnteriorExtension;
		}
		
		else if(left_neighbor == null && right_neignbor != null) {
			
			//int left_neighbor_zone = left_neighbor.getZone();
			int right_neignbor_zone = right_neignbor.getZone();
			
			if(right_neignbor_zone == 1 || right_neignbor_zone == 4)
				edentulous_type = EdentulousType.PosteriorExtension;
			else
				edentulous_type = EdentulousType.Undefined;
		}
		
		else if(left_neighbor != null && right_neignbor == null) {
			
			int left_neighbor_zone = left_neighbor.getZone();
			//int right_neignbor_zone = right_neignbor.getZone();
			
			if(left_neighbor_zone == 2 || left_neighbor_zone == 3)
				edentulous_type = EdentulousType.PosteriorExtension;
			else
				edentulous_type = EdentulousType.Undefined;
		}
		
		else {}
	}*/
}
