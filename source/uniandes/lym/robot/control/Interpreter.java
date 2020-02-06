package uniandes.lym.robot.control;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.SwingUtilities;

import sun.util.locale.ParseStatus;
import uniandes.lym.robot.kernel.*;

/**
 * Receives commands and relays them to the Robot.
 */

public class Interpreter {

	private RobotWorldDec world;

	private String[] alfabeto;

	private ArrayList<String[]> variablesSys;

	public Interpreter() {
		alfabeto = new String[] { "VARS", "ROBOT_R", "BEGIN", "END", ",", "R", "C", "M", "B", "b", "c" };
		variablesSys = new ArrayList<String[]>();
	}

	/**
	 * Creates a new interpreter for a given world
	 * 
	 * @param world
	 */

	public Interpreter(RobotWorld mundo) {
		this.world = (RobotWorldDec) mundo;

	}

	/**
	 * sets a the world
	 * 
	 * @param world
	 */

	public void setWorld(RobotWorld m) {
		world = (RobotWorldDec) m;

	}

	/**
	 * Processes a sequence of commands. A command is a letter followed by a ";" The
	 * command can be: M: moves forward R: turns right
	 * 
	 * @param input Contiene una cadena de texto enviada para ser interpretada
	 */

	public String process(String input) throws Error {

		int n;
		int i;
		n = input.length();
		StringBuffer output = new StringBuffer("SYSTEM RESPONSE: -->\n");
		boolean ok = true;
		i = 0;
		try {
			while (i < n && ok) {
				String[] comando = new String[n];
				comando = input.split(":");
				String evaluada = comando[0];
				for (String simb : this.alfabeto) {
					if (evaluada.equals(simb)) {
						switch (evaluada) {
						case "VARS":
							DeclarandoVars(comando[1]);
							output.append("Variables almacenadas \n");
							break;
						case "ROBOT_R":
							String instrucciones = "";
							for (int y = 1; y < comando.length; y++) {
								instrucciones = instrucciones + " " + comando[y];
							}
							RutinasRobot(instrucciones);
							output.append("Rutina guardada \n");
							break;
						case "M":
							world.moveForward(1);
							output.append("move \n");
							break;
						case "R":
							world.turnRight();
							output.append("turnRignt \n");
							break;
						case "C":
							world.putChips(1);
							output.append("putChip \n");
							break;
						case "B":
							world.putBalloons(1);
							output.append("putBalloon \n");
							break;
						case "c":
							world.pickChips(1);
							output.append("getChip \n");
							break;
						case "b":
							world.grabBalloons(1);
							output.append("getBalloon \n");
							break;
						default:
							output.append(" Unrecognized command:  " + input.charAt(i));
							ok = false;

						}
					}
				}

				if (ok) {
					if (i + 1 == n) {
						output.append("expected ';' ; found end of input; ");
						ok = false;
					} else if (input.charAt(i + 1) == ';') {
						i = i + 2;
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							System.err.format("IOException: %s%n", e);
						}

					} else {
						output.append(" Expecting ;  found: " + input.charAt(i + 1));
						ok = false;
					}
				}

			}

		} catch (Error e) {
			output.append("Error!!!  " + e.getMessage());

		}
		return output.toString();
	}

	public void DeclarandoVars(String pVariables) {
		pVariables.replace(";", "");
		String[] ListaVariables = new String[pVariables.length()];
		ListaVariables = pVariables.split(",");
		String[] innerList = new String[2];
		for (int b = 0; b < ListaVariables.length - 1; b++) {
			innerList[0] = ListaVariables[b];
			variablesSys.add(innerList);
			System.out.println(variablesSys);
		}
	}

	public void RutinasRobot(String instrucciones) {
		instrucciones.replace(";", ":");
		ArrayList<String> BloqueComandos = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(instrucciones);
		if (st.hasMoreTokens())
			st.nextToken();
		while (st.hasMoreElements()) {
			BloqueComandos.add(st.nextToken());
			System.out.println(BloqueComandos);
		}
		BloqueComandos.remove(BloqueComandos.size() - 1);
		Parser(BloqueComandos);
	}

	public void Parser(ArrayList<String> comandos) {
		StringBuffer output = new StringBuffer("SYSTEM RESPONSE: -->\n");
		for (int y = 0; y < comandos.size() - 1; y++) {
			String evaluado = comandos.get(y);
			switch (evaluado) {
			case "assign":
				int pos = BuscarPosVariable(comandos.get(y + 3));
				variablesSys.get(pos)[1] = comandos.get(y + 1);
				System.out.println(variablesSys);
				break;
			case "move":
				String varBuscada = variablesSys.get(BuscarPosVariable(comandos.get(y + 1)))[0];
				int val = Integer.parseInt(variablesSys.get(BuscarPosVariable(comandos.get(y + 1)))[1]);
				if (comandos.get(y + 2).equals("toThe")) {
					if (varBuscada != null) {
						MoveToThe(val, comandos.get(y + 3));
					} else {
						int n1 = Integer.parseInt(comandos.get(y + 1));
						MoveToThe(n1, comandos.get(y + 3));
					}
				} else if (comandos.get(y + 2).equals("inDir")) {
					if (varBuscada != null) {
						MoveInDir(val, comandos.get(y + 3));
					} else {
						int n2 = Integer.parseInt(comandos.get(y + 1));
						MoveInDir(n2, comandos.get(y + 3));
					}
				} else {
					if (varBuscada != null) {
						world.moveForward(val);
					} else {
						int n3 = Integer.parseInt(comandos.get(y + 1));
						world.moveForward(n3);
					}

				}
				break;
			case "turn":
				if (comandos.get(y + 1).equals("left")) {
					world.turnRight();
					world.turnRight();
					world.turnRight();
				} else if (comandos.get(y + 1).equals("right"))
					world.turnRight();
				else if (comandos.get(y + 1).equals("around")) {
					world.turnRight();
					world.turnRight();
					world.turnRight();
					world.turnRight();
				} else {
					output.append("Elija una instruccion valida \"left\"/\"right\"/\"around\"");
				}
				break;
			case "face":
				girar(comandos.get(y + 1));
				break;
			case "pick":
				int pos_ = BuscarPosVariable(comandos.get(y + 1));
				if (pos_ > -1) {
					if (comandos.get(y + 3).equals("Chips")) {
						int val_ = Integer.parseInt(variablesSys.get(pos_)[1]);
						if (world.chipsToPick() > val_)
							world.pickChips(val_);
					}
					else if(comandos.get(y+3).equals("Ballons")) {
						int val_ = Integer.parseInt(variablesSys.get(pos_)[1]);
						if (world.contarGlobos()>val_) {
							world.putBalloons(val_);
						}
					} else {
						output.append("Escoja una instruccion valida: \"Ballons\"/\"Chips\"");
					}
				} else {
					if (comandos.get(y + 3).equals("Chips")) {
						int val_ = Integer.parseInt(comandos.get(y+1));
						if (world.chipsToPick() > val_)
							world.pickChips(val_);
					}
					else if(comandos.get(y+3).equals("Ballons")) {
						int val_ = Integer.parseInt(comandos.get(y+1));
						if (world.contarGlobos()>val_) {
							world.putBalloons(val_);
						}
					} else {
						output.append("Escoja una instruccion valida: \"Ballons\"/\"Chips\"");
					}
				}
				break;
			case "put":
				break;
			}
		}
	}

	public int BuscarPosVariable(String pVar) {
		int rta = -1;
		for (int z = 0; z < variablesSys.size() - 1; z++) {
			for (int j = 0; j < variablesSys.get(z).length - 1; j++) {
				if (pVar.equals(variablesSys.get(j))) {
					rta = z;
				}
			}
		}
		return rta;
	}

	public void MoveToThe(int n, String d) {
		StringBuffer output = new StringBuffer("SYSTEM RESPONSE: -->\n");
		if (d == "Right") {
			world.moveHorizontally(n);
		} else if (d == "Left") {
			world.moveHorizontally(-n);
		} else if (d == "North") {
			world.moveVertically(-n);
		} else if (d == "back") {
			world.moveVertically(n);

		} else {
			output.append(" Porfavor ingrese alguna de las direcciones validas North/Back/Left/Right");
		}
	}

	public void MoveInDir(int n, String d) {
		StringBuffer output = new StringBuffer("SYSTEM RESPONSE: -->\n");
		boolean giro = false;
		giro = girar(d);
		if (giro) {
			world.moveForward(n);
		} else {
			output.append(" Porfavor ingrese alguna de las direcciones validas East/West/North/South");
		}
	}

	public boolean girar(String d) {
		boolean giro = false;
		int O = world.getOrientacion();
		if (d == "East") {
			switch (O) {
			case 1:
				world.turnRight();
				giro = true;
				break;
			case 0:
				world.turnRight();
				world.turnRight();
				world.turnRight();
				giro = true;
				break;
			case 3:
				world.turnRight();
				world.turnRight();
				giro = true;
				break;

			}
		} else if (d == "West") {
			switch (O) {
			case 0:
				world.turnRight();
				giro = true;
				break;
			case 1:
				world.turnRight();
				world.turnRight();
				world.turnRight();
				giro = true;
				break;
			case 2:
				world.turnRight();
				world.turnRight();
				giro = true;
				break;

			}
		} else if (d == "North") {
			switch (O) {
			case 2:
				world.turnRight();
				giro = true;
				break;
			case 3:
				world.turnRight();
				world.turnRight();
				world.turnRight();
				giro = true;
				break;
			case 0:
				world.turnRight();
				world.turnRight();
				giro = true;
				break;

			}
		} else if (d == "South") {
			switch (O) {
			case 3:
				world.turnRight();
				giro = true;
				break;
			case 0:
				world.turnRight();
				world.turnRight();
				world.turnRight();
				giro = true;
				break;
			case 1:
				world.turnRight();
				world.turnRight();
				giro = true;
				break;
			}

		}
		return giro;
	}
}
