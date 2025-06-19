package ticTacToe;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Value Iteration Agent, only very partially implemented. The methods to implement are: 
 * (1) {@link ValueIterationAgent#iterate}
 * (2) {@link ValueIterationAgent#extractPolicy}
 * 
 * You may also want/need to edit {@link ValueIterationAgent#train} - feel free to do this, but you probably won't need to.
 * @author ae187
 *
 */
public class ValueIterationAgent extends Agent {

	/**
	 * This map is used to store the values of states
	 */
	Map<Game, Double> valueFunction=new HashMap<Game, Double>();
	
	/**
	 * the discount factor
	 */
	double discount=0.9;
	
	/**
	 * the MDP model
	 */
	TTTMDP mdp=new TTTMDP();
	
	/**
	 * the number of iterations to perform - feel free to change this/try out different numbers of iterations
	 */
	int k=10;
	
	
	/**
	 * This constructor trains the agent offline first and sets its policy
	 */
	public ValueIterationAgent()
	{
		super();
		mdp=new TTTMDP();
		this.discount=0.9;
		initValues();
		train();
	}
	
	
	/**
	 * Use this constructor to initialise your agent with an existing policy
	 * @param p
	 */
	public ValueIterationAgent(Policy p) {
		super(p);
		
	}

	public ValueIterationAgent(double discountFactor) {
		
		this.discount=discountFactor;
		mdp=new TTTMDP();
		initValues();
		train();
	}
	
	/**
	 * Initialises the {@link ValueIterationAgent#valueFunction} map, and sets the initial value of all states to 0 
	 * (V0 from the lectures). Uses {@link Game#inverseHash} and {@link Game#generateAllValidGames(char)} to do this. 
	 * 
	 */
	public void initValues()
	{
		
		List<Game> allGames=Game.generateAllValidGames('X');//all valid games where it is X's turn, or it's terminal.
		for(Game g: allGames)
			this.valueFunction.put(g, 0.0);
		
		
		
	}
	
	
	
	public ValueIterationAgent(double discountFactor, double winReward, double loseReward, double livingReward, double drawReward)
	{
		this.discount=discountFactor;
		mdp=new TTTMDP(winReward, loseReward, livingReward, drawReward);
	}
	
	/**
	 
	
	/*
	 * Performs {@link #k} value iteration steps. After running this method, the {@link ValueIterationAgent#valueFunction} map should contain
	 * the (current) values of each reachable state. You should use the {@link TTTMDP} provided to do this.
	 * 
	 *
	 */
	public void iterate() {
		
		/* YOUR CODE HERE */
		
	    double Sum, Max; // variables to hold q-value and maximum 
	    
	    for (int i = 0; i < k; i++) {
	        for (Game game : this.valueFunction.keySet()) { // loop through all games in the value function
	            
	            if (game.isTerminal()) { // skip processing for terminal  states
	                this.valueFunction.put(game, 0.0); 
	                continue; //to next state
	            }
	            Max = -Integer.MAX_VALUE; // initialize max to a very small value
	            for (Move move : game.getPossibleMoves()) {
	                Sum = 0; // Reset sum for each move
	                for (TransitionProb tp : mdp.generateTransitions(game, move)) {// generate transitions for the given move and calculate the q-value	                    
	                    Sum += tp.prob * (tp.outcome.localReward + 
	                                     (discount * this.valueFunction.get(tp.outcome.sPrime)));
	                }
	                if (Sum > Max) {
	                    Max = Sum;
	                }
	            }
	            
	            this.valueFunction.put(game, Max);// update the value function
	            
	        }
	        
	        
	        
	    }
	    
	}

	/**
	 * Extracts a policy based on the value function computed by iterate().
	 * This method should be run AFTER the train method.
	 * 
	 * @return the policy mapping states to optimal actions.
	 */
	public Policy extractPolicy() {
		
		/* YOUR CODE HERE */
		// almost identical code like iterate function
		Policy policy = new Policy(); // creste new policy object
	    Move max_move = null; // variable to hold move with the maximum q-value
	    double Sum, Max;
	    for (Game game : this.valueFunction.keySet()) { //iterate through all games in the value function
	        if (game.isTerminal()) {
	            this.valueFunction.put(game, 0.0);
	            continue; // continue.....
	        }
	        Max = -Integer.MAX_VALUE;
	        for (Move move : game.getPossibleMoves()) { //lop through all possible moves for the state
	            Sum = 0; //reset sum for each mov
	            for (TransitionProb tp : mdp.generateTransitions(game, move)) {// calculate the q-value for the curent move
	                Sum += tp.prob * (tp.outcome.localReward + 
	                                 (discount * this.valueFunction.get(tp.outcome.sPrime)));
	            }
	            
	            
	            if (Sum > Max) { // update max and maxMove if the current Q-value is greater
	                Max = Sum;
	                max_move = move;
	            }
	        }// add the optimal move (maxMove) to the policy for the curent state
	        policy.policy.put(game, max_move);
	        
	    }
	    
	    
	    return policy; // return the commputed policy
	    
	    
	    
	}

	
	/**
	 * This method solves the mdp using your implementation of {@link ValueIterationAgent#extractPolicy} and
	 * {@link ValueIterationAgent#iterate}. 
	 */
	public void train()
	{
		/**
		 * First run value iteration
		 */
		this.iterate();
		/**
		 * now extract policy from the values in {@link ValueIterationAgent#valueFunction} and set the agent's policy 
		 *  
		 */
		
		super.policy=extractPolicy();
		
		if (this.policy==null)
		{
			System.out.println("Unimplemented methods! First implement the iterate() & extractPolicy() methods");
			//System.exit(1);
		}
		
		
		
	}

	public static void main(String a[]) throws IllegalMoveException
	{
		//Test method to play the agent against a human agent.
		ValueIterationAgent agent=new ValueIterationAgent();
		HumanAgent d=new HumanAgent();
		
		Game g=new Game(agent, d, d);
		g.playOut();
		
		
		

		
		
	}
}
