package ticTacToe;


import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Map;
/**
 * A policy iteration agent. You should implement the following methods:
 * (1) {@link PolicyIterationAgent#evaluatePolicy}: this is the policy evaluation step from your lectures
 * (2) {@link PolicyIterationAgent#improvePolicy}: this is the policy improvement step from your lectures
 * (3) {@link PolicyIterationAgent#train}: this is a method that should runs/alternate (1) and (2) until convergence. 
 * 
 * NOTE: there are two types of convergence involved in Policy Iteration: Convergence of the Values of the current policy, 
 * and Convergence of the current policy to the optimal policy.
 * The former happens when the values of the current policy no longer improve by much (i.e. the maximum improvement is less than 
 * some small delta). The latter happens when the policy improvement step no longer updates the policy, i.e. the current policy 
 * is already optimal. The algorithm should stop when this happens.
 * 
 * @author ae187
 *
 */
public class PolicyIterationAgent extends Agent {

	/**
	 * This map is used to store the values of states according to the current policy (policy evaluation). 
	 */
	HashMap<Game, Double> policyValues=new HashMap<Game, Double>();
	
	/**
	 * This stores the current policy as a map from {@link Game}s to {@link Move}. 
	 */
	HashMap<Game, Move> curPolicy=new HashMap<Game, Move>();
	
	double discount=0.9;
	
	/**
	 * The mdp model used, see {@link TTTMDP}
	 */
	TTTMDP mdp;
	
	/**
	 * loads the policy from file if one exists. Policies should be stored in .pol files directly under the project folder.
	 */
	public PolicyIterationAgent() {
		super();
		this.mdp=new TTTMDP();
		initValues();
		initRandomPolicy();
		train();
		
		
	}
	
	
	/**
	 * Use this constructor to initialise your agent with an existing policy
	 * @param p
	 */
	public PolicyIterationAgent(Policy p) {
		super(p);
		
	}

	/**
	 * Use this constructor to initialise a learning agent with default MDP paramters (rewards, transitions, etc) as specified in 
	 * {@link TTTMDP}
	 * @param discountFactor
	 */
	public PolicyIterationAgent(double discountFactor) {
		
		this.discount=discountFactor;
		this.mdp=new TTTMDP();
		initValues();
		initRandomPolicy();
		train();
	}
	/**
	 * Use this constructor to set the various parameters of the Tic-Tac-Toe MDP
	 * @param discountFactor
	 * @param winningReward
	 * @param losingReward
	 * @param livingReward
	 * @param drawReward
	 */
	public PolicyIterationAgent(double discountFactor, double winningReward, double losingReward, double livingReward, double drawReward)
	{
		this.discount=discountFactor;
		this.mdp=new TTTMDP(winningReward, losingReward, livingReward, drawReward);
		initValues();
		initRandomPolicy();
		train();
	}
	/**
	 * Initialises the {@link #policyValues} map, and sets the initial value of all states to 0 
	 * (V0 under some policy pi ({@link #curPolicy} from the lectures). Uses {@link Game#inverseHash} and {@link Game#generateAllValidGames(char)} to do this. 
	 * 
	 */
	public void initValues()
	{
		List<Game> allGames=Game.generateAllValidGames('X');//all valid games where it is X's turn, or it's terminal.
		for(Game g: allGames)
			this.policyValues.put(g, 0.0);
		
	}
	
	/**
	 *  You should implement this method to initially generate a random policy, i.e. fill the {@link #curPolicy} for every state. Take care that the moves you choose
	 *  for each state ARE VALID. You can use the {@link Game#getPossibleMoves()} method to get a list of valid moves and choose 
	 *  randomly between them. 
	 */
	public void initRandomPolicy()
	{
		Random rand = new Random();
		List<Move> moves; // list of all possible moves from game state (game)
		for (Game game : this.policyValues.keySet()){

			// if g is terminal, skip it
			if (game.isTerminal())
				continue;
			moves = game.getPossibleMoves(); // store the list of all possible moves from game state g in (move)
			// put random moves in curPolicy for g
			this.curPolicy.put(game, moves.get(rand.nextInt(moves.size())));
		}
	}
	
	
	/**
	 * Performs policy evaluation steps until the maximum change in values is less than {@code delta}, in other words
	 * until the values under the currrent policy converge. After running this method, 
	 * the {@link PolicyIterationAgent#policyValues} map should contain the values of each reachable state under the current policy. 
	 * You should use the {@link TTTMDP} {@link PolicyIterationAgent#mdp} provided to do this.
	 *
	 * @param delta
	 */
	protected void evaluatePolicy(double delta)
	{
		/* YOUR CODE HERE */
		for (Game game : this.policyValues.keySet()){
			if (game.isTerminal()){//terminal states have no future rewards their value is always 0
				this.policyValues.put(game, 0.0);
				continue;
			}
			double Sum, Prev;
			do {
				Sum = 0;// reset the q-value accumulator for the current state
				for (TransitionProb tp : this.mdp.generateTransitions(game, this.curPolicy.get(game))) {// iterate over all possible transitions for the current action in the policy
					Sum += tp.prob * (tp.outcome.localReward + (discount * this.policyValues.get(tp.outcome.sPrime)));// update the q-value with the transition probability, local reward, and discounted future value
				}

				
				Prev = this.policyValues.get(game);// save the old value for convergence checking
				this.policyValues.put(game, Sum);//update 

				//repeat until the change in value is < the convergence threshold delta
			} while (Math.abs(this.policyValues.get(game)-Prev) > delta);

		}
		
	}
		
	
	
	/**This method should be run AFTER the {@link PolicyIterationAgent#evaluatePolicy} train method to improve the current policy according to 
	 * {@link PolicyIterationAgent#policyValues}. You will need to do a single step of expectimax from each game (state) key in {@link PolicyIterationAgent#curPolicy} 
	 * to look for a move/action that potentially improves the current policy. 
	 * 
	 * @return true if the policy improved. Returns false if there was no improvement, i.e. the policy already returned the optimal actions.
	 */
	protected boolean improvePolicy()
	{
		/* YOUR CODE HERE */
		Policy copy_policy = new Policy();
		for (Map.Entry<Game, Move> entry : this.curPolicy.entrySet())// copy all existing state-action mappings into the backup policy
		{
			copy_policy.policy.put(entry.getKey(), entry.getValue());
			
		}		
		for(Game game : this.curPolicy.keySet()){//single-step expectimax for policy improvement
			double pre_value = this.policyValues.get(game);
			for (Move move : game.getPossibleMoves()){
				double Sum = 0;// reset q-value for the current move
				for(TransitionProb t : this.mdp.generateTransitions(game,move)){//calculate the q-value for this move by considering all possible outcomes
					Sum += t.prob*(t.outcome.localReward+(discount*this.policyValues.get(t.outcome.sPrime)));// update the q-value with the transition probability, local reward, and discounted future value
				}

				
				if (Sum > pre_value){// if move yields a higher q-value update the policy and state value
					pre_value = Sum;
					this.curPolicy.put(game,move);
				}
			}
		}		
		if (this.curPolicy.equals(copy_policy.policy) != true)//compare the updated policy with the backup to determine if improvement occurred
			return true;
		else{
			return false;
		}
	}
	
	/**
	 * The (convergence) delta
	 */
	double delta=0.1;
	
	/**
	 * This method should perform policy evaluation and policy improvement steps until convergence (i.e. until the policy
	 * no longer changes), and so uses your 
	 * {@link PolicyIterationAgent#evaluatePolicy} and {@link PolicyIterationAgent#improvePolicy} methods.
	 */
	public void train()
	{
		/* YOUR CODE HERE */
		this.initRandomPolicy();
		do{
			
			this.evaluatePolicy(delta);
		}
		while(this.improvePolicy());//improve the policy and repeat until the policy has no changes
		

		
		Policy new_policy = new Policy(curPolicy);
		super.policy = new_policy;//update the agent policy with the optimized one
		
		
	}
	
	public static void main(String[] args) throws IllegalMoveException
	{
		/**
		 * Test code to run the Policy Iteration Agent agains a Human Agent.
		 */
		PolicyIterationAgent pi=new PolicyIterationAgent();
		
		HumanAgent h=new HumanAgent();
		
		Game g=new Game(pi, h, h);
		
		g.playOut();
		
		
	}
	

}
