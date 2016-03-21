package agent.planningagent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import environnement.Action;
import environnement.Etat;
import environnement.MDP;
import environnement.gridworld.ActionGridworld;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.HashMapUtil;


/**
 * Cet agent met a jour sa fonction de valeur avec value iteration 
 * et choisit ses actions selon la politique calculee.
 * @author laetitiamatignon
 *
 */
public class ValueIterationAgent extends PlanningValueAgent{
	/**
	 * discount facteur
	 */
	protected double gamma;
        protected HashMapUtil v;
	//*** VOTRE CODE


	
	/**
	 * 
	 * @param gamma
	 * @param mdp
	 */
	public ValueIterationAgent(double gamma,MDP mdp) {
		super(mdp);
		this.gamma = gamma;
		//*** VOTRE CODE
                this.v = new HashMapUtil();
                reset();
	}
	
	
	public ValueIterationAgent(MDP mdp) {
		this(0.9,mdp);

	}
	
	/**
	 * 
	 * Mise a jour de V: effectue UNE iteration de value iteration 
	 */
	@Override
	public void updateV(){
		//delta est utilise pour detecter la convergence de l'algorithme
		//lorsque l'on planifie jusqu'a convergence, on arrete les iterations lorsque
		//delta < epsilon 
		this.delta=0.0;
                HashMapUtil cloneV = (HashMapUtil) this.v.clone();
                for (Etat e: this.mdp.getEtatsAccessibles()){
                    double max = -Double.MAX_VALUE;
                    List<Action> actionsPossibles = this.mdp.getActionsPossibles(e);
                    for (Action a: actionsPossibles){
                        Double sum = 0.0;
                        try {
                            Map<Etat,Double> transitions = this.mdp.getEtatTransitionProba(e, a);
                            for(Etat eTrans: transitions.keySet()){
                                sum += transitions.get(eTrans) * (this.mdp.getRecompense(e, a, eTrans) + this.gamma * cloneV.get(eTrans));
                            }
                        } catch (Exception ex) {
                            Logger.getLogger(ValueIterationAgent.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        max = Math.max(max, sum);
                    }
                    this.v.put(e, max);
                }
                
                Double diff_max = 0.0;
                for(Etat e : this.v.keySet()){
                    if (diff_max < Math.abs(this.v.get(e) - cloneV.get(e))){
                        diff_max = Math.abs(this.v.get(e) - cloneV.get(e));
                    }
                }
		super.delta = diff_max;
                
		// mise a jour vmax et vmin pour affichage du gradient de couleur:
                //vmax est la valeur de max pour tout s de V
		//vmin est la valeur de min pour tout s de V
		// ...
                Double t_vmin = Double.MAX_VALUE;
                double t_vmax = -Double.MAX_VALUE;
                for (Double v_value : this.v.values()){
                    t_vmin = Math.min(t_vmin, v_value);
                    t_vmax = Math.max(t_vmax, v_value);
                }
                super.vmin = t_vmin;
                super.vmax = t_vmax;
		
		//******************* a laisser a la fin de la methode
		this.notifyObs();
	}
	
	
	/**
	 * renvoi l'action executee par l'agent dans l'etat e
        * @param e
        * @return 
	 */
	@Override
	public Action getAction(Etat e) {
		List<Action> actionsPossibles = getPolitique(e);
                if(actionsPossibles.size() == 1){
                    return actionsPossibles.get(0);
                }
                else if (actionsPossibles.size() > 0){
                    Random r = new Random();
                    return actionsPossibles.get(r.nextInt(actionsPossibles.size()));
                }
                else{
                    return ActionGridworld.NONE;
                }
	}
	@Override
	public double getValeur(Etat _e) {
		return this.v.get(_e);
	}
        
	/**
	 * renvoi la (les) action(s) de plus forte(s) valeur(s) dans l'etat e 
	 * (plusieurs actions sont renvoyees si valeurs identiques, liste vide si aucune action n'est possible)
	 */
	@Override
	public List<Action> getPolitique(Etat _e) {
            List<Action> l = new ArrayList<Action>();
            List<Action> actionsPossibles = mdp.getActionsPossibles(_e);
            double maxVal = -Double.MAX_VALUE;
            for (Action a: actionsPossibles){
                try {
                    HashMap<Etat,Double> hmap = (HashMap<Etat,Double>) this.mdp.getEtatTransitionProba(_e, a);
                    double sum = 0.;
                    for (Etat e : hmap.keySet()){
                        sum += hmap.get(e) * (this.mdp.getRecompense(_e, a, e) + this.gamma * this.v.get(e));
                    }
                    if(sum > maxVal) {
                        maxVal = sum;
                        l.clear();
                        l.add(a);
                    }
                    else if (sum == maxVal){
                        l.add(a);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(ValueIterationAgent.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return l;
	}
	
	@Override
	public void reset() {
		super.reset();	
                for (Etat e : this.mdp.getEtatsAccessibles()){
                    this.v.put(e, 0.0);
                }
                super.vmin = Double.MAX_VALUE;
                super.vmax = -Double.MAX_VALUE;
		/*-----------------*/
		this.notifyObs();

	}


	@Override
	public void setGamma(double arg0) {
		this.gamma = arg0;
	}

	
}
