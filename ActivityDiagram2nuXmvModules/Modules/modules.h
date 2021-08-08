#ifndef MODULES
#define MODULES

MODULE MainActivator(controlFlowOut)
	VAR
		state : {active, inactive};
		
	ASSIGN
		init(state) := active;
		next(state) :=
			case
				state = active & controlFlowOut.backwardSignal : inactive;
				TRUE : state;
			esac;
			
		controlFlowOut.forwardSignal := state = active;


MODULE MainTerminator(controlFlowIn)
	VAR
		state : {waiting, terminated};
		
	ASSIGN
		init(state) := waiting;
		next(state) :=
			case
				state = waiting & controlFlowIn.forwardSignal : terminated;
				TRUE : state;
			esac;
			
		controlFlowIn.backwardSignal := state = terminated;



MODULE ControlFlow
	VAR
		forwardSignal : boolean;
		backwardSignal : boolean;



MODULE InitialPoint(createTokenSignal,controlFlowOut, TIMEOUT)
	VAR 
		state_tokengenerator : {S1, S2, S3};
		state : {active, inactive};
		
	ASSIGN
		init(state_tokengenerator) := S1;
		next(state_tokengenerator) :=
			case
				state_tokengenerator = S1 & TIMEOUT : S1;
				state_tokengenerator = S2 & TIMEOUT : S3;
				state_tokengenerator = S1 & createTokenSignal : S2;
				!createTokenSignal : S1;
				TRUE : state_tokengenerator;
			esac;		
	
		init(state) := inactive;
		next(state) :=
			case
				TIMEOUT : inactive;
				state_tokengenerator = S1 & createTokenSignal : active;
				state = active & controlFlowOut.backwardSignal : inactive;
				TRUE : state;
			esac;
			
		controlFlowOut.forwardSignal := (state = active) & !TIMEOUT;
			
	DEFINE
		ACTIVE := (state = active);


MODULE EndPoint(consumeTokenSignal, controlFlowIn, TIMEOUT)
	VAR 
		state_consumer : {S1, S2, S3};
		state : {active, inactive};
		
	ASSIGN
		init(state_consumer) := S1;
		next(state_consumer) :=
			case
				state_consumer = S1 & TIMEOUT : S1;
				state_consumer = S2 & TIMEOUT : S3;
				state_consumer = S1 & consumeTokenSignal : S2;
				!consumeTokenSignal : S1;
				TRUE : state_consumer;
			esac;
	
	
		init(state) := inactive;
		next(state) :=
			case
				TIMEOUT : inactive;
				state = inactive & controlFlowIn.forwardSignal : active;
				state_consumer = S1 & consumeTokenSignal : inactive;
				TRUE : state;
			esac;
			
		controlFlowIn.backwardSignal := state = inactive & controlFlowIn.forwardSignal & !TIMEOUT;
			
	DEFINE
		FINISHED := (state = active);
		ACTIVE := (state = active);


MODULE FlowEndPoint(controlFlowIn, TIMEOUT)
	VAR 
		state : {active, inactive};
		
	ASSIGN
		init(state) := inactive;
		next(state) :=
			case
				TIMEOUT : inactive;
				state = inactive & controlFlowIn.forwardSignal : active;
				TRUE : inactive;
			esac;
			
		controlFlowIn.backwardSignal := state = inactive & controlFlowIn.forwardSignal & !TIMEOUT;
			
	DEFINE
		ACTIVE := (state = active);



MODULE ReceiveAction(Pre, Post, TriggerIn, controlFlowIn, controlFlowOut, TIMEOUT)
	VAR
		state : {initial, S1, S2};
		triggerBuffer : boolean;

	ASSIGN
		init(state) := initial;
		next(state) :=
			case
				TIMEOUT : initial;
				state = initial & controlFlowIn.forwardSignal & Pre: S1;
				state = S1 & (TriggerIn | triggerBuffer) : S2;
				state = S2 & controlFlowOut.backwardSignal : initial;
				TRUE : state;
			esac;
			
		init(triggerBuffer) := FALSE;
		next(triggerBuffer) :=
			case
				state = S1 & next(state) = S2 : FALSE;
				TriggerIn : TRUE;
				TRUE: triggerBuffer;
			esac;

		controlFlowOut.forwardSignal := (state = S2) & Post & !TIMEOUT;
		controlFlowIn.backwardSignal := (state = initial) & controlFlowIn.forwardSignal & Pre & !TIMEOUT;

	DEFINE
		ACTIVE := (state != initial);
		BEH := (state = S1) & (TriggerIn | triggerBuffer) & !TIMEOUT;


MODULE Action(Pre, Post, controlFlowIn, controlFlowOut, TIMEOUT)
	VAR
		state : {initial, S1, S2};

	ASSIGN
		init(state) := initial;
		next(state) :=
			case
				TIMEOUT : initial;
				state = initial & controlFlowIn.forwardSignal & Pre: S1;
				state = S1 : S2;
				state = S2 & controlFlowOut.backwardSignal : initial;
				TRUE : state;
			esac;

		controlFlowOut.forwardSignal := (state = S2) & Post & !TIMEOUT;
		controlFlowIn.backwardSignal := (state = initial) & controlFlowIn.forwardSignal & Pre & !TIMEOUT;

	DEFINE
		ACTIVE := (state != initial);
		BEH := (state = S1) & !TIMEOUT;


MODULE SendAction(Pre, Post, controlFlowIn, controlFlowOut, TIMEOUT)
	VAR
		state : {initial, S1, S2};

	ASSIGN
		init(state) := initial;
		next(state) :=
			case
				TIMEOUT : initial;
				state = initial & controlFlowIn.forwardSignal & Pre: S1;
				state = S1 : S2;
				state = S2 & controlFlowOut.backwardSignal : initial;
				TRUE : state;
			esac;

		controlFlowOut.forwardSignal := (state = S2) & Post & !TIMEOUT;
		controlFlowIn.backwardSignal := (state = initial) & controlFlowIn.forwardSignal & Pre & !TIMEOUT;

	DEFINE
		ACTIVE := (state != initial);
		BEH := (state = S1) & !TIMEOUT;
		TRIGGEROUT := (state = S1) & !TIMEOUT;



MODULE Fork(controlFlowIn, controlFlowOut1, controlFlowOut2, TIMEOUT)
	VAR
		token1 : boolean;
		token2 : boolean;
		
	ASSIGN
		init(token1) := FALSE;
		next(token1) :=
			case
				TIMEOUT : FALSE;
				token1 = TRUE & controlFlowOut1.backwardSignal : FALSE;
				token1 = FALSE & controlFlowIn.forwardSignal & token2 = FALSE: TRUE;
				TRUE : token1;
			esac;
			
		init(token2) := FALSE;
		next(token2) :=
			case
				TIMEOUT : FALSE;
				token2 = TRUE & controlFlowOut2.backwardSignal : FALSE;
				token2 = FALSE & controlFlowIn.forwardSignal & token1 = FALSE : TRUE;
				TRUE : token2;
			esac;
			
		controlFlowOut1.forwardSignal := token1;
		controlFlowOut2.forwardSignal := token2;
		controlFlowIn.backwardSignal := token1 = FALSE & token2 = FALSE & controlFlowIn.forwardSignal & !TIMEOUT;
		
	DEFINE
		ACTIVE := ((token1 = TRUE) | (token2 = TRUE));


MODULE Join(controlFlowIn1, controlFlowIn2, controlFlowOut, TIMEOUT)
	VAR
		token1 : boolean;
		token2 : boolean;
		
	ASSIGN
		init(token1) := FALSE;
		next(token1) :=
			case
				TIMEOUT : FALSE;
				token1 = TRUE & controlFlowOut.backwardSignal : FALSE;
				token1 = FALSE & controlFlowIn1.forwardSignal : TRUE;
				TRUE : token1;
			esac;
		
		init(token2) := FALSE;
		next(token2) :=
			case
				TIMEOUT : FALSE;
				token2 = TRUE & controlFlowOut.backwardSignal : FALSE;
				token2 = FALSE & controlFlowIn2.forwardSignal : TRUE;
				TRUE : token2;
			esac;
			
		controlFlowOut.forwardSignal := token1 & token2 & !TIMEOUT;
		controlFlowIn1.backwardSignal := token1 = FALSE & controlFlowIn1.forwardSignal & !TIMEOUT;
		controlFlowIn2.backwardSignal := token2 = FALSE & controlFlowIn2.forwardSignal & !TIMEOUT;
			
	DEFINE
		ACTIVE := ((token1 = TRUE) | (token2 = TRUE));
		
		
MODULE Decision(Cond1, Cond2, controlFlowIn, controlFlowOut1, controlFlowOut2, TIMEOUT)  
---- Effentuell Error Flag wenn kein Pfad gegangen werden kann!?  
---- Effentuell TRANS nutzen um falls beide Conditions True sein sollten einen beliebigen zu gehen!

	VAR
		state : {initial, out1, out2};
		
	ASSIGN
		init(state) := initial;
		next(state) :=
			case
				TIMEOUT : initial;
				state = out1 & controlFlowOut1.backwardSignal : initial;
				state = out2 & controlFlowOut2.backwardSignal : initial;
				state = initial & Cond1 & Cond2 & controlFlowIn.forwardSignal : {out1, out2};
				state = initial & Cond1 & controlFlowIn.forwardSignal : out1;
				state = initial & Cond2 & controlFlowIn.forwardSignal : out2;
				TRUE : state;
			esac;
			
		controlFlowOut1.forwardSignal := (state = out1) & !TIMEOUT;
		controlFlowOut2.forwardSignal := (state = out2) & !TIMEOUT;
		controlFlowIn.backwardSignal := controlFlowIn.forwardSignal & state = initial & (Cond1 | Cond2) & !TIMEOUT;
			
	DEFINE
		ACTIVE := (state != initial);

		
MODULE Merge(controlFlowIn1, controlFlowIn2, controlFlowOut, TIMEOUT)
	VAR
		state : {initial, out};
		
	ASSIGN
		init(state) := initial;
		next(state) :=
			case
				TIMEOUT : initial;
				state = initial & (controlFlowIn1.forwardSignal | controlFlowIn2.forwardSignal) : out;
				state = out & controlFlowOut.backwardSignal : initial;
				TRUE : state;
			esac;
		
		controlFlowOut.forwardSignal := state = out & !TIMEOUT;
		controlFlowIn1.backwardSignal := state = initial & controlFlowIn1.forwardSignal & !TIMEOUT;
		controlFlowIn2.backwardSignal := state = initial & controlFlowIn2.forwardSignal & !controlFlowIn1.forwardSignal & !TIMEOUT;
		
			
	DEFINE
		ACTIVE := (state != initial);


MODULE ControlFlowIntercepter(controlFlowIn, controlFlowOut, allowed, TIMEOUT)
	VAR
		state : {initial, token, allowed_state, clear};
	
	ASSIGN
		init(state) := initial;
		next(state) :=
			case
				TIMEOUT : initial;
				state = clear : initial;
				state = allowed_state & controlFlowOut.backwardSignal : clear;
				state = token & allowed : allowed_state;
				state = initial & controlFlowIn.forwardSignal : token;
				TRUE : state;
			esac;
	
		controlFlowOut.forwardSignal := (state = allowed_state);
		controlFlowIn.backwardSignal := controlFlowIn.forwardSignal & state = initial;
	
	DEFINE
		ACTIVE := (state != initial);
		WAITING := (state = token);
		RESET_OUT := (state = clear);
		ALLOWED := (state = allowed_state);


MODULE LeaveRegionAction(ActionsActiveSignalIn, controlFlowOut, condition)
	VAR
		state : {initial, out};
		
	ASSIGN
		init(state) := initial;
		next(state) := 
			case
				state = initial & ActionsActiveSignalIn & condition: out;
				state = out & controlFlowOut.backwardSignal : initial;
				TRUE : state;
			esac;

		controlFlowOut.forwardSignal := (state = out);
	
	DEFINE
		RESET_OUT := (state = initial) & ActionsActiveSignalIn & condition;
		ACTIVE := (state != initial);


MODULE TimeTrigger(ActiveSignalIn, limit)
	VAR
		cnt : 0..1000;
		
	ASSIGN
		init(cnt) := 0;
		next(cnt) :=
			case
				! ActiveSignalIn: 0;
				cnt = limit : cnt;
				TRUE: cnt + 1;
			esac;
	
	DEFINE
		TRIG_OUT := cnt = limit;

#endif

