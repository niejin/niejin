package com.duowan.niejin.solr.extendeds.evalutors;

import java.util.List;
import java.util.Locale;

import org.apache.solr.handler.dataimport.Context;
import org.apache.solr.handler.dataimport.Evaluator;

/**
 *
 * @author  N.Jin{@link niejin@yy.com}
 * @Time    2017年4月12日
 *
**/
public class LowerCaseFunctionEvaluator extends Evaluator {

	@Override
	public String evaluate(String expression, Context context) {
		List<Object> l = parseParams(expression, context.getVariableResolver());
	      if (l.size() != 1) {
	          throw new RuntimeException("'toLowerCase' must have only one parameter ");
	      }
	      return l.get(0).toString().toLowerCase(Locale.ROOT);
	}

}
