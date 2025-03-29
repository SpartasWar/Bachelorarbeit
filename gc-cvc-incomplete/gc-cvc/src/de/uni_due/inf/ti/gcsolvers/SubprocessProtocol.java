package de.uni_due.inf.ti.gcsolvers;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.uni_due.inf.ti.graph.Edge;
import de.uni_due.inf.ti.graph.Graph;
import de.uni_due.inf.ti.graph.Morphism;
import de.uni_due.inf.ti.graph.Node;
import de.uni_due.inf.ti.graph.condition.GraphCondition;
import de.uni_due.inf.ti.graph.io.SgfWriter;

/**
 * Implements communication between a main process and a solver subprocess.
 * The main process sends the graph condition to the solver subprocess, which
 * then starts solving and eventually reports a result and possibly a model.
 * <p>
 * Conditions and models are transferred as SGF.
 * <p>
 * Identifiers of graph elements might change during (de)serialization, so
 * it is highly recommended to explicitly identify elements of e.g. root
 * objects by pointing named morphisms to them. The details are not specified
 * in the protocol, as future changes in the SGF reader/writer implementation
 * might make this mechanism obsolete. However, it is expected that the solver
 * process keeps all SGF objects that were sent to it, and returns them
 * together with a model, with their names (top-level) unchanged.
 * 
 * @author Lara Stoltenow
 *
 */
interface SubprocessProtocol {
	public class Caller {
		private OutputStream toSolver;
	
		public void sendCondition(GraphCondition cond) throws IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos);
			SgfWriter sw = new SgfWriter(ps);
	
			/* write the actual condition, which also results in names being assigned
			 * for e.g. the graph elements of the origin graph
			 */
			String condName = sw.writeGraphCondition(cond);
	
			/* now, create sgf objects that identify individual elements of the origin
			 * graph, so we can correctly map them when receiving a model
			 */
			Map<Node, String> originNodeMorphisms = new HashMap<>();
			Map<Edge, String> originEdgeMorphisms = new HashMap<>();
			Graph origin = cond.getOrigin();
			for (Node n : origin.getNodes()) {
				Morphism m = Morphism.create(origin, origin);
				m.put(n, n);
				String mName = sw.writeMorphism(m);
				originNodeMorphisms.put(n, mName);
			}
			for (Edge e : origin.getEdges()) {
				Morphism m = Morphism.create(origin, origin);
				m.put(e, e);
				String mName = sw.writeMorphism(m);
				originEdgeMorphisms.put(e, mName);
			}
	
			ps.flush();
	
			String delimiter = "/* EOF */";
			toSolver.write(("sgf until " + delimiter + "\n").getBytes());
			baos.writeTo(toSolver);
			toSolver.write((delimiter + "\n").getBytes());
		}
	}
	
	public class Solver {
		private InputStream fromCaller;
		private OutputStream toCaller;
		
		public Solver(InputStream fromCaller, OutputStream toCaller) {
			this.fromCaller = fromCaller;
			this.toCaller = toCaller;
		}
		
		public void receiveLoop() throws IOException {
			InputStreamReader reader = new InputStreamReader(fromCaller);
			BufferedReader br = new BufferedReader(reader);

			Pattern sgfCommand = Pattern.compile("sgf until (.*)");
			command_loop: while (true) {
				String line = br.readLine();
				if (line == null) break;
				
				Matcher m = sgfCommand.matcher(line);
				if (m.matches()) {
					String delimiter = m.group(1);
					System.out.println("delimiter: " + delimiter);
					
					StringBuilder sgf = new StringBuilder();
					while (true) {
						line = br.readLine();
						if (line == null) break;
						if (line.equals(delimiter)) break;
						sgf.append(line);
						sgf.append('\n');
					}
					System.out.println("consumed sgf: " + sgf);
					// TODO: Parse

					continue command_loop;
				}

				toCaller.write("?\n".getBytes());
			}
		}
	}
	
	public static void main(String[] args) throws IOException {
		
		Solver sp = new Solver(System.in, System.err);
		sp.receiveLoop();
	}
}
