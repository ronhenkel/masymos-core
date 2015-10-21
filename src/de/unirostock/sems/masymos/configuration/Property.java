package de.unirostock.sems.masymos.configuration;

public class Property {
	
	public class ModelType{
		/*
		 * Model Type Properties
		 */
		public final static  String SBML = "SBML";
		public final static  String CELLML = "CELLML";
		public final static  String XML = "XML";
		public final static  String SEDML = "SEDML";
	
	}
	
	public class General{
		/*
		 * General Properties
		 */
		public final static  String ID = "ID";
		public final static  String NAME = "NAME";
		public final static  String CREATED = "CREATED";
		public final static  String MODIFIED = "MODIFIED";
		public final static  String CREATOR = "CREATOR";
		public final static  String ENCODER = "ENCODER";
		public final static  String SUBMITTER = "SUBMITTER";
		public final static  String AUTHOR = "AUTHOR";
		public final static  String EMAIL = "EMAIL";
		public final static  String URI = "URI";
		public final static  String FILENAME = "FILENAME";
		public final static  String FILEID = "FILEID";
		public final static  String META = "META";
		public final static  String XMLDOC = "XMLDOC";
		public final static  String VERSIONID = "VERSIONID";
		public final static  String RESOURCETEXT = "RESOURCETEXT";
		public final static  String NONRDF = "NONRDF";
		public final static  String IS_INDEXED = "ISINDEXED";
	
	}
	
	public class XML{
	/*
	 * Properties to store XML in DB
	 */
		public final static  String VALUE = "VALUE";
		public final static  String ELEMENT = "ELEMENT";
		public final static  String ATTRIBUTE_NAMES = "ATTRIBUTENAMES";
		public final static  String ATTRIBUTE_VALUES = "ATTRIBUTEVALUES";
	}
	
	public class SBML{
	/*
	 * Properties to store SBML in DB
	 */	
		public final static  String VERSION = "VERSION";
		public final static  String LEVEL = "LEVEL";
		//public final static  String NAME = "NAME";
		public final static  String COMPARTMENT = "COMPARTMENT";
		public final static  String REACTION = "REACTION";
		public final static  String SPECIES = "SPECIES";
		public final static  String PARAMETER = "PARAMETER";
		public final static  String RULE = "RULE";
		public final static  String FUNCTION = "FUNCTION";
		public final static  String EVENT = "EVENT";
	}
	
	public class CellML{
	/*
	 * Properties to store CellML in DB
	 */	
		public final static  String VERSION = "VERSION";
		//public final static  String NAME = "NAME";
		public final static  String COMPONENT = "COMPONENT";
		//public final static  String GROUP = "GROUP";
		public final static  String VARIABLE = "VARIABLE";
		//public final static  String CONNECTION = "CONNECTION";	
		public final static  String REVERSIBLE = "REVERSIBLE";
		public final static  String REACTIONDIRECTION = "REACTIONDIRECTION";
		public final static  String ISPRIVATECONNECTION = "ISPRIVATECONNECTION";
	}
	
	

	public class Publication{
	/*
	 * Properties to store Publication in DB
	 */
		public final static  String AUTHOR = "AUTHOR";
		public final static  String TITLE = "TITLE";
		public final static  String ABSTRACT = "ABSTRACT";
		public final static  String JOURNAL = "JOURNAL";
		public final static  String YEAR = "YEAR";
		public final static  String AFFILIATION = "AFFILIATION";
		public final static  String SYNOPSIS = "SYNOPSIS";
		public final static  String ID = "PUBID";
		
	}	
	
	public class Person{
	/*
	 * Properties to store Publication in DB
	 */
		public final static  String FAMILYNAME = "FAMILYNAME";
		public final static  String GIVENNAME = "GIVENNAME";
		public final static  String EMAIL = "EMAIL";
		public final static  String ORGANIZATION = "ORGANIZATION";		
	}	
	
	public class SEDML{
		/*
		 * Properties to store SBML in DB
		 */	
			public final static  String VERSION = "VERSION";
			public final static  String LEVEL = "LEVEL";
			public final static  String NAME = "NAME";
			public final static  String MODELSOURCE = "MODELSOURCE";
			public final static  String MODELCHANGED = "MODELCHANGED";
			public final static  String OUTPUT_TYPE = "OUTPUTTYPE";
			public final static  String XDATA = "XDATA";
			public final static  String YDATA = "YDATA";
			public final static  String ZDATA = "ZDATA";
			public final static  String DATALABEL = "DATALABEL";
			public final static  String SIM_KISAO = "SIMKISAO";
			public final static  String SIM_TYPE = "SIMTYPE";
			public final static  String MATH = "MATH";
			public final static  String TARGET = "TARGET";



			
	}
	
	public class GroupCalc{
		public final static  String P = "Probability";
		public final static  String IC = "InformationContent";
		public final static  String COUNTofCONCEPTS = "COUNTofCONCEPTS";
		
		public final static  String Trissl = "TrisslScore";

		public final static  String ef = "entityfrequency";
		public final static  String EF = "aggregatedEntityFrequency";
		public final static  String EP = "EntityProbability";

		public final static  String df = "documentfrequency";
		public final static  String DF = "aggregatedDocumentFrequency";
		public final static  String DP = "DocumentProbability";

		
	}
	
	public class  Ontology {
		public static final String NODETYPE = "NODETYPE";
		public final static  String OntologyLongID = "OntologyLongID"; //old
		public final static  String TermID = "id";
		public final static  String isLeaf = "isLeaf";
		public final static  String depth = "depth";

		
	}
}
