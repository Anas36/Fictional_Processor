import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.IconifyAction;

public class Main {

	static String[] InstructionMemory = new String[1024]; 
	static int NoInst = 0;
	static byte [] DataMemory = new  byte[2048];  //since byte = 1 byte
	static byte [] Registers = new byte[64];
	static boolean [] SREG = new boolean[8];
	static int [] TempSREG = new int[8];
	static int PC = 0;
	static String FirstDecodedopcode; 
	static String FirstDecodedR1; 
	static String FirstDecodedR2;
	static String SecondDecodedopcode; 
	static String SecondDecodedR1; 
	static String SecondDecodedR2;
	static String FirstFetchedInstruction;
	static String SecondFetchedInstruction;
	static String pathOFfile;

	//parse the program file and put the instructions in the "InstructionMemory"
	public static void Parser(String path) throws FileNotFoundException

	{
		pathOFfile = path;
		File file = new File(path+".txt");
		Scanner sc = new Scanner(file);
		String Format = ""; 
		while(sc.hasNextLine())
		{
			String line = sc.nextLine();
			if(line.equals("") || line.equals(" ") || line.equals("/n"))
				continue;
			String[] instList = line.split(" ");
			String instruction = "";
			if(instList[0].equalsIgnoreCase("ADD")) //0
			{
				instruction += "0000";
				Format = "R";
			}
			else if(instList[0].equalsIgnoreCase("SUB")) //1
			{
				instruction += "0001";
				Format = "R";
			}
			else if(instList[0].equalsIgnoreCase("MUL")) //2
			{
				instruction += "0010";
				Format = "R";
			}
			else if(instList[0].equalsIgnoreCase("LDI")) //3
			{
				instruction += "0011";
				Format = "I";
			}
			else if(instList[0].equalsIgnoreCase("BEQZ")) //4
			{
				instruction += "0100";
				Format = "I";
			}
			else if(instList[0].equalsIgnoreCase("AND")) //5
			{
				instruction += "0101";
				Format = "R";
			}
			else if(instList[0].equalsIgnoreCase("OR")) //6
			{
				instruction += "0110";
				Format = "R";
			}
			else if(instList[0].equalsIgnoreCase("JR")) //7
			{
				instruction += "0111";
				Format = "R";
			}
			else if(instList[0].equalsIgnoreCase("SLC")) //8
			{
				instruction += "1000";
				Format = "I";
			}
			else if(instList[0].equalsIgnoreCase("SRC")) //9
			{
				instruction += "1001";
				Format = "I";
			}
			else if(instList[0].equalsIgnoreCase("LB")) //10
			{
				instruction += "1010";
				Format = "I";
			}
			else if(instList[0].equalsIgnoreCase("SB")) //11
			{
				instruction += "1011";
				Format = "I";
			}
			String R1 = Integer.toBinaryString(Integer.parseInt(instList[1].substring(1)))+"";
			for(int i = 0; R1.length() < 6; i++)
			{
				R1 = "0" + R1;
			}
			instruction += R1;
			if(Format.equals("R"))
			{
				String R2 = Integer.toBinaryString(Integer.parseInt(instList[2].substring(1)))+"";
				for(int i = 0; R2.length() < 6; i++)
				{
					R2 = "0" + R2;
				}
				instruction += R2;
			}
			else 
			{
				String I = Integer.toBinaryString(Integer.parseInt(instList[2]));
				for(int i = 0; I.length() < 6; i++)
				{
					I = "0" + I;
				}
				instruction += I;
			}
			setInstructionMemory(NoInst,instruction);
			NoInst +=1;

		}	
		sc.close();
	}
	
	//Fetching the instruction @ the PC 
	public static void Fetch(int TempPc) 
	{
		SecondFetchedInstruction =  InstructionMemory[TempPc];
	}
	
	//Decode the instruction to get the opcode and the rs & rt
	public static void Decode(String instruction)

	{
		SecondDecodedopcode = instruction.substring(0,4); //15:12
		SecondDecodedR1 = instruction.substring(4,10); //11:6
		SecondDecodedR2 = instruction.substring(10,16); //15:12
		
		//Execute(opcode, r1, r2);	
	}

	
	//Execute the decoded instruction
	private static void Execute(String opcode, String r1, String r2  ) 
	{
		byte result;
		int R1 = Integer.parseInt(r1, 2);
		int R2 = Integer.parseInt(r2, 2);
		byte Mem1 = getRegister(R1);
		byte Mem2 = getRegister(R2);
		byte IMM = (byte) R2;

		
		if(opcode.equals("0000"))   //ADD R1 R2
		{
			result = (byte) (Mem1+Mem2);
			setRegister(R1,result);
			UpdateSREG('c', result);
			byte overFlow = (byte) ((Mem1 > 0 && Mem2 > 0 && result < 0) || (Mem1 < 0 && Mem2 < 0 && result > 0) ? 1 : 0);
			UpdateSREG('v', overFlow);
			UpdateSREG('n',result);
			UpdateSREG('s',result);
			UpdateSREG('z',result);
		}
		else if(opcode.equals("0001")) //SUB R1 R2
		{
			result = (byte) (Mem1-Mem2);
			setRegister(R1,result);
			UpdateSREG('c', result);
			UpdateSREG('n',result);
			UpdateSREG('z',result);
			
		}
		else if(opcode.equals("0010"))  //MUL R1 R2 
		{
			result = (byte) (Mem1*Mem2);
			setRegister(R1,result);
			UpdateSREG('c', result);
			byte overFlow = (byte) ((Mem1 > 0 && Mem2 > 0 && result < 0) || (Mem1 < 0 && Mem2 < 0 && result > 0) ? 1 : 0);
			UpdateSREG('v', overFlow);
			UpdateSREG('n',result);
			UpdateSREG('s',result);
			UpdateSREG('z',result);
		}
		else if(opcode.equals("0011"))   //LDI
		{
			result = (byte) IMM;
			setRegister(R1,result);
		}
		else if(opcode.equals("0100"))   //BEQZ
		{
			if(R1 == 0) 
				PC = PC+1+IMM; 
		}
		else if(opcode.equals("0101"))   //AND
		{
			result = (byte) (Mem1 & Mem2);
			setRegister(R1,result);
			UpdateSREG('n',result);
			UpdateSREG('z',result);
		}
		else if(opcode.equals("0110"))   //OR
		{
			result = (byte) (Mem1 | Mem2);
			setRegister(R1,result);
			UpdateSREG('n',result);
			UpdateSREG('z',result);
		}
		else if(opcode.equals("0111"))   //JR
		{
			PC = Integer.parseInt((Mem1+""+Mem2), 2);
		} 
		else if(opcode.equals("1000"))    //SLC
		{
			result = (byte) (Mem1 << IMM | R1 >>> 8 - IMM);
			setRegister(R1,result);
			UpdateSREG('n',result);
			UpdateSREG('z',result);
		}
		else if(opcode.equals("1001"))    //SRC
		{
			result = (byte) (R1 >>> IMM | R1 << 8 - IMM);
			setRegister(R1,result);
			UpdateSREG('n',result);
			UpdateSREG('z',result);
		}
		else if(opcode.equals("1010"))  //LB
		{
			result = (byte) DataMemory[IMM];
			setRegister(R1,result);
		}
		else if(opcode.equals("1011")) //SB
		{
			DataMemory[IMM] = Mem1;
		}
		BoolToInt(SREG); 
		SREG = new boolean[8];
		
		
	}

	
	//Pipeline
	public static void RunAsPipeline() throws IOException 
	{
		int maxClkCycles = 3+(NoInst-1);

		for(int i = 1; i <= maxClkCycles; i++)
		{
			System.out.println("#####################################  Clock Cycle: " + i+"  #####################################33"+"\n");
			

				if(!(i >= maxClkCycles-1))
				{
					FetchedInst(PC);
					System.out.println("Fetch passed parameters of instruction "+(PC+1)+": PC =  "+(PC));
					Fetch(PC);
					System.out.println("\n");
				}
				
				if(!(i <= 1 || i >= maxClkCycles))
				{
					DecodedInst(PC - 1);
					System.out.println("Decode passed parameters of instruction "+(PC)+" : " +FirstFetchedInstruction);
					Decode(FirstFetchedInstruction);
					System.out.println("\n");

				}
				
				if(!(i <= 2)) 
				{
					
					Execute(FirstDecodedopcode, FirstDecodedR1, FirstDecodedR2);
					executedInst(PC-2);
					if(IsImmediate(FirstDecodedopcode))
					{
						System.out.println("Execute passed parameters of instruction "+(PC - 1)+" : " +" #Opcode: " + FirstDecodedopcode + " #First Register: " +FirstDecodedR1 + " #IMMEDIATE: " + FirstDecodedR2);

					}
					else 
					{
						System.out.println("Execute passed parameters of instruction "+(PC - 1)+" : "+" #Opcode: " + FirstDecodedopcode + " #First Register: " +FirstDecodedR1 + " #Second Register: " + FirstDecodedR2);

					}
					System.out.println("\n");
					System.out.print("SREG Register :");
					System.out.println(Arrays.toString(TempSREG));
					System.out.println("\n");


					CheckRegister(SecondDecodedopcode);
					CheckDataMemory(SecondDecodedopcode);

				}
					
		
			FirstFetchedInstruction = SecondFetchedInstruction;
			FirstDecodedopcode = SecondDecodedopcode;FirstDecodedR1 = SecondDecodedR1; FirstDecodedR2 = SecondDecodedR2;
			PC++;
//			CheckRegister(FirstDecodedopcode);
			System.out.println("\n");

			
		}
	}
	
	
	
	
	
	
	public static void BoolToInt(boolean[] SREG) 
	{
		for (int i = 0; i < SREG.length; i++) {
			 if (SREG[i] == true) {
		            TempSREG[i] = 1;
		        }
		        else {
		            TempSREG[i] = 0;
		        }
		}
	}
	
	
	private static void UpdateSREG(char flag,byte result) 
	{	
		switch (flag) 
		{
			case 'c': SREG[4] = result > 127 || result > -127;break;
			case 'v': SREG[3] = result == 1;break;
			case 'n': SREG[2] = result < 0;break;
			case 's': SREG[1] = (result < 0)^(result > 127 || result > -127);break;
			case 'z': SREG[0] = result == 0;break;
		}

	}
	
	public static String getInstructionMemory(int index) {
		return InstructionMemory[index];
	}

	public static void setInstructionMemory(int index,String value) {
		InstructionMemory[index] = value;
	}
	

	public static byte getRegister(int index) {
		return Registers[index];
	}

	public static void setRegister(int index,byte value) {
		Registers[index] = value;
	}
	public static void setDataMemory(int index,byte value) {
		DataMemory[index] = value;
	}
	public static byte getDataMemory(int index) {
		return DataMemory[index];
	}
	public static boolean IsImmediate(String opcode)
	{
		return opcode.equals("0011") || opcode.equals("0100") || opcode.equals("1000") || opcode.equals("1001") || opcode.equals("1010") || opcode.equals("1011");
	}
	public static void CheckRegister(String opcode)
	{
		ArrayList<String> I = new ArrayList<String>(3);
		I.add("1011");I.add("0100");I.add("0111");
		if(!I.contains(opcode))
		{
			System.out.println("Register "+Integer.parseInt(FirstDecodedR1,2)+" contains now " +getRegister(Integer.parseInt(FirstDecodedR1,2)));
		}
		else 
		{
			System.out.println("Nothing happend in the Registers");
		}
	}
	public static void CheckDataMemory(String opcode)
	{
		if(opcode.equals("1011"))
		{
			System.out.println("MEM["+Integer.parseInt(FirstDecodedR1,2)+"]"+" contains now " +getDataMemory(Integer.parseInt(FirstDecodedR2,2)));
			;
		}
		else 
		{
			System.out.println("Nothing happend in the Data Memory");
		}
	}
	public static void executedInst(int i) throws IOException
	{
		String specific_line_text = Files.readAllLines(Paths.get(pathOFfile+".txt")).get(i);
		System.out.println("The Executed Instruction is \""+specific_line_text+"\"");

	}
	public static void FetchedInst(int i) throws IOException
	{
		String specific_line_text = Files.readAllLines(Paths.get(pathOFfile+".txt")).get(i);
		System.out.println("The Fetched Instruction is \""+specific_line_text+"\"");

	}
	public static void DecodedInst(int i) throws IOException
	{
		String specific_line_text = Files.readAllLines(Paths.get(pathOFfile+".txt")).get(i);
		System.out.println("The Decoded Instruction is \""+specific_line_text+"\"");

	}
	public static void main(String[] args) throws IOException 
	{
		Parser("RunProgram");
		RunAsPipeline();
		System.out.print("Registers: ");
		System.out.println(Arrays.toString(Registers));
		System.out.print("Instruction Memory: ");
		System.out.println(Arrays.toString(InstructionMemory));
		System.out.print("Data Memory: ");
		System.out.println(Arrays.toString(DataMemory));

	}
}
