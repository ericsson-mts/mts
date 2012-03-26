/* 
 * Copyright 2012 Devoteam http://www.devoteam.com
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * 
 * This file is part of Multi-Protocol Test Suite (MTS).
 * 
 * Multi-Protocol Test Suite (MTS) is free software: you can redistribute
 * it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the
 * License.
 * 
 * Multi-Protocol Test Suite (MTS) is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Multi-Protocol Test Suite (MTS).
 * If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.devoteam.srit.xmlloader.core.report;

public final class HTML {
	
	public static String nl = System.getProperty("line.separator");
	public static String div(String id, String klass, String content) {
		if (id == null) id = "";
		if (klass == null) klass = "";
		return "<div id=\"" + id + "\" class=\""+klass+"\" >"+nl + content + "</div>"+nl;
	}
	public static String divO(String id, String klass) {
		return "<div id=\"" + id + "\" class=\""+klass+"\" >";
	}
	public static String divC() {
		return "</div>"+nl;
	}
	public static String img(String id, String klass, String filename){
		if (id == null) id = "";
		if (klass == null) klass = "";
		return "<img id=\"" + id + "\" class=\""+klass+"\" src=\""+filename+"\"/>"+nl;
		}
	public static String tableO(String id, String klass){
		return "<table id=\"" + id + "\" class=\""+klass+"\">"+nl;
	}
	public static String tableC(){
		return "</table>"+nl;
	}
	
	public static String br(){
		return "<br/>"+nl;
	}
	
	public static String span(String id, String klass, String content) {
		return "<span id=\"" + id + "\" class=\""+klass+"\">\n" + content + "</span>"+nl;
	}

	public static String table(String id, String klass, String content) {
		return "<table id=\"" + id + "\" class=\""+klass+"\">"+nl + content + "</table>";
	}

	public static String tableRow(String id, String klass, String content) {
		return "<tr id=\"" + id + "\" class=\""+ klass + "\">" + content + "</tr>"+nl;
	}

	public static String tableCell(String id, String klass, String content) {
		return "<td id=\"" + id + "\" class=\"" + klass + "\">" + content + "</td>"+nl;
	}

	public static String a(String id, String href, String content) {
		return "<a id=\"" + id + "\" href=\"" + href + "\">" + content + "</a>";
	}
}
