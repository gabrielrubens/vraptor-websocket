/**
 * 
 */
package br.com.caelum.vraptor.controller;

import br.com.caelum.vraptor.Get;
import br.com.caelum.vraptor.Resource;

/**
 * @author bglbruno
 *
 */
@Resource
public class HomeController {

	@Get("/")
	public void index(){}
	
}
