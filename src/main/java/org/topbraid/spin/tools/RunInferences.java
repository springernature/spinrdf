/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  See the NOTICE file distributed with this work for additional
 *  information regarding copyright ownership.
 */

package org.topbraid.spin.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;



import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileUtils;
import org.spinrdf.inference.SPINInferences;
import org.spinrdf.system.SPINModuleRegistry;
import org.spinrdf.util.JenaUtil;

/**
 * A stand-alone SPIN inference engine callable from the command line.
 * 
 * @author Holger Knublauch
 */
public class RunInferences {

	/**
	 * The command line entry point.
	 * @param args 
	 * 		[0]: the base URI/physical URL of the file
	 * 		[1]: the (optional) name of a local RDF file contains the base URI
	 */
	public static void main(String[] args) throws IOException {
		

		 if(args.length == 0) {
			System.out.println("Arguments: baseURI [fileName]");
			System.exit(0);
		}

		// Initialize system functions and templates
		SPINModuleRegistry.get().init();


		// Load main file
		String baseURI = args[0];
		Model baseModel = ModelFactory.createDefaultModel();
		if(args.length > 1) {
			String fileName = args[1];
			File file = new File(fileName);
			InputStream is = new FileInputStream(file);
			String lang = FileUtils.guessLang(fileName);
			baseModel.read(is, baseURI, lang);
		}
		else {
			String lang = FileUtils.guessLang(baseURI);
			baseModel.read(baseURI, lang);
		}
		
		// Create OntModel with imports
		OntModel ontModel = JenaUtil.createOntologyModel(OntModelSpec.OWL_MEM,baseModel);

		// Register locally defined functions
		SPINModuleRegistry.get().registerAll(ontModel, null);
		
		// Create and add Model for inferred triples
		Model newTriples = ModelFactory.createDefaultModel();
		newTriples.setNsPrefixes(ontModel);
		ontModel.addSubModel(newTriples);

		// Perform inferencing
		SPINInferences.run(ontModel, newTriples, null, null, false, null);
		
		// Create results model

		// Output results in Turtle
		newTriples.write(System.out, FileUtils.langTurtle);
	}
}
