package de.unirostock.sems.masymos.util;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.unirostock.sems.masymos.configuration.Relation;

/**
*
* Copyright 2016 Ron Henkel (GPL v3)
* @author ronhenkel
*/

public class SBGN2Cypher  {
	
	static Set<String> consumptionReaction =  new HashSet<String>(Arrays.asList("consumption"));
	static Set<String> productionReaction =  new HashSet<String>(Arrays.asList("production"));
	static Set<String> modificatinReaction =  new HashSet<String>(Arrays.asList("modulation","stimulation","catalysis",
											"inhibition","necessary stimulation"));
	
	
	public static String toCypher(String in) throws Exception{	
/*
		File f = new File(in);	
		Sbgn sbgn = SbgnUtil.readFromFile(f);
		Map map = sbgn.getMap();
		List<String> arcs = new LinkedList<String>();
		for (Arc a : map.getArc()) {
			StringBuilder sb = new StringBuilder();			
			sb.append("(");
			
			Glyph source = null;
			if (a.getSource() instanceof Glyph) source = (Glyph) a.getSource();
			else source = getParentGlyph(map, (Port) a.getSource());
			
			
			sb.append(source.getId());
			if (source.getClazz().equalsIgnoreCase("process")){
				sb.append(":SBML_REACTION)");
			} else {
				sb.append(":SBML_SPECIES)");
			}
			
			sb.append("-[:");
			if (consumptionReaction.contains(a.getClazz())) sb.append(Relation.SbmlRelTypes.IS_REACTANT.toString());
			else if (productionReaction.contains(a.getClazz())) sb.append(Relation.SbmlRelTypes.HAS_REACTANT.toString());
			else sb.append(Relation.SbmlRelTypes.IS_MODIFIER.toString());
			sb.append("]->(");	
			
			Glyph target = null;
			if (a.getTarget() instanceof Glyph) target = (Glyph) a.getTarget();
			else target = getParentGlyph(map, (Port) a.getTarget());
			
			sb.append(target.getId());
			if (target.getClazz().equalsIgnoreCase("process")){
				sb.append(":SBML_REACTION)");
			} else {
				sb.append(":SBML_SPECIES)");
			}
			arcs.add(sb.toString());
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("MATCH ");
		for (Iterator<String> iterator = arcs.iterator(); iterator.hasNext();) {
			String part = (String) iterator.next();
			sb.append(part);
			sb.append(", ");
		}
		sb.deleteCharAt(sb.length()-2); //remove the last ", "
		sb.append("RETURN XXX");
		
		return sb.toString();
		*/
		return "NOPE";
	}
	
	/*
	private static Glyph getParentGlyph(Map map, Port p){
		for (Glyph g : map.getGlyph())
		{
			List<Port>ports = g.getPort();
			for (Iterator<Port> iterator = ports.iterator(); iterator.hasNext();) {
				Port port = (Port) iterator.next();
				if (port.getId().equals(p.getId())) return g;
			}
		}
		return null;
	}
	*/
}
