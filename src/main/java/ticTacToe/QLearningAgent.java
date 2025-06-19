package ticTacToe;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * A Q-Learning agent with a Q-Table, i.e. a table of Q-Values. This table is implemented in the {@link QTable} class.
 * 
 *  The methods to implement are: 
 * (1) {@link QLearningAgent#train}
 * (2) {@link QLearningAgent#extractPolicy}
 * 
 * Your agent acts in a {@link TTTEnvironment} which provides the method {@link TTTEnvironment#executeMove} which returns an {@link Outcome} object, in other words
 * an [s,a,r,s']: source state, action taken, reward received, and the target state after the opponent has played their move. You may want/need to edit
 * {@link TTTEnvironment} - but you probably won't need to. 
 * @author ae187
 */

public class QLearningAgent extends Agent {
	
	/**
	 * The learning rate, between 0 and 1.
	 */
	double alpha=0.1;
	
	/**
	 * The number of episodes to train for
	 */
	int numEpisodes=30000;
	
	/**
	 * The discount factor (gamma)
	 */
	double discount=0.9;
	
	
	/**
	 * The epsilon in the epsilon greedy policy used during training.
	 */
	double epsilon=0.1;
	
	/**
	 * This is the Q-Table. To get an value for an (s,a) pair, i.e. a (game, move) pair.
	 * 
	 */
	
	QTable qTable=new QTable();
	
	
	/**
	 * This is the Reinforcement Learning environment that this agent will interact with when it is training.
	 * By default, the opponent is the random agent which should make your q learning agent learn the same policy 
	 * as your value iteration and policy iteration agents.
	 */
	TTTEnvironment env=new TTTEnvironment();
	
	
	/**
	 * Construct a Q-Learning agent that learns from interactions with {@code opponent}.
	 * @param opponent the opponent agent that this Q-Learning agent will interact with to learn.
	 * @param learningRate This is the rate at which the agent learns. Alpha from your lectures.
	 * @param numEpisodes The number of episodes (games) to train for
	 */
	public QLearningAgent(Agent opponent, double learningRate, int numEpisodes, double discount)
	{
		env=new TTTEnvironment(opponent);
		this.alpha=learningRate;
		this.numEpisodes=numEpisodes;
		this.discount=discount;
		initQTable();
		train();
	}
	
	/**
	 * Initialises all valid q-values -- Q(g,m) -- to 0.
	 *  
	 */
	
	protected void initQTable()
	{
		List<Game> allGames=Game.generateAllValidGames('X');//all valid games where it is X's turn, or it's terminal.
		for(Game g: allGames)
		{
			List<Move> moves=g.getPossibleMoves();
			for(Move m: moves)
			{
				this.qTable.addQValue(g, m, 0.0);
				//System.out.println("initing q value. Game:"+g);
				//System.out.println("Move:"+m);
			}
			
		}
		
	}
	
	/**
	 * Uses default parameters for the opponent (a RandomAgent) and the learning rate (0.2). Use other constructor to set these manually.
	 */
	public QLearningAgent()
	{
		this(new RandomAgent(), 0.5, 50000, 0.9);
		
	}
	
	
	/**
	 *  Implement this method. It should play {@code this.numEpisodes} episodes of Tic-Tac-Toe with the TTTEnvironment, updating q-values according 
	 *  to the Q-Learning algorithm as required. The agent should play according to an epsilon-greedy policy where with the probability {@code epsilon} the
	 *  agent explores, and with probability {@code 1-epsilon}, it exploits. 
	 *  
	 *  At the end of this method you should always call the {@code extractPolicy()} method to extract the policy from the learned q-values. This is currently
	 *  done for you on the last line of the method.
	 */
	public void train() {
	    /* 
	     * YOUR CODE HERE
	     */
	    
	    for (int u = 0; u < this.numEpisodes; u++) { // loop number of episodes  
	        env = new TTTEnvironment();  
	        Game current_state = env.getCurrentGameState(); 
	        Move current_action = null;  

	        while (!current_state.isTerminal()) { //terminal state    
	            Game current_stateCopy = new Game(current_state); //copy of the current state   
	            Random rand = new Random();
       
	            if (rand.nextDouble() < epsilon) { 
	                // Explore   
	                List<Move> moves = current_stateCopy.getPossibleMoves();   
	                current_action = moves.get(rand.nextInt(moves.size())); //random valid move  
	            } else { 
	                // Exploit
	                List<Move> moves = current_stateCopy.getPossibleMoves();   
	                Double maxActionValue = Double.NEGATIVE_INFINITY; //max value for comparison    
	                for (Move m : moves) {  
	                    if (qTable.getQValue(current_stateCopy, m) > maxActionValue) { 
	                        current_action = m; // update the best action
	                        maxActionValue = qTable.getQValue(current_stateCopy, m); // update the maximum q-value		
	                    } else {
	                        continue; // skip if q-value is worse
	                    }
	                }
	            } 

	            epsilon = epsilon * 0.9999; //decrease epsilon to shift from exploration to exploitation	 	
  
	            Outcome outcome = null; 
	            try { 
	                outcome = env.executeMove(current_action); // execute the chose n action and show the outcome	   
	            } catch  (IllegalMoveException e) {
	                e.printStackTrace(); // track illegal moves
	            }
 
	            current_state = env.getCurrentGameState(); // update the current state after move   
	            Double nextQVal;		 
	               
	            if (current_state.isTerminal() == false) { // if the next state is not  terminal find the maximum q-value of possible moves 
	                Move nextAction = null;			
	                List<Move> nextActions = current_state.getPossibleMoves();		
	                Double nextMaxActionValue = Double.NEGATIVE_INFINITY;
	                for (Move n : nextActions) {
	                    if (qTable.getQValue(current_state, n) > nextMaxActionValue) {			
	                        nextAction = n; // update the best next action		
	                        nextMaxActionValue = qTable.getQValue(current_state, n); //   update the max q-value
	                    }  
	                } 
	                nextQVal = qTable.getQValue(current_state, nextAction); // use the best q-value for the next state
	            } else { 
	                nextQVal = 0.00; // if the next state is terminal its q-value is 0
	            } 

	            Double currentQVal = qTable.getQValue(current_stateCopy, current_action) 
	                    + alpha * (outcome.localReward + (discount * nextQVal) 
	                    - qTable.getQValue(current_stateCopy, current_action));// updating the q-value using the q-learning update rule 
	            
	            qTable.addQValue(current_stateCopy, current_action, currentQVal); // store the updated q-value in the q-table
	            if (current_state.isTerminal()) {     
	                break; // end the episode if the game is over   
	                   
	            }   
	            
	        }   
	        
	        
	    }  

	    this.policy = extractPolicy();
	    
	    if (this.policy == null) {
	        System.out.println("Unimplemented methods! First implement the train() & extractPolicy methods");
	    }
	}

	/** 
	 * Extracts the policy from the Q-table. For each state, the policy selects the action with the highest Q-value.
	 * 
	 * @return the policy currently inherent in the QTable
	 */
	public Policy extractPolicy() {
		/* 
	     * YOUR CODE HERE
	     */
	    HashMap<Game, Move> policyMap = new HashMap<>();
	    List<Game> states = Game.generateAllValidGames('X');
	    
	    
	    for (Game state : states) {
	        if (state.isTerminal()) {
	        	
	            continue; // skip terminal states, as no moves are possible
	        }

	        
	        Move currentMaxMove = null; 
	        Double currentMaxMoveValue = Double.NEGATIVE_INFINITY; // initialize max value for comparison
	        HashMap<Move, Double> actions = qTable.get(state); // get all actions and their q-values for the current state

	        for (Move action : actions.keySet()) {
	        	
	            if (qTable.getQValue(state, action) > currentMaxMoveValue) {
	            	
	            	
	                currentMaxMoveValue = qTable.getQValue(state, action); // update the best Q-value
	                currentMaxMove = action; // update the best move
	                
	            }
	            
	        }
	        
	        policyMap.put(state, currentMaxMove); // map the best move to the current state
	        
	        
	    }
	    
	    Policy policy = new Policy(policyMap); // create a policy object from the policy map
	    return policy; // return the extracted policy
	}
	
	
	public static void main(String a[]) throws IllegalMoveException
	{
		//Test method to play your agent against a human agent (yourself.... if you're not human use a human).
		QLearningAgent agent=new QLearningAgent();
		
		HumanAgent d=new HumanAgent();
		
		Game g=new Game(agent, d, d);
		g.playOut();
	}
}
