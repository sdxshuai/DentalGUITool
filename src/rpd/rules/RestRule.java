package rpd.rules;

import java.util.ArrayList;
import java.util.List;

import exceptions.rpd.ClaspAssemblyException;
import exceptions.rpd.RuleException;
import exceptions.rpd.ToothPosException;
import rpd.RPDPlan;
import rpd.components.CircumferencialClaspAssembly;
import rpd.components.ClaspAssembly;
import rpd.components.RPAAssembly;
import rpd.components.RPIAssembly;
import rpd.components.RingClaspAssembly;
import rpd.components.WWClaspAssembly;
import rpd.conceptions.EdentulousType;
import rpd.conceptions.Position;
import rpd.oral.EdentulousSpace;
import rpd.oral.Mandibular;
import rpd.oral.Mouth;
import rpd.oral.Tooth;

//规则2
public class RestRule {

	public static List<RestRule> rest_rules = null;

	private static Mouth mouth = null;

	public List<RPDPlan> apply(List<RPDPlan> rpd_plan) throws RuleException, ClaspAssemblyException, ToothPosException {
		throw new RuleException("call from abstract class");
	}

	public int getRuleNum() throws RuleException {
		throw new RuleException("call from abstract class");
	}

	public String getExplaination() throws RuleException {
		throw new RuleException("call from abstract class");
	}

	public static void initRules(Mouth mouth_obj) {

		mouth = mouth_obj;
		rest_rules = new ArrayList<RestRule>();

		rest_rules.add(new RestRule() {

			public String getExplaination() {
				return "初始化搜索树，遍历选择支托";
			}

			public String toString() {
				return this.getExplaination();
			}

			public int getRuleNum() {
				return 1;
			}

			public List<RPDPlan> apply(List<RPDPlan> rpd_plans) throws RuleException {
				List<RPDPlan> res = new ArrayList<>();
				res.addAll(rpd_plans);
				return res;
			}
		});

		rest_rules.add(new RestRule() {

			public String getExplaination() {
				return "";
			}

			public String toString() {
				return this.getExplaination();
			}

			public int getRuleNum() {
				return 2;
			}

			public List<RPDPlan> apply(List<RPDPlan> rpd_plans) throws RuleException {
				List<RPDPlan> res = new ArrayList<>();
				res.addAll(rpd_plans);
				return res;
			}
		});
	}
}
