
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import ij.gui.Plot;


public class Diffusion {
///////////////////////////////Input Parameters////////////////
static final int height = 150;
static final int width = 150;
static final int steps = 10; //steps added together before moving  
static final float stepSize = 0.1f;
static final int move = 10000000; //move n positions //
static final int nReceptors = 14; //should not exceed height*width 
static final int hopProb = 5; //0 to 100
static final float concentration = 0.0000000052f; //M     //0.000001f   0.0000000052f
static final float kOn = 27000f;   //M^-1 * s^-1   //270000.0f;  
static final float kOff = 0.00014f;  //s^1            //0.00014f; 
static final float timeStep = 0.00000012f; //0.00000012f seconds 
static final int eventInterval = 100000;  //  interval based on move, not move*steps
static final int generateActive = 50;
///////////////////////////////////////////////////////////////

static final float[][] receptorArray = new float[nReceptors][3];
static final float[][] moveArray = new float[nReceptors][3];
static final float[][] stepArray = new float[nReceptors][3];
static final float[][] stateArray = new float[nReceptors][move];
static final float[][] copyTotalTimeArray = new float[nReceptors][move];
static final float[] totalTimeArray = new float[move];


static ArrayList<Float> xArray = new ArrayList<Float>();
static ArrayList<Float> yArray = new ArrayList<Float>();
static ArrayList<Float> collisionArray = new ArrayList<Float>();
static ArrayList<Float> timePointArray = new ArrayList<Float>();
static ArrayList<Float> collisionPerTimeArray = new ArrayList<Float>();
static ArrayList<Float> switchStateArray = new ArrayList<Float>();
static ArrayList<Float> switchTimeArray = new ArrayList<Float>();
static ArrayList<Float> hopArray = new ArrayList<Float>();
static ArrayList<Float> hopTimeArray = new ArrayList<Float>();
static ArrayList<Float> hopXLocationArray = new ArrayList<Float>();
static ArrayList<Float> hopYLocationArray = new ArrayList<Float>();
static ArrayList<Float> eventXLocationArray = new ArrayList<Float>();
static ArrayList<Float> eventYLocationArray = new ArrayList<Float>();


static float xElements = 0f;
static float yElements = 0f;
static float timePoint = 0.0f;
static float eventPerTime = 0.0f;
static float totalTime = 0.0f;
static float hops = 0.0f;


static int a = 0;  

//switchActive and SwitchInactive are independent probabilities

static ArrayList<Float> eventArray = new ArrayList<Float>();
static float events = 0;


	public static void main(String args[]) throws IOException{
		
		Thread t1 = new Thread(new Runnable(){
			
			@Override
			public void run(){
				long startTime1 =  System.currentTimeMillis();
						
				
				initialArray();
			
				checkArray();
				
				long endTime1 =  System.currentTimeMillis();
				long totalTime1 = endTime1 - startTime1;
				System.out.println("Current simulation runtime is: "+ totalTime1 +" milliseconds");
			}
			
		});
		Thread t2 = new Thread(new Runnable(){
			
			@Override 
			public void run(){
				long startTime2 =  System.currentTimeMillis();
				
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
				
					e.printStackTrace();
				}
				
				FileOutputStream fileOS = null;
				
				try {
					fileOS = new FileOutputStream("C:\\Users\\Mike\\Documents\\BinaryFile.bin");
				} catch (FileNotFoundException e1) {
					
					e1.printStackTrace();
				}
				int recCheck = 0;
				for(int i = 0; i <= move - 1; i++){  //move i times
					
					 //pass things into method when method is called, so initialize variable first
					 //pass m into movementArray so that it can keep track of number
					 //of moves within method (for conjunction with stateSwitchArray)
					
						int m = i;
						if(m%10000 == 0){
						System.out.println("m: "+m);
						}
						//System.out.println(Arrays.deepToString(receptorArray));
						movementArray(m);	
						
						//System.out.println(Arrays.deepToString(moveArray));
						//System.out.println("switchStateArray: "+switchStateArray);
						/////////////////////////////////System.out.println("move: "+i);
					
						addArrays(m); 
							
						ByteArrayOutputStream bas = new ByteArrayOutputStream();
						DataOutputStream ds = new DataOutputStream(bas);
						
						
						while(recCheck == 0){
						try {
							ds.writeFloat(nReceptors);
							ds.writeFloat(steps);
							ds.writeFloat(move);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						recCheck++;
						}
						for(int j = 0;  j <= receptorArray.length - 1; j++){
							for(int k = 0; k <= receptorArray[j].length - 1; k++){
							try {
								ds.writeFloat(receptorArray[j][k]);
								
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
										
							}
						}
						byte[] byteData = bas.toByteArray();
						
						
						try {
							fileOS.write(byteData, 0, byteData.length);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						try {
							fileOS.flush();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
			/////////////////////////////////////////////eventCheck()			
						boolean c = eventCheck();
						
						
						if(c == true){
						//takes timePoint when event occurs (when c == true)
						 timePoint = i*timeStep;
						
						 //tally's number of events at the ith timeStep
						 
						 eventPerTime++;
						 
						//System.out.println("Total number of events: "+events);
						//System.out.println("at time: "+timePoint+" seconds");
						//take data point every eventInterval moves
						}
						if( i%eventInterval == 0 ){
						
							//adds the count number to their respective arrays
							
							Float eventObject = new Float(events);
							Float timePointObject = new Float(timePoint);
							collisionArray.add(eventObject);
							timePointArray.add(timePointObject);
							collisionPerTimeArray.add(eventPerTime);
							
							//reset counter every interval
							eventPerTime = 0f;
							
						}
						
			
						for(int j = 0; j <= nReceptors - 1; j++){
							//System.out.println(receptorArray[j][0]);
							
							stateArray[j][i] = receptorArray[j][0];
						}
			//System.out.println("receptorArray: "+Arrays.deepToString(receptorArray));
			//System.out.println("stateArray: "+Arrays.deepToString(stateArray));
			
			totalTime = i*timeStep;
			
			totalTimeArray[i] = totalTime;
			//System.out.println("totalTime: "+totalTime);
			
			
			
			}  /////////////////////////////end of move for loop  
				//System.out.println("switchStateArray: "+switchStateArray);
				//System.out.println("switchTimeArray: "+switchTimeArray);
				//System.out.println("totalTimeArray: "+Arrays.toString(totalTimeArray));
				
			for(int i = 0; i<= nReceptors - 1; i++){
				for(int j = 0; j<= move - 1; j++){
					
					copyTotalTimeArray[i][j] = totalTimeArray[j];
				}	
			}
			
			//System.out.println("copyTotalTimeArray: "+Arrays.deepToString(copyTotalTimeArray));
			//System.out.println("events: "+events);
			//System.out.println(Arrays.deepToString(xPlot))
			//System.out.println("yArray is: "+yArray);
				try {
					fileOS.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//System.out.println("collisionArray: "+collisionArray);
				//System.out.println("timePointArray: "+timePointArray);
				//System.out.println("collisionPerTimeArray: "+collisionPerTimeArray);
				
/////////////////////////////PLOTS/////////////////////////////////////////////
				
				plotTrajectory();
				plotEvents();
				plotStates();
				//plotStateChanges();
				plotHop();
				//System.out.println(switchStateArray);
				long endTime2 =  System.currentTimeMillis();
				long totalTime2 = endTime2 - startTime2;
				System.out.println("Current simulation runtime is: "+ totalTime2 +" milliseconds");
			}
	});
		
		t1.start();
		t2.start();
	
		try {
			t1.join();
			t2.join();
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
								
				
	}	//main end
	
	 public static synchronized void initialArray(){
	
		 Random rand = new Random();
		 for(int i = 0; i <= nReceptors - 1; i++){
		
			//int randReceptor = rand.nextInt(2); //0 inactive, 1 active
			int randRow = rand.nextInt(height);//random row within height constraint 
			int randCol = rand.nextInt(width);//random column within width constraint 
		
				//receptorArray[i][0] = randReceptor;
			boolean goActive = randState();
			if (goActive == true){
				receptorArray[i][0] = 1.0f; ;
			}
			else{
				receptorArray[i][0] = 0.0f;
			}
				receptorArray[i][1] = randRow;
				receptorArray[i][2] = randCol;						
		}
		 	System.out.println("Initial Array is :");
			System.out.println(Arrays.deepToString(receptorArray)); 
			
	}/////////intialArray end
 	 
	 public static synchronized void checkArray(){// makes sure initialArray receptors don't occupy same spot initially
		 
		 
		 int jRandReplaceRow = 0;
		 int jRandReplaceCol = 0;
	
	
		 for(int i = 0; i <= receptorArray.length - 1; i++){
			 for(int j = 0; j <= receptorArray.length - 1; j++){
				 if(receptorArray[i][1] == receptorArray[j][1] && receptorArray[i][2] == receptorArray[j][2] && i != j){ //satisfied if there is a match
					    
						//System.out.println(receptorArray[i][1]+ " from " + i +" row "+ " matches " + receptorArray[j][1] + " from " + j + " row");
						//System.out.println(receptorArray[i][2]+ " from " + i +" column " + " matches " + receptorArray[j][2] + " from " + j + " column");

						int checkCount = 0;
						
						//if match, activate this for loop
						for(int k = 0; k <= receptorArray.length - 1; k++){
							
							jRandReplaceRow = jRow();
							jRandReplaceCol = jCol();
							if(receptorArray[k][1] == jRandReplaceRow && receptorArray[k][2] == jRandReplaceCol){//satisfied if random numbers match array elements									
							//System.out.println("current array pair " + receptorArray[k][1] + " and " + receptorArray[k][2] + " matches random " + jRandReplaceRow + " and " + jRandReplaceCol);
							//System.out.println("redo random numbers");
						
							jRandReplaceRow = jRow();
							jRandReplaceCol = jCol();
							k--;
							}
							else{
								//System.out.println("current array pair " + receptorArray[k][1] + " and " + receptorArray[k][2] + " doesn't match random " + jRandReplaceRow + " and " + jRandReplaceCol);
								
								checkCount++;
								//System.out.println("CheckCount: " + checkCount);
									if(checkCount == receptorArray.length){
										receptorArray[j][1] = jRandReplaceRow;
										receptorArray[j][2] = jRandReplaceCol;
										 //System.out.println("new receptor array is " + Arrays.deepToString(receptorArray));
									}
							}
						
						
						}//k loop end
						
				 }
				 else{ 
					continue;
				 }
			 }
		 }
     
	
  }////////checkArray() end
	 
	 
	//slightly modified, as checkArray() would could switch position of 
	//receptor already present instead of newly placed receptor
	//takes randomly generated position of new receptor if receptor hops in addArray() and 
	//compares to make sure it's not generated on occupied spot
public static void checkHopArray(int row, int col){    
	
	 
	 for(int i = 0; i <= receptorArray.length - 1; i++){
		 for(int j = 0; j <= receptorArray.length - 1; j++){
			 if(receptorArray[i][1] == row && receptorArray[i][2] == col && i != j){ //satisfied if there is a match
				    
					//System.out.println(receptorArray[i][1]+ " from " + i +" row "+ " matches " + receptorArray[j][1] + " from " + j + " row");
					//System.out.println(receptorArray[i][2]+ " from " + i +" column " + " matches " + receptorArray[j][2] + " from " + j + " column");

					int checkCount = 0;
					
					//if match, activate this for loop
					for(int k = 0; k <= receptorArray.length - 1; k++){
						
						row = jRow();
						col = jCol();
						if(receptorArray[k][1] == row && receptorArray[k][2] == col){//satisfied if random numbers match array elements									
						//System.out.println("current array pair " + receptorArray[k][1] + " and " + receptorArray[k][2] + " matches random " + jRandReplaceRow + " and " + jRandReplaceCol);
						//System.out.println("redo random numbers");
					
						row = jRow();
						col = jCol();
						k--;
						}
						else{
							//System.out.println("current array pair " + receptorArray[k][1] + " and " + receptorArray[k][2] + " doesn't match random " + jRandReplaceRow + " and " + jRandReplaceCol);
							
							checkCount++;
							//System.out.println("CheckCount: " + checkCount);
								if(checkCount == receptorArray.length){
									receptorArray[j][1] = row;
									receptorArray[j][2] = col;
									 //System.out.println("new receptor array is " + Arrays.deepToString(receptorArray));
								}
						}
					
					
					}//k loop end
					
			 }
			 else{ 
				continue;
			 }
		 }
	 }						 
    	 
     }
	 public static int jRow(){
		 Random randCheck = new Random();
		 int jRandRow = randCheck.nextInt(height);
		 
		 return jRandRow;
	 }///////jRow() end
	 
	 public static int jCol(){
		 Random randCheck = new Random();
		 int jRandCol = randCheck.nextInt(width);
		 
		 return jRandCol;
	 }///////jCol() end
	 
	 public static boolean randState(){
		 
		 Random rand = new Random();
		 int randState = rand.nextInt(100);
		 
		 if (randState < generateActive){
		 return true; 
		 }
		 return false;
	 }
	
	 public static boolean switchActive(){
		 
		 //increase concentration as variable with for loop later
	
		 float pOn = kOn*concentration*timeStep*100;  //%
			 
		 //System.out.println("pOn: "+pOn+"%");
		 
		 String strFloat = String.valueOf(pOn);
		 //System.out.println("strFloat: "+strFloat);
		 
		 int count = strFloat.indexOf('.');
		 //System.out.println("index of decimal: "+count);
		 
		 int decimalPlaces = strFloat.length() - count - 1;
		 //System.out.println("number of decimal places: "+decimalPlaces);
		 
		 float multiplier =  (float) Math.pow(10,decimalPlaces);
		 
		//System.out.println("multiplier: "+multiplier);
		//System.out.println("switchActive(): "+pOn*multiplier+" / "+100*multiplier);
		 //float aFraction = (pOn*multiplier)/(100*multiplier);
		//System.out.println("fraction: "+aFraction);
		 
		 
		Random randActive = new Random();
		
		int switchOn = randActive.nextInt((int) (100*multiplier));	 
		//System.out.println("switchActive() random number is: "+switchOn);
		 
		 if(switchOn < pOn*multiplier){
			 //System.out.println("switch on");
		 return true;
		 }
		
			//System.out.println("dont switch on");
		return false;
		 
	 }///////switchActive() end
	 
	 public static boolean switchInactive(){
		 
		 float pOff = kOff*timeStep*100;  //%
		 //System.out.println("pOff: "+pOff+"%");
		 
		 String strFloat = String.valueOf(pOff);
		 //System.out.println("strFloat: "+strFloat);
		 
		 int count = strFloat.indexOf('.');
		 //System.out.println("index of decimal: "+count);
		 
		 int decimalPlaces = strFloat.length() - count - 1;
		 //System.out.println("number of decimal places: "+decimalPlaces);
		 
		 float multiplier =  (float) Math.pow(10,decimalPlaces);
		 
		 //System.out.println("multiplier: "+multiplier);
		 //System.out.println("switchInactive(): "+pOff*multiplier+" / "+100*multiplier);
		 //float aFraction = (pOff*multiplier)/(100*multiplier);
		 //System.out.println("fraction: "+aFraction);
		 
		Random randInactive = new Random();
		
		float switchOff = randInactive.nextInt((int) (100*multiplier));	 
		//System.out.println("switchInactive() random number is: "+switchOff);
		 
		 if(switchOff < pOff*multiplier){
			 //System.out.println("switch off");
		 return true;
		 }
		 //System.out.println("don't switch off");
		return false;
		 
	 }///////switchInactive end
	 
	 
	 public static synchronized void movementArray(int mm){
		 Random rand = new Random();
		 //value of m passed into this method as mm (mm variable takes value of m)
		 
		 int moveCount = mm +a;
		 
		 //System.out.println("before move a: "+a);
		//System.out.println("moveCount: "+moveCount);
				
			for(int i = 0; i <= moveArray.length - 1; i++){			
					//System.out.println("before move a: "+a);
					//System.out.println("moveCount: "+moveCount);
					//System.out.println("mm: "+mm);
					//System.out.println("moveArray i: "+i);
				//System.out.println("moveArray beginning of loop: "+Arrays.deepToString(moveArray));
				 int randMove = rand.nextInt(2);
				 int randDir = rand.nextInt(2);
				// System.out.println("randMove is: " + randMove);
				 
				 //receptorArray state carried over to moveArray state
				 moveArray[i][0] = receptorArray[i][0];
				 
				 
				 //float randState = rand.nextInt((int) (2));
				 //moveArray[i][0] = randState;
				 
				//System.out.println("Initial moveArray state: "+moveArray[i][0]);
				 
				 if(randMove == 0){//row change
					if(randDir == 0){//negative value	
					 moveArray[i][1] = -stepSize;  
					 	
					 	if(moveArray[i][0] == 0.0f){  //if receptor is inactive
					 		//System.out.println("AAAinitial moveArray state  0: "+moveArray[i][0]);
					 		
					 		boolean on = switchActive();	
							if(on == true){
								//System.out.println("AAAswitch to ACTIVE");	
								moveArray[i][0] = 1.0f;  //switch to active
								switchStateArray.add(moveArray[i][0]);
								switchTimeArray.add((float) moveCount*timeStep);
								//System.out.println("moveCount: "+moveCount+"  timeStep: "+ timeStep);
								//System.out.println("moveCount*timeStep: "+moveCount*timeStep);
								///System.out.println("AAAnew moveArray state  1: "+moveArray[i][0]);
								 a++;
							}
							else if(on == false){
								//System.out.println("AAAstay INACTIVE");
								moveArray[i][0] = 0.0f;
								//System.out.println("AAAnew moveArray state  0: "+moveArray[i][0]);
								 a++;
							} 
					 	}
					 	else if(moveArray[i][0] == 1.0f){   //if receptor is active
					 		//System.out.println("BBBinitial moveArray state  1: "+moveArray[i][0]);
							boolean off = switchInactive();
							if(off == true){
								//System.out.println("BBBswitch to INACTIVE");
								moveArray[i][0] = 0.0f;
								switchStateArray.add(moveArray[i][0]);
								switchTimeArray.add((float) moveCount*timeStep);
								//System.out.println("moveCount: "+moveCount+"  timeStep: "+ timeStep);
								//System.out.println("moveCount*timeStep: "+moveCount*timeStep);
								//System.out.println("BBBnew moveArray state  0: "+moveArray[i][0]);
								 a++;
								 }
							else if(off == false){
								//System.out.println("BBBstay ACTIVE");
								moveArray[i][0] = 1.0f;
								//System.out.println("BBBnew moveArray state  1: "+moveArray[i][0]);
								 a++;
							}
					 	}			
					}
				 
					if(randDir == 1){//positive value
					  moveArray[i][1] = stepSize;
					  
					  if(moveArray[i][0]== 0.0f){  //if receptor is inactive
						  //System.out.println("CCCinitial moveArray state  0: "+moveArray[i][0]);
						  boolean on = switchActive();
							if(on == true){
								//System.out.println("CCCswitch to ACTIVE");
								moveArray[i][0] = 1.0f;  //switch to active
								switchStateArray.add(moveArray[i][0]);
								switchTimeArray.add((float) moveCount*timeStep);
								//System.out.println("moveCount: "+moveCount+"  timeStep: "+ timeStep);
								//System.out.println("moveCount*timeStep: "+moveCount*timeStep);
								//System.out.println("CCCnew moveArray state  1: "+moveArray[i][0]);
								 a++;
							}
							else if(on == false){
								//System.out.println("CCCstay INACTIVE");
								moveArray[i][0] = 0.0f;
								//System.out.println("CCCnew moveArray state  0: "+moveArray[i][0]);
								 a++;
							}
						}
						 
					  else if(moveArray[i][0] == 1.0f){   //if receptor is active
						  //System.out.println("DDDinitial moveArray state  1: "+moveArray[i][0]);
							 boolean off = switchInactive(); //0 to 99
							 if(off == true){
								 //System.out.println("DDDswitch to INACTIVE");
								 moveArray[i][0] = 0.0f;
								 switchStateArray.add(moveArray[i][0]);
								 switchTimeArray.add((float) moveCount*timeStep);
								 //System.out.println("moveCount: "+moveCount+"  timeStep: "+ timeStep);
								 //System.out.println("moveCount*timeStep: "+moveCount*timeStep);
								 //System.out.println("DDDnew moveArray state  0: "+moveArray[i][0]);
								  a++;
							 }
							 else if(off == false){
								 //System.out.println("DDDstay ACTIVE");
								 moveArray[i][0] = 1.0f;
								 //System.out.println("DDDnew moveArray state  1: "+moveArray[i][0]);
								 a++;
							 }
						 }
					}
				 }
				
				 if(randMove == 1){//column change
					 if(randDir == 0){//negative value	
					 moveArray[i][2] = -stepSize;
					 
					 	if(moveArray[i][0]== 0.0f){  //if receptor is inactive
					 		 //System.out.println("EEEinitial moveArray state  0: "+moveArray[i][0]);
							boolean on = switchActive();
							if(on == true){
								//System.out.println("EEEswitch to ACTIVE");
								moveArray[i][0] = 1.0f;  //switch to active
								switchStateArray.add(moveArray[i][0]);
								switchTimeArray.add((float) moveCount*timeStep);
								//System.out.println("moveCount: "+moveCount+"  timeStep: "+ timeStep);
								//System.out.println("moveCount*timeStep: "+moveCount*timeStep);
								//System.out.println("EEEnew moveArray state  1: "+moveArray[i][0]);
								a++;
							}
							else if(on == false){
								//System.out.println("EEEstay INACTIVE");
								moveArray[i][0] = 0.0f;
								//System.out.println("EEEnew moveArray state  0: "+moveArray[i][0]);
								a++;
							}
						}
						 
					 	else if(moveArray[i][0] == 1.0f){   //if receptor is active
					 		//System.out.println("FFFinitial moveArray state  1: "+moveArray[i][0]);
							 boolean off = switchInactive(); 
							 if(off == true){
								 //System.out.println("FFFswitch to INACTIVE");
								 moveArray[i][0] = 0.0f;
								 switchStateArray.add(moveArray[i][0]);
								 switchTimeArray.add((float) moveCount*timeStep);
								 //System.out.println("moveCount: "+moveCount+"  timeStep: "+ timeStep);
								 //System.out.println("moveCount*timeStep: "+moveCount*timeStep);
								 //System.out.println("FFFnew moveArray state  0: "+moveArray[i][0]);
								 a++;
							 }
							 else if(off == false){
								 //System.out.println("FFFstay ACTIVE");
								 moveArray[i][0] = 1.0f;
								 //System.out.println("FFFnew moveArray state  1: "+moveArray[i][0]); 
								a++;
							 }
						 }
					 }
					 if(randDir == 1){//positive value
					 moveArray[i][2] = stepSize;
					 
					 	if(moveArray[i][0] == 0.0f){  //if receptor is inactive
					 		//System.out.println("GGGinitial moveArray state  0: "+moveArray[i][0]);
							boolean on = switchActive();
							if(on == true){
								//System.out.println("GGGswitch to ACTIVE");
								moveArray[i][0] = 1.0f;  //switch to active
								switchStateArray.add(moveArray[i][0]);
								switchTimeArray.add((float) moveCount*timeStep);
								//System.out.println("moveCount: "+moveCount+"  timeStep: "+ timeStep);
								//System.out.println("moveCount*timeStep: "+moveCount*timeStep);
								//System.out.println("GGGnew moveArray state  1: "+moveArray[i][0]);
								a++;
							}
							else if(on == false){
								//System.out.println("GGGstay INACTIVE");
								moveArray[i][0] = 0.0f;
								//System.out.println("GGGnew moveArray state  0: "+moveArray[i][0]);
								a++;
							}
						}
						 
					 	else if(moveArray[i][0] == 1.0f){   //if receptor is active
					 		//System.out.println("HHHinitial moveArray state  1: "+moveArray[i][0]);
							 boolean off = switchInactive(); 
							 if(off == true){
								 //System.out.println("HHHswitch to INACTIVE");
								 moveArray[i][0] = 0.0f;
								 switchStateArray.add(moveArray[i][0]);
								 switchTimeArray.add((float) moveCount*timeStep);
								 //System.out.println("moveCount: "+moveCount+"  timeStep: "+ timeStep);
								 //System.out.println("moveCount*timeStep: "+moveCount*timeStep);
								 //System.out.println("HHHnew moveArray state  0: "+moveArray[i][0]);
								 a++;
							 }
							 else if(off == false){
								 //System.out.println("HHHstay ACTIVE");
								 moveArray[i][0] = 1.0f;
								 //System.out.println("HHHnew moveArray state  1: "+moveArray[i][0]);
								 a++;
							 }
						 }
					 }
				 }
				//System.out.println("moveArray after move loop: "+Arrays.deepToString(moveArray));
				 //System.out.println("New moveArray state: "+moveArray[i][0]);
				 
		}///////////////end of moveArray for loop
			
			//System.out.println("after move a: "+a);
			
			
//control number of steps added to moveArray until moveArray is added to receptorArray	
//each k iteration generates 1 stepArray and adds it to moveArray 
		for(int k = 0; k <= steps - 1; k++){  
		  //System.out.println("k steps: "+k);
			for(int i = 0; i <= stepArray.length - 1; i++){
				
				moveCount++;//// seems to have fixed issue...... work for multiple k steps??
				//multiply plot data by steps to scale time appropriately, b/c when stepArrays are 
				//added to moveArray, the time doesn't scale with the iterations
				
				//System.out.println("inside step a: "+a);
			
				//state of moveArray is carried over to stepArray 
				//without this, state would default to 0.0 and overwrite moveArray state
				//when moveArray[i][0] is set equal to stepArray[i][0] at bottom of method
				stepArray[i][0] = moveArray[i][0];
				
				//System.out.println("CHECK MATCH  initial stepArray[i][0]: "
				//+stepArray[i][0]+ " moveArray[i][0]: "+moveArray[i][0]);
				 int randMove = rand.nextInt(2);
				 int randDir = rand.nextInt(2);
				 //System.out.println("randMove is: " + randMove);
				 
				 //float randState = rand.nextInt((int) (2));
				 //stepArray[i][0] = randState;
						 
				 if(randMove == 0){//row change
					if(randDir == 0){//negative value		
					  stepArray[i][1] = -stepSize;  
					  
					  if(stepArray[i][0]== 0.0f){  //if receptor is inactive
						  //System.out.println("IIIinitial stepArray state 0: "+stepArray[i][0]);
							boolean on = switchActive();
							if(on == true){
							
								//System.out.println("IIIswitch to ACTIVE");
								stepArray[i][0] = 1.0f;  //switch to active
								//System.out.println("inside boolean step  a: "+a);
								switchStateArray.add(stepArray[i][0]);
								switchTimeArray.add((float)  (moveCount)*timeStep);
								//System.out.println("stepCount: "+(moveCount)+"  timeStep: "+ timeStep);
								//System.out.println("stepCount*timeStep: "+(moveCount)*timeStep);
								//System.out.println("IIInew stepArray state 1: "+stepArray[i][0]);
							}
							else if(on == false){
							
								//System.out.println("IIIstay INACTIVE");
								stepArray[i][0] = 0.0f;
								 //System.out.println("IIInew stepArray state 0: "+stepArray[i][0]);
							}
						}
						 
					  else if(stepArray[i][0] == 1.0f){   //if receptor is active
						  //System.out.println("JJJinitial stepArray state 1: "+stepArray[i][0]);
							 boolean off = switchInactive(); 
							 if(off == true){
								 
								 //System.out.println("JJJswitch to INACTIVE");
								 stepArray[i][0] = 0.0f;
								 //System.out.println("inside boolean step  a: "+a);
								 switchStateArray.add(stepArray[i][0]);
								 switchTimeArray.add((float)  (moveCount)*timeStep);
								 //System.out.println("stepCount: "+(moveCount)+"  timeStep: "+ timeStep);
								 //System.out.println("stepCount*timeStep: "+(moveCount)*timeStep);
								 //System.out.println("JJJnew stepArray state 0: "+stepArray[i][0]);
							 }
							 else if(off == false){
								
								 //System.out.println("JJJstay ACTIVE");
								 stepArray[i][0] = 1.0f;
								 //System.out.println("JJJnew stepArray state 1: "+stepArray[i][0]);
							 }
						 }
					}
					if(randDir == 1){//positive value
					  stepArray[i][1] = stepSize;
					  
					  if(stepArray[i][0]== 0.0f){  //if receptor is inactive
						  //System.out.println("KKKinitial stepArray state 0: "+stepArray[i][0]);
							boolean on = switchActive();
							if(on == true){
								
								//System.out.println("KKKswitch to ACTIVE");
								stepArray[i][0] = 1.0f;  //switch to active
								//System.out.println("inside boolean step  a: "+a);
								switchStateArray.add(stepArray[i][0]);
								switchTimeArray.add((float)  (moveCount)*timeStep);
								//System.out.println("stepCount: "+(moveCount)+"  timeStep: "+ timeStep);
								//System.out.println("stepCount*timeStep: "+(moveCount)*timeStep);
								//System.out.println("KKKnew stepArray state 1: "+stepArray[i][0]);
							}
							else if(on == false){
								
								//System.out.println("KKK stay INACTIVE");
								stepArray[i][0] = 0.0f;
								//System.out.println("KKKnew stepArray state 0: "+stepArray[i][0]);
							}
						}
						 
					  else if(stepArray[i][0] == 1.0f){   //if receptor is active
						  //System.out.println("LLLinitial stepArray state 1: "+stepArray[i][0]);
							 boolean off = switchInactive(); 
							 if(off == true){
								 
								 //System.out.println("LLLswitch to INACTIVE");
								 stepArray[i][0] = 0.0f;
								 //System.out.println("inside boolean step  a: "+a);
								 switchStateArray.add(stepArray[i][0]);
								 switchTimeArray.add((float)  (moveCount)*timeStep);
								 //System.out.println("stepCount: "+(moveCount)+"  timeStep: "+ timeStep);
								 //System.out.println("stepCount*timeStep: "+(moveCount)*timeStep);
								 //System.out.println("LLLnew stepArray state 0: "+stepArray[i][0]);
							 }
							 else if(off == false){
								
								 //System.out.println("LLLstay ACTIVE");
								 stepArray[i][0] = 1.0f;
								 //System.out.println("LLLnew stepArray state 1: "+stepArray[i][0]);
								 
							 }
						 }
					}
				 }
				 if(randMove == 1){//column change
					 if(randDir == 0){//negative value			
					 stepArray[i][2] = -stepSize;
					 
					 	if(stepArray[i][0]== 0.0f){  //if receptor is inactive
					 		//System.out.println("MMMinitial stepArray state 0: "+stepArray[i][0]);
							boolean on = switchActive();
							if(on == true){
								
								//System.out.println("MMMswitch to ACTIVE");
								stepArray[i][0] = 1.0f;  //switch to active
								//System.out.println("inside boolean step  a: "+a);
								switchStateArray.add(stepArray[i][0]);
								switchTimeArray.add((float) (moveCount)*timeStep);
								//System.out.println("stepCount: "+(moveCount)+"  timeStep: "+ timeStep);
								//System.out.println("stepCount*timeStep: "+(moveCount)*timeStep);
								//System.out.println("MMMnew stepArray state 1: "+stepArray[i][0]);
							}
							else if (on == false){
								
								//System.out.println("MMMstay INACTIVE");
								stepArray[i][0] = 0.0f;
								//System.out.println("MMMnew stepArray state 0: "+stepArray[i][0]);
							}
						}
						 
					 	else if(stepArray[i][0] == 1.0f){   //if receptor is active
					 		  //System.out.println("NNNinitial stepArray state 1: "+stepArray[i][0]);
							boolean off = switchInactive(); 
							 if(off == true){
								
								 //System.out.println("NNNswitch to INACTIVE");
								 stepArray[i][0] = 0.0f;
								 //System.out.println("inside boolean step  a: "+a);
								 switchStateArray.add(stepArray[i][0]);
								 switchTimeArray.add((float)  (moveCount)*timeStep);
								 //System.out.println("stepCount: "+(moveCount)+"  timeStep: "+ timeStep);
								 //System.out.println("stepCount*timeStep: "+(moveCount)*timeStep);
								 //System.out.println("NNNnew stepArray state 0: "+stepArray[i][0]);
							 }
							 else if(off == false){
								 
								 //System.out.println("NNNstay ACTIVE");
								 stepArray[i][0] = 1.0f;
								 //System.out.println("NNNnew stepArray state 1: "+stepArray[i][0]);
							 }
						 }
					 }
					 if(randDir == 1){//positive value
					 stepArray[i][2] = stepSize;
					 
					 	if(stepArray[i][0]== 0.0f){  //if receptor is inactive
					 		//System.out.println("OOOinitial stepArray state 0: "+stepArray[i][0]);
							boolean on = switchActive();
							if(on == true){
								
								//System.out.println("OOOswitch to ACTIVE");
								stepArray[i][0] = 1.0f;  //switch to active
								//System.out.println("inside boolean step  a: "+a);
								switchStateArray.add(stepArray[i][0]);
								switchTimeArray.add((float)  (moveCount)*timeStep);
								//System.out.println("stepCount: "+(moveCount)+"  timeStep: "+ timeStep);
								//System.out.println("stepCount*timeStep: "+(moveCount)*timeStep);
								//System.out.println("OOOnew stepArray state 1: "+stepArray[i][0]);
							}
							else if(on == false){
								
								//System.out.println("OOOstay INACTIVE");
								stepArray[i][0] = 0.0f;
								//System.out.println("OOOnew stepArray state 0: "+stepArray[i][0]);
							}
						}
						 
					 	else if(stepArray[i][0] == 1.0f){   //if receptor is active
					 		 //System.out.println("PPPinitial stepArray state 1: "+stepArray[i][0]);
							boolean off = switchInactive(); //0 to 99
							 if(off == true){
								
								 //System.out.println("PPPswitch to INACTIVE");
								 stepArray[i][0] = 0.0f;
								 //System.out.println("inside boolean step  a: "+a);
								 switchStateArray.add(stepArray[i][0]);
								 switchTimeArray.add((float)  (moveCount)*timeStep);
								 //System.out.println("stepCount: "+(moveCount)+"  timeStep: "+ timeStep);
								 //System.out.println("stepCount*timeStep: "+(moveCount)*timeStep);
								 //System.out.println("PPPnew stepArray state 0: "+stepArray[i][0]);
							 }
							 else if(off == false){
								
								 //System.out.println("PPPstay ACTIVE");
								 stepArray[i][0] = 1.0f;
								 //System.out.println("PPPnew stepArray state 1: "+stepArray[i][0]);
							 }
						 }
					 }
				 }
			//System.out.println("stepArray inside i loop: "+Arrays.deepToString(stepArray));
		}////////////////////end of i step for loop
			
		//System.out.println("Movement array BEFORE step addition is: ");
		//System.out.println(Arrays.deepToString(moveArray));
		//System.out.println("Step array is: ");
		//System.out.println(Arrays.deepToString(stepArray));
		
		for(int i = 0; i <= moveArray.length - 1; i++){
			//moveArray is first step
			//stepArray is subsequent step, so final state is given by stepArray
			//set equal to moveArray since moveArray is what's added to receptorArray
			
			moveArray[i][0] = stepArray[i][0];
			moveArray[i][1] = moveArray[i][1] + stepArray[i][1];
			moveArray[i][2] = moveArray[i][2] + stepArray[i][2];
			//System.out.println("stepArray state: "+ stepArray[i][0]+"  moveArray state: "+moveArray[i][0]);
			//System.out.println("moveArray state: "+ moveArray[i][0]);
			
		}
		
		for(int i = 0; i < stepArray.length; i++){
			Arrays.fill(stepArray[i], '\0');
		}
		//System.out.println("Movement array AFTER step addition is: ");
		//System.out.println(Arrays.deepToString(moveArray));
		//System.out.println("cleared step array is: " + Arrays.deepToString(stepArray));
		
	 }//////////////////////////////end of k step for loop
 }/////////movementArray() end
	 
	 public static boolean hopProbability(){
		 
		 Random rand = new Random();
		 
		 int randHop = rand.nextInt(100); // 0 to 99
		 //System.out.println(randHop);
		 
		 if(randHop < hopProb){
			 //System.out.println("Hop");
			 return true;
			 
		 }
		 	 //System.out.println("Don't hop");
		 return false;
		 
	 }

	 public static synchronized void addArrays(int mm){
		
		 int RandReplaceRow = 0;
		 int RandReplaceCol = 0;
		 //System.out.println("addArrays mm: "+mm);
		 
		 
		 for (int i = 0; i <= moveArray.length - 1; i++){	 
			 
			  //when receptor hops, replace with new receptor in random location
			
			 receptorArray[i][0] = moveArray[i][0];
			 //System.out.println(moveArray[i][0]);
			 //System.out.println("receptorArray before addition: "+Arrays.deepToString(receptorArray));
			 receptorArray[i][1] = receptorArray[i][1] + moveArray[i][1];
			 receptorArray[i][2] = receptorArray[i][2] + moveArray[i][2];
			 //System.out.println("receptorArray after addition: "+Arrays.deepToString(receptorArray));
			 //Boundary checking
			// System.out.println("receptorArray before bound check: "+Arrays.deepToString(receptorArray));
			 if(receptorArray[i][1] < 0){
				 //System.out.println("111before hop x: "+receptorArray[i][1]);
				//System.out.println("111before hop y: "+receptorArray[i][2]);
				 boolean hop = hopProbability();
				 if(hop == true){ //hop 
				 
					 hopXLocationArray.add(0.0f);
					 hopYLocationArray.add(receptorArray[i][2]);
					 hops++;
					 hopArray.add(hops);
					 hopTimeArray.add((float) mm*timeStep);			 
					 RandReplaceRow = jRow();
					 RandReplaceCol = jCol();
					 boolean goActive = randState();
					 if(goActive == true){
					receptorArray[i][0] = 1.0f;
					 }
					 else{
						 receptorArray[i][0] = 0.0f;
					 }
					receptorArray[i][1] = RandReplaceRow;
					receptorArray[i][2] = RandReplaceCol;			
					//System.out.println("111after hop x: "+receptorArray[i][1]);
					//System.out.println("111after hop y: "+receptorArray[i][2]);
					checkHopArray(RandReplaceRow,RandReplaceCol);
					//System.out.println("111after checkHopArray x: "+receptorArray[i][1]);
					//System.out.println("111after checkHopAray y: "+receptorArray[i][2]);
				 }
				 else{			  //don't hop
				 receptorArray[i][1] = 0;
				 }
			 }
			 else if(receptorArray[i][1] > height){	
				 //System.out.println("222before hop x: "+receptorArray[i][1]);
				 //System.out.println("222before hop y: "+receptorArray[i][2]);
				 boolean hop = hopProbability();
				 if(hop == true){  //hop
					
					 hopXLocationArray.add((float) height);
					 hopYLocationArray.add(receptorArray[i][2]);
					 hops++;
					 hopArray.add(hops);
					 hopTimeArray.add((float) mm*timeStep);
					 RandReplaceRow = jRow();
					 RandReplaceCol = jCol();
					 boolean goActive = randState();
					 if(goActive == true){
					 receptorArray[i][0] = 1.0f;
					 }
					 else{
						 receptorArray[i][0] = 0.0f;
					 }
					receptorArray[i][1] = RandReplaceRow;
					receptorArray[i][2] = RandReplaceCol;		
					//System.out.println("222after hop x: "+receptorArray[i][1]);
					//System.out.println("222after hop y: "+receptorArray[i][2]);
					checkHopArray(RandReplaceRow,RandReplaceCol);
					//System.out.println("222after checkHopArray x: "+receptorArray[i][1]);
					//System.out.println("222after checkHopArray y: "+receptorArray[i][2]);
				 }
				 else{			   //don't hop
				 receptorArray[i][1] = height;
				 }
			 }
			 	 
			 /////////////////receptorArray[i][2] = receptorArray[i][2] + moveArray[i][2];
			 //Boundary checking		
			 if(receptorArray[i][2] < 0){
				//System.out.println("333before hop x: "+receptorArray[i][1]);
				//System.out.println("333before hop y: "+receptorArray[i][2]);
				 boolean hop = hopProbability();
				 if(hop == true){  //hop
					
					 hopXLocationArray.add(receptorArray[i][1]);
					 hopYLocationArray.add(0.0f);
					 hops++;
					 hopArray.add(hops);
					 hopTimeArray.add((float) mm*timeStep);
					 RandReplaceRow = jRow();
					 RandReplaceCol = jCol();
					 boolean goActive = randState();
					 if(goActive == true){
					 receptorArray[i][0] = 1.0f;
					 }
					 else{
						 receptorArray[i][1] = 0.0f;
					 }
					receptorArray[i][1] = RandReplaceRow;
					receptorArray[i][2] = RandReplaceCol;		
					//System.out.println("333after hop x: "+receptorArray[i][1]);
					//System.out.println("333after hop y: "+receptorArray[i][2]);
					checkHopArray(RandReplaceRow,RandReplaceCol);
					//System.out.println("333after checkHopArray x: "+receptorArray[i][1]);
					//System.out.println("333after checkHopArray y: "+receptorArray[i][2]);
				 }
				 else{			   //don't hop
				 receptorArray[i][2] = 0;
				 }
			 }
			 else if(receptorArray[i][2] > width){
				 //System.out.println("444before hop x: "+receptorArray[i][1]);
				 //System.out.println("444before hop y: "+receptorArray[i][2]);
				 boolean hop = hopProbability();
				 if(hop == true){  //hop
					 hopXLocationArray.add(receptorArray[i][1]);
					 hopYLocationArray.add((float) width);
					 hops++;
					 hopArray.add(hops);
					 hopTimeArray.add((float) mm*timeStep);
					 RandReplaceRow = jRow();
					 RandReplaceCol = jCol();
					 boolean goActive = randState();
					 if(goActive == true){
						 receptorArray[i][0] = 1.0f;				 
					 }
					 else{
						 receptorArray[i][0] = 0.0f;
					 }
					 receptorArray[i][0] = 0.0f;
					 receptorArray[i][1] = RandReplaceRow;
					 receptorArray[i][2] = RandReplaceCol;	
					 //System.out.println("444after hop x: "+receptorArray[i][1]);
					 //System.out.println("444after hop y: "+receptorArray[i][2]);
					 checkHopArray(RandReplaceRow,RandReplaceCol);
					 //System.out.println("444after checkHopArray x: "+receptorArray[i][1]);
					 //System.out.println("444after checkHopArray y: "+receptorArray[i][2]);
				 }
				 else{			   //don't hop
				 receptorArray[i][2] = width;
				 }
			 }
			 //xElements = receptorArray[i][1];//////////////////////////zz
			 //yElements = receptorArray[i][2];//////////////////////////zz
			//System.out.println("moveArray size: "+(moveArray.length-1));
			//System.out.println("xElement: "+xElements);
					 
			 //xArray.add(xElements);///////////////////////////////////zz
			 //yArray.add(yElements);///////////////////////////////////zz
			//System.out.println("hopTimeArray: "+hopTimeArray);
			 // System.out.println("xArray is: ");
			// System.out.println(xArray);
			 
			// System.out.println("moveArray BEFORE deletion: "+Arrays.deepToString(moveArray));
			
			 //System.out.println("moveArray AFTER deletion: "+Arrays.deepToString(moveArray));
		 }////////////////////end of for loop
		 
		 //clear moveArray after ALL moveArrays have been added, so put after for loop
		 for(int j = 0; j < moveArray.length; j++){
				Arrays.fill(moveArray[j], '\0');
			}
		//System.out.println("hopXLocationArray"+hopXLocationArray);
		//System.out.println("hopYLocationArray"+hopYLocationArray);
		// System.out.println("AAAAAAAAAAAAAAAAAAAAAAAA  AFTER BOUND CHECK new receptor array is: ");
		//System.out.println("Final receptorArray: "+Arrays.deepToString(receptorArray));
		// System.out.println("xArray is: ");
		// System.out.println(xArray);
		// System.out.println("yArray is: ");
		// System.out.println(yArray);
		 
	
	 }////////addArrays() end
	 
	 public static synchronized boolean eventCheck(){
		 
		 Random rand = new Random();
		 
		 for(int i = 0; i <= receptorArray.length - 1; i++){
			 for(int j = 0; j <= receptorArray.length - 1; j++){
				 
				 if(receptorArray[i][0] == 1.0f && receptorArray[j][0]== 1.0f && receptorArray[i][1] == receptorArray[j][1] && receptorArray[i][2] == receptorArray[j][2] && i != j){
					 //if there is event, long straight line on plot,
					 //indicates path between endo receptors and newly generated receptors
					
					 
					// System.out.println("receptor 1 Active: "+receptorArray[i][0]);
					// System.out.println("receptor 2 Active: "+receptorArray[j][0]);
					 
					 
					 events++;
					 //System.out.println("Total number of events: "+events);
					 
					 eventXLocationArray.add(receptorArray[j][1]);
					 eventYLocationArray.add(receptorArray[j][2]);
					 
					 //Replace matching positions two new receptors
					    int iRandReceptor = rand.nextInt(2); 
						int iRandRow = rand.nextInt(height);
						int iRandCol = rand.nextInt(width);
					
						receptorArray[i][0] = iRandReceptor;
						receptorArray[i][1] = iRandRow;
						receptorArray[i][2] = iRandCol;	
						
						int jRandReceptor = rand.nextInt(2);
						int jRandRow = rand.nextInt(height);
						int jRandCol = rand.nextInt(width);
						
						receptorArray[j][0] = jRandReceptor;
						receptorArray[j][1] = jRandRow;
						receptorArray[j][2] = jRandCol;
										 
						return true;
				 }
				 
			 }		 
		 }
		 //System.out.println("EVENT ARRAY IS: "+eventArray);
		 //System.out.println("Number of Events: "+events);
		return false;
		
	 }///////eventCheck() end
	 
	 public static void plotTrajectory(){
		 
		// ArrayList<ArrayList<Float>> yFinalArray = new ArrayList<ArrayList<Float>>(); //zz
		// ArrayList<ArrayList<Float>> xFinalArray = new ArrayList<ArrayList<Float>>();//zz
		// float xP = 0f;//zz
		// float yP = 0f;//zz
		// float[][] xPlot = new float[nReceptors][move];//zz
		// float[][] yPlot = new float[nReceptors][move];//zz
		 
		 float xL = 0.0f;
		 float yL = 0.0f;
		 float[] xLocation = new float[hopXLocationArray.size()];
		 float[] yLocation = new float[hopYLocationArray.size()];
		 
		 float xE = 0.0f;
		 float yE = 0.0f;
		 float[] xEvent = new float[eventXLocationArray.size()];
		 float[] yEvent = new float[eventYLocationArray.size()];
		// System.out.println("eventXLocationArray"+eventXLocationArray);
		// System.out.println("eventyLocationArray"+eventYLocationArray);
		 
		 
//		 System.out.println("nReceptors: "+nReceptors);
//		 System.out.println("move: "+move);
//		 System.out.println("xArray is: "+xArray);
//		 System.out.println("xArray.size(): "+xArray.size());
//		 System.out.println("yArray is: "+yArray);
//		 System.out.println("yArray.size(): "+yArray.size());
		 
		 int a = 1;
		 
	
//		 if((xArray.size())%2 == 0){  //zz
//		 	for(int i = 0; i <= nReceptors - 1; i++){
//		 		
//		 		//create new array for each ith set of positions
//		 		ArrayList<Float> xFinalInnerArray = new ArrayList<Float>(); 
//		 		ArrayList<Float> yFinalInnerArray = new ArrayList<Float>(); 
//		 		for(int j = 0 + i; j <= xArray.size() - 1; j+= nReceptors){
//				 
//		 			//System.out.println("even");
//		 			//System.out.println("xArray.get(j): "+xArray.get(j)+" at j index "+ j + " i = "+i);
//		 			
//		 			xFinalInnerArray.add(xArray.get(j));
//		 			yFinalInnerArray.add(yArray.get(j));
//		 			
//		 		}
//		 	//System.out.println("xFinalInnerArray: " +xFinalInnerArray);
//		 	//System.out.println("yFinalInnerArray: "+yFinalInnerArray);
//		 	xFinalArray.add(xFinalInnerArray);
//		 	yFinalArray.add(yFinalInnerArray);
//		 	//System.out.println("xFinalArray: "+xFinalArray);
//		 	//System.out.println("yFinalArray: "+yFinalArray);
//		 	}
//		 }
//		 else{
//			 for(int i = 0; i <= nReceptors - 1; i++){
//				 
//				//create new array for each ith set of positions
//		 		 ArrayList<Float> xFinalInnerArray = new ArrayList<Float>(); 
//		 		 ArrayList<Float> yFinalInnerArray = new ArrayList<Float>(); 
//		 		 
//			 		for(int j = 0 + i; j <= (xArray.size() - (nReceptors - a )); j+= nReceptors){
//					 
//			 			//System.out.println("odd");
//			 			//System.out.println("xArray.get(j): "+xArray.get(j)+" at j index "+ j+ " i = "+i);
//			 			
//			 			xFinalInnerArray.add(xArray.get(j));
//			 			yFinalInnerArray.add(yArray.get(j));
//					
//				 }
//			 		//System.out.println("a: "+a);
//			 		a++;
//			 		//System.out.println("xFinalInnerArray:" +xFinalInnerArray);
//				 	xFinalArray.add(xFinalInnerArray);
//				 	yFinalArray.add(yFinalInnerArray);
//				 	//System.out.println("xFinalArray: "+xFinalArray);
//				 	//System.out.println("yFinalArray: "+yFinalArray);
//			 }
//		 } //zz
		//System.out.println("xFinalArray: "+xFinalArray);
		//System.out.println("yFinalArray: "+yFinalArray);
		
		 	
//	for(int i = 0; i<=xFinalArray.size() - 1; i++){	 	//zz
//	
//		 Float[] xO = xFinalArray.get(i).toArray(new Float[xFinalArray.size()]);
//		 Float[] yO = yFinalArray.get(i).toArray(new Float[yFinalArray.size()]);
//		 
//		 for(int j = 0; j<=xFinalArray.get(i).size() - 1; j++){
//			 
//			
//			// System.out.println("xO: "+xO[j]);
//			// System.out.println("yO: "+yO[j]);
//			 xP = xO[j].floatValue();
//			 yP = yO[j].floatValue();
//			 
//			 xPlot[i][j] = xP;
//			 yPlot[i][j] = yP;
//			 //System.out.println("xP: "+xP);
//			 //System.out.println("yP: "+yP);
//			 
//		 }
//	}		//zz

	for(int i = 0; i <= hopXLocationArray.size() - 1; i++){
		
		xL = hopXLocationArray.get(i).floatValue();
		yL = hopYLocationArray.get(i).floatValue();
		
		xLocation[i] = xL;
		yLocation[i] = yL;
		
	}
	
	for(int i = 0; i<= eventXLocationArray.size() - 1; i++){
		
		xE = eventXLocationArray.get(i).floatValue();
		yE = eventYLocationArray.get(i).floatValue();
		
		xEvent[i] = xE;
		yEvent[i] = yE;
		
	}
	
		//System.out.println(Arrays.deepToString(xPlot));  
		//System.out.println(Arrays.deepToString(yPlot));
	
		//System.out.println("R1 first point: "+xPlot[0][1]+" , "+yPlot[0][2]);
		
		 //Plot positionPlot = new Plot("positionPlot","X position (nm)","Y position (nm)",xPlot[0],yPlot[0]);//zz
		 Plot positionPlot = new Plot("positionPlot","X position (nm)","Y position (nm)");
		 //line width for dotted boundary line
		 
		 positionPlot.setLineWidth(2);
		 positionPlot.setColor(Color.BLUE);
		 positionPlot.drawLine(0.0, 0.0, width, 0.0);
		 positionPlot.drawLine(0.0, 0.0, 0.0, height);
		 positionPlot.drawLine(width, height,0.0,height );
		 positionPlot.drawLine(width, height, width, 0.0);
		 
		positionPlot.setColor(Color.BLACK);  //  color for first receptor data set
		 positionPlot.setLimits(-50, height + 50, -50, width + 50); //x1,x2,y1,y2
		//show() pertains to drawing first data set
		//so set parameters for first data set before testPlot.show()
		 //line width for first receptor data set 
		 positionPlot.setLineWidth(1);
		 
		
		 
	
		 positionPlot.show();  
		 
		 //additional data sets
		 //set methods apply to the first data set it encounters
		 //so put directly before method that draws respective data set
		 
//		 for(int i = 1; i<=nReceptors - 1; i++){	 //zz
//				//line width for subsequent receptors 
//				positionPlot.setLineWidth(1);
//				positionPlot.setColor(Color.BLACK);
//				positionPlot.addPoints(xPlot[i],yPlot[i],2); 
//				//System.out.println("plot receptor: "+i);		
//				
//			 }//zz
		 
		 //System.out.println(xPlot.length);
		 //System.out.println(nReceptors);
		 //skip first data set, already plotted (i=1)
		 
		 
		 positionPlot.setLineWidth(2);
		 positionPlot.setColor(Color.MAGENTA);
		 positionPlot.addPoints(xLocation,yLocation, 1);
		 
	
		 positionPlot.setLineWidth(2);
		 positionPlot.setColor(Color.RED);
		 positionPlot.addPoints(xEvent, yEvent, 0);
	 }/////////plotTrajectory() end
	 
	 
	public static void plotEvents(){
		
		float xP2 = 0f;
		float yP2 = 0f;
		float yP2Change = 0f;
		float[] xEvent = new float[timePointArray.size()]; 
		float[] yEvent = new float[collisionArray.size()];
		float[] yEventChange = new float[collisionPerTimeArray.size()];
		
		for(int i = 0; i<= collisionArray.size() - 1; i++){
			
		  xP2 =	timePointArray.get(i).floatValue();
		  yP2 = collisionArray.get(i).floatValue();
		  yP2Change = collisionPerTimeArray.get(i).floatValue();
		 
		  xEvent[i] = ((1+steps)*xP2);
		  yEvent[i] = yP2;
		  yEventChange[i] = yP2Change;
		  
		  
		}
		
		
		Plot eventPlot = new Plot("Total Events vs. Time","Time (s)","Total Number of Dimerizations");
		
		eventPlot.setLineWidth(2);
		eventPlot.setColor(Color.RED);
		eventPlot.addPoints(xEvent, yEvent, 0);
		
		eventPlot.show();
		
		

		Plot eventChangePlot = new Plot("Events vs. time","Time (s) ","Dimerizations");
		eventChangePlot.setLineWidth(2);
		eventChangePlot.setColor(Color.BLUE);
		eventChangePlot.addPoints(xEvent, yEventChange, 0);
		eventChangePlot.show();
	 }////////plotEvents() end
	 
	public static void plotStates(){
		
		//plotting for just one receptor
		//System.out.println(Arrays.toString(copyTotalTimeArray[0]));
		for(int i = 0; i <= copyTotalTimeArray[0].length - 1; i++){
			//+1 for the moveArray, which is first move in iteration
			copyTotalTimeArray[0][i] = ((1+steps)*copyTotalTimeArray[0][i]);  //only adds step to first timeArray for 
		}																	  //for one receptor for plot
		//System.out.println("new array:" +Arrays.toString(copyTotalTimeArray[0]));
		Plot statePlot = new Plot("State vs. Time", "Time (s)","State",copyTotalTimeArray[0],stateArray[0],2);
		statePlot.setLineWidth(2);
		statePlot.setColor(Color.RED);
		//statePlot.setLimits(0,totalTime, -1, 2);
		//statePlot.addPoints( copyTotalTimeArray[0] ,stateArray[0]  , 2);
		//System.out.println(Arrays.deepToString(stateArray));
		//System.out.println(Arrays.deepToString(copyTotalTimeArray));
	
		
		
		statePlot.show();
	}
	//////*****SET nRECEPTOR to 1 before plotting state changes, currently combines states into same array*****
	//*****Make 2D array if want to plot state changes while running with a lot of receptors*****
	public static void plotStateChanges(){
		
		float xT = 0.0f;
		float yS = 0.0f;
		
		//System.out.println(switchTimeArray.size());
		//System.out.println(switchStateArray.size());

		
		
		float[] xTime = new float[switchTimeArray.size()];
		float[] yState = new float[switchStateArray.size()];
		
		for(int i = 0; i <=switchTimeArray.size() - 1; i++){
			
			xT = switchTimeArray.get(i).floatValue();
			yS = switchStateArray.get(i).floatValue();
			
			xTime[i] = ((1+steps)*xT);
			yState[i] = yS;
			
			
		}
		
		Plot stateChangePlot = new Plot("State Change vs. Time", "Time (s)","State");
		stateChangePlot.setLineWidth(2);
		stateChangePlot.setColor(Color.RED);
		//stateChangePlot.setLimits(0,totalTime, -1,2);
		stateChangePlot.addPoints(xTime,yState , 2);
		
		stateChangePlot.show();
		
		
	}
	
	public static void plotHop(){
		
		//System.out.println("hopArray: "+hopArray);
		//System.out.println("hopTimeArray: "+hopTimeArray);
		
		
		float xH = 0.0f;
		float yH = 0.0f;
		
		
		float[] xHop = new float[hopTimeArray.size()];
		float[] yHop = new float[hopArray.size()];
		
	
		for(int i = 0; i <= hopTimeArray.size() - 1; i++){
			
			xH = hopTimeArray.get(i).floatValue();
			yH = hopArray.get(i).floatValue();
		
			
			xHop[i] = ((1 + steps)*xH);
			yHop[i] = yH;
			
		}
		
		Plot totalHopPlot = new Plot("Total Number of Hops vs. Time"  ,"Time (s)"," Number of Hops ");
		totalHopPlot.setLineWidth(2);
		totalHopPlot.setColor(Color.RED);
		totalHopPlot.addPoints(xHop, yHop, 0);
		
		
		totalHopPlot.show();
		
	}
	
}//Diffusion class end
