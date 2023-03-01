package sparql;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.resultset.ResultsFormat;

public class SparqlExec {

	public static void main(String[] args) throws Exception {
		Options options = new Options();
		options.addOption(Option.builder("n3").argName("n3").hasArg().desc("input N3 code")
				.numberOfArgs(Option.UNLIMITED_VALUES).valueSeparator(' ').required(true).build());
		options.addOption(Option.builder("query").argName("query").hasArg(true).desc("SPARQL query").numberOfArgs(1)
				.required(true).build());

		CommandLineParser parser = new DefaultParser();
		CommandLine line = null;
		try {
			line = parser.parse(options, args);
		} catch (ParseException exp) {
			System.err.println("ERROR: " + exp.getMessage());
			System.exit(1);
		}

		Model model = ModelFactory.createDefaultModel();

		String[] paths = line.getOptionValues("n3");
		for (String path : paths) {
			InputStream in = new FileInputStream(path);
			model.read(in, null, "TURTLE");
		}

		String queryString = Files.readString(new File(line.getOptionValue("query")).toPath());

		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		switch (query.queryType()) {

		case SELECT:
			ResultSet results = qexec.execSelect();
			// prints to System.out
			ResultSetFormatter.output(results, ResultsFormat.FMT_TEXT);

			break;

		case CONSTRUCT:
			Model m = qexec.execConstruct();
			m.write(System.out, "TURTLE");

			break;

		case ASK:
			boolean ret = qexec.execAsk();
			System.out.print("result: " + ret);

			break;

		default:
			System.err.println("unsupported query type: " + query.queryType());
			break;
		}
	}
}
